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
    public byte[] streamhash;
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
        this.streamhash = null;
        //noinspection RedundantIfStatement
        if (init() == null)
            invalid = true;


    }

    public BlockDataPacket(InputStream source, long len, byte[] senderluid) {
        super(HEADERSIZE);
        this.size = len;
        this.err = new int[ERRSIZE];
        this.body = new byte[0];
        this.senderluid = senderluid;
        this.source = source;
        this.isfile = true;
        this.invalid = false;
        this.text = false;
        this.sent = false;
        this.streamhash = null;
        if (len > Integer.MAX_VALUE)
            invalid = true;
        if (init() == null)
            invalid = true;
    }


    public String getHash() {
        String hash = null;
        try {
            if (isfile) {
                if(streamhash == null)
                    return null; 
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(senderluid, 0, senderluid.length);
                digest.update(streamhash, 0, streamhash.length);
                byte[] combined = digest.digest();
                hash = bytesToHex(combined);
            } else {
                byte[] combined = new byte[(int) (size + senderluid.length)];
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
            }

        } catch (NoSuchAlgorithmException nsa) {
            ScatterLogManager.e("BlockDataPacket", "SHA-1 needed for BlockData hashing.");
        }
        return hash;
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

        this.size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getLong();

        if (size > 0) {

            if(!isfile) {
                body = new byte[(int) size];
                for (int x = 0; x < size; x++) {
                    body[x] = contents[x + 23];
                }
            }


            //verify crc with stored copy
            CRC32 crc = new CRC32();
            crc.update(contents, 0, contents.length - 4);
            byte[] check = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)crc.getValue()).array();
            byte[] real = new byte[4];
            for (int x = 0; x < real.length; x++) {
                real[x] = contents[(contents.length - 4) + x];
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

        sizebytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(size).array();


        for (int i = 0; i < 8; i++) {
            contents[i + 15] = sizebytes[i];
        }

        for (int x = 0; x < body.length; x++) {
            contents[x + 23] = body[x];
        }
        //basic crc for integrity check
        CRC32 crc = new CRC32();
        crc.update(contents, 0, contents.length - 4);
        byte[] c = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)crc.getValue()).array();
        for (int x = 0; x < c.length; x++) {
            contents[(contents.length - 4) + x] = c[x];
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

        long size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getLong();
        if(size < 0)
            return -1;
        else
            return size;
    }

    public void catBody(OutputStream destination) {
        catBody(destination, 0);
    }

    public void catBody(OutputStream destination, long delaymillis) {
        ScatterLogManager.v("REMOVETHIS", "catbody");
        final int MAXBLOCKSIZE = 512;
        if(isfile) {
            int bytesread = 0;
            final int read;
            if(size < MAXBLOCKSIZE) {
                read = (int)size;
            } else {
                read = MAXBLOCKSIZE;
            }
            ScatterLogManager.v("REMOVETHIS", "catting read " + read + " size " + size);
            byte[] byteblock = new byte[read];

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");

                digest.update(senderluid, 0, senderluid.length);
                int count = (int)size;
                boolean go = true;
                while (go) {

                    if(source.available() > 0) {
                        go = (bytesread = source.read(byteblock)) != -1;
                        count -= bytesread;
                        if (count < 0 - bytesread) {
                            //overrun this shouldn't happen
                            ScatterLogManager.e("REMOVETHIS", "err: overrun");
                            invalid = true;
                            break;
                        }
                        if (count == 0) {
                            ScatterLogManager.e("REMOVETHIS", "done: count is 0");
                            go = false;
                        } else if (count < 0) {
                            bytesread = bytesread + count;
                            ScatterLogManager.e("REMOVETHIS", "done: count < 0 corrected: " + bytesread);
                            go = false;
                        }
                        ScatterLogManager.v("BlockDataPacket", "catbody read " + bytesread + " " + count);

                        destination.write(byteblock, 0, bytesread);
                        destination.flush();
                        digest.update(byteblock, 0, bytesread);
                    }
                }
                this.streamhash = digest.digest();
                ScatterLogManager.v("BlockDataPacket", "CAT DONE streamhash: " +
                        BlockDataPacket.bytesToHex(streamhash) );

            } catch (IOException e) {
                ScatterLogManager.e("Packet", "IOEXception when reading from filestream");
                this.invalid = true;
            } catch(NoSuchAlgorithmException n) {
                ScatterLogManager.e("BlockDataPacket", "NosuchAlgorithm");
            }
            this.sent = true;
        } else {
            this.invalid = true;
        }
    }
}

