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
import android.os.Parcelable;
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
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final BluetoothAdapter adapter;
    public final static int REQUEST_ENABLE_BT = 1;
    private final ArrayList<BluetoothDevice> foundList;
    public final HashMap<String, LocalPeer> connectedList; //discovered devices
    private final ArrayList<BluetoothDevice> scatterList; //devices confirmed to run scatterbrain
    private final NetTrunk trunk;
    private boolean runScanThread;
    private Handler bluetoothHan;
    private final BluetoothLooper looper;
    private Runnable scanr;
    private final boolean isAccepting;
    public boolean acceptThreadRunning;
    private boolean threadPaused;
    private int currentUUID; //the device we are currently querying for uuid.
    private int targetUUID; //the number of devices to stop at
    @SuppressWarnings("FieldCanBeLocal")
    private final int PARALLELUUID = 1; //number of devices to scan at a time.
    private Method setDuration;


    /* listens for events thrown by bluetooth adapter when scanning for devices
     * and calls actions for different scenarios.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            //if find a device, add it to a temprorary found list.
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                ScatterLogManager.v(TAG, "Found a bluetooth device!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                synchronized (foundList) {
                    foundList.add(device);
                }
            }

            //when we are done scanning, attempt to connect to devices we found
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ScatterLogManager.v(TAG, "Device disvovery finished.");

                resetBluetoothDiscoverability();
                //stop discovering and attempt to gently fetch UUIDs from devices.
                //This can fail with an overloaded adapter or saturated channels, so we do it slowly.
                pauseDiscoverLoopThread();
                synchronized (foundList) {
                    targetUUID = foundList.size();
                    for (int x = currentUUID; x < foundList.size() && x < (PARALLELUUID + currentUUID); x++) {
                        foundList.get(x).fetchUuidsWithSdp();
                    }
                }
                if(currentUUID < targetUUID)
                    currentUUID += PARALLELUUID;
                else {
                    currentUUID = 0;
                    connectToDevice(scatterList);
                    unpauseDiscoverLoopThread();
                }

                //if we are still turned on, rescan later at interval defined by settings
                if (runScanThread) {
                    int scan = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(trunk.mainService).getString("sync_frequency", "7"));
                    ScatterLogManager.v(TAG,"Posting new scanning runnable (running)");
                    bluetoothHan.postDelayed(scanr, scan * 1000);
                } else
                    ScatterLogManager.v(TAG, "Stopping wifi direct scan thread");
            }

            //handle UUID events. May not be a good idea to do this when we are discovering.
            else if (BluetoothDevice.ACTION_UUID.equals(action)) {
                Parcelable[] p = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(p != null) {
                    for(Parcelable uu : p) {
                        if(uu != null) {
                            if(UID.toString().compareTo(uu.toString()) == 0) {
                                ScatterLogManager.v(TAG, "found a scatterbrain UUID");
                                scatterList.add(device);
                            }
                            else {
                                ScatterLogManager.v(TAG, "found UUID " + uu.toString());
                            }
                        }
                    }
                }

                synchronized (foundList) {
                    for (int x = currentUUID; x < foundList.size() && x < (PARALLELUUID + currentUUID); x++) {
                        foundList.get(x).fetchUuidsWithSdp();
                    }
                }
                if(currentUUID < targetUUID)
                    currentUUID += PARALLELUUID;
                else {
                    currentUUID = 0;
                    connectToDevice(scatterList);
                    unpauseDiscoverLoopThread();
                }
            }
        }
    };


    //this should return a handler object later
    private void connectToDevice(List<BluetoothDevice> device) {
        foundList.clear();
        if (!isAccepting) {
            ScatterConnectThread currentconnection;
            currentconnection = new ScatterConnectThread(device, trunk);
            bluetoothHan.post(currentconnection);
        }
        else {
            ScatterLogManager.e(TAG, "Tried to connect, but accept thread is dead for some reason");
        }
    }

    //constructor
    public ScatterBluetoothManager(NetTrunk trunk) {
        this.trunk = trunk;
        looper = new BluetoothLooper();
        bluetoothHan = new Handler();
        foundList = new ArrayList<>();
        connectedList = new HashMap<>();
        scatterList = new ArrayList<>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        currentUUID = 0;
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        trunk.mainService.registerReceiver(mReceiver, filter);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter !=  null) {
            ScatterLogManager.e(TAG, "ERROR, bluetooth not supported");
        }
        isAccepting = false;
        acceptThreadRunning = false;
        threadPaused = false;
        try {
            if(adapter != null) {
                Class c = Class.forName(adapter.getClass().getName());
                Method[] methods = c.getDeclaredMethods();
                setDuration = null;
                    /* why on earth do I have to do this?
                     * for some reason getDeclaredMethod("setDiscoverableTimeout") can't find the
                     * method, but this can.
                     */
                for (Method m : methods) {
                    if (m.getName().contains("setScanMode"))
                        setDuration = m;
                }
            }
        }
        catch(Exception e) {
            ScatterLogManager.e(TAG,e.getMessage());
        }
    }

    //some initialization routines that can't be called at the same time as constructur.
    public void init() {
        if (!acceptThreadRunning) {
            ScatterAcceptThread acceptThread = new ScatterAcceptThread(trunk, adapter);
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
                ScatterLogManager.v(TAG, "Scanning...");

                if(!threadPaused) {
                    if (adapter != null)
                        adapter.startDiscovery();
                }
                else if(runScanThread){
                    int scan = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(trunk.mainService).getString("sync_frequency", "7"));
               //     ScatterLogManager.v(TAG,"Posting new scanning runnable (paused)");
                    bluetoothHan.postDelayed(scanr, scan * 1000);
                }

            }
        };

        bluetoothHan.post(scanr);
    }


    //function called when a packet is received from a connected device
    public void onSuccessfulReceive(byte[] incoming) {
      //  ScatterLogManager.v(TAG, "Called onSuccessfulReceive for incoming message");
        if (!NormalActivity.active)
            trunk.mainService.startMessageActivity();
        final BlockDataPacket bd = new BlockDataPacket(incoming);
        if (bd.isInvalid()) {
            ScatterLogManager.e(TAG, "Received corrupt blockdata packet.");
            return;
        }
        if(trunk.mainService.dataStore.enqueueMessageNoDuplicate(bd) ==0) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (NormalActivity.active) {
                        trunk.mainService.getMessageAdapter().data.add(new DispMessage(new String(bd.body),
                                Base64.encodeToString(bd.senderluid, Base64.DEFAULT)));
                        trunk.mainService.getMessageAdapter().notifyDataSetChanged();
                        //    ScatterLogManager.e(TAG, "Appended message to message list");
                    }
                }
            });
        }
    }


    //function called when we connect to a nearby peer
    public void onSuccessfulConnect(BluetoothSocket socket) {
        try {
            InputStream i = socket.getInputStream();
            OutputStream o = socket.getOutputStream();
            trunk.mainService.noticeNotify();
            AdvertisePacket outpacket = new AdvertisePacket(trunk.profile);
            o.write(outpacket.getContents());
            byte[] buffer = new byte[AdvertisePacket.PACKET_SIZE];
            if(i.read(buffer) == AdvertisePacket.PACKET_SIZE) {
                AdvertisePacket inpacket;
                inpacket = new AdvertisePacket(buffer);
                if (!inpacket.isInvalid()) {

                    ScatterLogManager.v(TAG, "Adding new device " + Arrays.toString(inpacket.convertToProfile().getLUID()));
                    synchronized (connectedList) {
                        connectedList.put(socket.getRemoteDevice().getAddress(), new LocalPeer(inpacket.convertToProfile(), socket));

                        //   ScatterLogManager.v(TAG, "List size = " + connectedList.size());
                    }

                    synchronized (connectedList) {
                        trunk.mainService.updateUiOnDevicesFound(connectedList);
                    }

                } else {
                    byte b = inpacket.getContents()[0];
                    ScatterLogManager.e(TAG, "Received an advertise stanza, but it is invalid (" + b + ")");
                    socket.close();
                }
            }
            else {
                ScatterLogManager.e(TAG, "Received an advertisePacket with a wrong size");
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
    public void offloadRandomPacketsToBroadcast() {
        synchronized (connectedList) {
            for (Map.Entry<String, LocalPeer> ent : connectedList.entrySet()) {
                offloadRandomPackets(500, ent.getKey());
            }
        }
    }

    //sends a random set of packets from the datastore to a nearby device
    @SuppressWarnings("SameParameterValue")
    private void offloadRandomPackets(int count, final String device) {
        final ArrayList<BlockDataPacket> ran = trunk.mainService.dataStore.getTopRandomMessages(count);
        pauseDiscoverLoopThread();
        for (BlockDataPacket p : ran) {
            if (p.isInvalid()) {
                ScatterLogManager.e(TAG, "sent invalid packet with offloadRandomPackets()");
            }
            sendRaw(device, p.contents, false);
        }
        unpauseDiscoverLoopThread();
    }

    //stops (and kills) the discovery thread
    public synchronized void stopDiscoverLoopThread() {
        ScatterLogManager.v(TAG, "Stopping bluetooth discovery thread");
        runScanThread = false;
        if(adapter != null)
            adapter.cancelDiscovery();
    }

    //temporarilly stops the discovery thread with the option to quickly resume without loss of data
    public synchronized void pauseDiscoverLoopThread() {
        if(!threadPaused) {
            ScatterLogManager.v(TAG, "Pausing bluetooth discovery thread");
            threadPaused = true;
            if(adapter != null)
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


    //sends a BlockDataPacket to all connected peers
    @SuppressWarnings("unused")
    public void sendMessageToBroadcast(byte[] message, boolean text) {
        synchronized (connectedList) {
           // ScatterLogManager.v(TAG, "Sendint message to " + connectedList.size() + " local peers");
            for (Map.Entry<String, LocalPeer> ent : connectedList.entrySet()) {
                sendMessageToLocalPeer(ent.getKey(), message, text);
            }
        }
    }

    /*
     * Send a message to an already connected scatterbrain peer.
     * This method also has a function to connect to a local tcp debug
     * server outside of android for unit testing.
     */
    private void sendMessageToLocalPeer(final String mactarget, final byte[] message,
                                        final boolean text) {
        //ScatterLogManager.v(TAG, "Sending message to peer " + mactarget);
        BlockDataPacket bd = new BlockDataPacket(message,text, trunk.mainService.luid );
        sendRaw(mactarget,bd.getContents(), false);
    }


    //sends a BlockDataPacket to all connected peers
    public void sendRawToBroadcast(byte[] message) {
        synchronized (connectedList) {
            //ScatterLogManager.v(TAG, "Sending RAW message to " + connectedList.size() + " local peers");
            for (Map.Entry<String, LocalPeer> ent : connectedList.entrySet()) {
                sendRaw(ent.getKey(), message, false);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void sendRaw(final String mactarget, final byte[] message, final boolean fake) {
        //ScatterLogManager.v(TAG, "Sending message to peer " + mactarget);

        final OutputStream ostream;
        final Socket sock;
        final boolean isConnected;
        if(!fake) {
            LocalPeer target;
            synchronized (connectedList) {
                target = connectedList.get(mactarget);
            }
            target.socket.isConnected();
            try {
                ostream = target.socket.getOutputStream();
                isConnected = true;
            }
            catch(IOException e) {
                ScatterLogManager.e(TAG, "IOException on sending packet to " + mactarget);
                return;
            }
        }
        else {
            try {
                sock = new Socket(InetAddress.getByName("127.0.0.1"), 8877);
                ostream = sock.getOutputStream();
                isConnected = true;
            }
            catch(UnknownHostException u) {
                System.out.println("Cannot connect to local debug server");
                return;
            }
            catch(IOException e) {
                System.out.println("IOException when connecting to local debug server");
                return;
            }

        }
        final BlockDataPacket blockDataPacket = new BlockDataPacket(message);
        Runnable messageSendThread = new Runnable() {
            @Override
            public void run() {
                if (!fake)
                    trunk.mainService.dataStore.enqueueMessageNoDuplicate(blockDataPacket);
                //noinspection ConstantConditions
                if (isConnected) {
                    try {
                        if (blockDataPacket.invalid) {
                            ScatterLogManager.e(TAG, "Tried to send a corrupt packet");
                            return;
                        }
                        ostream.write(blockDataPacket.getContents());
                        //ScatterLogManager.v(TAG, "Sent message successfully to " + mactarget );
                    } catch (IOException e) {

                        ScatterLogManager.e(TAG, "Error on sending message to " + mactarget);
                    }
                }
            }
        };

        bluetoothHan.post(messageSendThread);
    }

    public void resetBluetoothDiscoverability() {
        try {
            setDuration.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 300);
        }
        catch(Exception e) {
            ScatterLogManager.e(TAG, e.getMessage());
        }

    }


}
