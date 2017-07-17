package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import net.ballmerlabs.scatterbrain.network.GlobalNet;

import java.net.InetAddress;
import java.util.HashMap;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Created by user on 5/25/16.
 */
@SuppressWarnings("FieldCanBeLocal")
class DirectConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
    @SuppressWarnings("unused")
    private final HashMap<WifiP2pDevice, WifiP2pConfig> connectedList;
    private final String TAG = "ConnectionInfoListener";
    @SuppressWarnings("unused")
    private final GlobalNet globnet;
    @SuppressWarnings("unused")
    private final WifiManager manager;

    public DirectConnectionInfoListener(HashMap<WifiP2pDevice, WifiP2pConfig> connectedList,
                                        WifiManager manager, GlobalNet globnet) {

        this.manager = manager;
        this.globnet = globnet;
        this.connectedList = connectedList;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        InetAddress address = info.groupOwnerAddress;
        if(info.groupFormed && info.isGroupOwner) {
            //start server and make others connect
            ScatterLogManager.i(TAG, "Device is the group owner with address " + address.toString());

        }
        else if(info.groupFormed) {
            ScatterLogManager.i(TAG, "Device is connected to group with address "+ address.toString());
            //connect to group leader
        }
    }
}
