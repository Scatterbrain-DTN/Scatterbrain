package net.ballmerlabs.scatterbrain.network;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
@SuppressWarnings("FieldCanBeLocal")
class HighLevelAPI {
    private final DeviceProfile MyProfile;

    public HighLevelAPI(DeviceProfile profile) {
        this.MyProfile = profile;
    }

    @SuppressWarnings("EmptyMethod")
    public void Advertise() {

    }

    @SuppressWarnings("EmptyMethod")
    public void sendData(DeviceProfile target) {

    }

    @SuppressWarnings("EmptyMethod")
    public void queryServices(DeviceProfile target) {

    }
}
