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


    protected deviceType type;
    protected MobileStatus status;
    protected HardwareServices services;

    public final byte protocolVersion = 2;
    protected byte congestion;
    public DeviceProfile (deviceType type, MobileStatus status, HardwareServices services) {
        this.type = type;
        this.services = services;
        this.status = status;
        congestion = 0;
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

    public byte getCongestion() { return congestion; }

    public void setCongestion(byte congestion) { this.congestion = congestion; }
}
