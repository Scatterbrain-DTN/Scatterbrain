package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import net.ballmerlabs.scatterbrain.network.BLE.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BLE.BLEPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
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
    private BroadcastReceiver p2preceiver;
    public WifiManager directmanager;
    private Thread wifiScanThread;

    public GlobalNet(Activity mainActivity, DeviceProfile me) {
        packetqueue = new ArrayList<>();
        main = mainActivity;
        prof = me;
        directmanager = new WifiManager(main, this);
        wifiScanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    directmanager.scan();
                    Thread.sleep(1000);
                }
                catch(InterruptedException e) {
                    Log.e(TAG, "Caught InterruptedException in wifi update thread");
                }
            }
        });
    }



    /* appends a packet to the queue */
    public void appendPacket(BLEPacket p) {
        packetqueue.add(p);
    }

    public BLEPacket dequeuePacket() {
        if (packetqueue.size() > 0) {
            BLEPacket result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        } else
            return null;

    }

    /* sends a packet asynchronously */
    public void sendPacket(BLEPacket s) {

    }

    public BroadcastReceiver getP2preceiver() {
        return p2preceiver;
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
    private BLEPacket decodePacket(byte in[]) {
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

    public void startWifiDirectLoopThread() {
        wifiScanThread.start();
    }

    public void stopWifiDirectLoopThread() {
        wifiScanThread.stop();
    }
}
