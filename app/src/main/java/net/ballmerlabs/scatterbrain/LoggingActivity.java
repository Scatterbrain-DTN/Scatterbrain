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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.util.logging.LogManager;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
@SuppressWarnings("unused")
public class LoggingActivity extends AppCompatActivity {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ScatterRoutingService mService;
    @SuppressWarnings("unused")
    private boolean scatterBound;
    private final String TAG = "LoggingActivity";
    @SuppressWarnings("FieldCanBeLocal")
    private Spinner sp;
    @SuppressWarnings("unused")
    private ListViewAutoScrollHelper autoScroll;
    private ArrayAdapter<String> listViewArrayAdapter;
    @SuppressWarnings("unused")
    private LogManager logManager;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

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
    public void onResume() {
        super.onResume();
        if(listViewArrayAdapter != null)
            ScatterLogManager.init(listViewArrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        LogPrinter logPrinter = new LogPrinter(Log.VERBOSE, this.TAG);
        sp = (Spinner) this.findViewById(R.id.filterchooser);
        String items[] = {"Verbose", "Info", "Error"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                items);

        sp.setAdapter(spinnerArrayAdapter);

        ListView listView = (ListView) this.findViewById(R.id.loggerlist);
        listViewArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1);


        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);
        listView.setAdapter(listViewArrayAdapter);

        ScatterLogManager.init(listViewArrayAdapter);
        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
