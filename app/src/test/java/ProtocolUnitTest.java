/*
 * Performs tests on the scatterbrain protocol outside android to
 * reduce the chance of bugged out packets.
 **/
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterReceiveThread;

import org.junit.Test;
import org.mockito.cglib.core.Block;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@SuppressWarnings("unused")
public class ProtocolUnitTest {

    @SuppressWarnings("unused")
    @Test
    public void AdvertisePacketFromProfileIsValid() {
        byte[] test = {1,2,3,4,5,6};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap  = new AdvertisePacket(profile);

        assertThat(ap.isInvalid() , is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void AdvertisePacketFromDataAndProfileIsValid() {
        byte[] test = {1,2,3,4,5,6};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap  = new AdvertisePacket(profile);
        byte[] data = ap.getContents();
        AdvertisePacket newpacket = new AdvertisePacket(data);

        System.out.println("AdvertisePacketFromDataAndProfileIsValid");
        for(byte b : newpacket.err)  {
            System.out.print(b + " ");
        }
        System.out.println();
        assertThat(newpacket.isInvalid(), is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketIsValid() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false,senderluid);
        assertThat(bd.isInvalid(), is(false));
    }


    

    @SuppressWarnings("unused")
    @Test
    public void BlockDataFilePacketDryRun() {
        File tmp = new File("/tmp/t3fghju");
        ScatterRoutingService service = new ScatterRoutingService();
        NetTrunk trunk = new NetTrunk(service);
        ScatterBluetoothManager bman = new ScatterBluetoothManager(trunk);

        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        try {
            FileOutputStream firstout = new FileOutputStream(tmp);
            firstout.write(randomdata);
            File tmpin = new File("/tmp/t3fghju");
            FileInputStream in = new FileInputStream(tmpin);
            BlockDataPacket bd = new BlockDataPacket(in, tmpin.length(),senderluid);

            final ServerSocket ssocket = new ServerSocket(8877);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket sock =  ssocket.accept();
                        ScatterReceiveThread res = new ScatterReceiveThread(sock);
                        res.start();
                        res.join();
                        ssocket.close();
                        assertThat(res.fakedone, is(true));

                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    catch(InterruptedException i) {
                        i.printStackTrace();
                    }
                }
            });
            t.start();
            Thread.sleep(1000);
            assertThat(ssocket.isClosed(), is(false));

            bman.sendRawStream("nothing", bd.getContents(),in,tmp.length(), true);  //TODO: left off here

            t.join();
            assertThat(ssocket.isClosed(), is(true));

        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException i) {
            i.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketDryRun() {
        ScatterRoutingService service = new ScatterRoutingService();
        NetTrunk trunk = new NetTrunk(service);
        ScatterBluetoothManager bman = new ScatterBluetoothManager(trunk);

        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        boolean go = true;
        try {
            final ServerSocket ssocket = new ServerSocket(8877);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       Socket sock =  ssocket.accept();
                        ScatterReceiveThread res = new ScatterReceiveThread(sock);
                        res.start();
                        res.join();
                        ssocket.close();
                        assertThat(res.fakedone, is(true));

                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    catch(InterruptedException i) {
                        i.printStackTrace();
                    }
                }
            });
            t.start();
            Thread.sleep(1000);
            assertThat(ssocket.isClosed(), is(false));

            bman.sendRaw("nothing", bd.getContents(), true);  //TODO: left off here

