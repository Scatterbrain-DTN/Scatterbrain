package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.DispMessage;
import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.NormalActivity;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterStanza;
import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Abstraction for android bluetooth stack for Scatterbrain.
 */
public class ScatterBluetoothManager {
    public final String TAG = "BluetoothManager";
    public final java.util.UUID UID = UUID.fromString("cc1f06c5-ce01-4538-bc15-2a1d129c8b28");
    public final String NAME = "Scatterbrain";
    public BluetoothAdapter adapter;
    public final static int REQUEST_ENABLE_BT = 1;
    public ArrayList<BluetoothDevice> foundList;
    public ArrayList<BluetoothDevice> tmpList;
    public HashMap<String, LocalPeer> connectedList;
    public NetTrunk trunk;
    public boolean runScanThread;
    public Handler bluetoothHan;
    public BluetoothLooper looper;
    public IntentFilter filter;
    public Runnable scanr;
    public ScatterAcceptThread acceptThread;
    public boolean isAccepting;
    public boolean acceptThreadRunning;
    public boolean threadPaused;

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                ScatterLogManager.v(TAG, "Found a bluetooth device!");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                foundList.add(device);
                // connectToDevice(device)
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ScatterLogManager.v(TAG, "Device disvovery finished. Scanning services");
                Thread discoveryFinishedThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(BluetoothDevice d : foundList)  {
                            d.fetchUuidsWithSdp();
                        }

