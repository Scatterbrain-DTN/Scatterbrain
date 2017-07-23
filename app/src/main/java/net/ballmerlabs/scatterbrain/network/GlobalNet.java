package net.ballmerlabs.scatterbrain.network;


import android.content.IntentFilter;


import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiManager;

import java.util.ArrayList;

/**
 * Global network management framework
 * Currently not used much as ScatterBluetoothManager is
 * easier
 */
public class GlobalNet {
    private final ArrayList<ScatterStanza> packetqueue;
    @SuppressWarnings("unused")
    public final String TAG = "GlobNet";
    private final WifiManager directmanager;
    private final NetTrunk trunk;
    @SuppressWarnings("unused")
    public ScatterPeerListener peerlistener;


    public GlobalNet(NetTrunk trunk) {
        packetqueue = new ArrayList<>();
        this.trunk = trunk;
        directmanager = new WifiManager(trunk);
        directmanager.stopWifiDirectLoopThread();
    }


    /* appends a packet to the queue */
    @SuppressWarnings("unused")
    public void appendPacket(ScatterStanza p) {
        packetqueue.add(p);
    }

    @SuppressWarnings("unused")
    public ScatterStanza dequeuePacket() {
        if (packetqueue.size() > 0) {
            ScatterStanza result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        } else
            return null;

    }


    /*
     * Takes a message object and parameters for routing over bluetooth and generates
     * a string for transmit over Scatterbrain protocol
     */
    @SuppressWarnings({"unused", "UnusedParameters"})
    public BlockDataPacket encodeBlockData(byte body[], boolean text, boolean file,  DeviceProfile to) {
        return new BlockDataPacket(body, text, file,  trunk.mainService.luid);
    }


    @SuppressWarnings("unused")
    public IntentFilter getP2pIntentFilter() {
        return directmanager.getP2pIntenetFilter();
    }



    public WifiManager getWifiManager() {
        return directmanager;
    }


    @SuppressWarnings("unused")
    public BlockDataPacket decodeBlockData(byte in[]) {
        return new BlockDataPacket(in);
    }


    @SuppressWarnings({"SameReturnValue", "unused"})
    public boolean broadcastData() {
        return false;
    }





}
