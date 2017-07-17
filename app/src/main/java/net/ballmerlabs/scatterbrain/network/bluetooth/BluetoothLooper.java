package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * handles delayed tasks for periodic bluetooth scanning
 */
class BluetoothLooper extends Thread {

    private Handler handler;

    public BluetoothLooper() {
        super();
        handler = new Handler();
    }

    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });

                Looper.loop();


    }
}
