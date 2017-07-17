package net.ballmerlabs.scatterbrain.network.BLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;

import com.bitgarage.blemingledroid.BleUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Created by gnu3ra on 10/31/15.
 * interface for the BLEMingle library for iOS / Android bluetooth communication.
 */
class BluetoothSpewer implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter adapter;
    private BluetoothLeAdvertiser advertiser;
    @SuppressWarnings("unused")
    private boolean CONNECTED = false;
    @SuppressWarnings("unused")
    private android.os.Handler threadHandler = new android.os.Handler();
    private String stagedMsg;
    private boolean isscanning = false;
    private final String[] used = new String[3];
    private int ui = 0;
    private DeviceProfile currentDevice;

    /*);
     * Remember to ca);ll this constructor in OnCreate()? maybe?
     */
    @SuppressWarnings({"unused", "UnusedParameters"})
    public BluetoothSpewer(Activity mainActivity, DeviceProfile me, GlobalNet globnet) throws LeNotSupportedException {
        if (!BleUtil.isBLESupported(mainActivity)) {
            throw (new LeNotSupportedException());
        }

        BluetoothManager manager = BleUtil.getManager(mainActivity);
        if (manager != null) {
            adapter = manager.getAdapter();
        }

        if (adapter == null) {
            throw (new LeNotSupportedException());


        }
        currentDevice = me;

    }



    /*
     * Starts discovery. Remember to run on pause and resume. Needs to be running while daemon
     * is running.
     */
    @SuppressWarnings("deprecation")
    public void startScan() {
        if((adapter != null) && (!isscanning)) {
            adapter.startLeScan(this); //may need main activity.
            isscanning = true;
        }
    }

    @SuppressWarnings("deprecation")
    public void stopScan() {
        if(adapter != null) {
            adapter.stopLeScan(this);
        }

        isscanning = false;
    }

    /*
     * Recieve a message.
     * large parts of this are copied from the BLEMingle example.
     *
     *
     */
    @Override
    public void onLeScan(final BluetoothDevice newDevice, final int newRssi,
             final byte[] newScanRecord) {
        int startByte = 0;
        String hex = BleUtil.asHex(newScanRecord).substring(0,29);
        while(startByte <=5) {
            if(!Arrays.asList(used).contains(hex) ) {
                used[ui] = hex;
                String message = new String(newScanRecord);  //actually gets the message.
                String firstChar = message.substring(5,6);
                Pattern pattern = Pattern.compile("[ a-zA-Z0-9~!@#$%^&*()_+{}|:\"<>?`\\-=;',\\./\\[\\]\\\\]", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(firstChar);

                if(firstChar.equals("L")) {
                    firstChar = message.substring(6,7);
                    pattern =  Pattern.compile("[ a-zA-Z0-9~!@#$%^&*()_+{}|:\"<>?`\\-=;',\\./\\[\\]\\\\]", Pattern.DOTALL);
                    matcher = pattern.matcher(firstChar);
                }

                if(matcher.matches()) {
                    //OMMITED fetching gui elements
                    int len = 0;
                    String subMessage = "";
                    while(matcher.matches()) {
                        subMessage = message.substring(5, 6+len);
                        matcher = pattern.matcher(message.substring(5+len, 6+len));
                        len++;
                    }

                    subMessage = subMessage.substring(5,6+len);
                    ScatterLogManager.e("Address", newDevice.getAddress());
                    ScatterLogManager.e("Data", BleUtil.asHex(newScanRecord));


                    //gui element manimulating ommitted
                    //textViewToChange.setText(oldText + subMessage.substring(0, subMessage.length() - 1) + (enter ? "\n" : ""))


                    ui = ui == 2 ? -1 : ui;
                    ui ++;
                    ScatterLogManager.e("String (submessage)", subMessage);
                }
                break;
            }
            startByte++;
        }
    }


    /* fancy parser for parsing a recieved advertise packet */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    public void makeDeviceProfile(String advertiseMessage, String address) {

    }


    /*
     * Takes a message object and parameters for routing over bluetooth and generates
     * a string for transmit over Scatterbrain protocol
     */
    private BlockDataPacket encodeBlockData(byte body[], boolean text, DeviceProfile to) {
        return new BlockDataPacket(body, text, to);
    }


    private AdvertisePacket encodeAdvertise() {
        return new AdvertisePacket(currentDevice);
    }



    private AdvertisePacket decodeAdvertise(byte in[]) {
        return new AdvertisePacket(in);
    }

    private BlockDataPacket decodeBlockData(byte in[]) {
        return new BlockDataPacket(in);
    }






    /*
     *Sends a message. Hopefully will not be corruped between iOS and Android
     * (I am trying to stick to standards)
     */
    public void transmitMesage(byte mesg[]) {
        stagedMsg = new String(mesg);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(adapter == null) {
                    return;
                }
                if(advertiser == null) {
                    advertiser = adapter.getBluetoothLeAdvertiser();
                }
                while(stagedMsg.length() > 0) {
                    String subMesssage;
                    if(stagedMsg.length() > 8) {
                        subMesssage = stagedMsg.substring(0,8) + "-";
                        stagedMsg = stagedMsg.substring(8);
                        for(int i=0; i<20;i++) {
                            AdvertiseData ad = BleUtil.makeAdvertiseData(subMesssage);
                            advertiser.startAdvertising(BleUtil.createAdvSettings(100), ad, adCallback);
                            advertiser.stopAdvertising(adCallback);
                        }
                    }
                    else {
                        subMesssage = stagedMsg;
                        stagedMsg = "";
                        for(int i=0;i<5;i++) {
                            AdvertiseData ad = BleUtil.makeAdvertiseData(subMesssage);
                            advertiser.startAdvertising(BleUtil.createAdvSettings(40) ,ad, adCallback);
                            advertiser.stopAdvertising(adCallback);

                        }
                    }
                }
                //ommited GUI elements
            }
        });
        thread.start();
    }



    private final AdvertiseCallback adCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            CONNECTED = false;
            super.onStartFailure(errorCode);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            CONNECTED = true;
            if(settingsInEffect == null) {
                String TAG = "BLE_daemon";
                ScatterLogManager.d(TAG);
            }
            super.onStartSuccess(settingsInEffect);
        }
    };




}


