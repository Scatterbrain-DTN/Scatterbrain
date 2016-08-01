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


        trunk.blman.init();
        if(!trunk.blman.getAdapter().isEnabled()) {
            Intent enableAndDiscoverBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enableAndDiscoverBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
            startActivity(enableAndDiscoverBtIntent);
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
