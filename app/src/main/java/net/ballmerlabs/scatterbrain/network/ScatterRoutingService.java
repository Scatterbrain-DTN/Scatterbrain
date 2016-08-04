package net.ballmerlabs.scatterbrain.network;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

/**
 * Created by user on 8/3/16.
 */
public class ScatterRoutingService extends Service {

    private final IBinder mBinder = new ScatterBinder();
    private NetTrunk trunk;
    private Service me;

    public class ScatterBinder extends Binder {
        public ScatterRoutingService getService() {
            return ScatterRoutingService.this;
        }
    }

    public ScatterRoutingService() {
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        //Notification notification = new Notification(R.drawable.icon)
        return 0;

    }

    public ScatterBluetoothManager getBluetoothManager() {
        return trunk.blman;
    }
    public NetTrunk getTrunk() {
        return trunk;
    }

    @Override
    public IBinder onBind(Intent i) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        me = this;
        trunk = new NetTrunk(this);
    }

    @Override
    public void onDestroy() {

    }
}
