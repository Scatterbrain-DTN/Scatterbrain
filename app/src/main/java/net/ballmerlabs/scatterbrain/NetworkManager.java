package net.ballmerlabs.scatterbrain;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import android.util.Log;

/**
 * Created by gnu3ra on 9/9/15.
 */



public class NetworkManager {

    private NsdServiceInfo ninfo;
    private NsdServiceInfo currentService;
    private NsdManager nman;
    private ServerSocket sSocket;
    private NsdManager.ResolveListener resolveListener;
    private NsdManager.RegistrationListener regListener;
    private String serviceName;
    private Context context;
    private boolean regListenerRegistered;
    private boolean discovering;
    private NetworkCallback callback;
    public final String SERVICE_NAME = "Scatterbrain";
    public final String SERVICE_TYPE = "_http._tcp.";
    public final String TAG = "LanCommunications";
    private int port;

    public NetworkManager(Context myContext, int port, NetworkCallback onFound) {
        context = myContext;
        callback = onFound;
        this.port  = port;
        regListenerRegistered = false;
        discovering = false;

    }

    public void init() {
        this.startRegistrationListener();
        this.register(port);
        this.initializeServerSocket();
        this.initializeDiscoveryListener();
        this.initializeResolveListener();
    }

    // NsdHelper's tearDown method
    public void tearDown() {
            if(regListenerRegistered)
        nman.unregisterService(regListener);
            if(discovering)
        nman.stopServiceDiscovery(discoveryListener);
    }

    /* server methods. These are for registering a service */

    public void register(int port) {
        ninfo = new NsdServiceInfo();
        ninfo.setServiceName(SERVICE_NAME);
        ninfo.setServiceType(SERVICE_TYPE);
        ninfo.setPort(port);
        ninfo.setHost(getIp());

        nman =  (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nman.registerService(ninfo,NsdManager.PROTOCOL_DNS_SD, regListener);
    }


    /*gets the ip of the active interface. TODO: this method just autoselects the first active interface */
    public InetAddress getIp(){
        Enumeration<NetworkInterface> ifaces = null;
        try {
             ifaces = NetworkInterface.getNetworkInterfaces();

        if(ifaces != null) {
           while(ifaces.hasMoreElements()) {
               NetworkInterface inter = ifaces.nextElement();
                if(inter.isUp()) {
                    Enumeration<InetAddress> addressList = inter.getInetAddresses();
                    return addressList.nextElement();  //TODO: stupid error handling here (and down)
                }

            }
        }
        }
        catch(SocketException s) {
            Log.e(TAG, "AAAAAAGH! a socketException has occurred");
            return null;
        }

        return null;
    }


    public void initializeServerSocket() {
        try {
            sSocket = new ServerSocket(0);
        }
        catch(IOException e ){
            Log.i(TAG,"IOException on creating ServerSocket");
        }

    }

    public void startRegistrationListener() {
        regListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG, "Failed registration for some reason.");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG, "Failed service unregistration");
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                    serviceName = serviceInfo.getServiceName();
                    regListenerRegistered = true;
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                regListenerRegistered = false;
            }
        };


      //  nman.discoverServices(SERVICE_TYPE, nman.PROTOCOL_DNS_SD, discoveryListener);

    }

    /* client methods. These are for connecting to a service. */
    NsdManager.DiscoveryListener discoveryListener;

    public void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG,"Service discovery failed on start!");
                nman.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG,"Service discovery failed on stop!");
                nman.stopServiceDiscovery(this);

            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG,"Service discovery started!");
                discovering = true;

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG,"Service discovery stopped!");
                discovering = false;

            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG,"We found a service! Senpai noticed us!");

                if(!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.i(TAG, "Darn. Unknown type: " + serviceInfo.getServiceName());
                }
                else if(serviceInfo.getServiceType().equals(serviceName)) {
                    Log.i(TAG, "Service name matches, but I had to change it.");
                    nman.resolveService(serviceInfo, resolveListener);
                } else if (serviceInfo.getServiceName().contains(SERVICE_NAME)) {
                    Log.i(TAG, "Service name matches. Nice to meet ya.");
                    try {
                        nman.resolveService(serviceInfo, resolveListener);
                    }catch (IllegalArgumentException e) {
                        Log.e(TAG,
                                "Horible hack going on. The developer is a total malglico");
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                //TODO: add cleanup here.
                Log.d(TAG, "We seem to have lost the service. He's dead, Jim.");
            }
        };


    }

    public void handleServiceDiscovered(NsdServiceInfo serviceInfo) {

        if(serviceInfo.getHost() != null) {
                callback.run();

        }
        else {
            Log.e(TAG,"Error: host is null somehow");
        }

    }

    public void discoverServices() {

        if(nman != null)
            nman.discoverServices(SERVICE_TYPE, nman.PROTOCOL_DNS_SD, discoveryListener);
        else {
            Log.d(TAG,"Could not run a discovery, maybe next time?");
        }

    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo info, int errcode) {
                Log.i(TAG,"Resolve failed. Please report to designated suicide booth.");
            }

            @Override
            public void onServiceResolved(NsdServiceInfo in) {
                Log.i(TAG, "Resolve Succeded. You get a cookie. " + in);

                if(ninfo.getServiceName().equals(serviceName)) {
                    Log.i(TAG, "Same IP.");
                    return;
                }

                currentService = in;
                int port = currentService.getPort();
                InetAddress host = currentService.getHost();
                handleServiceDiscovered(currentService);

            }
        };

    }


    }



