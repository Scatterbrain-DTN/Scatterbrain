package net.ballmerlabs.scatterbrain.network;

import android.util.Log;

/**
 * Created by gnu3ra on 3/28/16.
 */
public class AdvertisePacket extends ScatterStanza {

    public static String TAG = "AdvertisePacket";
    public byte devicetype;
    public byte mobilestatus;
    public byte protocolversion[];
    public byte congestion;
    public byte hwservices;
    public byte[] luid;

    public AdvertisePacket(DeviceProfile dv) {
        super(13);
        protocolversion = new byte[2];
        this.luid = new byte[6];
        invalid = false;
        if(init(dv) == null)
            invalid = true;
    }


    public AdvertisePacket(byte raw_in[]) {
        super(13);
        this.luid = new byte[6];
        protocolversion = new byte[2];
        byte[] raw = new byte[13];

        if(raw.length < 13) {
            invalid = true;
            Log.e(TAG, "Packet length wrong");
            return;
        }
        else {
            for(int i=0;i<7;i++) {
                raw[i] = raw_in[i];
            }
            contents = raw;

            if(contents[0] != 0) {
                invalid = true;
                return;
            }
            devicetype = contents[1];
            mobilestatus = contents[2];
            protocolversion[0] = contents[3];
            protocolversion[1] = contents[4];
            congestion = contents[5];
            hwservices = contents[6];
            for(int i=1;i<=luid.length;i++) {
                luid[i-1] = contents[6+i];
            }
        }
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
        else {
            Log.e(TAG, "Wrong device type");
            return null;
        }
        devicetype = contents[1];

        DeviceProfile.MobileStatus mob = dv.getStatus();
        if(mob == DeviceProfile.MobileStatus.STATIONARY)
            contents[2] = 0;
        else if(mob == DeviceProfile.MobileStatus.MOBILE)
            contents[2] = 1;
        else if(mob == DeviceProfile.MobileStatus.VERYMOBILE)
            contents[2] = 2;
        else {
            Log.e(TAG, "Wrong mobile status");
            return null;
        }




        mobilestatus = contents[2];

        contents[3] = dv.getProtocolVersion(); //TODO: change this when operation
        contents[4] = 0; //this is just dumb
        protocolversion[0] = contents[3];
        protocolversion[1] = contents[4];

        contents[5] = dv.getCongestion(); //TODO: implimnet congestion checking.

        congestion = contents[5];

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

        hwservices = contents[6];

        for(int i=1;i<=6;i++) {
            contents[6+i] = dv.getLUID()[i-1];
        }
        return contents;
    }

    public DeviceProfile convertToProfile() {
        DeviceProfile.deviceType type = null;
        if(devicetype == 0)
            type = DeviceProfile.deviceType.ANDROID;
        else if(devicetype == 1)
            type = DeviceProfile.deviceType.IOS;
        else if(devicetype == 2)
            type = DeviceProfile.deviceType.LINUX;

        DeviceProfile.MobileStatus mob = null;
        if(mobilestatus == 0)
            mob = DeviceProfile.MobileStatus.STATIONARY;
        else if(mobilestatus == 1)
            mob = DeviceProfile.MobileStatus.MOBILE;
        else if(mobilestatus == 2)
            mob = DeviceProfile.MobileStatus.VERYMOBILE;

        DeviceProfile.HardwareServices serv = null;

        if((hwservices & (1<<0)) == 1)
            serv = DeviceProfile.HardwareServices.WIFIP2P;
        else if((hwservices & (1<<1)) == 1)
            serv = DeviceProfile.HardwareServices.WIFICLIENT;
        else if((hwservices & (1<<2)) == 1)
            serv = DeviceProfile.HardwareServices.WIFIAP;
        else if((hwservices & (1<<3)) == 1)
            serv = DeviceProfile.HardwareServices.BLUETOOTH;
        else if((hwservices & (1<<4)) == 1)
            serv = DeviceProfile.HardwareServices.INTERNET;




        //todo: Replace mac with universalID
        return new DeviceProfile(type, mob, serv, this.luid);
    }
}

