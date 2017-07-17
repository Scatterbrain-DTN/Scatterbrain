package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothSocket;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Represents a local bluetooth device believed to be within
 * range at the time
 */
@SuppressWarnings("FieldCanBeLocal")
public class LocalPeer {
    private final DeviceProfile profile;
    public final BluetoothSocket socket;
    private final ScatterReceiveThread receiveThread;

    public LocalPeer(DeviceProfile profile, BluetoothSocket socket) {
        this.profile = profile;
        this.socket = socket;
        this.receiveThread = new ScatterReceiveThread(socket);
        receiveThread.start();
    }

    @SuppressWarnings("unused")
    public DeviceProfile getProfile() {
        return profile;
    }

    @SuppressWarnings("unused")
    public BluetoothSocket getSocket() {
        return socket;
    }

}
