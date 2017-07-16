package net.ballmerlabs.scatterbrain.network;

/**
 * represents a generic BLE packet. Has byte array for packet contents
 */
public abstract class ScatterStanza {
    public byte contents[];
    public boolean invalid;
    public final int PACKET_SIZE = 0;

    public ScatterStanza() {
        invalid = false;
    }
    public ScatterStanza(int size) {
        contents = new byte[size];
        invalid = false;
    }

    public byte getHeader() { return contents[0];}

    public boolean isInvalid() {
        return invalid;
    }
    public byte[] getContents() { return contents;}
}
