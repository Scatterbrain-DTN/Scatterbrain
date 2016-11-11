package net.ballmerlabs.scatterbrain;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Centralized logging framework for redirecting android logcat
 * for "field use"
 */

public  class ScatterLogManager {
    private static ArrayAdapter<String> adapter;
    public static void init(ArrayAdapter<String> madapter) {
        adapter = madapter;

    }

    public static void v(final String tag, final String msg) {
        Log.v(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + msg);
                }
            });
        }
    }

    public static void e(final String tag, final String msg) {
        Log.e(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + msg);
                }
            });
        }

    }

    public static void d(final String tag, final String msg) {
        Log.d(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + msg);
                }
            });
        }
    }

    public static void i(final String tag,final String msg) {
        Log.i(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("["+tag+"]: "+msg);
                }
            });
        }
    }
}
