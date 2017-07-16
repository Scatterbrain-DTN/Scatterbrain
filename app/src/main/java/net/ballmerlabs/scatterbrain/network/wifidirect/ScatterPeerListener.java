package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import java.util.ArrayList;

/**
 * Listens for new peers when scanning and pushes them onto a queue
 */
public class ScatterPeerListener implements WifiP2pManager.PeerListListener {
    public Boolean haspeers;
    public ArrayList<WifiP2pDeviceList> peerstack;
    public final int maxsize = 5;
    private TextView peersView;
    private WifiManager manager;
    @SuppressWarnings("FieldCanBeLocal")
    private GlobalNet globnet;
    private WifiP2pManager.Channel channel;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "PeerListener";
    public ScatterPeerListener(NetTrunk trunk) {
        this.manager = trunk.globnet.getWifiManager();
        this.globnet = trunk.globnet;
        this.channel = globnet.getWifiManager().getChannel();
        haspeers = false;
        peerstack = new ArrayList<>();


    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        ScatterLogManager.v(TAG, "Found peers!");
        haspeers = true;
        peerstack.add(peers);
        peersView.setText(dumpStack());
        //trim so we don't get too big
        if(peerstack.size() > maxsize) {
            int size = peerstack.size();
            for(int x=0;x<size-maxsize;x++) {
                    peerstack.remove(0);
            }
        }

        for(WifiP2pDevice d : peers.getDeviceList()) {
            manager.connectToPeer(channel, d);
        }
    }

    private String dumpStack () {
        String result = "";
        for(WifiP2pDeviceList dev : peerstack) {
            result.concat(dev.toString() + "\n");
        }
        return result;
    }

    public WifiP2pDeviceList getPeers() {
        haspeers = false;
        if(peerstack.size() > 0) {
            WifiP2pDeviceList tmp = peerstack.get(0);
            peerstack.remove(0);
            return tmp;
        }
        else {
            WifiP2pDeviceList tmp = new WifiP2pDeviceList();
            return tmp;
        }
    }
}
