package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.R;
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
    private Handler wifiHandler;
    private boolean runScanThread;

    //used for service discovery
    final HashMap<String, String> buddies = new HashMap<String, String>();


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
        channel = directmanager.getChannel();
        manager = directmanager.getManager();
    }

    public WifiP2pManager.Channel getChannel() {
        return channel;
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

        Map record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("protocolVersion", "0"); //TODO: add actual version
        //record.put("deviceType", profile.getType().toString());
        //record.put("mobileStatus", profile.getStatus().toString());
        //record.put("congestion", String.valueOf(profile.getCongestion()));
        //record.put("hwServices", profile.getServices().toString());

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_Scatterbrain", "_presence._tcp",record);

        /*
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
        */
        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Successfully registered Scatterbrain service for discovery");
            }

            @Override
            public void onFailure(int reason) {
                String reason_s;
                switch(reason) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        reason_s = "P2P_UNSUPPORTED";
                        break;
                    case WifiP2pManager.BUSY:
                        reason_s = "P2P_BUSY";
                        break;
                    case WifiP2pManager.ERROR:
                        reason_s = "P2P_ERROR";
                        break;
                    default:
                        reason_s = "HUH?";
                        break;
                }
                Log.e(TAG, "Failed to register Scatterbrain discovery service: " + reason_s );
            }
        });
    }

    /* discovers nearby scatterbrain devices */
    public void discoverServices() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                //buddies.put(device.deviceAddress, (String)record.get("buddyname"));

                // Add to the custom adapter defined specifically for showing
                // wifi devices.
                TextView peersView = (TextView) main.findViewById(R.id.PeersView);
                peersView.setText("Senpai noticed you!!");
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress) ? buddies
                        .get(resourceType.deviceAddress) : resourceType.deviceName;

                // Add to the custom adapter defined specifically for showing
                // wifi devices.
               TextView peersView = (TextView) main.findViewById(R.id.PeersView);
                peersView.setText("Senpai noticed you!!");
                Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
            }
        };

        manager.setDnsSdResponseListeners(channel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.v(TAG, "Suggesfully added a servicerequest");
                    }

                    @Override
                    public void onFailure(int reason) {
                            Log.e(TAG, "Failed to add servicerequest");
                    }
                });

        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Log.v(TAG, "Discovered a service. Senpai noticed you!");
            }

            @Override
            public void onFailure(int reason) {
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.e(TAG, "P2P not supported");
                }
                else if(reason == WifiP2pManager.ERROR) {
                    Log.e(TAG, "Service discovery failed, internal error");
                }
                else if(reason == WifiP2pManager.BUSY) {
                    Log.e(TAG, "Service discovery failed, busy");
                }
                else if(reason == WifiP2pManager.NO_SERVICE_REQUESTS) {
                    Log.e(TAG, "Service discovery failed, no service requests");
                }
                else {
                    Log.e(TAG, "Discovery failed with code " + reason);
                }
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
        final Handler wifiHan =looper.getHandler();
        registerService(prof);
        Runnable scanr = new Runnable() {
            @Override
            public void run() {
                //directmanager.scan();
                  //

                discoverServices();
                    Log.v(TAG, "Scanning...");
                    if(runScanThread)
                        wifiHan.postDelayed(this,scanTimeMillis);
                    else
                        Log.v(TAG, "Stopping wifi direct scan thread");
            }
        };
        wifiHan.postDelayed(scanr, 1000);
    }

    public void stopWifiDirectLoopThread() {
        runScanThread = false;
    }
}
