package net.ballmerlabs.scatterbrain.utils;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Scatterbrain settings
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
