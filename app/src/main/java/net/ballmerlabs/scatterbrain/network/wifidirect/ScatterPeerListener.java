package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.R;

import java.util.ArrayList;

/**
 * Listens for new peers when scanning and pushes them onto a queue
 */
public class ScatterPeerListener implements WifiP2pManager.PeerListListener {
    public Boolean haspeers;
    public ArrayList<WifiP2pDeviceList> peerstack;
    public final int maxsize = 5;
    private Activity mainActivity;
    private TextView peersView;
    public ScatterPeerListener(Activity mainActivity) {
        haspeers = false;
        this.mainActivity  =  mainActivity;
        peerstack = new ArrayList<>();
       peersView = (TextView) mainActivity.findViewById(R.id.PeersView);

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
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
