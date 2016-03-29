package net.ballmerlabs.scatterbrain.network.BLE;

import android.bluetooth.BluetoothAdapter;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends BLEPacket{

    private byte body[];
    private boolean text;
    private DeviceProfile to;
    public BlockDataPacket(byte body[], boolean text, DeviceProfile to) {
            super(26+body.length);
            this.body = body;
            this.text = text;
            this.to = to;

        if(init() == null)
            invalid = true;
    }

    private byte[] init() {
        contents[0] = 1;
        String sendermac = BluetoothAdapter.getDefaultAdapter().getAddress().replace(":","");
        if(sendermac.length() != 12)
            return null; //TODO: error logging here
        byte sendermacbytes[] = sendermac.getBytes();
        for(int x=0;x<sendermacbytes.length;x++) {
            contents[x+1] = sendermacbytes[x];
        }
        String receivermac = to.getMac().replace(":","");
        if(receivermac.length() != 12)
            return null;
        byte receivermacbytes[] = receivermac.getBytes();
        for(int x=0;x<receivermacbytes.length;x++) {
            contents[x+13] = receivermacbytes[x];
        }
        if(text)
            contents[26] = 1;
        else
            contents[26] = 0;

        for(int x=0;x<body.length;x++) {
            contents[x+27] = body[x];
        }

        return contents;
    }
}
