package net.ballmerlabs.scatterbrain;

import android.app.Activity;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * holds most of the objects needed to keep clutter off of
 * the main activity
 */
@SuppressWarnings("FieldCanBeLocal")
class MainTrunk {

    @SuppressWarnings("unused")
    private final DeviceProfile profile;
    @SuppressWarnings("unused")
    private final SettingsManager settings;
    @SuppressWarnings("unused")
    private final Activity mainActivity;

    @SuppressWarnings("unused")
    public MainTrunk(Activity main) {
        byte tmp[] = {0,0,0,0,0};
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE,tmp );
        settings = new SettingsManager();
        mainActivity = main;
        //globnet.registerService(profile);

    }
}
