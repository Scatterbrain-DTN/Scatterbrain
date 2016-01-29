package net.ballmerlabs.scatterbrain.network;

/**
 * General device information and settings storage.
 */
public class DeviceProfile {

    public static enum deviceType {
        ANDROID, IOS, LINUX
    };

    public static enum MobileStatus {
      STATIONARY, MOBILE, VERYMOBILE
    };

    public static enum HardwareServices {
        WIFIP2P, WIFICLIENT, WIFIAP, BLUETOOTH,
        BLUETOOTHLE, INTERNET
    };


    private deviceType type;
    private MobileStatus status;
    private HardwareServices services;

    public final byte protocolVersion = 2;
    private byte congestion;
    public DeviceProfile (deviceType type, MobileStatus status, HardwareServices services) {
        this.type = type;
        this.services = services;
        this.status = status;
    }

    public void  update(deviceType type, MobileStatus status, HardwareServices services) {
        this.type = type;
        this.services = services;
        this.status = status;
    }

    public deviceType getType() {
        return type;
    }

    public HardwareServices getServices() {
        return services;
    }

    public MobileStatus getStatus() {
        return status;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }
}
