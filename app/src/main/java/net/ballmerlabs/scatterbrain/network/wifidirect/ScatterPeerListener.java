package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import java.util.ArrayList;

/**
 * Listens for new peers when scanning and pushes them onto a queue
 */
public class ScatterPeerListener implements WifiP2pManager.PeerListListener {
    private Boolean haspeers;
    private final ArrayList<WifiP2pDeviceList> peerstack;
    private TextView peersView;
    private final WifiManager manager;
    @SuppressWarnings("FieldCanBeLocal")
    private final GlobalNet globnet;
    private final WifiP2pManager.Channel channel;
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
        int maxsize = 5;
        if(peerstack.size() > maxsize) {
            int size = peerstack.size();
            for(int x = 0; x<size- maxsize; x++) {
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
            result = result + dev.toString() + "\n";
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
            return new WifiP2pDeviceList();
        }
    }
}
