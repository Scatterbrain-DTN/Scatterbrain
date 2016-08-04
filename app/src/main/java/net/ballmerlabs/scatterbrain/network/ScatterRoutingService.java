package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

/**
 * Created by user on 8/3/16.
 */
public class ScatterRoutingService extends Service {

    private final IBinder mBinder = new ScatterBinder();
    private NetTrunk trunk;
    private Activity mainActivity;

    public class ScatterBinder extends Binder {
        public ScatterRoutingService getService() {
            return ScatterRoutingService.this;
        }
    }

    public ScatterRoutingService() {

    }
    public ScatterRoutingService(final Activity mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if(!trunk.blman.getAdapter().isEnabled()) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mainActivity.startActivityForResult(enableBtIntent, trunk.blman.REQUEST_ENABLE_BT);
                }
            });

        }
        else {
            trunk.blman.init();
        }

        trunk.blman.startDiscoverLoopThread();
        return 0;

    }

    @Override
    public IBinder onBind(Intent i) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        trunk = new NetTrunk(mainActivity);
    }

    @Override
    public void onDestroy() {

    }
}
