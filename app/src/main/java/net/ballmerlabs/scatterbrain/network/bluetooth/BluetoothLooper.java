package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.ballmerlabs.scatterbrain.network.GlobalNet;

/**
 * handles delayed tasks for periodic bluetooth scanning
 */
public class BluetoothLooper extends Thread {

    private Handler handler;
    private final GlobalNet globnet;

    public BluetoothLooper(GlobalNet globnet) {
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
