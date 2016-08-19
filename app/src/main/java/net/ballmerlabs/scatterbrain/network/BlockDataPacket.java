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
    public DeviceProfile profile;


    public BlockDataPacket(byte body[], boolean text, DeviceProfile profile, String senderluid) {
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

        byte recieverluid[] = new byte[6];
        byte senderluid[] = new byte[6];

        int counter3 = 0;
        for(int x = 1;x<7;x++) {
            senderluid[x-1] = contents[x];
            System.out.println(counter3++);
        }
        counter3 = 0;
        for(int x=7;x<13;x++) {
            recieverluid[x-7] = contents[x];
            System.out.println(counter3++);
        }

        this.senderluid = new String(senderluid);
        this.receiverluid = new String(recieverluid);

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
        if(senderluid.getBytes().length != 6) {
            System.out.println("GUUUUU");
            return null; //TODO: error logging here
        }
        byte senderluidbytes[] = senderluid.getBytes();
        int counter1 = 0;
        for(int x=1;x<7;x++) {
            contents[x] = senderluidbytes[x-1];
            System.out.println(counter1++);
        }
        String receiverluid = "555555" ;

        this.receiverluid = receiverluid;
        byte receiverluidbytes[] = receiverluid.getBytes();
        if(receiverluid.getBytes().length != 6) {
            invalid = true;
            return null;
        }

        int counter = 0;
        for(int x=0;x<receiverluidbytes.length;x++) {
            contents[x+7] = receiverluidbytes[x];
            System.out.println(counter++);
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
