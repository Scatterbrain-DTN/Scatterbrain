package net.ballmerlabs.scatterbrain.network;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
public class HighLevelAPI {
    private DeviceProfile profile;

    public HighLevelAPI(DeviceProfile profile) {
        this.profile = profile;
    }

    public void Advertise() {

    }
}
