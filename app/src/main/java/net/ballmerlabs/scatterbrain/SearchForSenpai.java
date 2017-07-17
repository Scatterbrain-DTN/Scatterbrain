package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.net.URL;
import java.io.BufferedReader;
import android.content.pm.PackageInfo;
import java.io.InputStreamReader;
import java.util.Map;

import android.app.AlertDialog;


import net.ballmerlabs.scatterbrain.network.PeersChangedCallback;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;
import net.ballmerlabs.scatterbrain.network.bluetooth.LocalPeer;
import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

import org.w3c.dom.Text;

/*
 * Main 'Home screen' activity for the scatterbrain testing phase.
 */
public class SearchForSenpai extends AppCompatActivity {
    private TextView senpai_notice;
    private boolean scatterBound = false;
    private ScatterRoutingService mService;
    private String TAG = "SenpaiActivity";
    final Activity main  = this;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            mService.registerPeersChangedCallback(new PeersChangedCallback() {
                                                      @Override
                                                      public void run(final Map<String, LocalPeer> connectedList) {
                                              runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {

                                                      String peers = "Connected peers:\n";
                                                      for(Map.Entry d : connectedList.entrySet()) {
                                                          peers = peers + d.getKey() + "\n";
                                                      }

                                                      TextView pt = (TextView) findViewById(R.id.peersText);
                                                      pt.setText("peers : " + connectedList.size());
                                                      pt.setTextColor(Color.GREEN);
                                                      TextView senpai_notice = (TextView) findViewById(R.id.notice_text);
                                                      senpai_notice.setVisibility(View.VISIBLE);
                                                      senpai_notice.setText(peers);
                                                  }
                                              });
                                          }
                                      });

                    //mService.getBluetoothManager().startDiscoverLoopThread();
                    launchBtDialog();
            mService.dataStore.flushDb();
            scatterBound = true;



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scatterBound = false;
        }
    };


    /* This Thread checks for Updates in the Background */
    private Thread checkUpdate = new Thread() {
        public void run() {
            try {
                URL updateURL = new URL("https://scatterbrain.xyz/Update.txt"); //place update text link here
                BufferedReader in = new BufferedReader(new InputStreamReader(updateURL.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    // str is one line of text; readLine() strips the newline character(s)
                /* Get current Version Number */
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int curVersion = packageInfo.versionCode;
                    int newVersion = Integer.valueOf(str);

                /* Is a higher version than the current already out? */
                    if (newVersion > curVersion) {
                    /* Post a Handler for the UI to pick up and open the Dialog */
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(showUpdate);

                    }

                }
                in.close();
            } catch (Exception e) {
                ScatterLogManager.e(TAG, e.getStackTrace().toString());
            }
        }

    };

    /* This Runnable creates a Dialog and asks the user to open the Market */
    private Runnable showUpdate = new Runnable() {
        public void run() {
            new AlertDialog.Builder(main)
                    .setIcon(R.drawable.ic_drawer)
                    .setTitle("Update Available")
                    .setMessage("An update for the latest version is available!\n\nOpen Update page and download?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /* User clicked OK so do some stuff */
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dl.scatterbrain.xyz/senpai-current.apk"));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /* User clicked Cancel */
                        }
                    })
                    .show();
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
        ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setProgress(0);

        TextView peersText = (TextView) findViewById(R.id.peersText);
        peersText.setText("peers: 0");
        peersText.setTextColor(Color.RED);
        Button castButton = (Button) findViewById(R.id.castbutton);
        final Intent launchMessagingIntent = new Intent(this,NormalActivity.class);

        castButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(launchMessagingIntent);
                    }
                });
            }
        });

        senpai_notice = (TextView) findViewById(R.id.notice_text);
        senpai_notice.setVisibility(View.INVISIBLE);

        TextView peerDisplay = (TextView) findViewById(R.id.peerdisplay);
        peerDisplay.setText("");


        ScatterRoutingService service = new ScatterRoutingService();

        ScatterLogManager.v(TAG, "Initial Initialization");
        Intent srs = new Intent(this,ScatterRoutingService.class);
        startService(srs);
        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);


    }
    public void launchBtDialog() {
            ScatterLogManager.v(TAG,"Running bluetooth prompt dialog");
            if(mService.getBluetoothManager().getAdapter() != null) {
                if (!mService.getBluetoothManager().getAdapter().isEnabled()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, ScatterBluetoothManager.REQUEST_ENABLE_BT);
                        }
                    });
                } else {
                    if (mService.getBluetoothManager().getAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent enableAndDiscoverBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        enableAndDiscoverBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                        startActivity(enableAndDiscoverBtIntent);
                    }
                    mService.getBluetoothManager().init();
                    Handler handler = new Handler(this.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mService.getBluetoothManager().startDiscoverLoopThread();
                        }
                    }, 5000);
                }
            }
            else{
                ScatterLogManager.e(TAG, "Tried to start bluetooth dialog without bluetooth");
            }
    }


    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent intenet) {
        if(requestcode ==  ScatterBluetoothManager.REQUEST_ENABLE_BT) {
            Intent enableAndDiscoverBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enableAndDiscoverBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
            startActivity(enableAndDiscoverBtIntent);
            mService.getBluetoothManager().init();
            Handler handler = new Handler(this.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mService.getBluetoothManager().startDiscoverLoopThread();
                    mService.getBluetoothManager().resetBluetoothDiscoverability(300);

                }
            }, 5000);
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

    public void startLogViewer(MenuItem item) {
        Intent intent = new Intent(this, LoggingActivity.class);
        startActivity(intent);
    }

    public void startDatastoreCommand(MenuItem item) {
        Intent intent = new Intent(this,DatastoreCommandActivity.class);
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
        //noinspection PointlessBooleanExpression

    }



    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
        //noinspection PointlessBooleanExpression

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
