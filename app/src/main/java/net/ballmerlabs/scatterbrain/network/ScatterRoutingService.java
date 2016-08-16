package net.ballmerlabs.scatterbrain.network;

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

import net.ballmerlabs.scatterbrain.NormalActivity;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Represents a background service for routing packets
 * for the scatterbrain protocol.
 */
public class ScatterRoutingService extends Service {

    private final IBinder mBinder = new ScatterBinder();
    private static NetTrunk trunk;
    private Service me;
    private boolean bound = false;
    public final String TAG = "ScatterRoutingService";
    private  Runnable onDevicesFound;
    public SharedPreferences sharedPreferences;
    public String luid;
    private ArrayAdapter<String> Messages;


    public class ScatterBinder extends Binder {
        public ScatterRoutingService getService() {
            return ScatterRoutingService.this;
        }
    }

    public ScatterRoutingService() {
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Notification notification = new Notification(R.drawable.icon, getText(R.string.service_ticker),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, ScatterRoutingService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent,0);
        notification.setLatestEventInfo(this, getText(R.string.service_title),
                getText(R.string.service_body), pendingIntent);
        startForeground(1, notification);

        trunk.blman.startDiscoverLoopThread();
        return 0;

    }

    public void registerOnDeviceConnectedCallback(Runnable run) {
        if(bound) {
            onDevicesFound = run;
        }
        else {
            Log.v(TAG,"Attempted to register UI callback when not bound for some odd reason");
        }
    }

    public boolean updateUiOnDevicesFound() {
        if((onDevicesFound != null) && bound) {
            onDevicesFound.run();
            return true;
        }
        else
        return false;
    }

    public void noticeNotify(String title, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(text);
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

    public ScatterBluetoothManager getBluetoothManager() {
        return trunk.blman;
    }
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
        me = this;
        trunk = new NetTrunk(this);
        Context context = this.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.scatter_preference_key),
                Context.MODE_PRIVATE);

        String uuid = sharedPreferences.getString(getString(R.string.scatter_uuid), getString(R.string.uuid_not_present));
        if(uuid.equals(getString(R.string.uuid_not_present))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            this.luid = genLUID();
            editor.putString(getString(R.string.scatter_uuid),this.luid );
            editor.commit();
        }
        else {
            this.luid = uuid;
        }
    }


    public void regenerateUUID() {
        Context context = this.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.scatter_preference_key),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        this.luid = genLUID();
        editor.putString(getString(R.string.scatter_uuid),this.luid );
        editor.commit();
    }

    private String genLUID() {
        byte[] rand = new byte[6];
        Random r = new Random();
        r.nextBytes(rand);
        return new String(Base64.encode(rand,Base64.DEFAULT));
    }

    public static NetTrunk getNetTrunk() {
        return trunk;
    }

    public void startMessageActivity() {
        Intent startIntent = new Intent(this, NormalActivity.class);
        startActivity(startIntent);
    }

    @Override
    public void onDestroy() {
        trunk.blman.stopDiscoverLoopThread();
    }

    public ArrayAdapter<String> getMessageAdapter() {
        if(bound) {
            return Messages;
        }
        else {
            return null;
        }
    }

    public void registerMessageArrayAdapter(ArrayAdapter<String> messages) {
        this.Messages = messages;
    }
}
