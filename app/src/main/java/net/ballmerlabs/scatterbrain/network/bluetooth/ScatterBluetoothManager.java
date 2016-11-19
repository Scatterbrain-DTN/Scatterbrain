package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;


import net.ballmerlabs.scatterbrain.DispMessage;
import net.ballmerlabs.scatterbrain.NormalActivity;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Abstraction for android bluetooth stack for Scatterbrain.
 */
public class ScatterBluetoothManager {
    public final String TAG = "BluetoothManager";

    //UUID for scatterbrain bluetooh. If you want to make a 3rd party app, plz copy this.
    public final java.util.UUID UID = UUID.fromString("cc1f06c5-ce01-4538-bc15-2a1d129c8b28");

    public final String NAME = "Scatterbrain";
    public BluetoothAdapter adapter;
    public final static int REQUEST_ENABLE_BT = 1;
    public ArrayList<BluetoothDevice> foundList;
    public ArrayList<String> blackList; //todo: clear blacklist after some time
    public HashMap<byte[], LocalPeer> connectedList;
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

    /* listens for events thrown by bluetooth adapter when scanning for devices
     * and calls actions for different scenarios.
     */
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            //if find a device, add it to a temprorary found list.
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                ScatterLogManager.v(TAG, "Found a bluetooth device!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundList.add(device);
            }

            //when we are done scanning, attempt to connect to devices we found
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ScatterLogManager.v(TAG, "Device disvovery finished. Attempting to connect to peers");
                Thread discoveryFinishedThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectToDevice(foundList);
                        foundList.clear();
                    }
                });
                discoveryFinishedThread.start();

                //prune any peer that disconnected from the connected list
                final Thread prunePeer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<byte[], LocalPeer> s : connectedList.entrySet()) {
                            if (!s.getValue().socket.isConnected()) {
                                ScatterLogManager.v(TAG, "Removing unneeded device " + s.getKey().toString());
                                connectedList.remove(s);
                            }
                        }
                    }
                });
                prunePeer.start();

                //if we are still turned on, rescan later at interval defined by settings
                if (runScanThread) {
                    int scan = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(trunk.mainService).getString("sync_frequency", "7"));
                    ScatterLogManager.v(TAG,"Posting new scanning runnable (running)");
                    bluetoothHan.postDelayed(scanr, scan * 1000);
                } else
                    ScatterLogManager.v(TAG, "Stopping wifi direct scan thread");
            }
        }
    };


    //this should return a handler object later
    public void connectToDevice(List<BluetoothDevice> device) {
        if (!isAccepting) {
            ScatterConnectThread currentconnection;
            currentconnection = new ScatterConnectThread(device, trunk);
            currentconnection.start();
        }
        else {
            ScatterLogManager.e(TAG, "Tried to connect, but accept thread is dead for some reason");
        }
    }

    //constructor
    public ScatterBluetoothManager(NetTrunk trunk) {
        this.trunk = trunk;
        looper = new BluetoothLooper(trunk.globnet);
        bluetoothHan = new Handler();
        foundList = new ArrayList<>();
        connectedList = new HashMap<>();
        blackList = new ArrayList<>();
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

    //some initialization routines that can't be called at the same time as constructur.
    public void init() {
        if (!acceptThreadRunning) {
            acceptThread = new ScatterAcceptThread(trunk, adapter);
            acceptThread.start();
        }
    }

    //returns a handle to the bluetooth adapter object
    public BluetoothAdapter getAdapter() {
        return adapter;
    }


    //start the scatterbrain discovery loop (ie. start the routing service fully)
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
                else if(runScanThread){
                    int scan = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(trunk.mainService).getString("sync_frequency", "7"));
                    ScatterLogManager.v(TAG,"Posting new scanning runnable (paused)");
                    bluetoothHan.postDelayed(scanr, scan * 1000);
                }
            }
        };

        bluetoothHan.post(scanr);
    }


    //function called when a packet is received from a connected device
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
                    trunk.mainService.dataStore.enqueueMessage(bd);
                    ScatterLogManager.e(TAG, "Appended message to message list");
                }
            });
        }
        else
            ScatterLogManager.e(TAG, "received a non-text message in a text context");
    }


    //function called when we connect to a nearby peer
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
                    connectedList.put(inpacket.luid, new LocalPeer(inpacket.convertToProfile(), socket));
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
        catch(Exception e) {
            ScatterLogManager.e(TAG, "Generic error in onSucessfulConnect");
        }
    }

    //attempt to intelligently transmit a number of packets from datastore to peer nearby
    public void offloadRandomPackets(int count) {
        final ArrayList<BlockDataPacket> ran = trunk.mainService.dataStore.getTopRandomMessages(count);
        pauseDiscoverLoopThread();
        Thread offloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(BlockDataPacket p : ran) {
                    sendMessageToBroadcast(p.contents,true);
                }
            }
        });
        offloadThread.start();
    }

    //stops (and kills) the discovery thread
    public synchronized void stopDiscoverLoopThread() {
        ScatterLogManager.v(TAG, "Stopping bluetooth discovery thread");
        runScanThread = false;
        adapter.cancelDiscovery();
    }

    //temporarilly stops the discovery thread with the option to quickly resume without loss of data
    public synchronized void pauseDiscoverLoopThread() {
        if(!threadPaused) {
            ScatterLogManager.v(TAG, "Pausing bluetooth discovery thread");
            threadPaused = true;
            adapter.cancelDiscovery();
        }
    }

    //resumes after calling pauseDiscoverLoopThread()
    public void unpauseDiscoverLoopThread() {
        if(threadPaused) {
            ScatterLogManager.v(TAG, "Resuming bluetooth discovery thread");
            threadPaused = false;
        }
    }

    //grabs a nearby connected device by local user ID
    public LocalPeer getPeerByLuid(byte[] luid) {
        return connectedList.get(luid);
    }


    //sends a BlockDataPacket to all connected peers
    public void sendMessageToBroadcast(byte[] message, boolean text) {
        ScatterLogManager.v(TAG, "Sendint message to " + connectedList.size() + " local peers");
        for(Map.Entry<byte[], LocalPeer> ent : connectedList.entrySet()) {
            sendMessageToLocalPeer(ent.getKey(),message, text);
        }
    }

    //send a direct private message to a nearby peer in a BlockDataPacket
    public void sendMessageToLocalPeer(final byte[] luid, final byte[] message,final  boolean text) {
        ScatterLogManager.v(TAG, "Sending message to peer " + luid);
       final LocalPeer target = trunk.blman.getPeerByLuid(luid);
        final BlockDataPacket blockDataPacket = new BlockDataPacket(message, text,
                trunk.mainService.luid);
        Thread messageSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int x=0;x<5;x++) {
                    trunk.dataStore.enqueueMessage(blockDataPacket);
                    if (target.socket.isConnected()) {
                        try {
                            byte[] tmp = {5,5,5,5,5,5};
                            target.socket.getOutputStream().write(
                                    new BlockDataPacket(message,text,tmp).getContents());
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
