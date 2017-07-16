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
    private ArrayList<ScatterStanza> packetqueue;
    public final String TAG = "GlobNet";
    public WifiManager directmanager;
    private NetTrunk trunk;
    public ScatterPeerListener peerlistener;


    public GlobalNet(NetTrunk trunk) {
        packetqueue = new ArrayList<>();
        DeviceProfile prof = trunk.profile;
        this.trunk = trunk;
        directmanager = new WifiManager(trunk);
        directmanager.stopWifiDirectLoopThread();
    }


    /* appends a packet to the queue */
    public void appendPacket(ScatterStanza p) {
        packetqueue.add(p);
    }

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
    public BlockDataPacket encodeBlockData(byte body[], boolean text, DeviceProfile to) {
        BlockDataPacket bdpacket = new BlockDataPacket(body, text, trunk.mainService.luid);
        return bdpacket;
    }


    public IntentFilter getP2pIntentFilter() {
        return directmanager.getP2pIntenetFilter();
    }



    public WifiManager getWifiManager() {
        return directmanager;
    }


    public BlockDataPacket decodeBlockData(byte in[]) {
        return new BlockDataPacket(in);
    }


    public boolean broadcastData() {
        return false;
    }





}
