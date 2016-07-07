package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.wifidirect.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiDirectLooper;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiManager;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Global network management framework
 */
public class GlobalNet {
    private ArrayList<WifiPacket> packetqueue;
    private Activity main;
    private DeviceProfile prof;
    public final String TAG = "GlobNet";
    public WifiManager directmanager;
    private MainTrunk trunk;
    public ScatterPeerListener peerlistener;


    public GlobalNet(final Activity mainActivity, MainTrunk trunk) {
        packetqueue = new ArrayList<>();
        main = mainActivity;
        prof = trunk.profile;
        this.trunk = trunk;
        directmanager = new WifiManager(main, trunk);
        //peerlistener = new ScatterPeerListener(trunk);

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
