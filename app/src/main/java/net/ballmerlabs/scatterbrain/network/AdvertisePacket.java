package net.ballmerlabs.scatterbrain.network;

import android.util.Log;
import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

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
    public byte[] err;
    public final int ERR_SIZE = 7;
    public static final int PACKET_SIZE = 17;
    public static final byte MAGIC = -87;

    public AdvertisePacket(DeviceProfile dv) {
        super(PACKET_SIZE);
        protocolversion = new byte[2];
        this.luid = new byte[6];
        invalid = false;
        err = new byte[ERR_SIZE];
        if(init(dv) == null)
            invalid = true;
    }


    public AdvertisePacket(byte raw[]) {
        super(PACKET_SIZE);
        this.err = new byte[ERR_SIZE];
        this.luid = new byte[6];
        protocolversion = new byte[2];

        if(raw.length < PACKET_SIZE) {
            invalid = true;
            err[0] = 1;
        }
        else {
            contents = raw;

            if(contents[0] != AdvertisePacket.MAGIC) {
                invalid = true;
                err[1] = 1;
                return;
            }
            devicetype = contents[1];
            mobilestatus = contents[2];
            protocolversion[0] = contents[3];
            protocolversion[1] = contents[4];
            congestion = contents[5];
            hwservices = contents[6];
            System.arraycopy(contents, 7, luid, 0, luid.length);

            //check stored CRC against calculated one.
            CRC32 crc = new CRC32();
            crc.update(contents,0, contents.length - 4);
            byte[] check = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)crc.getValue()).array();
            byte[] current = new byte[4];

            System.arraycopy(contents, 13, current, 0, current.length);

            if(!Arrays.equals(check, current)) {
                invalid = true;
                err[3] = 1;
            }
        }
    }

    private byte[] init(DeviceProfile dv) {
        contents[0] = AdvertisePacket.MAGIC;

        if(dv.getLUID().length != 6) {
            err[4] = 1;
            return null;
        }

        DeviceProfile.deviceType type = dv.getType();
        if(type == DeviceProfile.deviceType.ANDROID)
            contents[1] = 0;
        else if(type == DeviceProfile.deviceType.IOS)
            contents[1] = 1;
        else if(type == DeviceProfile.deviceType.LINUX)
            contents[1] = 2;
        else {
            ScatterLogManager.e(TAG, "Wrong device type");
            err[5] = 1;
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
            ScatterLogManager.e(TAG, "Wrong mobile status");
            err[6] = 1;
            return null;
        }




        mobilestatus = contents[2];

        contents[3] = dv.getProtocolVersion();
        contents[4] = 0; //this is just dumb
        protocolversion[0] = contents[3];
        protocolversion[1] = contents[4];

        contents[5] = dv.getCongestion();

        congestion = contents[5];

        contents[6] = 0;


        DeviceProfile.HardwareServices serv = dv.getServices();

        if(serv == DeviceProfile.HardwareServices.WIFIP2P)
            contents[6] |= (1);
        if(serv == DeviceProfile.HardwareServices.WIFICLIENT)
            contents[6] |= (1<<1);
        if(serv == DeviceProfile.HardwareServices.WIFIAP)
            contents[6] |= (1<<2);
        if(serv == DeviceProfile.HardwareServices.BLUETOOTH)
            contents[6] |= (1<<3);
        if(serv == DeviceProfile.HardwareServices.INTERNET)
            contents[6] |= (1<<4);

        hwservices = contents[6];

        System.arraycopy(dv.getLUID(), 0, contents, 7, 6);

        //CRC for integrity check
        CRC32 crc = new CRC32();
        crc.update(contents,0, contents.length - 4);
        byte[] c = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) crc.getValue()).array();
        System.arraycopy(c, 0, contents, 13, c.length);

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

        if((hwservices & 1) == 1)
            serv = DeviceProfile.HardwareServices.WIFIP2P;
        else if((hwservices & (1<<1)) == (1<<1))
            serv = DeviceProfile.HardwareServices.WIFICLIENT;
        else if((hwservices & (1<<2)) == (1<<2))
            serv = DeviceProfile.HardwareServices.WIFIAP;
        else if((hwservices & (1<<3)) == (1<<3))
            serv = DeviceProfile.HardwareServices.BLUETOOTH;
        else if((hwservices & (1<<4)) == (1<<4))
            serv = DeviceProfile.HardwareServices.INTERNET;




        //todo: Replace mac with universalID
        return new DeviceProfile(type, mob, serv, this.luid);
    }
}