            t.join();
            assertThat(ssocket.isClosed(), is(true));

        } catch(IOException e) {
            e.printStackTrace();
        }catch(InterruptedException i) {
            i.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHandlesNullData() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        assertThat(bd.isInvalid(), is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHandlesFileBit() {
        byte[] senderluid = {1,2,3,4,5,6};
        boolean works = false;
        File testfile = new File("/dev/null");
        File infile = new File("/dev/zero");
        try {
            FileOutputStream out = new FileOutputStream(testfile);
            FileInputStream in = new FileInputStream(infile);
            BlockDataPacket bd = new BlockDataPacket(in, 4096, senderluid);
            int fileStatusFromData = BlockDataPacket.getFileStatusFromData(bd.getContents());
            if(fileStatusFromData == 1) {
                works = true;
            }
        } catch(IOException e) {
            works = false;
        }

        assertThat(works, is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataFilePacketHasStableContects() {
        byte[] senderluid = {1,2,3,4,5,6};
        boolean works;
        File f = new File("/dev/urandom");
        try {
            FileInputStream i = new FileInputStream(f);
            BlockDataPacket bd = new BlockDataPacket(i, 4096 , senderluid);
        } catch (IOException e) {

        }

    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHasSameHashWhenReconstructedFromFile() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        final String filename = "/tmp/77246out";
        File out = new File(filename);
        File in = new File(filename);
        String hash = null;
        boolean works;
        try {
            FileOutputStream os = new FileOutputStream(out);
            FileInputStream is = new FileInputStream(in);
            os.write(randomdata);
            BlockDataPacket bd = new BlockDataPacket(is, randomdata.length, senderluid);
            bd.catBody(os);
            hash = BlockDataPacket.bytesToHex(bd.streamhash);
            System.out.println(BlockDataPacket.bytesToHex(bd.streamhash));
            works = true;

        } catch(IOException e) {
            works = false;
        }

        assertThat(works, is(true));
        assertThat(hash != null, is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHandlesStreamingIO() {
        byte[] senderluid = {1,2,3,4,5,6};
        File sourcefile = new File("/dev/zero");
        boolean sent = false;
        boolean valid = true;
        try {
            FileInputStream fstream = new FileInputStream(sourcefile);
            BlockDataPacket bd = new BlockDataPacket(fstream, 4096, senderluid);
            File out = new File("/dev/null");
            FileOutputStream outstream = new FileOutputStream(out);
            bd.catBody(outstream);
            valid =!bd.isInvalid();
            sent = bd.sent;
        } catch(IOException e) {
            valid = false;
        }

        assertThat(valid, is(true));
        assertThat(sent, is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketFromDataIsValid() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());

        System.out.println("BlockDataPacketFromDataIsValid() ");
        for(int x : ne.err) {
            System.out.print(x + " ");
        }
        System.out.println();
        System.out.println(new String(bd.body));
        System.out.println(new String(ne.body));
        assertThat(ne.isInvalid(), is(false));
    }


    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHasSameHashWhenReconstructed() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());

        assertThat(bd.getHash().equals(ne.getHash()), is(true));
    }
    @SuppressWarnings("unused")
    @Test
    public void AdvertisePacketIsInvalidWithBogusLUID() {
        byte[] test = {1, 2, 3, 4};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap = new AdvertisePacket(profile);

        byte[] test2 = {1, 2, 3, 4, 8};
        DeviceProfile profile2 = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test2);
        AdvertisePacket ap2 = new AdvertisePacket(profile2);

        assertThat(ap.isInvalid() && ap2.isInvalid(), is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketWithNullDataIsValid() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());
        if(bd.getContents().length == BlockDataPacket.HEADERSIZE) {
            System.out.println("HEADERSIZE");
        }
        System.out.println(bd.getContents().length);
        System.out.println(ne.getContents().length);
        System.out.println("err");
        for(int b : ne.err) {
            System.out.print(b + " ");
        }
        System.out.println();
        assertThat(bd.getHash().equals(ne.getHash()), is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataSizeOperatorReturnsCorrectSize() {
        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);

        assertThat(bd.size == BlockDataPacket.getSizeFromData(bd.getContents()), is(true));

        byte[] senderluid2 = {1,2,3,4,5,6};
        byte[] randomdata2 = {3,3,65,34,6,3,52,52,5,2,5};

        BlockDataPacket bd2 = new BlockDataPacket(randomdata2, false, senderluid2);

        assertThat(bd2.size == BlockDataPacket.getSizeFromData(bd2.getContents()), is(true));
    }

}
