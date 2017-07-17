package net.ballmerlabs.scatterbrain.network.BLE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;

/**
 * Represents a block data packet
 */
@SuppressWarnings({"MismatchedReadAndWriteOfArray", "ManualArrayCopy"})
class BlockDataPacket extends BLEPacket{

    private byte[] body;
    private final boolean text;
    @SuppressWarnings("unused")
    private String sendermac;
    @SuppressWarnings("unused")
    private String receivermac;

    private DeviceProfile to;



    @SuppressWarnings("unused")
    public BlockDataPacket(byte body[], boolean text, DeviceProfile to) {
            super(27+body.length);
            this.body = body;
            this.text = text;
            this.to = to;

        if(init() == null)
            invalid = true;
    }


    @SuppressWarnings("unused")
    public BlockDataPacket(byte raw[]) {
        super(26+raw.length);
        contents = raw;
        byte body[] = new byte[raw.length - 26];
        for(int x=25;x<contents.length;x++) {
            body[x-25] = contents[x];
        }
        byte recievermac[] = new byte[12];
        byte sendermac[] = new byte[12];

        for(int x = 1;x<(1+12);x++) {
            sendermac[x-1] = contents[x];
        }

        for(int x=13;x<(13+12);x++) {
            recievermac[x-13] = contents[x];
        }

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
        for(int x=0;x<sendermacbytes.length;x++) {
            contents[x+1] = sendermacbytes[x];
        }
        String receivermac = new String(to.getLUID()).replace(":","");
        if(receivermac.length() != 12)
            return null;

        this.receivermac = receivermac;
        byte receivermacbytes[] = receivermac.getBytes();
        for(int x=0;x<receivermacbytes.length;x++) {
            contents[x+13] = receivermacbytes[x];
        }
        if(text)
            contents[26] = 1;
        else
            contents[26] = 0;

        for(int x=27;x<body.length;x++) {
            contents[x] = body[x];
        }

        return contents;
    }
}