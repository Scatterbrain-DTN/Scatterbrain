package net.ballmerlabs.scatterbrain.network;

import java.nio.ByteBuffer;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public boolean text;
    public byte[] senderluid;
    public byte[] receiverluid;
    public DeviceProfile profile;


    public BlockDataPacket(byte body[], boolean text, DeviceProfile profile, byte[] senderluid) {
        super(14+body.length);
        this.body = body;
        this.text = text;
        this.senderluid = senderluid;
        invalid = false;
        this.profile = profile;

        if(init() == null)
            invalid = true;
    }


    public BlockDataPacket(final byte raw[]) {
        super(raw.length);
        if(raw.length > 14)
            contents = raw.clone();
        else
            invalid = true;

        if(contents[0] != 1)
            invalid = true;

        byte recieverluid[] = new byte[6];
        byte senderluid[] = new byte[6];

        int counter3 = 0;
        for(int x = 1;x<7;x++) {
            senderluid[x-1] = contents[x];
        }
        counter3 = 0;
        for(int x=7;x<13;x++) {
            recieverluid[x-7] = contents[x];
        }

        this.senderluid = senderluid;
        this.receiverluid = recieverluid;

        if(contents[13] == 1)
            text = true;
        else
            text = false;

        body = new byte[contents.length - 14];

        for(int x=0;x<contents.length - 14;x++) {
            body[x] = contents[x+14];
        }
    }

    private byte[] init() {
        contents[0] = 1;
        if(senderluid.length != 6) {
            return null; //TODO: error logging here
        }
        byte senderluidbytes[] = senderluid;
        int counter1 = 0;
        for(int x=1;x<7;x++) {
            contents[x] = senderluidbytes[x-1];
        }
        byte[] receiverluid = {5,5,5,5,5,5} ;

        this.receiverluid = receiverluid;
        byte receiverluidbytes[] = receiverluid;
        if(receiverluid.length != 6) {
            invalid = true;
            return null;
        }

        int counter = 0;
        for(int x=0;x<receiverluidbytes.length;x++) {
            contents[x+7] = receiverluidbytes[x];
        }

        if(text)
            contents[13] = 1;
        else
            contents[13] = 0;

        for(int x=0;x<body.length;x++) {
            contents[x+14] = body[x];
        }

        return contents;
    }
}
