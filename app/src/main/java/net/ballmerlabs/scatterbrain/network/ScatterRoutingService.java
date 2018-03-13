package net.ballmerlabs.scatterbrain.network;


import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.UUID;

import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.API.HighLevelAPI;
import net.ballmerlabs.scatterbrain.network.API.OnRecieveCallback;
import net.ballmerlabs.scatterbrain.network.API.ScatterTransport;
import net.ballmerlabs.scatterbrain.network.bluetooth.LocalPeer;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;

import net.ballmerlabs.scatterbrain.utils.ScatterLogManager;
import net.ballmerlabs.scatterbrain.datastore.MsgDbHelper;
/**
 * Represents a background service for routing packets
 * for the scatterbrain protocol.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ScatterRoutingService extends Service
        implements HighLevelAPI {

    private final IBinder mBinder = new ScatterBinder();
    private static NetTrunk trunk;
    private boolean bound = false;
    private final String TAG = "ScatterRoutingService";
    private PeersChangedCallback onDevicesFound;
    private SharedPreferences sharedPreferences;
    public byte[] luid;
    @SuppressWarnings("unused")
    private ArrayAdapter<String> logbuffer;
    public LeDataStore dataStore;
    private final Application fakeapp;
    public OnRecieveCallback onRecieveCallback;


    @Override
    public DeviceProfile getProfile() {
        return this.getTrunk().profile;
    }

    @Override
    public void setProfile(DeviceProfile profile) {
        this.getTrunk().profile = profile;
    }

    @Override
    public void startService() {
        NotificationCompat.Builder not = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Scatterbrain")
                .setContentText("Discoverting Peers...");

        Intent result = new Intent(this, ScatterRoutingService.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        result,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        not.setContentIntent(resultPendingIntent);

        int notificationId = 1;
        NotificationManager man = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            man.notify(notificationId, not.build());
        } catch(NullPointerException e) {
            ScatterLogManager.e(TAG, "Could not create notification, NullPointerException");
        }
    }


    @Override
    public void stopService() {
        int notificationId = 1;
        NotificationManager man = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            man.cancel(notificationId);
        } catch(NullPointerException e) {
            ScatterLogManager.e(TAG, "Could not create notification, NullPointerException");
        }
        this.getTrunk().blman.stopDiscoverLoopThread();
        //TODO: check this, stop additional components
    }

    @Override
    public SharedPreferences getPref() {
        return this.sharedPreferences;
    }

    @Override
    public void setPref(SharedPreferences p) {
        this.sharedPreferences = p;
    }

    @Override
    public ScatterTransport[] getTransports() {
        ScatterTransport[] t = new ScatterTransport[1];
        t[0] = new ScatterTransport() {
            @Override
            public UUID getUUID() {
                return trunk.blman.UID;
            }

            @Override
            public String getNameString() {
                return "Bluetooth";
            }

            @Override
            public int getPriority() {
                return 1;
            }

            @Override
            public void setPriority(int priority) {

            }
        };

        return t;
    }


    @Override
    public void scanOn(ScatterTransport t) {
        for(ScatterTransport n : getTransports()) {
            if(t.getUUID().compareTo(n.getUUID()) == 0) {
                this.getBluetoothManager().startDiscoverLoopThread();
                return;
            }
        }
    }

    @Override
    public void scanOff(ScatterTransport t) {
        for(ScatterTransport n : getTransports()) {
            if(t.getUUID().compareTo(n.getUUID()) == 0) {
                this.getBluetoothManager().stopDiscoverLoopThread();
                return;
            }
        }
    }


    //TODO: expand
    @Override
    public DeviceProfile[] getPeers() {
        ArrayList<DeviceProfile> res = new ArrayList<>();
        for(Map.Entry<String, LocalPeer> e : this.getBluetoothManager().connectedList.entrySet()) {
            res.add(e.getValue().getProfile());
        }
        return (DeviceProfile[]) res.toArray();
    }

    @Override
    public boolean sendDataDirected(DeviceProfile target, byte[] data) {
        BlockDataPacket bd = new BlockDataPacket(data, false, this.luid);
        LocalPeer p = this.getBluetoothManager().luidConnectedList.get(target);
        if(p == null)
            return false;
        else {
            this.getBluetoothManager().sendMessageToLocalPeer(p.getSocket().getRemoteDevice().getAddress(), bd);
            return true;
        }
    }


    @Override
    public void sendDataMulticast(byte[] data) {
        BlockDataPacket bd = new BlockDataPacket(data, false, this.luid);
        this.getBluetoothManager().sendMessageToBroadcast(bd);
    }


    //TODO: adjust enqueue
    @Override
    public boolean sendFileDirected(DeviceProfile target, InputStream file, String name, long len) {
        LocalPeer p = this.getBluetoothManager().luidConnectedList.get(target);
        if(p == null)
            return false;

        BlockDataPacket bd = new BlockDataPacket(file, name, len, this.luid);
        this.getBluetoothManager().sendRawStream(p.getSocket().getRemoteDevice().getAddress(), bd,
                false, true);
        return true;
    }


    @Override
    public void sendFileMulticast(InputStream file, String name, long len) {
        BlockDataPacket bd = new BlockDataPacket(file, name, len, this.luid);
        this.getBluetoothManager().sendStreamToBroadcast(bd, false);
    }

    @Override
    public void registerOnRecieveCallback(OnRecieveCallback r) {
        this.onRecieveCallback = r;
    }

    @Override
    public BlockDataPacket[] getTopMessages(int num) {
        return (BlockDataPacket[]) this.dataStore.getTopMessages(num).toArray();
    }

    @Override
    public BlockDataPacket[] getRandomMessages(int num) {
        return (BlockDataPacket[]) this.dataStore.getTopRandomMessages(num).toArray();
    }

    @Override
    public void setDatastoreLimit(int size) {
        this.dataStore.setDataTrimLimit(size);
    }

    @Override
    public int getDatastoreLimit() {
        return this.dataStore.getDataTrimLimit();
    }

    @Override
    public void flushDatastore() {
        this.dataStore.flushDb();
    }

    public class ScatterBinder extends Binder {
        public ScatterRoutingService getService() {
            return ScatterRoutingService.this;
        }
    }

    @SuppressWarnings("unused")
    public ScatterRoutingService() {
        fakeapp = null;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {

        this.startService();

        trunk.blman.startDiscoverLoopThread();
        return Service.START_STICKY_COMPATIBILITY;

    }

    public void registerPeersChangedCallback(PeersChangedCallback run) {
        if(bound) {
            onDevicesFound = run;
        }
        else {
            ScatterLogManager.v(TAG,"Attempted to register UI callback when not bound for some odd reason");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean updateUiOnDevicesFound(Map<String, LocalPeer> connectedList) {
        if((onDevicesFound != null) && bound) {
            onDevicesFound.run(connectedList);
            return true;
        }
        else
        return false;
    }

    public void noticeNotify() {
        NotificationCompat.Builder mBuilder =
                new  NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Senpai NOTICED YOU!!")
                .setContentText("There is a senpai in your area somewhere");
        Intent resultIntent = new Intent(this, ScatterRoutingService.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(ScatterRoutingService.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //2 is an id that should be updated to modify unique notifications
        mNotificationManager.notify(2,mBuilder.build());
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    void checkForUpdates() {

    }

    public static byte[] getHashForStream(InputStream i) {
        byte[] hash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024];
            int bytes_recieved;
            int offset = 0;
            while ((bytes_recieved = i.read(buffer)) != -1) {
                digest.update(buffer, 0, bytes_recieved);
                offset += bytes_recieved;
            }
            hash = digest.digest();
            return hash;

        } catch(Exception e) {
            ScatterLogManager.e("Static hashing", Log.getStackTraceString(e));
            return null;
        }
    }


    private ScatterBluetoothManager getBluetoothManager() {
        return trunk.blman;
    }

    private NetTrunk getTrunk() {
        return trunk;
    }

    @Override
    public IBinder onBind(Intent i) {
        bound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent i) {
        bound = false;
        return true;
    }


    @Override
    public void onCreate() {
        Context context = this.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.scatter_preference_key),
                Context.MODE_PRIVATE);

        String uuid = sharedPreferences.getString(getString(R.string.scatter_uuid), getString(R.string.uuid_not_present));
        if(uuid.equals(getString(R.string.uuid_not_present))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            this.luid = genLUID();
            editor.putString(getString(R.string.scatter_uuid), Base64.encodeToString(this.luid, Base64.DEFAULT));
            editor.apply();
        }
        else {
            this.luid = Base64.decode(uuid, Base64.DEFAULT);
        }
        trunk = new NetTrunk(this);
        SQLiteOpenHelper helper = new MsgDbHelper(this.getApplicationContext());
        this.dataStore = new LeDataStore(this, trunk, helper);
        dataStore.connect();
    }

    @SuppressWarnings("unused")
    public void registerLoggingArrayAdapter(ArrayAdapter<String> ad) {
        this.logbuffer = ad;
    }


    public void regenerateUUID() {
        Context context = this.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.scatter_preference_key),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        this.luid = genLUID();
        editor.putString(getString(R.string.scatter_uuid),new String(this.luid) );
        editor.apply();
    }

    private byte[] genLUID() {
        byte[] rand = new byte[6];
        Random r = new Random();
        r.nextBytes(rand);
        return rand;
    }

    public static NetTrunk getNetTrunk() {
        return trunk;
    }

    @SuppressWarnings("EmptyMethod")
    public void startMessageActivity() {
     //   Intent startIntent = new Intent(this, NormalActivity.class);
       // startActivity(startIntent);
    }

    @Override
    public void onDestroy() {
        trunk.blman.stopDiscoverLoopThread();
    }

}
