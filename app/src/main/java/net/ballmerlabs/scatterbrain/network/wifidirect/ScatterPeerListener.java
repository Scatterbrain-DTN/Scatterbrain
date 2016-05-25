package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;

/**
 * Listens for new peers when scanning and pushes them onto a queue
 */
public class ScatterPeerListener implements WifiP2pManager.PeerListListener {
    public Boolean haspeers;
    public ArrayList<WifiP2pDeviceList> peerstack;
    public final int maxsize = 5;
    public ScatterPeerListener() {
        haspeers = false;
        peerstack = new ArrayList<>();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        haspeers = true;
        peerstack.add(peers);

        //trim so we don't get too big
        if(peerstack.size() > maxsize) {
            int size = peerstack.size();
            for(int x=0;x<size-maxsize;x++) {
                    peerstack.remove(0);
            }
        }
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
