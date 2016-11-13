package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

public class DatastoreCommandActivity extends AppCompatActivity {
    private ScatterRoutingService mService;
    private String TAG = "DatastoreCommand";
    private Button connectButton;
    private Button disconnectButton;
    private TextView dbDisplay;
    private LeDataStore ds;
    private boolean scatterBound;
    private boolean dbConnected;
    private Button refresh_button;
    private TextView dbTextView;
    private Button clearButton;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            //mService.getBluetoothManager().startDiscoverLoopThread();
            scatterBound = true;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scatterBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datastore_command);

        connectButton = (Button) findViewById(R.id.dbconnect);
        disconnectButton = (Button) findViewById(R.id.dbdisconnect);
        dbDisplay = (TextView) findViewById(R.id.dboverviewtext);
        if(!dbConnected) {
            dbDisplay.setText("DISCONNECTED");
            dbDisplay.setTextColor(Color.RED);
        }

        final Activity main = this;
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!dbConnected) {
                    ds = new LeDataStore(main, 100);
                    ds.connect();
                    dbDisplay.setText("CONNECTED");
                    dbDisplay.setTextColor(Color.GREEN);
                    dbConnected = true;
                }
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dbConnected) {
                    ds.disconnect();
                    dbDisplay.setText("DISCONNECTED");
                    dbDisplay.setTextColor(Color.RED);
                    dbConnected = false;
                }
            }
        });


        refresh_button = (Button) findViewById(R.id.refreshdb_button);
        dbTextView = (TextView) findViewById(R.id.db_textview2);
        clearButton = (Button) findViewById(R.id.clear_button);

        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.enqueueMessage("Testfefefefeef", "contentsfefef", 5, "goobyfefefefef", "sexy data" , "quantum fruit", "ternary gender", "flagsfrgrgrrfref", "sigfefefefefefefefef", 3);
                    dbTextView.setText(ds.getMessages().toString());
                }
                else {
                    dbTextView.setText("No connection to datastore. Please try again.");
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.flushDb();
                }
            }
        });
        dbTextView.setText("");

    }
}
