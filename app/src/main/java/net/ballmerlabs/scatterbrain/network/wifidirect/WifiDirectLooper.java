package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by user on 5/29/16.
 */
public class WifiDirectLooper extends Thread {

    public Handler handler;

    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg)  {

            }
        };

        Looper.loop();


    }
}