                        foundList.clear();
                    }
                });
                discoveryFinishedThread.start();
                if (runScanThread) {
                    int scan = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(trunk.mainService).getString("sync_frequency", "7"));
                    bluetoothHan.postDelayed(scanr, scan * 1000);
                } else
                    ScatterLogManager.v(TAG, "Stopping wifi direct scan thread");
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                //TODO: find some parcelable extra to avoid
                final Thread prunePeer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, LocalPeer> s : connectedList.entrySet()) {
                            if (!s.getValue().socket.isConnected()) {
                                ScatterLogManager.v(TAG, "Removing unneeded device " + s.getKey().toString());
                                connectedList.remove(s);
                            }
                        }
                    }
                });
                prunePeer.start();

            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                ScatterLogManager.v(TAG, "Received a uuid action");
                Thread connectToDeviceThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Parcelable[] uuidlist = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                        if((device != null) && (device.getUuids() != null)) {
                            for (ParcelUuid u : device.getUuids()) {
                                if (u != null) {
                                    ScatterLogManager.v(TAG, "Parcel string: " + u.toString() + " uuid: " + UID.toString());
                                    if (u.toString().equals(UID.toString())) {
                                        ScatterLogManager.v(TAG, "UUID is scatterbrain!");
                                        connectToDevice(device);
                                    }
                                }
                            }
                        }
                    }
                });
                connectToDeviceThread.start();
            }
        }
    };


    //this should return a handler object later
    public void connectToDevice(BluetoothDevice device) {
        if (!isAccepting) {
            ScatterConnectThread currentconnection;
            currentconnection = new ScatterConnectThread(device, trunk);
            currentconnection.start();
        }
    }

    public ScatterBluetoothManager(NetTrunk trunk) {
        this.trunk = trunk;
        looper = new BluetoothLooper(trunk.globnet);
        bluetoothHan = new Handler();
        runScanThread = false;
        foundList = new ArrayList<>();
        tmpList = new ArrayList<>();
        connectedList = new HashMap<>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.filter = filter;
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        trunk.mainService.registerReceiver(mReceiver, filter);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            ScatterLogManager.e(TAG, "ERROR, bluetooth not supported");
        }
        isAccepting = false;
        acceptThreadRunning = false;
        threadPaused = false;
    }

    public void init() {
        if (!acceptThreadRunning) {
            acceptThread = new ScatterAcceptThread(trunk, adapter);
            acceptThread.start();
        }
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public synchronized void startDiscoverLoopThread() {
        ScatterLogManager.v(TAG, "Starting bluetooth scan thread");
        runScanThread = true;
        bluetoothHan = looper.getHandler();
        scanr = new Runnable() {
            @Override
            public void run() {
                //directmanager.scan();
                //
                ScatterLogManager.v(TAG, "Scanning...");

                if(!threadPaused)
                    adapter.startDiscovery();
            }
        };

        bluetoothHan.post(scanr);
    }


    public void onSuccessfulReceive(byte[] incoming) {
        if(!NormalActivity.active)
            trunk.mainService.startMessageActivity();
        final BlockDataPacket bd = new BlockDataPacket(incoming);
        if(bd.isInvalid())
            ScatterLogManager.e(TAG, "Received corrupt blockdata packet.");
        else if(true) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    trunk.mainService.getMessageAdapter().data.add(new DispMessage(new String(bd.body),
                            Base64.encodeToString(bd.senderluid, Base64.DEFAULT)));
                    trunk.mainService.getMessageAdapter().notifyDataSetChanged();
                    ScatterLogManager.e(TAG, "Appended message to message list");
                }
            });
        }
        else
            ScatterLogManager.e(TAG, "received a non-text message in a text context");
    }


    public void onSuccessfulConnect(BluetoothSocket socket) {
        try {
            InputStream i = socket.getInputStream();
            OutputStream o = socket.getOutputStream();
            BluetoothDevice d = socket.getRemoteDevice();
            trunk.mainService.noticeNotify("Senpai NOTICED YOU!!", "There is a senpai in your area somewhere");
            AdvertisePacket outpacket = trunk.globnet.encodeAdvertise(trunk.profile);
            o.write(outpacket.getContents());
            byte[] buffer = new byte[50];
            i.read(buffer);
            AdvertisePacket inpacket= null;
            if(buffer != null) {
                inpacket = trunk.globnet.decodeAdvertise(buffer);
                if(!inpacket.isInvalid()) {
                    trunk.mainService.updateUiOnDevicesFound();
                    ScatterLogManager.v(TAG, "Adding new device " + inpacket.convertToProfile().getLUID());
                    connectedList.put(new String(inpacket.luid), new LocalPeer(inpacket.convertToProfile(), socket));
                    ScatterLogManager.v(TAG, "List size = " + connectedList.size());

                }
                else {
                    ScatterLogManager.e(TAG, "Received an advertise stanza, but it is invalid");
                    socket.close();
                }

            }

        }
        catch(IOException c) {
            ScatterLogManager.e(TAG, "IOException in onSuccessfulConnect");

        }
    }

    public synchronized void stopDiscoverLoopThread() {
        ScatterLogManager.v(TAG, "Stopping bluetooth discovery thread");
        runScanThread = false;
        adapter.cancelDiscovery();
    }

    public synchronized void pauseDiscoverLoopThread() {
        ScatterLogManager.v(TAG, "Pausing bluetooth discovery thread");
        threadPaused = true;
        adapter.cancelDiscovery();
    }

    public void unpauseDiscoverLoopThread() {
        ScatterLogManager.v(TAG,"Resuming bluetooth discovery thread");
        threadPaused = false;
    }

    public LocalPeer getPeerByLuid(String luid) {
        return connectedList.get(luid);
    }
    public LocalPeer getPeerByLuid(byte[] luid) {
        return connectedList.get(new String(luid));
    }

    public void sendMessageToBroadcast(byte[] message, boolean text) {
        ScatterLogManager.v(TAG, "Sendint message to " + connectedList.size() + " local peers");
        for(Map.Entry<String, LocalPeer> ent : connectedList.entrySet()) {
            sendMessageToLocalPeer(ent.getKey(),message, text);
        }
    }

    public void sendMessageToLocalPeer(final String luid, final byte[] message,final  boolean text) {
        ScatterLogManager.v(TAG, "Sending message to peer " + luid);
       final LocalPeer target = trunk.blman.getPeerByLuid(luid);
        BlockDataPacket blockDataPacket = new BlockDataPacket(message, text, target.profile,
                trunk.mainService.luid);
        Thread messageSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int x=0;x<5;x++) {
                    if (target.socket.isConnected()) {
                        try {
                            byte[] tmp = {5,5,5,5,5,5};
                            target.socket.getOutputStream().write(
                                    new BlockDataPacket(message,text, target.profile,tmp).getContents());
                            ScatterLogManager.v(TAG, "Sent message successfully to " + luid );
                            break;
                        } catch (IOException e) {
                            ScatterLogManager.e(TAG, "Error on sending message to " + luid);
                        }
                    }
                    else{
                        break; //we moved out of range
                    }
                }
            }
        });

        messageSendThread.start();
    }

}