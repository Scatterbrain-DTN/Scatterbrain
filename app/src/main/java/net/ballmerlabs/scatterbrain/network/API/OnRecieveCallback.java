package net.ballmerlabs.scatterbrain.network.API;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;

/**
 * Used by highlevel api. run() is called when packets are recieved.
 */

public interface OnRecieveCallback {

    void run(BlockDataPacket[] packets);
}
