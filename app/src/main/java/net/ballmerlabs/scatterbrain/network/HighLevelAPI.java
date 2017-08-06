package net.ballmerlabs.scatterbrain.network;

import android.content.SharedPreferences;

import java.io.InputStream;

/**
 * basic interface to the Scatterbrain protocol, to be used by
 * external applications
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
interface HighLevelAPI {

    //system
    void startService();
    void stopService();
    SharedPreferences getPref();
    void setPref(SharedPreferences pref);
    DeviceProfile getProfile();
    void setProfile(DeviceProfile prof);

    //peers
    void scanOn();
    void scanOff();
    DeviceProfile[] getPeers();

    //communications
    void sendDataDirected(DeviceProfile target, byte[] data);
    void sendDataMulticast(byte[] data);
    void sendFile(InputStream file);
    void postMessagesRecievedHandler(Runnable run); //TODO: make a handler that accepts messages
    BlockDataPacket[] getTopMessages(int num);
    BlockDataPacket[] getRandomMessages(int num);
    BlockDataPacket[] getTopMessages();
    BlockDataPacket[] getRandomMessages();

    //datastore systems tasks
    int getQueueSize();
    void flushDatastore();
    void setDatastoreLimit(int limit);
    int getDatastoreLimit();
}
