package net.ballmerlabs.scatterbrain;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SearchForSenpai extends AppCompatActivity {
    private ProgressBar progress;
    private TextView senpai_notice;
    private MainTrunk trunk;
    private SeekBar scanFrequencyBar;
    private TextView scanFrequencyText;
    private ProgressBar scanningViz;
    private Thread scanningvUpdateThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_senpai);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setProgress(0);

        senpai_notice = (TextView) findViewById(R.id.notice_text);
        senpai_notice.setVisibility(View.INVISIBLE);
        trunk = new MainTrunk(this);

        scanFrequencyText = (TextView) findViewById(R.id.scanTimeText);

        scanFrequencyBar = (SeekBar) findViewById(R.id.scanTimeSlider);
        scanFrequencyBar.setProgress(50);
        Integer i = ((50000-500)/50)+500;
        scanFrequencyText.setText(i.toString() + "ms");
        trunk.settings.scanTimeMillis = i;
        scanFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Integer i = progress * ((50000-500)/100)+500;
                    scanFrequencyText.setText(i.toString()+"ms");
                    trunk.settings.scanTimeMillis = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trunk.globnet.getWifiManager().stopWifiDirectLoopThread();
                trunk.globnet.getWifiManager().startWifiDirctLoopThread();
            }
        });

        scanningViz = (ProgressBar) findViewById(R.id.scanningViz);

         scanningvUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    for(int x=0;x<100;x++) {
                        scanningViz.setProgress(x);
                        try {
                            Thread.sleep(trunk.settings.scanTimeMillis/1000);
                        }
                        catch(Exception e) {

                        }
                    }
                }
            }
        });

        scanningvUpdateThread.start();

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
        // trunk.globnet.startWifiDirectLoopThread();
        super.onResume();
        if(trunk.globnet.getWifiManager().getP2preceiver() != null &&
                trunk.globnet.getP2pIntentFilter() != null)
            this.registerReceiver(trunk.globnet.getWifiManager().getP2preceiver(), trunk.globnet.getP2pIntentFilter());
        trunk.globnet.getWifiManager().startWifiDirctLoopThread();
    }

    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
        if(trunk.globnet.getWifiManager().getP2preceiver() != null)
            this.unregisterReceiver(trunk.globnet.getWifiManager().getP2preceiver());

        trunk.globnet.getWifiManager().stopWifiDirectLoopThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
