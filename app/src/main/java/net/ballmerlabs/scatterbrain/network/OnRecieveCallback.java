package net.ballmerlabs.scatterbrain.network;

/**
 * Used by highlevel api. run() is called when packets are recieved.
 */

public interface OnRecieveCallback {

    void run(BlockDataPacket[] packets);
}
