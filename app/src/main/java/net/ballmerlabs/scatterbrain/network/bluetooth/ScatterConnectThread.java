package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.IOException;

/**
 * Created by user on 8/1/16.
 */
public class ScatterConnectThread extends Thread {
    private  BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private ScatterBluetoothManager bleman;
    private NetTrunk trunk;
    public ScatterConnectThread(BluetoothDevice device, NetTrunk trunk) {

        this.trunk = trunk;
        mmDevice = device;
        this.bleman = trunk.blman;
        mmSocket = null;

    }

    public void run() {
        BluetoothSocket tmp = null;
        try {
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(this.bleman.UID);
        }
        catch(IOException e) {

        }
        mmSocket = tmp;
        Log.v(trunk.blman.TAG,"Attempting to connect");
        bleman.stopDiscoverLoopThread();
        try {
            mmSocket.connect();
        }
        catch(IOException e) {
            Log.e(trunk.blman.TAG, "Failed to connect, IOException");
            e.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException c) {

            }
            bleman.startDiscoverLoopThread();
            return;
        }

        Log.v(trunk.blman.TAG, "Connection successful");
        trunk.blman.onSucessfulConnect(mmDevice, mmSocket);
        setSenpai();
        try {
            mmSocket.close();
        }
        catch(IOException e) {

        }
        bleman.startDiscoverLoopThread();

    }

    public void setSenpai() {

    }
}
