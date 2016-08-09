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
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
        public void onReceive(Context context,final Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.v(TAG,"Found a bluetooth device!");
                Thread devCreateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        tmpList.add(device);
                        connectToDevice(device);
                    }
                });
                devCreateThread.start();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
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
        Log.v(TAG, "Starting wifi direct scan thread");
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

    public synchronized void onSucessfulAccept(BluetoothSocket socket) {

    }

    public synchronized void onSuccessfulConnect(BluetoothDevice device, BluetoothSocket socket) {

    }

    public void stopDiscoverLoopThread() {
        runScanThread = false;
        adapter.cancelDiscovery();
    }

}