package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.ballmerlabs.scatterbrain.network.GlobalNet;

/**
 * Created by user on 5/29/16.
 */
public class WifiDirectLooper extends Thread {

    public Handler handler;
    public GlobalNet globnet;

    public WifiDirectLooper(GlobalNet globnet) {
        super();
        this.globnet = globnet;
        handler = new Handler();
    }

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
