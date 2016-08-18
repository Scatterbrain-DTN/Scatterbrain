package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;

/**
 * Thread started to wait for messages sent by peers
 */
public class ScatterReceiveThread extends Thread{
    BluetoothSocket socket;
    NetTrunk trunk;
    public ScatterReceiveThread(BluetoothSocket socket) {
        this.socket = socket;
        this.trunk = ScatterRoutingService.getNetTrunk();
    }


    @Override
    public void run() {
        while(true) {
            try {
                byte[] buffer = new byte[50];
                socket.getInputStream().read(buffer);
                Log.v(trunk.blman.TAG, "Received a stanza!!");
                trunk.blman.onSuccessfulReceive(buffer);
            }
            catch (IOException e) {
                Log.e(trunk.blman.TAG, "IOException when receiving stanza");
            }
        }
    }
}
