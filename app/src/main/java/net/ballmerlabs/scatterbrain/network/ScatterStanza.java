package net.ballmerlabs.scatterbrain.network;

/**
 * represents a generic BLE packet. Has byte array for packet contents
 */
public abstract class ScatterStanza {
    public byte contents[];
    public boolean invalid;
    ScatterStanza() {
        invalid = false;
    }
    ScatterStanza(int size) {
        contents = new byte[size];
        invalid = false;
    }

    @SuppressWarnings("unused")
    public byte getHeader() { return contents[0];}

    public boolean isInvalid() {
        return invalid;
    }
    public byte[] getContents() { return contents;}
}
