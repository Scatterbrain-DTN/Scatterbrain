package net.ballmerlabs.scatterbrain.network.BLE;

/**
 * represents a generic BLE packet. Has byte array for packet contents
 */
abstract class BLEPacket {
    byte[] contents;
    boolean invalid;

    BLEPacket() {
        invalid = true;
    }
    BLEPacket(int size) {
        contents = new byte[size];
        invalid = false;
    }

    public byte getHeader() { return contents[0];}
    public boolean isInvalid() {
        return invalid;
    }
    public byte[] getContents() { return contents;}
}
