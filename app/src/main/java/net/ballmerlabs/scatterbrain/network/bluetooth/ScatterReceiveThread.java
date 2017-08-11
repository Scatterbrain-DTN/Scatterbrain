package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Thread started to wait for messages sent by peers
 */
class ScatterReceiveThread extends Thread{
    private final BluetoothSocket socket;
    private final Socket fakesocket;
    private final NetTrunk trunk;
    private int errcount;
    private final boolean fake;
    private boolean go;
    public ScatterReceiveThread(BluetoothSocket socket) {
        this.fake = false;
        this.socket = socket;
        this.fakesocket = null;
        this.trunk = ScatterRoutingService.getNetTrunk();
        go = true;
        errcount = 0;
    }

    public ScatterReceiveThread(Socket socket) {
        this.fake = true;
        this.fakesocket = socket;
        this.socket = null;
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


                if(!fake) {
                    if (socket.getInputStream().read(header) == -1) {
                        ScatterLogManager.e(trunk.blman.TAG, "Received an incomplete blockdata header");
                        continue;
                    }
                } else {
                    if (fakesocket.getInputStream().read(header) == -1) {
                        ScatterLogManager.e(trunk.blman.TAG, "Received an incomplete blockdata header");
                        continue;
                    }
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

                    if(!fake) {
                        while (socket.getInputStream().read(block) != -1) {
                            for (int x = 0; (x < block.length) && (counter < buffer.length); x++) {
                                buffer[counter] = block[x];
                                counter++;
                            }
                            if (counter >= buffer.length)
                                break;
                        }
                    } else {
                        while (fakesocket.getInputStream().read(block) != -1) {
                            for (int x = 0; (x < block.length) && (counter < buffer.length); x++) {
                                buffer[counter] = block[x];
                                counter++;
                            }
                            if (counter >= buffer.length)
                                break;
                        }
                    }
                    // ScatterLogManager.v(trunk.blman.TAG, "Received a stanza!!");

                    trunk.blman.onSuccessfulReceive(buffer);
                }
                else if(file == 1) {
                    BlockDataPacket bd;

                    if(!fake) {
                        bd = new BlockDataPacket(header, socket.getInputStream());
                    } else {
                        bd = new BlockDataPacket(header, fakesocket.getInputStream());
                    }
                    ScatterLogManager.v(trunk.blman.TAG, "Recieved packet len " + size + " streamlen " + bd.streamlen);
                    if(bd.isInvalid()) {
                        ScatterLogManager.e(trunk.blman.TAG, "Recieved corrupt filepacket");
                        continue;
                    }

                    trunk.blman.onSuccessfulFileRecieve(bd);
                }

            }
            catch (IOException e) {
                errorcount++;
                if(errorcount > 50) {
                    try {
                        if(!fake) {
                            socket.close();
                        } else {
                            fakesocket.close();
                        }
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
                        if(!fake) {
                            trunk.blman.connectedList.remove(socket.getRemoteDevice().getAddress());
                        }
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