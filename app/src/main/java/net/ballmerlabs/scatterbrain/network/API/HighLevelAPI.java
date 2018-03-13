package net.ballmerlabs.scatterbrain.network.API;

import android.content.SharedPreferences;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;

import java.io.InputStream;

/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public interface HighLevelAPI {

    //system
    void startService();
    void stopService();
    SharedPreferences getPref();
    void setPref(SharedPreferences pref);
    DeviceProfile getProfile();
    void setProfile(DeviceProfile prof);
    ScatterTransport[] getTransports();

    //peers
    void scanOn(ScatterTransport transport);
    void scanOff(ScatterTransport transport);
    DeviceProfile[] getPeers();

    //communications
    boolean sendDataDirected(DeviceProfile target, byte[] data);
    void sendDataMulticast(byte[] data);
    boolean sendFileDirected(DeviceProfile target, InputStream file, String name, long len);
    void sendFileMulticast(InputStream file, String name, long len); //TODO: java file object
    void registerOnRecieveCallback(OnRecieveCallback callback);


   //datastore
    BlockDataPacket[] getTopMessages(int num);
    BlockDataPacket[] getRandomMessages(int num);

    //datastore systems tasks
    void flushDatastore();
    void setDatastoreLimit(int limit);
    int getDatastoreLimit();
}
