package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.datastore.Message;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.util.ArrayList;

public class DatastoreCommandActivity extends AppCompatActivity {
    private ScatterRoutingService mService;
    private String TAG = "DatastoreCommand";
    private TextView dbDisplay;
    private LeDataStore ds;
    private boolean scatterBound;
    private boolean dbConnected;
    private Button refresh_button;
    private TextView dbTextView;
    private Button clearButton;
    private Button trimButton;
    private Button randButton;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            //mService.getBluetoothManager().startDiscoverLoopThread();
            scatterBound = true;

            ds = mService.dataStore;

            dbConnected = mService.dataStore.connected;
            if (dbConnected) {
                ScatterLogManager.v(TAG, "DatastoreCommandActivity connected");
                dbDisplay.setText("CONNECTED");
                dbDisplay.setTextColor(Color.GREEN);
            }
            else {
                ScatterLogManager.e(TAG, "Tried to initialize DatastoreCommandActivity with no connection");
                dbDisplay.setText("DISCONNECTED");
                dbDisplay.setTextColor(Color.RED);
            }



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scatterBound = false;
        }
    };



    @Override
    public void onResume() {
        super.onResume();
        if(scatterBound) {
            dbConnected = mService.dataStore.connected;
            if (dbConnected) {
                dbDisplay.setText("CONNECTED");
                dbDisplay.setTextColor(Color.GREEN);
            } else {
                dbDisplay.setText("DISCONNECTED");
                dbDisplay.setTextColor(Color.RED);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datastore_command);
        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        dbDisplay = (TextView) findViewById(R.id.dboverviewtext);

        final Activity main = this;


        dbDisplay.setText("DISCONNECTED");
        dbDisplay.setTextColor(Color.RED);

        refresh_button = (Button) findViewById(R.id.refreshdb_button);
        dbTextView = (TextView) findViewById(R.id.db_textview2);
        clearButton = (Button) findViewById(R.id.clear_button);
        trimButton = (Button) findViewById(R.id.button_trim);
        randButton = (Button) findViewById(R.id.db_rand_button);



        randButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ArrayList<BlockDataPacket> ran = ds.getTopRandomMessages(5);
                    String print = "";
                    for(BlockDataPacket p : ran) {
                        print = print + Base64.encodeToString(p.senderluid,Base64.DEFAULT) +
                        new String(p.body)+ "\n";
                    }
                    dbTextView.setText(print);
                }
            }
        });

        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.enqueueMessage("NOHASH",0,  "contentsfefef",  "sexy data" ,0,-1,
                            "quantum fruit",  "flagsfrgrgrrfref", "sigfefefefefefefefef", "sig" ,
                            "flags");
                    ArrayList<Message> res = ds.getMessages();
                    String result = "";
                    for(Message m : res) {
                        result = result.concat(m.body + "\n");
                    }
                    dbTextView.setText(result);
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

        trimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.trimDatastore(100);
                }
            }
        });

        dbTextView.setText("");

    }
}
