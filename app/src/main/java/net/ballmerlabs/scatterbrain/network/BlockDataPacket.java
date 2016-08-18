package net.ballmerlabs.scatterbrain.network;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public boolean text;
    public String senderluid;
    public String receiverluid;

    private DeviceProfile to;



    public BlockDataPacket(byte body[], boolean text, DeviceProfile to, String senderluid) {
            super(14+body.length);
            this.body = body;
            this.text = text;
            this.to = to;
        this.senderluid = senderluid;
        invalid = false;

        if(init() == null)
            invalid = true;
    }


    public BlockDataPacket(byte raw[]) {
        super(14+raw.length);
        if(raw.length > 14)
            contents = raw;
        else
            invalid = true;

        byte recieverluid[] = new byte[6];
        byte senderluid[] = new byte[6];

        for(int x = 1;x<(1+6);x++) {
            senderluid[x-1] = contents[x];
        }

        for(int x=7;x<(7+6);x++) {
            recieverluid[x-7] = contents[x];
        }

        this.senderluid = new String(senderluid);
        this.receiverluid = new String(recieverluid);

        if(contents[13] == 1)
            text = true;
        else
            text = false;

        byte body[] = new byte[raw.length - 14];
        for(int x=0;x<body.length;x++) {
            body[x] = contents[x+14];
        }
    }

    private byte[] init() {
        contents[0] = 1;
        if(senderluid.length() != 12)
            return null; //TODO: error logging here
        byte senderluidbytes[] = senderluid.getBytes();
        int counter1 = 0;
        for(int x=1;x<7;x++) {
            contents[x] = senderluidbytes[x-1];
        }
        if(counter1 != 5) {
            return null;
        }
        String receiverluid = to.getLUID();

        this.receiverluid = receiverluid;
        byte receiverluidbytes[] = receiverluid.getBytes();
        int counter = 0;
        for(int x=0;x<receiverluidbytes.length;x++) {
            contents[x+6] = receiverluidbytes[x];
            counter++;
        }
        if(counter != 5) {
            return null;
        }
        if(text)
            contents[13] = 1;
        else
            contents[13] = 0;

        for(int x=14;x<body.length;x++) {
            contents[x] = body[x];
        }

        return contents;
    }
}