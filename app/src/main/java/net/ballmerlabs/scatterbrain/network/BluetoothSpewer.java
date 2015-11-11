package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Handler;
import android.util.Log;

import com.bitgarage.blemingledroid.BleUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ballmerlabs.scatterbrain.MainActivity;
import net.ballmerlabs.scatterbrain.network.LeNotSupportedException;

/**
 * Created by gnu3ra on 10/31/15.
 * interface for the BLEMingle library for iOS / Android bluetooth communication.
 */
public class BluetoothSpewer implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter adapter;
    private BluetoothLeAdvertiser advertiser;
    public boolean CONNECTED = false;
    private String TAG = "BLE_daemon";
    private android.os.Handler threadHandler = new android.os.Handler();
    public String stagedMsg;
    private Activity mainActivity;
    public boolean isscanning = false;
    public String[] used = new String[3];
    public int ui = 0;


    /*
     * Remember to call this constructor in OnCreate()? maybe?
     */
    public BluetoothSpewer(Activity mainActivity, NetworkCallback rcv) throws LeNotSupportedException {
        this.mainActivity = mainActivity;
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

        stopScan();
        startScan();
    }



    /*
     * Starts discovery. Remember to run on pause and resume. Needs to be running while daemon
     * is running.
     */
    public void startScan() {
        if((adapter != null) && (!isscanning)) {
            adapter.startLeScan(this); //may need main activity.
            isscanning = true;
        }
    }

    public void stopScan() {
        if(adapter != null) {
            adapter.stopLeScan(this);
        }

        isscanning = false;
    }

    /*
     * Recieve a message.
     * large parts of this are copied from the BLEMingle example.
     * TODO: find out what is really needed in onLeScan()
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
                    Log.e("Address", newDevice.getAddress());
                    Log.e("Data", BleUtil.asHex(newScanRecord));


                    //gui element manimulating ommitted
                    //textViewToChange.setText(oldText + subMessage.substring(0, subMessage.length() - 1) + (enter ? "\n" : ""));

                    //TODO: handle incoming message (right here)

                    ui = ui == 2 ? -1 : ui;
                    ui ++;
                    Log.e("String (submessage)", subMessage);
                }
                break;
            }
            startByte++;
        }
    }



    /*
     *Sends a message. Hopefully will not be corruped between iOS and Android
     * (I am trying to stick to standards)
     */
    public void sendMessage(String msg) {
        stagedMsg = msg;
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
                    if(stagedMsg.length() > 0) {
                        subMesssage = stagedMsg.substring(0,8) + "-";
                        stagedMsg = stagedMsg.substring(8);
                        for(int i=0; i<20;i++) {
                            AdvertiseData ad = BleUtil.makeAdvertiseData(subMesssage);
                            advertiser.startAdvertising(BleUtil.createAdvSettings(true, 100), ad, adCallback);
                            advertiser.stopAdvertising(adCallback);
                        }
                    }
                    else {
                        subMesssage = stagedMsg;
                        stagedMsg = "";
                        for(int i=0;i<5;i++) {
                            AdvertiseData ad = BleUtil.makeAdvertiseData(subMesssage);
                            advertiser.startAdvertising(BleUtil.createAdvSettings(true, 40) ,ad, adCallback);
                            advertiser.stopAdvertising(adCallback);

                        }
                    }
                }
                //ommited GUI elements
            }
        });
        thread.start();
    }



    private AdvertiseCallback adCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            CONNECTED = false;
            super.onStartFailure(errorCode);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            CONNECTED = true;
            if(settingsInEffect != null) {
            }
            else {
                Log.d(TAG,"onStartSuccess, settingInEffect is null");
            }
            super.onStartSuccess(settingsInEffect);
        }
    };




}


