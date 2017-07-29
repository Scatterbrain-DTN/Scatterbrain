package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Represents a thread used for asychronous connections to bluetooth
 * peers. It calls a callback function on successful connect
 */
class ScatterConnectThread extends Thread {
    private  BluetoothSocket mmSocket;
    private final List<BluetoothDevice> devicelist;
    private final ScatterBluetoothManager bleman;
    private final NetTrunk trunk;

    public ScatterConnectThread(final List<BluetoothDevice> devicelist, NetTrunk trunk) {

        this.trunk = trunk;
        this.devicelist = devicelist;
        this.bleman = trunk.blman;
        mmSocket = null;

    }

    public void run() {
        ScatterLogManager.v(trunk.blman.TAG, "Attempting to connect to " +devicelist.size() + " devices");
        for(BluetoothDevice mmDevice : devicelist) {
            synchronized (trunk.blman.connectedList) {
                if (!trunk.blman.connectedList.containsKey(mmDevice.getAddress())) {
                    boolean success = false;
                    BluetoothSocket tmp = null;
                    try {
                        tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(this.bleman.UID);
                    } catch (IOException e) {
                        ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(e.getStackTrace()));

                    }
                    mmSocket = tmp;
                    try {

                        if(mmSocket != null)
                            mmSocket.connect();
                        if (mmSocket != null && mmSocket.isConnected()) {
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
                            ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(c.getStackTrace()));
                        }

                    }
                    //noinspection StatementWithEmptyBody
                    if (!success) {
                        //   trunk.blman.blackList.add(mmDevice.getAddress());
                    }
                }
            }
        }

        devicelist.clear();

        trunk.blman.offloadRandomPacketsToBroadcast();

    }

}
