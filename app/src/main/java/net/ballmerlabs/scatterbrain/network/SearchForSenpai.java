package net.ballmerlabs.scatterbrain.network;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.NetworkCallback;
import net.ballmerlabs.scatterbrain.network.NetworkManager;

public class SearchForSenpai extends AppCompatActivity {
    private ProgressBar progress;
    private NetworkManager net;
    private NetworkCallback foundRun;
    private TextView senpai_notice;
    private final int PORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_senpai);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setProgress(0);
        senpai_notice = (TextView) findViewById(R.id.notice_text);
        senpai_notice.setVisibility(View.INVISIBLE);
        foundRun = new NetworkCallback() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senpai_notice.setVisibility(View.VISIBLE);
                    }
                });
            }
        };
        net = new NetworkManager(super.getApplicationContext(), PORT, foundRun);


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
    protected void onPause() {
        super.onPause();
        if(net != null) {
            net.tearDown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        net.init();
        if(net != null) {
            //net.register(PORT);
            net.discoverServices();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        net.tearDown();
    }
}
