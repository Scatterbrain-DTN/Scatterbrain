package net.ballmerlabs.scatterbrain.network.BLE;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Created by gnu3ra on 3/28/16.
 */
public class AdvertisePacket extends BLEPacket {
    public AdvertisePacket(DeviceProfile dv) {
        super(7);

        if(init(dv) == null)
            invalid = true;
    }

    private byte[] init(DeviceProfile dv) {
        contents[0] = 0;

        DeviceProfile.deviceType type = dv.getType();
        if(type == DeviceProfile.deviceType.ANDROID)
            contents[1] = 0;
        else if(type == DeviceProfile.deviceType.IOS)
            contents[1] = 1;
        else if(type == DeviceProfile.deviceType.LINUX)
            contents[1] = 2;
        else
            return null;
        ;

        DeviceProfile.MobileStatus mob = dv.getStatus();
        if(mob == DeviceProfile.MobileStatus.STATIONARY)
            contents[2] = 0;
        else if(mob == DeviceProfile.MobileStatus.MOBILE)
            contents[2] = 1;
        else if(mob == DeviceProfile.MobileStatus.VERYMOBILE)
            contents[2] = 2;
        else
            return null;

        contents[3] = dv.getProtocolVersion(); //TODO: change this when operation
        contents[4] = 0; //this is just dumb

        contents[5] = dv.getCongestion(); //TODO: implimnet congestion checking.

        contents[6] = 0;

        DeviceProfile.HardwareServices serv = dv.getServices();

        if(serv == DeviceProfile.HardwareServices.WIFIP2P)
            contents[6] |= (1<<0);
        if(serv == DeviceProfile.HardwareServices.WIFICLIENT)
            contents[6] |= (1<<1);
        if(serv == DeviceProfile.HardwareServices.WIFIAP)
            contents[6] |= (1<<2);
        if(serv == DeviceProfile.HardwareServices.BLUETOOTH)
            contents[6] |= (1<<3);
        if(serv == DeviceProfile.HardwareServices.INTERNET)
            contents[6] |= (1<<4);
        return contents;
    }
}

