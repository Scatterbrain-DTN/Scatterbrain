package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a thread used for asychronous connections to bluetooth
 * peers. It calls a callback function on successful connect
 */
public class ScatterConnectThread extends Thread {
    private  BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private ScatterBluetoothManager bleman;
    private NetTrunk trunk;
    public boolean success = false;
    public ScatterConnectThread(BluetoothDevice device, NetTrunk trunk) {

        this.trunk = trunk;
        mmDevice = device;
        this.bleman = trunk.blman;
        mmSocket = null;

    }

    public void run() {
        bleman.pauseDiscoverLoopThread();
        BluetoothSocket tmp = null;
        try {
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(this.bleman.UID);
        }
        catch(IOException e) {

        }
        mmSocket = tmp;
        Log.v(trunk.blman.TAG,"Attempting to connect");
        try {
            Handler handler = new Handler(trunk.mainService.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!success) {
                            Log.e(trunk.blman.TAG, "Socket timed out durring connect");
                            bleman.unpauseDiscoverLoopThread();
                            mmSocket.close();
                        }
                    }
                    catch(IOException e) {
                        bleman.unpauseDiscoverLoopThread();
                    }
                }
            },600);
            mmSocket.connect();
            success = true;
            //call this function in the context of the bluetoothManager
            bleman.unpauseDiscoverLoopThread();
            Log.v(trunk.blman.TAG, "Connection successful");
        }
        catch(IOException e) {
            Log.e(trunk.blman.TAG, "Failed to connect, IOException");
            // e.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException c) {

            }

        }
        finally {
            trunk.blman.onSuccessfulConnect(mmSocket);

        }


    }

}
