package net.ballmerlabs.scatterbrain;

import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Centralized logging framework for redirecting android logcat
 * for "field use"
 */

public  class ScatterLogManager {
    public static ArrayAdapter<String> adapter;

    public static void v(String tag, String msg) {
        Log.v(tag,msg);
        if(adapter != null)
                adapter.add("[" + tag + "]: " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag,msg);
        if(adapter != null)
            adapter.add("[" + tag + "]: " + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag,msg);
        if(adapter != null)
            adapter.add("[" + tag + "]: " + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag,msg);
        if(adapter != null)
            adapter.add("[" + tag + "]: " + msg);
    }
}
