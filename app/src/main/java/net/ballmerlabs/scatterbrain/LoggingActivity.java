package net.ballmerlabs.scatterbrain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
public class LoggingActivity extends AppCompatActivity {

    private ScatterRoutingService mService;
    private boolean scatterBound;
    private final String TAG = "LoggingActivity";
    private Spinner sp;
    private ListViewAutoScrollHelper autoScroll;
    private ExpandableListView listView;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayAdapter<String> listViewArrayAdapter;
    private LogPrinter logPrinter;
    private LogManager logManager;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            mService.registerOnDeviceConnectedCallback(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView senpai_notice = (TextView) findViewById(R.id.notice_text);
                            if(senpai_notice != null) {
                                senpai_notice.setVisibility(View.VISIBLE);
                                senpai_notice.setText("Senpai NOTICED YOU! \n and the packet was not corrupt");
                            }
                        }
                    });
                }
            });


            ScatterLogManager.v(TAG, "Bound to routing service");
            scatterBound = true;



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ScatterLogManager.e(TAG, "Disconnected from routing service");
            scatterBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        logPrinter = new LogPrinter(Log.VERBOSE,this.TAG);
        sp = (Spinner) this.findViewById(R.id.filterchooser);
        String items[] = {"Verbose", "Info", "Error"};
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                items);

        sp.setAdapter(spinnerArrayAdapter);

        listView = (ExpandableListView) this.findViewById(R.id.loggerlist);
        listViewArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1);

        mService.registerLoggingArrayAdapter(listViewArrayAdapter);

        listView.setAdapter(listViewArrayAdapter);

        ScatterLogManager.adapter = listViewArrayAdapter;

        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
