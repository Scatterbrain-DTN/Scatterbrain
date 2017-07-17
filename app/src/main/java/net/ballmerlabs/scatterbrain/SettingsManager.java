package net.ballmerlabs.scatterbrain;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Created by user on 7/7/16.
 */
public class SettingsManager {
    public DeviceProfile profile;
    public final int scanTimeMillis = 10001;
    public int SERVER_PORT = 8222;
    public int bluetoothScanTimeMillis = 15000;

    public SettingsManager() {

    }
}
