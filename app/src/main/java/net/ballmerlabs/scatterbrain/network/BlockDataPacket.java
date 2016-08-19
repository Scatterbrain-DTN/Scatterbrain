package net.ballmerlabs.scatterbrain.network;

import java.nio.ByteBuffer;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public boolean text;
    public String senderluid;
    public String receiverluid;
    public int datasize;
    public DeviceProfile profile;



    public BlockDataPacket(byte body[], boolean text, DeviceProfile profile, String senderluid) {
        super(18+body.length);
        this.body = body;
        this.text = text;
        this.senderluid = senderluid;
        this.profile = profile;
        invalid = false;

        if(init() == null)
            invalid = true;
    }


    public BlockDataPacket(final byte raw[]) {
        super(raw.length);
        if(raw.length > 18)
            contents = raw.clone();
        else
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

        this.senderluid = new String(senderluid);
        this.receiverluid = new String(recieverluid);

        if(contents[13] == 1)
            text = true;
        else
            text = false;

        ByteBuffer b  = ByteBuffer.allocate(4);
        byte[] out = new byte[4];
        for(int x=0;x<out.length;x++) {
            out[x] = contents[x+14];
        }
        b.put(out);
        b.get(datasize);
        body = new byte[datasize];

        for(int x=0;x<datasize;x++) {
            body[x] = contents[x+14];
        }
    }

    private byte[] init() {
        contents[0] = 1;
        if(senderluid.getBytes().length != 6) {
            return null; //TODO: error logging here
        }
        byte senderluidbytes[] = senderluid.getBytes();
        int counter1 = 0;
        for(int x=1;x<7;x++) {
            contents[x] = senderluidbytes[x-1];
        }
        String receiverluid = "555555";

        this.receiverluid = receiverluid;
        byte receiverluidbytes[] = receiverluid.getBytes();
        if(receiverluid.getBytes().length != 6) {
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

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(body.length); //TODO: this assumes small sizes

        byte[] out = b.array();

        for(int i=0;i< out.length;i++) {
            contents[i+14] = out[i];
        }

        for(int x=0;x<body.length;x++) {
            contents[x+18] = body[x];
        }

        return contents;
    }
}
