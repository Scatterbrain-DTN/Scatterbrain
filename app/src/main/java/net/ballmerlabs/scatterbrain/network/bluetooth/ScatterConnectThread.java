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
 * Represents a thread used for asychronous connections to bluetooth
 * peers. It calls a callback function on successful connect
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
        //call this function in the context of the bluetoothManager
        trunk.blman.onSuccessfulConnect(mmDevice, mmSocket);
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
