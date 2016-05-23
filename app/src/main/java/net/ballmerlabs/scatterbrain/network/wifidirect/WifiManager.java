package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.bitgarage.blemingledroid.BleUtil;

import net.ballmerlabs.scatterbrain.network.BLE.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BLE.BLEPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.BLE.LeNotSupportedException;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.RecievedCallback;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    }

    /* registers a listener for actions when peers changed */
    public void registerPeerListener(WifiP2pManager.PeerListListener listener) {
        peerlistener = listener;
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
    }
}


