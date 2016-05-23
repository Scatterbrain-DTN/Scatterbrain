package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import net.ballmerlabs.scatterbrain.network.BLE.BLEPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BluetoothSpewer;
import net.ballmerlabs.scatterbrain.network.BLE.LeNotSupportedException;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiManager;

import java.util.ArrayList;

/**
 * Global network management framework
 */
public class GlobalNet {
    private ArrayList<BLEPacket> packetqueue;
    private Activity main;
    private DeviceProfile prof;
    public final String TAG = "GlobNet";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    ;private BroadcastReceiver p2preceiver;
    private IntentFilter p2pIntenetFilter;

    public GlobalNet(Activity mainActivity, DeviceProfile me) {
        packetqueue = new ArrayList<>();
        main = mainActivity;
        prof = me;
    }





    /* appends a packet to the queue */
    public void appendPacket(BLEPacket p) {
        packetqueue.add(p);
    }

    public BLEPacket dequeuePacket() {
        if(packetqueue.size() > 0) {
            BLEPacket result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        }
        else
            return null;

    }

    /* sends a packet asynchronously */
    public void sendPacket(BLEPacket s) {

    }

    public BroadcastReceiver getP2preceiver() {
        return p2preceiver;
    }

    public IntentFilter getP2pIntenetFilter() {
        return p2pIntenetFilter;
    }


    /* inits on startup of app */
    public void init() {
        manager = (WifiP2pManager) main.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(main, main.getMainLooper(), null);
        p2preceiver = new WifiManager(main, prof,this, manager,channel);
        p2pIntenetFilter = new IntentFilter();
        p2pIntenetFilter.addAction(manager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntenetFilter.addAction(manager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntenetFilter.addAction(manager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntenetFilter.addAction(manager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
}
