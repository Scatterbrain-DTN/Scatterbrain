/**
 * Performs tests on the scatterbrain protocol outside android to
 * reduce the chance of bugged out packets.
 */
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class ProtocolUnitTest {

    @Test
    public void AdvertisePacketFromProfileIsValid() {
        byte[] test = {1,2,3,4,5};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap  = new AdvertisePacket(profile);

        assertThat(ap.isInvalid() , is(false));
    }

    @Test
    public void AdvertisePacketFromDataAndProfileIsValid() {
        byte[] test = {1,2,3,4,5};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap  = new AdvertisePacket(profile);
        byte[] data = ap.getContents();
        AdvertisePacket newpacket = new AdvertisePacket(data);

        assertThat(newpacket.isInvalid(), is(false));
    }

    @Test
    public void BlockDataPacketIsValid() {
        byte[] senderluid = {1,2,3,4,5};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        assertThat(bd.isInvalid(), is(false));
    }

    @Test
    public void BlockDataPacketHandlesNullData() {
        byte[] senderluid = {1,2,3,4,5};
        byte[] randomdata = {};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        assertThat(bd.isInvalid(), is(false));
    }

    @Test
    public void BlockDataPacketFromDataIsValid() {
        byte[] senderluid = {1,2,3,4,5};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());

        assertThat(ne.isInvalid(), is(false));
    }


    @Test
    public void BlockDataPacketHasSameHashWhenReconstructed() {
        byte[] senderluid = {1,2,3,4,5};
        byte[] randomdata = {4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2,
                4,2,26,2,6,46,2,2,6,21,6,5,1,7,1,7,1,87,2,78,2};
        BlockDataPacket bd = new BlockDataPacket(randomdata, false, senderluid);
        BlockDataPacket ne = new BlockDataPacket(bd.getContents());

        assertThat(bd.getHash().equals(ne.getHash()), is(true));
    }

    @Test
    public void AdvertisePacketIsInvalidWithBogusLUID() {
        byte[] test = {1, 2, 3, 4};
        DeviceProfile profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test);
        AdvertisePacket ap = new AdvertisePacket(profile);

        byte[] test2 = {1, 2, 3, 4, 8, 4};
        DeviceProfile profile2 = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTH, test2);
        AdvertisePacket ap2 = new AdvertisePacket(profile2);

        assertThat(ap.isInvalid() && ap2.isInvalid(), is(true));
    }
}
