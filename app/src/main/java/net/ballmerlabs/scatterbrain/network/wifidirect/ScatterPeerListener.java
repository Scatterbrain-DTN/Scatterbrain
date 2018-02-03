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
@SuppressWarnings("unused")
public class ScatterPeerListener implements WifiP2pManager.PeerListListener {
    @SuppressWarnings("unused")
    private Boolean haspeers;
    private final ArrayList<WifiP2pDeviceList> peerstack;
    @SuppressWarnings("unused")
    private TextView peersView;
    @SuppressWarnings("FieldCanBeLocal")
    private final GlobalNet globnet;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "PeerListener";
    @SuppressWarnings("unused")
    public ScatterPeerListener(NetTrunk trunk) {
        this.globnet = trunk.globnet;
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
            //broke
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
