package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.ballmerlabs.scatterbrain.network.GlobalNet;

/**
 * Created by user on 5/29/16.
 */
@SuppressWarnings({"FieldCanBeLocal", "DefaultFileTemplate"})
class WifiDirectLooper extends Thread {

    private Handler handler;
    @SuppressWarnings("unused")
    private final GlobalNet globnet;

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
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });

                Looper.loop();


    }
}
