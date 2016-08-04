package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;

import net.ballmerlabs.scatterbrain.SettingsManager;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

/**
 * Created by user on 8/3/16.
 */
public class NetTrunk {

    public GlobalNet globnet;
    public NetTrunk trunk;
    public ScatterBluetoothManager blman;
    public DeviceProfile profile;
    public SettingsManager settings;
    public Activity mainActivity;


    public NetTrunk(Activity main) {
        this.mainActivity = main;
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE, "000000000000");
        globnet = new GlobalNet(this);
        settings = new SettingsManager();
        globnet.getWifiManager().startWifiDirctLoopThread();
        blman = new ScatterBluetoothManager(this);

    }
}
