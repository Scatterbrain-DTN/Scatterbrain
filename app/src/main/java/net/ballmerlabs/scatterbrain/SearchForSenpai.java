package net.ballmerlabs.scatterbrain;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.bluetooth.ScatterBluetoothManager;

import org.w3c.dom.Text;

public class SearchForSenpai extends AppCompatActivity {
    private ProgressBar progress;
    private TextView senpai_notice;
    private MainTrunk trunk;
    private TextView scanFrequencyText;
    private ScatterBluetoothManager blman;
    private SeekBar scanFrequencyBar;


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

        scanFrequencyText = (TextView) findViewById(R.id.scanTimeText);

        scanFrequencyBar = (SeekBar) findViewById(R.id.scanTimeSlider);
        scanFrequencyBar.setProgress(50);
        Integer i = ((30000-2000)/50)+2000;
        scanFrequencyText.setText(i.toString() + "ms");
        trunk.settings.scanTimeMillis = i;
        scanFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Integer i = progress * ((30000-2000)/100)+2000;
                scanFrequencyText.setText(i.toString()+"ms");
                trunk.settings.bluetoothScanTimeMillis = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trunk.blman.stopDiscoverLoopThread();
                trunk.blman.startDiscoverLoopThread();
            }
        });

        if(!trunk.blman.getAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, trunk.blman.REQUEST_ENABLE_BT);
        }




        trunk.blman.startDiscoverLoopThread();

    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent intenet) {
        if(requestcode == trunk.blman.REQUEST_ENABLE_BT) {
            Intent enableAndDiscoverBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enableAndDiscoverBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
            startActivity(enableAndDiscoverBtIntent);
            trunk.blman.init();
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
        if(trunk.blman.mReceiver != null &&
                trunk.globnet.getP2pIntentFilter() != null)
            this.registerReceiver(trunk.blman.mReceiver, trunk.blman.filter);
        trunk.blman.startDiscoverLoopThread();
    }

    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
        if(trunk.blman.mReceiver != null)
            this.unregisterReceiver(trunk.blman.mReceiver);

        trunk.blman.stopDiscoverLoopThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
