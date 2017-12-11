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
public class ScatterReceiveThread extends Thread{
    private final BluetoothSocket socket;
    private final Socket fakesocket;
    private final NetTrunk trunk;
    public BlockDataPacket fakeres;
    public boolean fakedone;
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
        this.fakedone = false;
        this.fakesocket = socket;
        this.socket = null;
        this.go = true;
        this.trunk = ScatterRoutingService.getNetTrunk();
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
            if(fake) {
                go = false;
                System.out.println("enter");
            }
            try {
                errorcount = 0;

                byte[] header = new byte[BlockDataPacket.HEADERSIZE];


                if(!fake) {
                    if (socket.getInputStream().read(header) == -1) {
                        ScatterLogManager.e(trunk.blman.TAG, "Received an incomplete blockdata header");
                        continue;
                    }
                } else {
                    System.out.println("preread header");
                    if (fakesocket.getInputStream().read(header) == -1) {
                        System.out.println("Received an incomplete blockdata header");
                        continue;
                    }
                }

                long size = BlockDataPacket.getSizeFromData(header);
               // ScatterLogManager.v("Receive", "Got header with size " +size);

                int file =  BlockDataPacket.getFileStatusFromData(header);



                if(file < 0)
                    continue;
                else if(file == 0) {
                    if(!fake)
                        ScatterLogManager.v(trunk.blman.TAG, "Received blockdata size " + size);
                    //temporary 15mb filesize limit. Sorry.
                    if (size < 0 || size > 15728640)
                        continue;

                    byte[] buffer = new byte[(int) size +
                            BlockDataPacket.HEADERSIZE];

                    System.arraycopy(header, 0, buffer, 0, header.length);

                    byte[] block = new byte[1024];
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
                        System.out.println("preread");
                        while (fakesocket.getInputStream().read(block) != -1) {
                            System.out.println("read " + counter);
                            for (int x = 0; (x < block.length) && (counter < buffer.length); x++) {
                                buffer[counter] = block[x];
                                counter++;
                            }
                            if (counter >= buffer.length)
                                break;
                        }
                    }
                    // ScatterLogManager.v(trunk.blman.TAG, "Received a stanza!!");

                    if(!fake)
                        trunk.blman.onSuccessfulReceive(buffer, false);
                    else {
                        ScatterBluetoothManager blman = new ScatterBluetoothManager(new NetTrunk(new ScatterRoutingService()));
                        blman.onSuccessfulReceive(buffer, true);
                        System.out.println("leave");
                        fakedone = true;
                        fakeres = null;
                        System.out.println("fakereceived nonfile packet");
                        //go = false;
                    }
                }
                else if(file == 1) {
                    BlockDataPacket bd;

                    if(!fake) {
                        bd = new BlockDataPacket(header, socket.getInputStream());
                        ScatterLogManager.v(trunk.blman.TAG, "Received blockdata size " + bd.size);
                    } else {
                        bd = new BlockDataPacket(header, fakesocket.getInputStream());
                        System.out.println( "Recieved packet len " + size + " streamlen " + bd.size);
                    }
                    if(!fake)
                        ScatterLogManager.v(trunk.blman.TAG, "Recieved packet len " + size + " streamlen " + bd.size);
                    if(bd.isInvalid()) {
                        if(!fake)
                            ScatterLogManager.e(trunk.blman.TAG, "Recieved corrupt filepacket");
                        else
                            System.out.println("Reecieved corrupt filepacket");
                        continue;
                    }

                    if(!fake)
                        trunk.blman.onSuccessfulFileRecieve(bd, fake);
                    if(fake) {
                        fakedone = true;
                        fakeres = bd;
                        System.out.println("fakereceived packet with hash " + bd.getHash());
                        //go = false;
                    }
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

                        if(fake)
                            e.printStackTrace();
                    }
                    break;
                }

                ScatterLogManager.e(trunk.blman.TAG, "IOException when receiving stanza");
                if(fake)
                    e.printStackTrace();
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
                        if(fake)
                            e.printStackTrace();
                    }

                    break;
                }

            }
            catch(Exception e) {
                if(!fake) {
                    ScatterLogManager.e(trunk.blman.TAG, "Generic exception in ScatterReciveThread:\n" +
                            Arrays.toString(e.getStackTrace()));
                }
                if(fake)
                    e.printStackTrace();
            }
        }
    }
}