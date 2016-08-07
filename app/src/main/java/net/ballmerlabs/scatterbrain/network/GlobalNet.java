package net.ballmerlabs.scatterbrain.network;


import android.content.IntentFilter;


import net.ballmerlabs.scatterbrain.network.wifidirect.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiManager;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiPacket;

import java.util.ArrayList;

/**
 * Global network management framework
 * Currently not used much as ScatterBluetoothManager is
 * easier
 */
public class GlobalNet {
    private ArrayList<WifiPacket> packetqueue;
    private DeviceProfile prof;
    public final String TAG = "GlobNet";
    public WifiManager directmanager;
    private NetTrunk trunk;
    public ScatterPeerListener peerlistener;


    public GlobalNet(NetTrunk trunk) {
        packetqueue = new ArrayList<>();
        prof = trunk.profile;
        this.trunk = trunk;
        directmanager = new WifiManager(trunk);
        //peerlistener = new ScatterPeerListener(trunk);
        directmanager.stopWifiDirectLoopThread();

    }




    /* appends a packet to the queue */
    public void appendPacket(WifiPacket p) {
        packetqueue.add(p);
    }

    public WifiPacket dequeuePacket() {
        if (packetqueue.size() > 0) {
            WifiPacket result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        } else
            return null;

    }


    /*
     * Takes a message object and parameters for routing over bluetooth and generates
     * a string for transmit over Scatterbrain protocol
     */
    private BlockDataPacket encodeBlockData(byte body[], boolean text, DeviceProfile to) {
        BlockDataPacket bdpacket = new BlockDataPacket(body, text, to);
        return bdpacket;
    }


    public IntentFilter getP2pIntentFilter() {
        return directmanager.getP2pIntenetFilter();
    }


    /*
     * encodes advertise packet with current device profile as source
     */
    private AdvertisePacket encodeAdvertise() {
        byte result[] = new byte[7];
        AdvertisePacket adpack = new AdvertisePacket(prof);
        return adpack;
    }

    public WifiManager getWifiManager() {
        return directmanager;
    }


    /*
     * decodes a packet for casting into packet types
     */
    private WifiPacket decodePacket(byte in[]) {
        if (in[0] == 0)
            return decodeAdvertise(in);
        else if (in[0] == 1)
            return decodeBlockData(in);
        else
            return null;
    }

    private AdvertisePacket decodeAdvertise(byte in[]) {
        return new AdvertisePacket(in);
    }

    private BlockDataPacket decodeBlockData(byte in[]) {
        return new BlockDataPacket(in);
    }


}
