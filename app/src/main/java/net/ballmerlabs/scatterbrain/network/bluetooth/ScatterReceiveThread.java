package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Thread started to wait for messages sent by peers
 */
public class ScatterReceiveThread {
    BluetoothSocket socket;
    public ScatterReceiveThread(BluetoothSocket socket) {
        this.socket = socket;
    }
}
