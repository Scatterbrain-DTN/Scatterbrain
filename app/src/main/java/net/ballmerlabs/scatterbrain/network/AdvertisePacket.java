package net.ballmerlabs.scatterbrain.network;

import net.ballmerlabs.scatterbrain.utils.ScatterLogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * AdvertisePacket. Represents an Advertise stanza
 */
@SuppressWarnings({"ManualArrayCopy", "MismatchedReadAndWriteOfArray"})
public class AdvertisePacket extends ScatterStanza {

    private static final String TAG = "AdvertisePacket";
    private byte devicetype;
    private byte mobilestatus;
    private final byte[] protocolversion;
    @SuppressWarnings("unused")
    private byte congestion;
    private byte hwservices;
    private final byte[] luid;
    public final byte[] err;
    private final int ERR_SIZE = 7;
    public static final int PACKET_SIZE = 17;
    private static final byte MAGIC = -87;

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
            for(int i=1;i<=luid.length;i++) {
                luid[i-1] = contents[6+i];
            }

            //check stored CRC against calculated one.
            CRC32 crc = new CRC32();
            crc.update(contents,0, contents.length - 4);
            byte[] check = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)crc.getValue()).array();
            byte[] current = new byte[4];

            for(int x=0;x<current.length;x++) {
                current[x] = contents[13+x];
            }

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

        for(int i=1;i<=6;i++) {
            contents[6+i] = dv.getLUID()[i-1];
        }

        //CRC for integrity check
        CRC32 crc = new CRC32();
        crc.update(contents,0, contents.length - 4);
        byte[] c = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) crc.getValue()).array();
        for(int x=0;x<c.length;x++) {
            contents[13+x] = c[x];
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
