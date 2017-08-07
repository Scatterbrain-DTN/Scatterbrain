package net.ballmerlabs.scatterbrain.network;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;

import net.ballmerlabs.scatterbrain.MessageBoxAdapter;
import net.ballmerlabs.scatterbrain.NormalActivity;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.SearchForSenpai;
import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.bluetooth.LocalPeer;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Represents a background service for routing packets
 * for the scatterbrain protocol.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ScatterRoutingService extends Service {

    private final IBinder mBinder = new ScatterBinder();
    private static NetTrunk trunk;
    private boolean bound = false;
    private final String TAG = "ScatterRoutingService";
    private  PeersChangedCallback onDevicesFound;
    private SharedPreferences sharedPreferences;
    public byte[] luid;
    private MessageBoxAdapter Messages;
    @SuppressWarnings("unused")
    private ArrayAdapter<String> logbuffer;
    public LeDataStore dataStore;




    public class ScatterBinder extends Binder {
        public ScatterRoutingService getService() {
            return ScatterRoutingService.this;
        }
    }

    @SuppressWarnings("unused")
    public ScatterRoutingService() {
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {

        NotificationCompat.Builder not = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Scatterbrain")
                .setContentText("Discoverting Peers...");

        Intent result = new Intent(this, SearchForSenpai.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        result,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        not.setContentIntent(resultPendingIntent);

        int notificationId = 001;
        NotificationManager man = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        man.notify(notificationId, not.build());

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
        Intent resultIntent = new Intent(this, NormalActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(NormalActivity.class);
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
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
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


    public ScatterBluetoothManager getBluetoothManager() {
        return trunk.blman;
    }
    @SuppressWarnings("unused")
    public NetTrunk getTrunk() {
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
        this.dataStore = new LeDataStore(this);
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

    public MessageBoxAdapter getMessageAdapter() {
        if(bound) {
            return Messages;
        }
        else {
            return null;
        }
    }

    public void registerMessageArrayAdapter(MessageBoxAdapter messages) {
        this.Messages = messages;
    }
}
