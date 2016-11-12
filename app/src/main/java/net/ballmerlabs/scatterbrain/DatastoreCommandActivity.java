package net.ballmerlabs.scatterbrain;

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

import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

public class DatastoreCommandActivity extends AppCompatActivity {
    private ScatterRoutingService mService;
    private String TAG = "DatastoreCommand";
    private Button connectButton;
    private Button disconnectButton;
    private TextView dbDisplay;
    private boolean scatterBound;

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
        dbDisplay.setText("DISCONNECTED");
        dbDisplay.setTextColor(Color.RED);

    }
}
