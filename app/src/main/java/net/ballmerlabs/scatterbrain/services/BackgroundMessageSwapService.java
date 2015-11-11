package net.ballmerlabs.scatterbrain.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.BluetoothSpewer;
import net.ballmerlabs.scatterbrain.network.LeNotSupportedException;
import net.ballmerlabs.scatterbrain.network.NetworkCallback;

import java.io.FileDescriptor;

public class BackgroundMessageSwapService extends Service {

    BluetoothSpewer bleSpew;
    LeDataStore store;
    Activity mainActivity;
    public final String TAG = "MessageSwapService";

    IBinder myBinder = new BinderInterface();

    public BackgroundMessageSwapService(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;

    }

    public class BinderInterface extends Binder {

        public BackgroundMessageSwapService getInstance() {
            return BackgroundMessageSwapService.this;
        }

    }


    /*
      * main thread for all scanning / transfer ops. This will probably execute
      * in order on this thread. May not be needed, as BLEMingle uses callbacks.
     */
    @Override
    public void onCreate() {
        //TODO: remove hardcoded datastore trim size.
        store = new LeDataStore(mainActivity,1000);
        LeGet le = new LeGet();
        try {
            bleSpew = new BluetoothSpewer(mainActivity,le);

        }
        catch(LeNotSupportedException e) {
            Log.e(TAG,"BLE is not supported. Sorry.");
        }


    }

    public String processMessage(String msg) {
        return null; //left off here
    }

    public class LeGet implements NetworkCallback {
        @Override
        public void run(String recv) {

        }
    }



    @Override
    public void onDestroy() {

    }
}
