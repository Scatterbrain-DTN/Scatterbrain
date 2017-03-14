package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Thread started to wait for messages sent by peers
 */
public class ScatterReceiveThread extends Thread{
    BluetoothSocket socket;
    NetTrunk trunk;
    public boolean fake;
    public ScatterReceiveThread(BluetoothSocket socket, boolean fake) {
        this.fake = fake;
        this.socket = socket;
        this.trunk = ScatterRoutingService.getNetTrunk();
    }


    @Override
    public void run() {
        int errorcount = 0;
        while(true) {
            try {
                errorcount = 0;
                int pos = 0;

                byte[] header = new byte[18];

                if(socket.getInputStream().read(header) == -1) {
                    ScatterLogManager.e(trunk.blman.TAG, "Received an incomplete blockdata header");
                    continue;
                }


                BlockDataPacket intermediate_header = new BlockDataPacket(header);
                if(intermediate_header.isInvalid()) {
                    ScatterLogManager.e(trunk.blman.TAG, "Received a corrupt blockdata header");
                    continue;
                }
                ScatterLogManager.v("Receive", "Got header with size " + intermediate_header.size);


                byte[] buffer = new byte[intermediate_header.size+18];

                for(int i=0;i<header.length; i++) {
                    buffer[i] = header[i];
                }

                byte[] block = new byte[100];
                int counter = 18;
                while(socket.getInputStream().read(block) != -1) {
                    for(int x=0;(x<block.length) && (counter < buffer.length);x++) {
                        buffer[counter] = block[x];
                        counter++;
                    }
                    if(counter >= buffer.length)
                        break;
                }
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
