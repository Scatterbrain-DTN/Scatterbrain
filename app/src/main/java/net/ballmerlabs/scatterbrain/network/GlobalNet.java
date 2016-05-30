package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.ballmerlabs.scatterbrain.network.wifidirect.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiDirectLooper;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiManager;
import net.ballmerlabs.scatterbrain.network.wifidirect.WifiPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Global network management framework
 */
public class GlobalNet {
    private ArrayList<WifiPacket> packetqueue;
    private Activity main;
    private DeviceProfile prof;
    public final String TAG = "GlobNet";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver p2preceiver;
    public WifiManager directmanager;
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private WifiDirectLooper looper;
    private final Handler wifiHandler;
    private boolean runScanThread;

    public final int scanTimeMillis = 5000;
    public final int SERVER_PORT = 8222;

    public GlobalNet(final Activity mainActivity, DeviceProfile me) {
        packetqueue = new ArrayList<>();
        main = mainActivity;
        prof = me;
        directmanager = new WifiManager(main, this);
        runScanThread = false;
        looper = new WifiDirectLooper(this);
        wifiHandler = looper.getHandler();
    }



    /* appends a packet to the queue */
    public void appendPacket(WifiPacket p) {
        packetqueue.add(p);
    }

    public WifiPacket dequeuePacket() {
        if (packetqueue.size() > 0) {
            WifiPacket result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        } else
            return null;

    }

    /*
     * Registers a service for autodiscovery
     */
    public void registerService(DeviceProfile profile) {
        manager.removeLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Deregistered discovery service");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed to deregister discovery service");
            }
        });
        Map record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("protocolVersion", "0"); //TODO: add actual version
        record.put("deviceType", profile.getType().toString());
        record.put("mobileStatus", profile.getStatus().toString());
        record.put("congestion", String.valueOf(profile.getCongestion()));
        record.put("hwServices", profile.getServices().toString());

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp",record);
        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Successfully registered Scatterbrain service for discovery");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed to register Scatterbrain discovery service: + " + reason );
            }
        });
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
    private WifiPacket decodePacket(byte in[]) {
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

    public void startWifiDirctLoopThread() {
        Log.v(TAG, "Starting wifi direct scan thread");
        runScanThread = true;
        Runnable scanr = new Runnable() {
            @Override
            public void run() {
                    directmanager.scan();
                    if(runScanThread)
                        wifiHandler.postDelayed(this,scanTimeMillis);
                    else
                        Log.v(TAG, "Stopping wifi direct scan thread");
            }
        };
        wifiHandler.postDelayed(scanr, 1000);
    }

    public void stopWifiDirectLoopThread() {
        runScanThread = false;
    }
}
