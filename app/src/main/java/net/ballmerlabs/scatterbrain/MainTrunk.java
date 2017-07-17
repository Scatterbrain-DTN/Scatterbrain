package net.ballmerlabs.scatterbrain;

import android.app.Activity;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * holds most of the objects needed to keep clutter off of
 * the main activity
 */
public class MainTrunk {

    public DeviceProfile profile;
    public SettingsManager settings;
    public Activity mainActivity;

    public MainTrunk(Activity main) {
        byte tmp[] = {0,0,0,0,0};
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE,tmp );
        settings = new SettingsManager();
        mainActivity = main;
        //globnet.registerService(profile);

    }
}
