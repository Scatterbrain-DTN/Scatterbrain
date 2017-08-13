package net.ballmerlabs.scatterbrain.network;

import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Represents a block data packet
 */
@SuppressWarnings("ManualArrayCopy")
public class BlockDataPacket extends ScatterStanza {

    public byte body[];
    public final boolean text;
    public final byte[] senderluid;
    public byte[] receiverluid;
    public long size;
    public final int[] err;
    private final int ERRSIZE = 10;
    public boolean isfile;
    public InputStream source;
    public boolean sent;
    public static final int HEADERSIZE = 27;
    private static final byte MAGIC = 124;

    public BlockDataPacket(byte body[], boolean text, byte[] senderluid) {
        super(HEADERSIZE + body.length);
        this.body = body;
        this.text = text;
        this.senderluid = senderluid;
        this.size = (long) body.length;
        this.err = new int[ERRSIZE];
        this.isfile = false;
        this.invalid = false;
        //noinspection RedundantIfStatement
        if (init() == null)
            invalid = true;


    }

    public BlockDataPacket(InputStream source, long len, byte[] senderluid) {
        super(HEADERSIZE);
        this.size = (long) HEADERSIZE;
        this.err = new int[ERRSIZE];
        this.body = new byte[0];
        this.senderluid = senderluid;
        this.source = source;
        this.isfile = true;
        this.invalid = false;
        this.text = false;
        this.sent = false;
        if (len > Integer.MAX_VALUE)
            invalid = true;
        if (init() == null)
            invalid = true;
    }


    public String getHash() {

        if (isfile) {
            String hash = null;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");

                byte[] buf = new byte[1024];
                int bytesread = 0;
                int offset = 0;
                try {
                    while ((bytesread = this.source.read(buf)) != -1) {
                        digest.update(buf, 0, bytesread);
                        offset += bytesread;
                    }
                    // this.source.reset();
                } catch (IOException e) {
                    return null;
                }

                digest.update(senderluid, 0, senderluid.length);
                byte[] res = digest.digest();
                hash = bytesToHex(res);


            } catch (NoSuchAlgorithmException nsa) {
                ScatterLogManager.e("BlockDataPacket", "SHA-1 needed for BlockData hashing.");
            }
            return hash;
        } else {
            String hash = null;
            try {
                byte[] combined = new byte[(int) (size + (long) senderluid.length)];
                for (int i = 0; i < size; i++) {
                    combined[i] = body[i];
                }

                for (long i = size; i < senderluid.length; i++) {
                    combined[(int) i] = senderluid[(int) (i - size)];
                }
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(combined, 0, combined.length);
                combined = digest.digest();
                hash = bytesToHex(combined);


            } catch (NoSuchAlgorithmException nsa) {
                ScatterLogManager.e("BlockDataPacket", "SHA-1 needed for BlockData hashing.");
            }
            return hash;
        }
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public BlockDataPacket(final byte raw[]) {
        super(raw.length);
        if (size < 0 || size > Integer.MAX_VALUE) {
            invalid = true;
        }
        this.err = new int[ERRSIZE];
        if (raw.length >= HEADERSIZE)
            contents = raw.clone();
        else if (raw.length < HEADERSIZE) {
            err[0] = 1;
            invalid = true;
        }

        if (contents[0] != BlockDataPacket.MAGIC) {
            err[1] = 1;
            invalid = true;
        }

        byte recieverluid[] = new byte[6];
        byte senderluid[] = new byte[6];

        for (int x = 1; x < 7; x++) {
            senderluid[x - 1] = contents[x];
        }

        for (int x = 7; x < 13; x++) {
            recieverluid[x - 7] = contents[x];
        }

        //noinspection UnusedAssignment
        this.senderluid = senderluid;
        this.receiverluid = recieverluid;

        //noinspection RedundantIfStatement
        if (contents[13] == 1)
            text = true;
        else
            text = false;

        if (contents[14] == 1)
            isfile = true;
        else
            isfile = false;

        byte[] sizearr = new byte[8];

        for (int i = 0; i < 8; i++) {
            sizearr[i] = contents[i + 15];
        }

        this.size = ByteBuffer.wrap(sizearr).order(ByteOrder.BIG_ENDIAN).getLong();

        System.out.println("Deserialized size " + this.size);
        if (size > 0) {

            body = new byte[(int) size];
            for (int x = 0; x < size; x++) {
                body[x] = contents[x + 23];
            }


            //verify crc with stored copy
            CRC32 crc = new CRC32();
            crc.update(contents, 0, contents.length - 8);
            byte[] check = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(crc.getValue()).array();
            byte[] real = new byte[8];
            for (int x = 0; x < real.length; x++) {
                real[x] = contents[(contents.length - 8) + x];
            }

            if (!Arrays.equals(real, check)) {
                err[2] = 1;
                invalid = true;
            }
        }
    }

