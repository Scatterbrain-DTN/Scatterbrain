package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import net.ballmerlabs.scatterbrain.network.wifidirect.ScatterPeerListener;

import java.util.UUID;

/**
 * Abstraction for android bluetooth stack for Scatterbrain.
 */
public class ScatterBluetoothManager {
    public final String TAG = "BluetoothManager";
    public final java.util.UUID UID = UUID.fromString("cc1f06c5-ce01-4538-bc15-2a1d129c8b28");
    public final String NAME = "Scatterbrain";
    public BluetoothAdapter adapter;
    public final static int REQUEST_ENABLE_BT = 1;

    public ScatterBluetoothManager() {

    }

    public void init() {

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            Log.e(TAG, "ERROR, bluetooth not supported");
        }


    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

}

