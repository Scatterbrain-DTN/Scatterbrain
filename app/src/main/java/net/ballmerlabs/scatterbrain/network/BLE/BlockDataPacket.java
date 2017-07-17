package net.ballmerlabs.scatterbrain.network.BLE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends BLEPacket{

    public byte body[];
    public boolean text;
    public String sendermac;
    public String receivermac;

    private DeviceProfile to;



    public BlockDataPacket(byte body[], boolean text, DeviceProfile to) {
            super(27+body.length);
            this.body = body;
            this.text = text;
            this.to = to;

        if(init() == null)
            invalid = true;
    }


    public BlockDataPacket(byte raw[]) {
        super(26+raw.length);
        contents = raw;
        byte body[] = new byte[raw.length - 26];
        System.arraycopy(contents, 25, body, 0, contents.length - 25);
        byte recievermac[] = new byte[12];
        byte sendermac[] = new byte[12];

        System.arraycopy(contents, 1, sendermac, 0, 1 + 12 - 1);

        System.arraycopy(contents, 13, recievermac, 0, 13 + 12 - 13);

        this.sendermac = new String(sendermac);
        this.receivermac = new String(recievermac);

        //noinspection RedundantIfStatement
        if(contents[24] == 1)
            text = true;
        else
            text = false;
    }

    private byte[] init() {
        contents[0] = 1;
        @SuppressLint("HardwareIds") String sendermac = BluetoothAdapter.getDefaultAdapter().getAddress().replace(":","");
        if(sendermac.length() != 12)
            return null;
        this.sendermac = sendermac;
        byte sendermacbytes[] = sendermac.getBytes();
        System.arraycopy(sendermacbytes, 0, contents, 1, sendermacbytes.length);
        String receivermac = new String(to.getLUID()).replace(":","");
        if(receivermac.length() != 12)
            return null;

        this.receivermac = receivermac;
        byte receivermacbytes[] = receivermac.getBytes();
        System.arraycopy(receivermacbytes, 0, contents, 13, receivermacbytes.length);
        if(text)
            contents[26] = 1;
        else
            contents[26] = 0;

        System.arraycopy(body, 27, contents, 27, body.length - 27);

        return contents;
    }
}