    public BlockDataPacket(byte[] data, InputStream source) {
        this(data);
        this.isfile = true;
        this.source = source;
        this.body = new byte[0];
    }

    private byte[] init() {
        contents[0] = BlockDataPacket.MAGIC;
        if (senderluid.length != 6) {
            ScatterLogManager.e("BDinit", "Senderluid wrong length");
            err[3] = 1;
            return null;
        }

        for (int x = 1; x < 7; x++) {
            contents[x] = senderluid[x - 1];
        }
        byte[] receiverluid = {0, 0, 0, 0, 0, 0}; //not implemented yet

        this.receiverluid = receiverluid;
        if (receiverluid.length != 6) {
            err[4] = 1;
            invalid = true;
            ScatterLogManager.e("BDinit", "ReceiverLuid wrong size");
            return null;
        }

        for (int x = 0; x < receiverluid.length; x++) {
            contents[x + 7] = receiverluid[x];
        }

        if (text)
            contents[13] = 1;
        else
            contents[13] = 0;

        if (isfile)
            contents[14] = 1;
        else
            contents[14] = 0;

        byte[] sizebytes;
        if(body.length > 0)
            sizebytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong((long)body.length).array();
        else
            sizebytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putInt(body.length).array();


        System.out.println("serialized size " + body.length );
        for (int i = 0; i < 8; i++) {
            contents[i + 15] = sizebytes[i];
        }

        for (int x = 0; x < body.length; x++) {
            contents[x + 23] = body[x];
        }
        //basic crc for integrity check
        CRC32 crc = new CRC32();
        crc.update(contents, 0, contents.length - 8);
        byte[] c = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(crc.getValue()).array();
        for (int x = 0; x < c.length; x++) {
            contents[(contents.length - 8) + x] = c[x];
        }

        return contents;
    }


    public static int getFileStatusFromData(byte[] data) {
        if(data.length < HEADERSIZE) {
            return -1;
        }

        if(data[0] != BlockDataPacket.MAGIC) {
            return -2;
        }

        if(data[14] == 1)
            return 1;
        else
            return 0;
    }

    public static long getSizeFromData(byte[] data) {
        byte[] sizearr = new byte[8];

        if (data.length < HEADERSIZE) {
            return -1;
        }

        if(data[0] != BlockDataPacket.MAGIC) {
            return -1;
        }

        for(int i=0;i<8;i++) {
            sizearr[i] = data[i+15];
        }

        long size = ByteBuffer.wrap(sizearr).order(ByteOrder.BIG_ENDIAN).getLong();
        if(size < 0)
            return -1;
        else
            return size;
    }

    public void catBody(OutputStream destination) {
        if(isfile) {
            byte[] byteblock = new byte[1024];
            int bytesread = 0;
            int bytestotal = 0;
            try {
                while ((bytesread= source.read(byteblock)) != -1 && (bytestotal < size)) {
                    System.out.println("read " + bytesread);
                    destination.write(byteblock);
                    if (bytestotal > size) {
                        this.invalid = true;
                        break;
                    }
                    bytestotal += bytesread;
                }
            } catch (IOException e) {
                ScatterLogManager.e("Packet", "IOEXception when reading from filestream");
                this.invalid = true;
            }
            this.sent = true;
        } else {
            this.invalid = true;
        }
    }
}

