package net.ballmerlabs.scatterbrain.network;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
public class HighLevelAPI {
    private DeviceProfile MyProfile;

    public HighLevelAPI(DeviceProfile profile) {
        this.MyProfile = profile;
    }

    public void Advertise() {

    }

    public void sendData(DeviceProfile target) {

    }

    public void queryServices(DeviceProfile target) {

    }
}
