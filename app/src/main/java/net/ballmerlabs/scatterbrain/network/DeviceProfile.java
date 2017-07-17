package net.ballmerlabs.scatterbrain.network;

/**
 * General device information and settings storage.
 * Used to refer to a device
 */
public class DeviceProfile {

    private byte[] luid;

    public enum deviceType {
        ANDROID, IOS, LINUX
    }

    public enum MobileStatus {
      STATIONARY, MOBILE, VERYMOBILE
    }

    public enum HardwareServices {
        WIFIP2P, WIFICLIENT, WIFIAP, BLUETOOTH,
        BLUETOOTHLE, INTERNET
    }


    private deviceType type;
    private MobileStatus status;
    private HardwareServices services;

    private final byte protocolVersion = 2;
    private byte congestion;
    public DeviceProfile (deviceType type, MobileStatus status, HardwareServices services, byte[] id) {
        this.type = type;
        this.services = services;
        this.status = status;
        congestion = 0;
        this.luid = id;
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
    public byte[] getLUID(){ return this.luid;}

    public void setLUID(byte[] id){this.luid = id;}
}
