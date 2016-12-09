package net.ballmerlabs.scatterbrain.network;

import android.util.Base64;

import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public boolean text;
    public byte[] senderluid;
    public byte[] receiverluid;
    public Integer size;


    public BlockDataPacket(byte body[], boolean text, byte[] senderluid) {
        super(18+body.length);
        this.body = body;
        this.text = text;
        this.senderluid = senderluid;
        this.size = body.length;
        invalid = false;
        if(init() == null)
            invalid = true;

    }


    public String getHash() {

        String hash = null;
        try {
            byte[] combined = new byte[size+ senderluid.length];
            for(int i=0;i<size;i++) {
                combined[i] = body[i];
            }

            for(int i=size;i<senderluid.length;i++) {
                combined[i] = senderluid[i-size];
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(combined, 0, combined.length);
            combined = digest.digest();
            hash = bytesToHex(combined);


        }
        catch(NoSuchAlgorithmException nsa) {
            ScatterLogManager.e("BlockDataPacket", "SHA-1 needed for BlockData hashing.");
        }
        return hash;
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
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

        byte[] sizearr = new byte[4];

        for(int i=0;i<4;i++) {
            sizearr[i] = contents[i+14];
        }

        this.size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getInt();

        body = new byte[contents.length - 18];

        for(int x=0;x<contents.length - 18;x++) {
            body[x] = contents[x+18];
        }
    }

    private byte[] init() {
        contents[0] = 1;
        if(senderluid.length != 6) {
            return null;
        }
        byte senderluidbytes[] = senderluid;
        int counter1 = 0;
        for(int x=1;x<7;x++) {
            contents[x] = senderluidbytes[x-1];
        }
        byte[] receiverluid = {0,0,0,0,0,0}; //not implemented yet

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

        byte[] sizebytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(body.length).array();

        for(int i=0;i<4;i++) {
            contents[i+14] = sizebytes[i];
        }

        for(int x=0;x<body.length;x++) {
            contents[x+18] = body[x];
        }

        return contents;
    }
}
