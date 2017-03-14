package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Represents a local bluetooth device believed to be within
 * range at the time
 */
public class LocalPeer {
    public DeviceProfile profile;
    public BluetoothSocket socket;
    public ScatterReceiveThread receiveThread;

    public LocalPeer(DeviceProfile profile, BluetoothSocket socket) {
        this.profile = profile;
        this.socket = socket;
        this.receiveThread = new ScatterReceiveThread(socket, false);
        receiveThread.start();
    }

}
