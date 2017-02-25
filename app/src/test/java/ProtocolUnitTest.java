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

}
