/*
 * Performs tests on the scatterbrain protocol outside android to
 * reduce the chance of bugged out packets.
 **/
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.ballmerlabs.scatterbrain.DatastoreCommandActivity;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.datastore.MsgDbHelper;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterReceiveThread;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.cglib.core.Block;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSQLiteConnection;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unused")


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProtocolUnitTest {


    public static LeDataStore getConnectedDatastore() {
        ScatterRoutingService mainService = Robolectric.setupService(ScatterRoutingService.class);

        NetTrunk netTrunk = new NetTrunk(mainService);
        SQLiteOpenHelper helper = new MsgDbHelper(RuntimeEnvironment.application);
        LeDataStore dataStore = new LeDataStore(mainService, netTrunk, helper);
        dataStore.connect();
        return dataStore;
    }

    @Test
    public void DataStoreConnectDisconnectTest() {
        LeDataStore dataStore = getConnectedDatastore();
        assertThat(dataStore.connected, is(true));
        dataStore.disconnect();
        assertThat(dataStore.connected, is(false));

    }


    @Test
    public void datastoreBlockDataOperations() {
        LeDataStore dataStore = getConnectedDatastore();
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        dataStore.enqueueMessageNoDuplicate(bd);
        ArrayList<BlockDataPacket> res = dataStore.getTopRandomMessages(1);
        assertThat(res.size() == 1, is(true));
        assertThat(res.get(0).isInvalid(), is(false));
        dataStore.disconnect();
    }


    @Test
    public void datastoreBlockDataAcceptsDuplicates() {
        LeDataStore dataStore = getConnectedDatastore();
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        dataStore.enqueueMessageNoDuplicate(bd);
        dataStore.enqueueMessageNoDuplicate(bd);
        ArrayList<BlockDataPacket> res = dataStore.getTopRandomMessages(2);
        assertThat(res.size() == 1, is(true));
        assertThat(res.get(0).isInvalid(), is(false));
        dataStore.flushDb();
        dataStore.disconnect();
    }

    @Test
    public void datastoreBlockDataAcceptsSingular() {
        LeDataStore dataStore = getConnectedDatastore();
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] senderluid2 = {1, 3, 3, 4, 5, 6};
        byte[] randomdata2 = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 6, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 8, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd1 = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket bd2 = new BlockDataPacket(randomdata2, false, senderluid);
        BlockDataPacket bd3 = new BlockDataPacket(randomdata, false, senderluid2);
        dataStore.enqueueMessageNoDuplicate(bd1);
        ArrayList<BlockDataPacket> res0 = dataStore.getTopRandomMessages(3);
        System.out.println("ins1 size " + res0.size());
        dataStore.enqueueMessageNoDuplicate(bd2);
        ArrayList<BlockDataPacket> res1 = dataStore.getTopRandomMessages(3);
        System.out.println("ins2 size " + res1.size());
        dataStore.enqueueMessageNoDuplicate(bd3);
        ArrayList<BlockDataPacket> res = dataStore.getTopRandomMessages(3);
        System.out.println("ins3 size " + res.size());
        assertThat(res.size() == 3, is(true));
        for (BlockDataPacket bd : res) {
            assertThat(bd.isInvalid(), is(false));
        }
        dataStore.flushDb();
        dataStore.disconnect();
    }


    @Test
    public void datastoreBlockDataAcceptsFileSingular() {
        final String tmp1_path = "/tmp/t29dn3d";
        final String tmp2_path = "/tmp/i3f3jof";
        File tmp1 = new File(tmp1_path);
        File tmp2 = new File(tmp2_path);
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] senderluid2 = {1, 2, 3, 4, 5, 6};

        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 1, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        byte[] randomdata2 = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};

        try {
            FileOutputStream firstout = new FileOutputStream(tmp1);
            FileOutputStream secondout = new FileOutputStream(tmp2);
            FileOutputStream devnull = new FileOutputStream("/dev/null");
            firstout.write(randomdata);
            secondout.write(randomdata2);
            File tmpin = new File(tmp1_path);
            File tmpin2 = new File(tmp2_path);
            FileInputStream in = new FileInputStream(tmpin);
            FileInputStream in2 = new FileInputStream(tmpin2);
            BlockDataPacket bd1 = new BlockDataPacket(in, "fakename", tmpin.length(), senderluid);
            BlockDataPacket bd2 = new BlockDataPacket(in2, "fakename", tmpin2.length(), senderluid);
            bd1.catBody(devnull);
            bd2.catBody(devnull);
            String hash1 = BlockDataPacket.bytesToHex(bd1.streamhash);
            String hash2 = BlockDataPacket.bytesToHex(bd2.streamhash);
            System.out.println("hash1 " + hash1);
            System.out.println("hash2 " + hash2);

            LeDataStore dataStore = getConnectedDatastore();
            assertThat(dataStore.connected, is(true));
            dataStore.enqueueMessageNoDuplicate(bd1);
            dataStore.enqueueMessageNoDuplicate(bd2);
            ArrayList<BlockDataPacket> res = dataStore.getTopRandomMessages(2);
            assertThat(res.size() == 2, is(true));
            dataStore.flushDb();
            dataStore.disconnect();
            assertThat(dataStore.connected, is(false));
        } catch(IOException e) {
            assertThat(false, is(true));
        }
    }

    @Test
    public void datastoreBlockDataAcceptsFileDuplicates() {
        final String path1 = "/tmp/wfeefef3";
        File tmp = new File(path1);
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] senderluid2 = {1, 2, 3, 4, 5, 6};

        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};

        try {
            FileOutputStream firstout = new FileOutputStream(tmp);
            FileOutputStream devnull = new FileOutputStream("/dev/null");
            firstout.write(randomdata);
            File tmpin = new File(path1);
            File tmpin2 = new File(path1);
            FileInputStream in = new FileInputStream(tmpin);
            FileInputStream in2 = new FileInputStream(tmpin2);
            BlockDataPacket bd = new BlockDataPacket(in, "fakename", tmpin.length(), senderluid);
            BlockDataPacket bdnew = new BlockDataPacket(in2, "fakename", tmpin2.length(), senderluid);
            bd.catBody(devnull);
            bdnew.catBody(devnull);
            String hash1 = BlockDataPacket.bytesToHex(bd.streamhash);
            String hash2 = BlockDataPacket.bytesToHex(bdnew.streamhash);
            System.out.println("insize: " + bd.size + "\noutsize " + bdnew.size);
            System.out.println("hash1 " + hash1);
            System.out.println("hash2 " + hash2);

            LeDataStore dataStore = getConnectedDatastore();
            assertThat(dataStore.connected, is(true));
            dataStore.enqueueMessageNoDuplicate(bd);
            dataStore.enqueueMessageNoDuplicate(bd);
            ArrayList<BlockDataPacket> res = dataStore.getTopRandomMessages(2);
            assertThat(res.size() == 1, is(true));

            dataStore.flushDb();
            dataStore.disconnect();
            assertThat(dataStore.connected, is(false));
        } catch(IOException e) {
            assertThat(false, is(true));
        }
    }


    @Test
    public void datastoreTrimsWhenFull() {
        LeDataStore dataStore = getConnectedDatastore();
        dataStore.setDataTrimLimit(120);
        for(byte x =0;x<127;x++) {
            for (byte y = 0; y < 2; y++) {
                byte[] senderluid = {1, 2, 3, 4, 5, 6};
                byte[] randomdata = {4, 2, 26, x, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 6, 87, 2, 78, 2,
                        4, 2, 26, 2, 6, 46, 2, y, 8, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
                BlockDataPacket bd1 = new BlockDataPacket(randomdata, false, senderluid);
                dataStore.enqueueMessageNoDuplicate(bd1);
            }
        }
        assertThat(dataStore.getTopRandomMessages(127*2).size(), is(120));
        dataStore.trimDatastore(10);
        assertThat(dataStore.getTopRandomMessages(127*2).size(), is(10));
        dataStore.flushDb();
        dataStore.disconnect();
    }


    @SuppressWarnings("unused")
    @Test
    public void AdvertisePacketFromProfileIsValid() {
        byte[] test = {1, 2, 3, 4, 5, 6};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap = new AdvertisePacket(profile);

        assertThat(ap.isInvalid(), is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void AdvertisePacketFromDataAndProfileIsValid() {
        byte[] test = {1, 2, 3, 4, 5, 6};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap = new AdvertisePacket(profile);
        byte[] data = ap.getContents();
        AdvertisePacket newpacket = new AdvertisePacket(data);

        System.out.println("AdvertisePacketFromDataAndProfileIsValid");
        for (byte b : newpacket.err) {
            System.out.print(b + " ");
        }
        System.out.println();
        assertThat(newpacket.isInvalid(), is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketIsValid() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        assertThat(bd.isInvalid(), is(false));
    }


    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketFileStreamHashesWork() {
        File tmp = new File("/tmp/t3fghju2");
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2, 4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};

        try {
            FileOutputStream firstout = new FileOutputStream(tmp);
            FileOutputStream devnull = new FileOutputStream("/dev/null");
            firstout.write(randomdata);
            File tmpin = new File("/tmp/t3fghju2");
            File tmpin2 = new File("/tmp/t3fghju2");
            FileInputStream in = new FileInputStream(tmpin);
            FileInputStream in2 = new FileInputStream(tmpin2);
            BlockDataPacket bd = new BlockDataPacket(in, "fakename", tmpin.length(), senderluid);
            BlockDataPacket bdnew = new BlockDataPacket(bd.getContents(), in2);
            bd.catBody(devnull);
            bdnew.catBody(devnull);
            String hash1 = BlockDataPacket.bytesToHex(bd.streamhash);
            String hash2 = BlockDataPacket.bytesToHex(bdnew.streamhash);
            System.out.println("insize: " + bd.size + "\noutsize " + bdnew.size);
            System.out.println("hash1 " + hash1);
            System.out.println("hash2 " + hash2);
            System.out.println(new String(bd.getContents()));
            System.out.println(new String(bdnew.getContents()));
            assertThat(Arrays.equals(bd.getContents(), bdnew.getContents()), is(true));
            assertThat(Arrays.equals(bd.streamhash, bdnew.streamhash), is(true));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    @SuppressWarnings("unused")
    @Test
    public void MultipleBlockDataPacketDryRun() {
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
            ArrayList<File> files = new ArrayList<>();
            ArrayList<BlockDataPacket> bdlist = new ArrayList<>();
            for (int x = 0; x < 5; x++) {
                File tmp = new File("/tmp/t3fghju" + x);
                FileOutputStream firstout = new FileOutputStream(tmp);
                firstout.write(randomdata);
                files.add(new File("/tmp/t3fghju" + x));

                FileInputStream in = new FileInputStream(files.get(x));
                bdlist.add(new BlockDataPacket(in, files.get(x).length(), senderluid));
            }

            final ServerSocket ssocket = new ServerSocket(8877);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket sock =  ssocket.accept();
                        ScatterReceiveThread res = new ScatterReceiveThread(sock);
                        res.start();
                        res.join(); //TODO: implement hash check

                        // System.out.println(res.fakeres.getHash());
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

            int x = 0;
            for(BlockDataPacket  bd :  bdlist) {
                bman.sendRawStream("nothing", bd.getContents(), bd.source, files.get(x).length(), true);
                x++;
            }

            t.join();

            ssocket.close();
            assertThat(ssocket.isClosed(), is(true));

        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException i) {
            i.printStackTrace();
        }
    }

*/

    /*
    //region Description
    @SuppressWarnings("unused")
    @Test
    public void BlockDataFilePacketDryRun() {
        ScatterLogManager.fake = true;
        File tmp = new File("/tmp/t3fghju");
        ScatterRoutingService service = new ScatterRoutingService();
        NetTrunk trunk = new NetTrunk(service);
        ScatterBluetoothManager bman = new ScatterBluetoothManager(trunk);

        byte[] senderluid = {1,2,3,4,5,6};
        byte[] randomdata =  new byte[4096];
        for(int x=0;x<randomdata.length;x++) {
            randomdata[x] = 3;
        }
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
                        res.join(); //TODO: implement hash check

                       // System.out.println(res.fakeres.getHash());
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

            bman.sendStreamToLocalPeer("nothing", bd.getContents(),in,tmp.length(), true);

            t.join();

            ssocket.close();
            assertThat(ssocket.isClosed(), is(true));

        } catch(IOException e) {
            e.printStackTrace();
        } catch(InterruptedException i) {
            i.printStackTrace();
        }
    }
    //endregion


*/


    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketDryRun() {
        ScatterRoutingService service = new ScatterRoutingService();
        NetTrunk trunk = new NetTrunk(service);
        ScatterBluetoothManager bman = new ScatterBluetoothManager(trunk);

        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        boolean go = true;
        try {
            final ServerSocket ssocket = new ServerSocket(8877);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket sock = ssocket.accept();
                        ScatterReceiveThread res = new ScatterReceiveThread(sock);
                        res.start();
                        res.join();
                        ssocket.close();
                        //   assertThat(res.fakedone, is(true));

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException i) {
                        i.printStackTrace();
                    }
                }
            });
            t.start();
            Thread.sleep(1000);
            assertThat(ssocket.isClosed(), is(false));

            bman.sendRaw("nothing", bd.getContents(), true);

            t.join();
            assertThat(ssocket.isClosed(), is(true));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException i) {
            i.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHandlesNullData() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        assertThat(bd.isInvalid(), is(false));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHandlesFileBit() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        boolean works = false;
        File testfile = new File("/dev/null");
        File infile = new File("/dev/zero");
        try {
            FileOutputStream out = new FileOutputStream(testfile);
            FileInputStream in = new FileInputStream(infile);
            BlockDataPacket bd = new BlockDataPacket(in, "fakename", 4096, senderluid);
            int fileStatusFromData = BlockDataPacket.getFileStatusFromData(bd.getContents());
            if (fileStatusFromData == 1) {
                works = true;
            }
        } catch (IOException e) {
            works = false;
        }

        assertThat(works, is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataFilePacketHasStableContects() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        boolean works;
        File f = new File("/dev/urandom");
        try {
            FileInputStream i = new FileInputStream(f);
            BlockDataPacket bd = new BlockDataPacket(i, "fakename", 4096, senderluid);
        } catch (IOException e) {

        }

    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketHasSameHashWhenReconstructedFromFile() {
        //TODO: hangs here while cating file
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        final String filename = "/tmp/77246out";
        File out = new File(filename);
        File in = new File(filename);
        String hash = null;
        boolean works;
        try {
            FileOutputStream os = new FileOutputStream(out);
            FileInputStream is = new FileInputStream(in);
            os.write(randomdata);
            BlockDataPacket bd = new BlockDataPacket(is, "fakename", randomdata.length, senderluid);
            bd.catBody(os);
            hash = BlockDataPacket.bytesToHex(bd.streamhash);
            System.out.println(BlockDataPacket.bytesToHex(bd.streamhash));
            works = true;

        } catch (IOException e) {
            works = false;
        }

        assertThat(works, is(true));
        assertThat(hash != null, is(true));
    }


    @Test
    public void blockDataStreamPacketHasSaneFileName() {
        File f = new File("/dev/zero");
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        boolean err = false;
        try {
            FileInputStream fi = new FileInputStream(f);
            BlockDataPacket bd = new BlockDataPacket(fi, "test name", 100, senderluid);
            System.out.println(bd.getFilename());
            //names are not fully equal because encoding is UTF-8
            assertThat(bd.getFilename().equals("test name"), is(true));
        } catch (IOException e) {
            System.err.println("IOException");
            err = true;
        }

        assertThat(err, is(false));
    }

    /*
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
            e.printStackTrace();
            valid = false;
        }

        assertThat(valid, is(true));
        assertThat(sent, is(true));
    }

*/

    @SuppressWarnings("unused")
    @Test
    public void BlockDataPacketFromDataIsValid() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());

        System.out.println("BlockDataPacketFromDataIsValid() ");
        for (int x : ne.err) {
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
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] senderluid2 = {9, 2, 3, 4, 5, 6};
        byte[] randomdata = {4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};
        byte[] randomdata2 = {9, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2,
                4, 2, 26, 2, 6, 46, 2, 2, 6, 21, 6, 5, 1, 7, 1, 7, 1, 87, 2, 78, 2};

        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket bd2 = new BlockDataPacket(randomdata, false, senderluid2);
        BlockDataPacket bd3 = new BlockDataPacket(randomdata2, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());
        BlockDataPacket ne2 = new BlockDataPacket(bd2.getContents());
        BlockDataPacket ne3 = new BlockDataPacket(bd3.getContents());

        assertThat(bd.getHash().equals(ne.getHash()), is(true));
        assertThat(bd2.getHash().equals(ne2.getHash()), is(true));
        assertThat(bd3.getHash().equals(ne3.getHash()), is(true));
        assertThat(bd.getHash().equals(bd2.getHash()), is(false));
        assertThat(bd2.getHash().equals(bd3.getHash()), is(false));
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
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());
        if (bd.getContents().length == BlockDataPacket.HEADERSIZE) {
            System.out.println("HEADERSIZE");
        }
        System.out.println(bd.getContents().length);
        System.out.println(ne.getContents().length);
        System.out.println("err");
        for (int b : ne.err) {
            System.out.print(b + " ");
        }
        System.out.println();
        assertThat(bd.getHash().equals(ne.getHash()), is(true));
    }

    @SuppressWarnings("unused")
    @Test
    public void BlockDataSizeOperatorReturnsCorrectSize() {
        byte[] senderluid = {1, 2, 3, 4, 5, 6};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);

        assertThat(bd.size == BlockDataPacket.getSizeFromData(bd.getContents()), is(true));

        byte[] senderluid2 = {1, 2, 3, 4, 5, 6};
        byte[] randomdata2 = {3, 3, 65, 34, 6, 3, 52, 52, 5, 2, 5};

        BlockDataPacket bd2 = new BlockDataPacket(randomdata2, false, senderluid2);

        assertThat(bd2.size == BlockDataPacket.getSizeFromData(bd2.getContents()), is(true));
    }
}