package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pManager;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;

/**
 * holds most of the objects needed to keep clutter off of
 * the main activity
 */
public class MainTrunk {

    public GlobalNet globnet;
    public DeviceProfile profile;
    public SettingsManager settings;
    public Activity mainActivity;

    public MainTrunk(Activity main) {
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE, "000000000000");
        globnet = new GlobalNet(main, this);

        settings = new SettingsManager();
        mainActivity = main;
        //globnet.registerService(profile);
        globnet.getWifiManager().startWifiDirctLoopThread();
    }
}
