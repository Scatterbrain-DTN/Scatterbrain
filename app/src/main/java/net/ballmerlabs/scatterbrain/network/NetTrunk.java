package net.ballmerlabs.scatterbrain.network;

import net.ballmerlabs.scatterbrain.SettingsManager;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

/**
 * Collection of global objects for use by scatterbrain
 * network (inb4 'global is bad')
 */
public class NetTrunk {

    public GlobalNet globnet;
    public ScatterBluetoothManager blman;
    public DeviceProfile profile;
    public SettingsManager settings;
    public ScatterRoutingService mainService;


    public NetTrunk(ScatterRoutingService mainService) {
        this.mainService = mainService;
        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE, mainService.luid);
        globnet = new GlobalNet(this);
        settings = new SettingsManager();
       // globnet.getWifiManager().startWifiDirctLoopThread();
        blman = new ScatterBluetoothManager(this);
    }
}
