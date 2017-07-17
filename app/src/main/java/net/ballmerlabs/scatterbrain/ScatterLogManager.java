package net.ballmerlabs.scatterbrain;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Centralized logging framework for redirecting android logcat
 * for "field use"
 */

public  class ScatterLogManager {
    private static ArrayAdapter<String> adapter;
    private static final ArrayList<String> buffer = new ArrayList<>();
    private static final int MAXBUFFER = 1000;
    public static void init(ArrayAdapter<String> madapter) {
        adapter = madapter;
        for(String line : buffer) {
            adapter.add(line);
        }
    }

    private static void checkBuffer() {
        if(buffer.size() > MAXBUFFER) {
            buffer.clear();
        }
        if(adapter != null) {
            if (adapter.getCount() > MAXBUFFER) {
                adapter.clear();
         }
        }
    }
    public static void v(final String tag, final String msg) {
        checkBuffer();
        Log.v(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + msg);
                }
            });
        }
        else {
            buffer.add("[" + tag + "]: " + msg);
        }
    }

    public static void e(final String tag, final String msg) {
        checkBuffer();
        Log.e(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + msg);
                }
            });
        }
        else {
            buffer.add("[" + tag + "]: " + msg);
        }

    }

    public static void d(final String tag) {
        checkBuffer();
        Log.d(tag, "onStartSuccess, settingInEffect is null");
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("[" + tag + "]: " + "onStartSuccess, settingInEffect is null");
                }
            });
        }
        else {
            buffer.add("[" + tag + "]: " + "onStartSuccess, settingInEffect is null");
        }
    }

    public static void i(final String tag,final String msg) {
        checkBuffer();
        Log.i(tag,msg);
        if(adapter != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.add("["+tag+"]: "+msg);
                }
            });
        }
        else {
            buffer.add("[" + tag + "]: " + msg);
        }
    }
}
