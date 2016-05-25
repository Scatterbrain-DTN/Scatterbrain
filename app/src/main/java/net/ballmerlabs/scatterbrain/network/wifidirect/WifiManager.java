package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import net.ballmerlabs.scatterbrain.network.GlobalNet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gnu3ra on 10/31/15.
 * interface for the BLEMingle library for iOS / Android bluetooth communication.
 */
public class WifiManager extends BroadcastReceiver {
    public boolean CONNECTED = false;
    private String TAG = "WiFi_daemon";
    private android.os.Handler threadHandler = new android.os.Handler();
    private GlobalNet net;
    private Activity mainActivity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel chan;
    private WifiP2pManager.PeerListListener peerlistener;
    private WifiP2pManager.ActionListener connectlistener;
    private WifiP2pManager.ActionListener scanlistener;
    private HashMap<WifiP2pDevice, WifiP2pConfig> connectedList;

    /*
     * Remember to call this constructor in OnCreate()? maybe?
     */
    public WifiManager(Activity mainActivity, GlobalNet globnet, WifiP2pManager p2pman,
                       WifiP2pManager.Channel chan) {
        this.mainActivity = mainActivity;
        this.mainActivity = mainActivity;
        this.chan = chan;
        this.manager = p2pman;
        net = globnet;
        connectedList = new HashMap<>();
    }

    /* registers a listener for actions when peers changed (onPeersAvailable)
     *  Call before scan() to set a function to catch peers
     *  */
    public void registerPeerListListener(WifiP2pManager.PeerListListener listener) {
        peerlistener = listener;
    }

    /* handling if scan succeeded or failed. Does nothing with peers */
    public void registerScanActionListener(WifiP2pManager.ActionListener scan) {
        this.scanlistener = scan;
    }

    /* registers a listener for action on connect to a peer */
    public void registerConnectActionListener(WifiP2pManager.ActionListener listener) {
        connectlistener = listener;
    }


    /* gets the manager */
    public WifiP2pManager getManager() {
        return manager;
    }


    /* connect to a peer and push it onto the connected list */
    public void connectToPeer(final WifiP2pManager.Channel c,final  WifiP2pDevice target) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = target.deviceAddress;
        manager.connect(c, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectedList.put(target, config);
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed to connect to device " + target.deviceName + "with address " +
                target.deviceAddress);
            }
        });
    }

    /* garbage collector like function run periodically to remove disconnected devices. */
    private void cleanupConnections() {
        final Thread connectionGC = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (connectedList) {
                    for (Map.Entry<WifiP2pDevice, WifiP2pConfig> s : connectedList.entrySet()) {
                        if (s.getKey().status != WifiP2pDevice.CONNECTED) {
                            connectedList.remove(s.getKey());
                        }
                    }
                }
            }
        });

    }

    public void scan() {
        manager.discoverPeers(chan, scanlistener);
    }

    /* Receiver for intents from wifi p2p framework */
    @Override
    public void onReceive(Context c, Intent i) {
        String action = i.getAction();
        //detect if the connection changes state
        if(manager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            int state = i.getIntExtra(manager.EXTRA_WIFI_STATE, -1);
            if( state == manager.WIFI_P2P_STATE_ENABLED) {
                //wifi p2p is enabled
            }
            else {
                //not enabled
            }
        }
        else if(manager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(peerlistener != null)
                manager.requestPeers(chan, peerlistener);
            else {
                Log.e(TAG, "PeerListener is null. Is it not set?");
            }
        }
        else if(manager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            cleanupConnections();
        }
    }
}


