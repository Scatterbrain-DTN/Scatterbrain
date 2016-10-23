package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
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
        int errorcount = 0;
        while(true) {
            try {
                errorcount = 0;
                byte[] buffer = new byte[1000];
                socket.getInputStream().read(buffer);
                ScatterLogManager.v(trunk.blman.TAG, "Received a stanza!!");
                trunk.blman.onSuccessfulReceive(buffer);

            }
            catch (IOException e) {
                errorcount++;
                if(errorcount > 50) {
                    try {
                        socket.close();
                    }
                    catch(IOException f) {
                        ScatterLogManager.e(trunk.blman.TAG, "Error in receiving thread. Did we disconnect?");
                    }
                    break;
                }

                ScatterLogManager.e(trunk.blman.TAG, "IOException when receiving stanza");
            }
        }
    }
}
