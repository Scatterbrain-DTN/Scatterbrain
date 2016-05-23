package net.ballmerlabs.scatterbrain.network.wifidirect;

/**
 * represents a generic BLE packet. Has byte array for packet contents
 */
public abstract class WifiPacket {
    public byte contents[];
    public boolean invalid;

    public WifiPacket() {
        invalid = true;
    }
    public WifiPacket(int size) {
        contents = new byte[size];
        invalid = false;
    }

    public byte getHeader() { return contents[0];};
    public boolean isInvalid() {
        return invalid;
    }
    public byte[] getContents() { return contents;}
}
