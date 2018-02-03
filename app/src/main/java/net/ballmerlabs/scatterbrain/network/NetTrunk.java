package net.ballmerlabs.scatterbrain.network;

import android.content.Context;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.SettingsManager;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;
import net.ballmerlabs.scatterbrain.network.filesystem.FileHelper;

/**
 * Collection of global objects for use by scatterbrain
 * network (inb4 'global is bad')
 */
public class NetTrunk {

    public final GlobalNet globnet;
    public final ScatterBluetoothManager blman;
    public final DeviceProfile profile;
    public final SettingsManager settings;
    public final ScatterRoutingService mainService;
    public final FileHelper filehelper;


    public NetTrunk(ScatterRoutingService mainService) {
        this.mainService = mainService;
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE, mainService.luid);
        globnet = new GlobalNet(this);
        settings = new SettingsManager();
       // globnet.getWifiManager().startWifiDirctLoopThread();
        blman = new ScatterBluetoothManager(this);
        Context c = null;
        try {
             c = mainService.getApplicationContext();
        } catch(NullPointerException n) {
            n.printStackTrace();
        }
        filehelper = new FileHelper(c, this);

    }
}
