package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.util.Log;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gnu3ra on 10/31/15.
 * interface for the BLEMingle library for iOS / Android bluetooth communication.
 */
public class WifiManager extends BroadcastReceiver {
    @SuppressWarnings("unused")
    public boolean CONNECTED = false;
    private final String TAG = "WiFi_daemon";
    @SuppressWarnings("unused")
    private android.os.Handler threadHandler = new android.os.Handler();
    private final GlobalNet net;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel chan = null;
    private WifiP2pManager.ActionListener scanlistener;
    private final HashMap<WifiP2pDevice, WifiP2pConfig> connectedList;
    private final IntentFilter p2pIntenetFilter;
    private final WifiP2pManager.Channel channel;
    private Handler wifiHan;
    private boolean runScanThread;
    @SuppressWarnings("unused")
    private WifiP2pDnsSdServiceInfo serviceInfo;
    private final WifiDirectLooper looper;
    @SuppressWarnings("unused")
    private BroadcastReceiver p2preceiver;
    private final NetTrunk trunk;



    //used for service discovery
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "Convert2Diamond"})
    private final HashMap<String, String> buddies = new HashMap<String, String>();


    /*
     * Remember to call this constructor in OnCreate()? maybe?
     */
    @SuppressWarnings("unused")
    public WifiManager(NetTrunk trunk) {
        this.trunk = trunk;
        net = trunk.globnet;
        connectedList = new HashMap<>();
        this.manager = (WifiP2pManager) trunk.mainService.getSystemService(Context.WIFI_P2P_SERVICE);
        if(manager != null) {
            this.chan = manager.initialize(trunk.mainService, trunk.mainService.getMainLooper(), null);
        }
        p2pIntenetFilter = new IntentFilter();
        p2pIntenetFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pIntenetFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        p2pIntenetFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pIntenetFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        channel = getChannel();
        manager = getManager();
        runScanThread = false;
        looper = new WifiDirectLooper(trunk.globnet);
        wifiHan = looper.getHandler();
    }


    private void scanServices() {
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

    @SuppressWarnings("unused")
    public BroadcastReceiver getP2preceiver() {
        return p2preceiver;
    }


    @SuppressWarnings({"unused", "UnusedAssignment"})
    public void startWifiDirctLoopThread() {
        Log.v(TAG, "Starting wifi direct scan thread");
        runScanThread = true;
        wifiHan =looper.getHandler();
        registerService(trunk.settings.profile);
        discoverServices();
        Runnable scanr = new Runnable() {
            @Override
            public void run() {
                //directmanager.scan();
                //
                scanServices();
                Log.v(TAG, "Scanning...");
                if(runScanThread)
                    wifiHan.postDelayed(this,trunk.settings.scanTimeMillis);
                else
                    Log.v(TAG, "Stopping wifi direct scan thread");
            }
        };
    }

    public void stopWifiDirectLoopThread() {
        runScanThread = false;
    }


    public WifiP2pManager.Channel getChannel() {
        return chan;
    }

    /* handling if scan succeeded or failed. Does nothing with peers */
    @SuppressWarnings("unused")
    public void registerScanActionListener(WifiP2pManager.ActionListener scan) {
        this.scanlistener = scan;
    }

    /* registers a listener for action on connect to a peer */
    @SuppressWarnings({"EmptyMethod", "unused", "UnusedParameters"})
    public void registerConnectActionListener(WifiP2pManager.ActionListener listener) {
    }

    public IntentFilter getP2pIntenetFilter() {
        return p2pIntenetFilter;
    }

    /* gets the manager */
    private WifiP2pManager getManager() {
        return manager;
    }


    @SuppressWarnings("unused")
    public ScatterPeerListener getPeerListener() {
        return trunk.globnet.peerlistener;
    }



    /* connect to a peer and push it onto the connected list */
    public void connectToPeer(final WifiP2pManager.Channel c,final  WifiP2pDevice target) {
        Log.i(TAG, "Manually connecting to peer " + target.deviceName + " with  address " +
        target.deviceAddress);

        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = target.deviceAddress;
        manager.connect(c, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectedList.put(target, config);
                Log.v(TAG, "Connection succeeded for " + target.deviceName);
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
        Log.v(TAG, "Running connection list garbage collector");
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

        connectionGC.start();

    }

    @SuppressWarnings("unused")
    public void scan() {
        Log.v(TAG, "Scanning for peers");
        manager.discoverPeers(chan, scanlistener);
    }


    /*
    * Registers a service for autodiscovery
    */
    @SuppressWarnings({"unchecked", "UnusedParameters"})
    private void registerService(DeviceProfile profile) {

        HashMap record = new HashMap<>();
       // record.put("listenport", String.valueOf(trunk.mainService));
        //record.put("protocolVersion", "0"); //TODO: add actual version
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

    private void connectToDevice(WifiP2pDevice dev) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = dev.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //this gets broadcast to WifiDirectBroadcastReceiver
                //here
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Failed to connect to wifidirect device");
            }
        });
    }


    /* discovers nearby scatterbrain devices */
    private void discoverServices() {
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
                // wifi devices




                connectToDevice(device);
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



    }


    /* Receiver for intents from wifi p2p framework */
    @Override
    public void onReceive(Context c, Intent i) {
        String action = i.getAction();
        //detect if the connection changes state
        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.v(TAG, "Recieved WIFI_P2P_CONNECTION_CHANGED_ACTION");
            int state = i.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if( state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.v(TAG, "Wifi p2p is enabled!");
            }
            else {
                Log.v(TAG, "Wifi p2p is not enabled");
                //not enabled
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.v(TAG, "Received WIFI_P2P_PEERS_CHANGED_ACTION");
            if(trunk.globnet.peerlistener != null)
                manager.requestPeers(chan,trunk.globnet.peerlistener);
            else {
                Log.e(TAG, "PeerListener is null. Is it not set?");
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.v(TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION");
            if(manager == null)
                return;
            NetworkInfo networkInfo = (NetworkInfo) i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()) {
                Log.v(TAG, "We are connected!!");
                /*
                 * uses DirectConnnectionInfoListener to start actual tcp/ip
                 * connection
                 */
                manager.requestConnectionInfo(chan, new DirectConnectionInfoListener(connectedList, this, net ));
            }
            else {
                Log.v(TAG, "Disconnected or failed connection.");
                cleanupConnections();
            }
        }
    }
}


