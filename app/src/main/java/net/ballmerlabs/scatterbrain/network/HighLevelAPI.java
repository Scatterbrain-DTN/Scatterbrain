package net.ballmerlabs.scatterbrain.network;
/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
interface HighLevelAPI {
    void startService();
    void stopService();
    void Advertise();
    Object getTransports();
    void sendDataDirected(DeviceProfile target, byte[] data);
    void sendDataMulticast(byte[] data);
    BlockDataPacket[] getTopMessage(int num);
    int getQueueSize();
    Object queryServices(DeviceProfile target);
}
