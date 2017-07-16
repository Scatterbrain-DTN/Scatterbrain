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
import java.util.List;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Represents a thread used for asychronous connections to bluetooth
 * peers. It calls a callback function on successful connect
 */
public class ScatterConnectThread extends Thread {
    private  BluetoothSocket mmSocket;
    private final List<BluetoothDevice> devicelist;
    private ScatterBluetoothManager bleman;
    private NetTrunk trunk;
    public boolean success = false;
    public ScatterConnectThread(final List<BluetoothDevice> devicelist, NetTrunk trunk) {

        this.trunk = trunk;
        this.devicelist = devicelist;
        this.bleman = trunk.blman;
        mmSocket = null;

    }

    public void run() {
        bleman.pauseDiscoverLoopThread();
        ScatterLogManager.v(trunk.blman.TAG, "Attempting to connect to " +devicelist.size() + " devices");
        for(BluetoothDevice mmDevice : devicelist) {
            synchronized (trunk.blman.connectedList) {
                if (!trunk.blman.connectedList.containsKey(mmDevice.getAddress())) {
                    success = false;
                    BluetoothSocket tmp = null;
                    try {
                        tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(this.bleman.UID);
                    } catch (IOException e) {
                        ScatterLogManager.e(trunk.blman.TAG, e.getStackTrace().toString());

                    }
                    mmSocket = tmp;
                    try {

                        mmSocket.connect();
                        if (mmSocket.isConnected()) {
                            success = true;

                            //call this function in the context of the bluetoothManager
                            ScatterLogManager.v(trunk.blman.TAG, "Connection successful");
                            trunk.blman.onSuccessfulConnect(mmSocket);
                        } else {
                            ScatterLogManager.e(trunk.blman.TAG, "Connection raised no exception, but failed");
                        }
                    } catch (IOException e) {
                        ScatterLogManager.e(trunk.blman.TAG, "Failed to connect, IOException");
                        // e.printStackTrace();
                        try {
                            mmSocket.close();
                        } catch (IOException c) {
                            ScatterLogManager.e(trunk.blman.TAG, c.getStackTrace().toString());
                        }

                    }
                    if (!success) {
                        //   trunk.blman.blackList.add(mmDevice.getAddress());
                    }
                }
            }
        }

        devicelist.clear();

        trunk.blman.offloadRandomPacketsToBroadcast(500);
        bleman.unpauseDiscoverLoopThread();


    }

}
