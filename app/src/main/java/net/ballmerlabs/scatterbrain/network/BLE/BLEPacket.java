package net.ballmerlabs.scatterbrain.network.BLE;

/**
 * represents a generic BLE packet. Has byte array for packet contents
 */
public abstract class BLEPacket {
    public byte contents[];
    public boolean invalid;

    public BLEPacket() {
        invalid = true;
    }
    public BLEPacket(int size) {
        contents = new byte[size];
        invalid = false;
    }

    public byte getHeader() { return contents[0];}
    public boolean isInvalid() {
        return invalid;
    }
    public byte[] getContents() { return contents;}
}
