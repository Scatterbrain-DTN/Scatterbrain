package net.ballmerlabs.scatterbrain;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Created by user on 7/7/16.
 */
public class SettingsManager {
    @SuppressWarnings("unused")
    public DeviceProfile profile;
    public final int scanTimeMillis = 10001;
    @SuppressWarnings("unused")
    public int SERVER_PORT = 8222;
    @SuppressWarnings("unused")
    public int bluetoothScanTimeMillis = 15000;

    public SettingsManager() {

    }
}
