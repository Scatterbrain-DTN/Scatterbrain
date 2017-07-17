package net.ballmerlabs.scatterbrain.network;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
class HighLevelAPI {
    @SuppressWarnings("unused")
    private final DeviceProfile MyProfile;

    @SuppressWarnings("unused")
    public HighLevelAPI(DeviceProfile profile) {
        this.MyProfile = profile;
    }

    @SuppressWarnings("EmptyMethod")
    public void Advertise() {

    }

    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    public void sendData(DeviceProfile target) {

    }

    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    public void queryServices(DeviceProfile target) {

    }
}
