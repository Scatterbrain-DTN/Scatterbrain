package net.ballmerlabs.scatterbrain.network;

import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestException;
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
    public byte[] filename;
    public static final int FILENAMELEN = 256;
    public long size;
    public byte[] streamhash;
    public final int[] err;
    private final int ERRSIZE = 10;
    public boolean isfile;
    public InputStream source;
    public boolean sent;
    public File diskfile;
    public boolean streamresettable;
    public static final int HEADERSIZE = 27 + FILENAMELEN;
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
        this.filename = null;
        streamresettable = false;
        //noinspection RedundantIfStatement
        if (init() == null)
            invalid = true;


    }

    public BlockDataPacket(InputStream source, String name,  long len, byte[] senderluid) {
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
        this.diskfile = null;
        this.streamhash = null;
        this.streamresettable = false;
        try {
            byte[] b = name.getBytes("UTF-8");
            if(b.length > FILENAMELEN) {
                invalid = true;
                return;
            }
            this.filename = Arrays.copyOf(b,FILENAMELEN);
        } catch(UnsupportedEncodingException e) {

        }
        if (len > Integer.MAX_VALUE)
            invalid = true;
        if (init() == null)
            invalid = true;
    }

    public BlockDataPacket(File source, String name,  long len, byte[] senderluid) {
        super(HEADERSIZE);
        this.size = len;
        this.err = new int[ERRSIZE];
        this.body = new byte[0];
        this.text = false;
        this.senderluid = senderluid;
        FileInputStream f = null;
        try {
            f = new FileInputStream(source);
        } catch(IOException e) {
            invalid = true;
            return;
        }
        this.diskfile = source;
        this.source = f;
        this.isfile = true;
        this.invalid = false;
        this.sent = false;
        this.streamhash = null;
        this.streamresettable = true;
        try {
            byte[] b = name.getBytes("UTF-8");
            if(b.length > FILENAMELEN) {
                invalid = true;
                return;
            }
            this.filename = Arrays.copyOf(b,FILENAMELEN);
        } catch(UnsupportedEncodingException e) {

        }
        if (len > Integer.MAX_VALUE)
            invalid = true;
        if (init() == null)
            invalid = true;
    }

    public String getHash() {
        String hash = null;
        try {
            FileInputStream fo = null;
            if (isfile) {
                if(streamhash == null) {
                    if(diskfile == null)
                        return null;
                    if(diskfile.length() != this.size)
                        return null;

                    try {
                         fo = new FileInputStream(this.diskfile);
                    } catch(IOException e) {
                        return null;
                    }
                    updateStreamHash(fo);
                }
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(senderluid, 0, senderluid.length);
                digest.update(filename, 0, filename.length);
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
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
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


    //only call if you know the file is correct
    public boolean updateStreamHash(FileInputStream f) {
        try {
            byte[] byteblock = new byte[16384];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int count = (int)size;
            int bytesread = 0;
            boolean go = true;
            while (go) {

                if(f.available() > 0) {
                    go = (bytesread = source.read(byteblock)) != -1;
                    count -= bytesread;
                    if (count < 0 - bytesread) {
                        //overrun this shouldn't happen
                        invalid = true;
                        break;
                    }
                    if (count == 0) {
                        go = false;
                    } else if (count < 0) {
                        bytesread = bytesread + count;
                        go = false;
                    }
                    digest.update(byteblock, 0, bytesread);
                }
            }
            this.streamhash = digest.digest();

        } catch(NoSuchAlgorithmException e) {
            ScatterLogManager.e("BlockDataPacket" ,"NoSuchAlgorithm");
            return false;
        } catch(IOException e) {
            ScatterLogManager.e("BlockDataPacket", "IOException when updating streamhash");
            return false;
        }
        return true;

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

        if(isfile) {
            this.filename = new byte[FILENAMELEN];
            for (int i = 0; i < FILENAMELEN; i++) {
                this.filename[i] = contents[i + 23];
            }
        }

        this.size = ByteBuffer.wrap(sizearr).order(ByteOrder.LITTLE_ENDIAN).getLong();

        if (size > 0) {

            if(!isfile) {
                body = new byte[(int) size];
                for (int x = 0; x < size; x++) {
                    body[x] = contents[x + 23 + FILENAMELEN];
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

    public BlockDataPacket(byte[] data, File filesource) {
        this(data);
        try {
            FileInputStream f = new FileInputStream(filesource);
            this.isfile = true;
            this.source = f;
            this.body = new byte[0];
            this.diskfile = filesource;
        } catch(IOException e) {
            ScatterLogManager.e("BlockDataPacket", "IOException in file input");
        }

    }

    public BlockDataPacket(byte[] data, InputStream source) {
        this(data);
        this.isfile = true;
        this.source = source;
        this.body = new byte[0];
        this.diskfile = null;
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

        for(int i=0;i<FILENAMELEN;i++) {
            if(isfile) {
                contents[i+23] = filename[i];
            } else {
                contents[i+23] = 0;
            }
        }

        for (int x = 0; x < body.length; x++) {
            contents[x + 23 + FILENAMELEN] = body[x];
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

    public String getFilename() {
        if(this.filename == null) {
            return "";
        }
        try {
            return new String(this.filename, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            return null;
        }
    }


    public boolean catFile(OutputStream destination) {
        final int MAXBLOCKSIZE = 512;
        if(isfile && diskfile != null) {
            if(diskfile.length() != size)
                return false;


            int bytesread = 0;
            final int read;
            if(size < MAXBLOCKSIZE) {
                read = (int)size;
            } else {
                read = MAXBLOCKSIZE;
            }
            byte[] byteblock = new byte[read];

            try {

                FileInputStream fi = new FileInputStream(this.diskfile);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");


                int count = (int)size;
                boolean go = true;
                while (go) {

                    if(fi.available() > 0) {
                        go = (bytesread = fi.read(byteblock)) != -1;
                        count -= bytesread;
                        if (count < 0 - bytesread) {
                            //overrun this shouldn't happen
                            invalid = true;
                            break;
                        }
                        if (count == 0) {
                            go = false;
                        } else if (count < 0) {
                            bytesread = bytesread + count;
                            go = false;
                        }

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
                return false;
            } catch(NoSuchAlgorithmException n) {
                ScatterLogManager.e("BlockDataPacket", "NosuchAlgorithm");
                return false;
            }
            this.sent = true;
        } else {
            this.invalid = true;
            return false;
        }
        return true;
    }


    public void catBody(OutputStream destination) {
        catBody(destination, 0);
    }

    public void catBody(File destination, long delaymillis) {
        try {
            FileOutputStream fo = new FileOutputStream(destination);
            this.diskfile = destination;
            catBody(fo, delaymillis);
        } catch(IOException e) {
            ScatterLogManager.e("BlockDataPacket" , "Tried to cat to bad file");
        }
    }

    public void catBody(OutputStream destination, long delaymillis) {
        final int MAXBLOCKSIZE = 512;
        if(isfile) {
            int bytesread = 0;
            final int read;
            if(size < MAXBLOCKSIZE) {
                read = (int)size;
            } else {
                read = MAXBLOCKSIZE;
            }
            byte[] byteblock = new byte[read];

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");


                int count = (int)size;
                boolean go = true;
                while (go) {

                    if(source.available() > 0) {
                        go = (bytesread = source.read(byteblock)) != -1;
                        count -= bytesread;
                        if (count < 0 - bytesread) {
                            //overrun this shouldn't happen
                            invalid = true;
                            break;
                        }
                        if (count == 0) {
                            go = false;
                        } else if (count < 0) {
                            bytesread = bytesread + count;
                            go = false;
                        }

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

    public boolean setDiskFile(File f) {
        if(f.length() == this.size || f.length() == 0) {
            this.diskfile = f;
            return true;
        } else {
            return false;
        }
    }


    public File getDiskFile() {
        return this.diskfile;
    }
}

