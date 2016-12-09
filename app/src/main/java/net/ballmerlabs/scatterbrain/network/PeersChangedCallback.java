package net.ballmerlabs.scatterbrain.network;

import net.ballmerlabs.scatterbrain.network.bluetooth.LocalPeer;

import java.util.Map;

/**
 * Class to handle events related to devices changed
 */

public interface PeersChangedCallback {
    void run(Map<String, LocalPeer> connectedList);
}
