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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    public HashMap<String,LocalPeer> connectedList;
    public NetTrunk trunk;
    public boolean runScanThread;
    public Handler bluetoothHan;
    public BluetoothLooper looper;
    public IntentFilter filter;
    public  Runnable scanr;
    public ScatterAcceptThread acceptThread;
    public ScatterConnectThread currentconnection;
    public boolean isAccepting;
    public boolean acceptThreadRunning;

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.v(TAG, "Found a bluetooth device!");
                Thread devCreateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        tmpList.add(device);
                        connectToDevice(device);
                    }
                });
                devCreateThread.start();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Thread discoveryFinishedThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        foundList = (ArrayList<BluetoothDevice>) tmpList.clone();
                        tmpList.clear();
                        //for(BluetoothDevice d : foundList)  {

                        // }
                    }
                });
                discoveryFinishedThread.start();
                if (runScanThread)
                    bluetoothHan.postDelayed(scanr, 10000);
                else
                    Log.v(TAG, "Stopping wifi direct scan thread");
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                //TODO: find some parcelable extra to avoid
                Thread prunePeer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(Map.Entry<String,LocalPeer > s : connectedList.entrySet()) {
                            if(!s.getValue().socket.isConnected()) {
                                Log.v(TAG, "Removing unneeded device " + s.getKey().toString());
                                connectedList.remove(s);
                            }
                        }
                    }
                });

            }
        }


    };

    //this should return a handler object later
    public void connectToDevice(BluetoothDevice device) {
        if(!isAccepting) {
            currentconnection = new ScatterConnectThread(device, trunk);
            currentconnection.run();
        }
    }

    public ScatterBluetoothManager(NetTrunk trunk) {
        this.trunk = trunk;
        looper = new BluetoothLooper(trunk.globnet);
        bluetoothHan = new Handler();
        runScanThread =false;
        foundList = new ArrayList<>();
        tmpList = new ArrayList<>();
        connectedList = new HashMap<>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.filter = filter;
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        trunk.mainService.registerReceiver(mReceiver,filter);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "ERROR, bluetooth not supported");
        }
        isAccepting = false;
        acceptThreadRunning = false;
    }

    public void init() {
        if(!acceptThreadRunning) {
            acceptThread = new ScatterAcceptThread(trunk, adapter);
            acceptThread.start();
        }
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public void startDiscoverLoopThread() {
        Log.v(TAG, "Starting bluetooth scan thread");
        runScanThread = true;
        bluetoothHan =looper.getHandler();
        scanr = new Runnable() {
            @Override
            public void run() {
                //directmanager.scan();
                //
                Log.v(TAG, "Scanning...");

                adapter.startDiscovery();
            }
        };
        bluetoothHan.post(scanr);
    }

    public void onSuccessfulReceive(byte[] incoming) {
        if(!NormalActivity.active)
            trunk.mainService.startMessageActivity();
        BlockDataPacket bd = new BlockDataPacket(incoming);
        if(bd.isInvalid())
            Log.e(TAG, "Received corrupt blockdata packet.");
        else if(bd.text) {
            trunk.mainService.getMessageAdapter().add(new String(bd.body));
            Log.e(TAG, "Appended message to message list");
        }
        else
            Log.e(TAG, "received a non-text message in a text context");
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
                    Log.v(TAG, "Adding new device " + inpacket.convertToProfile().getLUID());
                    connectedList.put(new String(inpacket.luid), new LocalPeer(inpacket.convertToProfile(), socket));
                    Log.v(TAG, "List size = " + connectedList.size());
                }
                else {
                    Log.e(TAG, "Received an advertise stanza, but it is invalid");
                }


            }

        }
        catch(IOException c) {
            Log.e(TAG, "IOException in onSuccessfulConnect");

        }
        startDiscoverLoopThread();
    }

    public void stopDiscoverLoopThread() {
        Log.v(TAG, "Stopping bluetooth discovery thread");
        runScanThread = false;
        adapter.cancelDiscovery();
    }

    public LocalPeer getPeerByLuid(String luid) {
        return connectedList.get(luid);
    }
    public LocalPeer getPeerByLuid(byte[] luid) {
        return connectedList.get(new String(luid));
    }

    public void sendMessageToBroadcast(byte[] message, boolean text) {
        Log.v(TAG, "Sendint message to " + connectedList.size() + " local peers");
        for(Map.Entry<String, LocalPeer> ent : connectedList.entrySet()) {
            sendMessageToLocalPeer(ent.getKey(),message, text);
        }
    }

    public void sendMessageToLocalPeer(final String luid, final byte[] message,final  boolean text) {
        Log.v(TAG, "Sending message to peer " + luid);
       final LocalPeer target = trunk.blman.getPeerByLuid(luid);
        BlockDataPacket blockDataPacket = new BlockDataPacket(message, text, target.profile,
                trunk.mainService.luid);
        Thread messageSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int x=0;x<5;x++) {
                    if (target.socket.isConnected()) {
                        try {
                            target.socket.getOutputStream().write(
                                    new BlockDataPacket(message,text, target.profile,trunk.mainService.luid).getContents());
                            Log.v(TAG, "Sent message successfully to " + luid);
                            break;
                        } catch (IOException e) {
                            Log.e(TAG, "Error on sending message to " + luid);
                        }
                    }
                    else{
                        break; //we moved out of range
                    }
                }
            }
        });
    }

}