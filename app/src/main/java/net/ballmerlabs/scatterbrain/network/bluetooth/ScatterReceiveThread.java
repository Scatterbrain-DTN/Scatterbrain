package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;
import java.util.Arrays;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Thread started to wait for messages sent by peers
 */
class ScatterReceiveThread extends Thread{
    private final BluetoothSocket socket;
    private final NetTrunk trunk;
    private int errcount;
    private final boolean fake;
    private boolean go;
    public ScatterReceiveThread(BluetoothSocket socket) {
        this.fake = false;
        this.socket = socket;
        this.trunk = ScatterRoutingService.getNetTrunk();
        go = true;
        errcount = 0;
    }


    @SuppressWarnings("unused")
    public boolean getFake() {
        return fake;
    }

    @SuppressWarnings("unused")
    public int getErrcount() {
        return errcount;
    }


    @Override
    public void run() {
        int errorcount = 0;
        while(go) {
            try {
                errorcount = 0;

                byte[] header = new byte[BlockDataPacket.HEADERSIZE];

                if(socket.getInputStream().read(header) == -1) {
                    ScatterLogManager.e(trunk.blman.TAG, "Received an incomplete blockdata header");
                    continue;
                }

                int size = BlockDataPacket.getSizeFromData(header);
               // ScatterLogManager.v("Receive", "Got header with size " +size);

                int file =  BlockDataPacket.getFileStatusFromData(header);


                if(file < 0)
                    continue;
                else if(file == 0) {

                    //temporary 15mb filesize limit. Sorry.
                    if (size < 0 || size > 15728640)
                        continue;

                    byte[] buffer = new byte[size +
                            BlockDataPacket.HEADERSIZE];

                    System.arraycopy(header, 0, buffer, 0, header.length);

                    byte[] block = new byte[100];
                    int counter = BlockDataPacket.HEADERSIZE;
                    while (socket.getInputStream().read(block) != -1) {
                        for (int x = 0; (x < block.length) && (counter < buffer.length); x++) {
                            buffer[counter] = block[x];
                            counter++;
                        }
                        if (counter >= buffer.length)
                            break;
                    }
                    // ScatterLogManager.v(trunk.blman.TAG, "Received a stanza!!");

                    trunk.blman.onSuccessfulReceive(buffer);
                }
                else if(file == 1) {

                }

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
                errcount++;
                if(errcount > 20) {
                    synchronized (trunk.blman.connectedList) {
                        trunk.blman.connectedList.remove(socket.getRemoteDevice().getAddress());
                    }
                    go = false;
                    try {
                        socket.close();
                    }
                    catch(IOException c) {
                        ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(c.getStackTrace()));
                    }

                    break;
                }

            }
            catch(Exception e) {
                ScatterLogManager.e(trunk.blman.TAG, "Generic exception in ScatterReciveThread:\n" +
                        Arrays.toString(e.getStackTrace()));
            }
        }
    }
}