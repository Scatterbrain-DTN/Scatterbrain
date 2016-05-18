package net.ballmerlabs.scatterbrain.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.BLE.BluetoothSpewer;
import net.ballmerlabs.scatterbrain.network.BLE.LeNotSupportedException;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.NetworkCallback;

public class BackgroundMessageSwapService extends Service {

    BluetoothSpewer bleSpew;
    LeDataStore store;
    Activity mainActivity;
    public final String TAG = "MessageSwapService";
    public DeviceProfile myprofile;

    IBinder myBinder = new BinderInterface();

    public BackgroundMessageSwapService(Activity mainActivity, DeviceProfile myprofile, BluetoothSpewer spew) {
        this.mainActivity = mainActivity;
        this.myprofile = myprofile;
        this.bleSpew = spew;
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
