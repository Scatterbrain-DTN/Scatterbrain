package net.ballmerlabs.scatterbrain;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;
/*
 * Main 'Home screen' activity for the scatterbrain testing phase.
 */
public class SearchForSenpai extends AppCompatActivity {
    private ProgressBar progress;
    private TextView senpai_notice;
    private ScatterRoutingService service;
    private boolean scatterBound = false;
    private ScatterRoutingService mService;
    private String TAG = "SenpaiActivity";

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
                            senpai_notice.setVisibility(View.VISIBLE);
                            senpai_notice.setText("Senpai NOTICED YOU! \n and the packet was not corrupt");
                        }
                    });
                }
            });

            mService.getBluetoothManager().startDiscoverLoopThread();
            launchBtDialog();
            scatterBound = true;



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scatterBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_senpai);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setProgress(0);

        senpai_notice = (TextView) findViewById(R.id.notice_text);
        senpai_notice.setVisibility(View.INVISIBLE);



        service = new ScatterRoutingService();

        Log.v(TAG, "Initial Initialization");
        Intent srs = new Intent(this,ScatterRoutingService.class);
        startService(srs);
        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);




    }
    public void launchBtDialog() {
            Log.v(TAG,"Running bluetooth prompt dialog");
            if(!mService.getBluetoothManager().getAdapter().isEnabled()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, mService.getBluetoothManager().REQUEST_ENABLE_BT);
                    }
                });
            }
            else {
                mService.getBluetoothManager().init();
            }


            mService.getBluetoothManager().startDiscoverLoopThread();
    }


    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent intenet) {
        if(requestcode == mService.getBluetoothManager().REQUEST_ENABLE_BT) {
            Intent enableAndDiscoverBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enableAndDiscoverBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
            startActivity(enableAndDiscoverBtIntent);
            mService.getBluetoothManager().init();
        }
    }

    public void setNoticeVisibility() {
        senpai_notice.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_for_senpai, menu);
        return true;
    }

    public void launchSettings(MenuItem item) {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void toggleService(MenuItem item) {
        if(scatterBound) {
            Intent stop = new Intent(this, ScatterRoutingService.class);
            unbindService(mConnection);
            stopService(stop);
            scatterBound = false;
            item.setTitle("StartService");
        }
        else {
            Intent start = new Intent(this, ScatterRoutingService.class);
            startService(start);
            Intent bindIntent = new Intent(this, ScatterRoutingService.class);
            bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        }

    }

    public void resetText(MenuItem item) {
        senpai_notice.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // trunk.globnet.startWifiDirectLoopThread();
        if(scatterBound && false) {
            if (mService.getBluetoothManager().mReceiver != null )
                mService.registerReceiver(mService.getBluetoothManager().mReceiver, mService.getBluetoothManager().filter);
            mService.getBluetoothManager().startDiscoverLoopThread();
        }
    }

    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
        if(scatterBound && false) {
            if (mService.getBluetoothManager().mReceiver != null)
                mService.unregisterReceiver(mService.getBluetoothManager().mReceiver);
            mService.getBluetoothManager().stopDiscoverLoopThread();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
