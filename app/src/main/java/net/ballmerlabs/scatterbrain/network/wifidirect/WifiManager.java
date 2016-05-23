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
    public DeviceProfile currentDevice;

    /*
     * Remember to call this constructor in OnCreate()? maybe?
     */
    public WifiManager(Activity mainActivity, DeviceProfile me, GlobalNet globnet, WifiP2pManager p2pman,
                       WifiP2pManager.Channel chan) throws LeNotSupportedException {
        this.mainActivity = mainActivity;
        this.mainActivity = mainActivity;
        this.chan = chan;
        this.manager = p2pman;
        net = globnet;
        currentDevice = me;

    }
     /*
     * Takes a message object and parameters for routing over bluetooth and generates
     * a string for transmit over Scatterbrain protocol
     */
    private BlockDataPacket encodeBlockData(byte body[], boolean text, DeviceProfile to) {
        BlockDataPacket bdpacket = new BlockDataPacket(body, text, to);
        return bdpacket;
    }


    private AdvertisePacket encodeAdvertise() {
        byte result[] = new byte[7];
        AdvertisePacket adpack = new AdvertisePacket(currentDevice);
        return adpack;
    }


    private BLEPacket decodePacket(byte in[]) {
        if(in[0] == 0)
            return decodeAdvertise(in);
        else if(in[0] == 1)
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


    /* Receiver for intents from wifi p2p framework */
    @Override
    public void onReceive(Context c, Intent i) {

    }
}


