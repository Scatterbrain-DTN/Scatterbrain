package net.ballmerlabs.scatterbrain.network;

import android.util.Base64;

import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.CRC32;

/**
 * Represents a block data packet
 */
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public boolean text;
    public byte[] senderluid;
    public byte[] receiverluid;
    public Integer size;
    public int[] err;
    public final int ERRSIZE =10;
    public static final int HEADERSIZE = 22;
    public static final byte MAGIC = 124;
    public BlockDataPacket(byte body[], boolean text, byte[] senderluid) {
        super(HEADERSIZE+body.length);
        this.body = body;
        this.text = text;
        this.senderluid = senderluid;
        this.size = body.length;
        this.err = new int[ERRSIZE];
        invalid = false;
        //noinspection RedundantIfStatement
        if(init() == null)
            invalid = true;


    }


    public String getHash() {

        String hash = null;
        try {
            byte[] combined = new byte[size+ senderluid.length];
            System.arraycopy(body, 0, combined, 0, size);

            System.arraycopy(senderluid, size - size, combined, size, senderluid.length - size);
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
        this.err = new int[ERRSIZE];
        if(raw.length >= 22)
            contents = raw.clone();
        else if (raw.length < 22) {
            err[0] = 1;
            invalid = true;
        }

        if(contents[0] != BlockDataPacket.MAGIC) {
            err[1] = 1;
            invalid = true;
        }

        byte recieverluid[] = new byte[6];
        byte senderluid[] = new byte[6];

        int counter3 = 0;
        System.arraycopy(contents, 1, senderluid, 0, 6);
        counter3 = 0;
        System.arraycopy(contents, 7, recieverluid, 0, 6);

        this.senderluid = senderluid;
        this.receiverluid = recieverluid;

        //noinspection RedundantIfStatement
        if(contents[13] == 1)
            text = true;
        else
            text = false;

        byte[] sizearr = new byte[4];

        System.arraycopy(contents, 14, sizearr, 0, 4);

        this.size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getInt();


        if(size > 0) {
            body = new byte[size];
            System.arraycopy(contents, 18, body, 0, size);


            //verify crc with stored copy
            CRC32 crc = new CRC32();
            crc.update(contents, 0, contents.length - 4);
            byte[] check = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) crc.getValue()).array();
            byte[] real = new byte[4];
            System.arraycopy(contents, (contents.length - 4) + 0, real, 0, real.length);

            if (!Arrays.equals(real, check)) {
                err[2] = 1;
                invalid = true;
            }
        }
    }

    private byte[] init() {
        contents[0] = BlockDataPacket.MAGIC;
        if(senderluid.length != 6) {
            ScatterLogManager.e("BDinit", "Senderluid wrong length");
            err[3] = 1;
            return null;
        }
        byte senderluidbytes[] = senderluid;
        int counter1 = 0;
        System.arraycopy(senderluidbytes, 0, contents, 1, 6);
        byte[] receiverluid = {0,0,0,0,0,0}; //not implemented yet

        this.receiverluid = receiverluid;
        if(receiverluid.length != 6) {
            err[4] = 1;
            invalid = true;
            ScatterLogManager.e("BDinit", "ReceiverLuid wrong size");
            return null;
        }

        int counter = 0;
        System.arraycopy(receiverluid, 0, contents, 7, receiverluid.length);

        if(text)
            contents[13] = 1;
        else
            contents[13] = 0;

        byte[] sizebytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(body.length).array();

        System.arraycopy(sizebytes, 0, contents, 14, 4);

        System.arraycopy(body, 0, contents, 18, body.length);
        //basic crc for integrity check
        CRC32 crc = new CRC32();
        crc.update(contents,0, contents.length - 4);
        byte[] c = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)crc.getValue()).array();
        System.arraycopy(c, 0, contents, (contents.length - 4) + 0, c.length);

        return contents;
    }


    public static int getSizeFromData(byte[] data) {
        byte[] sizearr = new byte[4];

        if (data.length < 22) {
            return -1;
        }

        if(data[0] != BlockDataPacket.MAGIC) {
            return -1;
        }

        System.arraycopy(data, 14, sizearr, 0, 4);

        int size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if(size < 0)
            return -1;
        else
            return size;
    }
}
