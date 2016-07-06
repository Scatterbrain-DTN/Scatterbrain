package net.ballmerlabs.scatterbrain;

import android.content.BroadcastReceiver;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;

public class NormalActivity extends AppCompatActivity {

    private EditText MsgBox;
    private ListView messageTimeline;
    private ArrayAdapter<String> Messages;
    private Button sendButton;
    private GlobalNet globnet;
    private DeviceProfile profile;
    private TextView peersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        messageTimeline = (ListView) this.findViewById(R.id.timeline);

        MsgBox = (EditText) this.findViewById(R.id.editText);
        Messages = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        messageTimeline.setAdapter(Messages);

        profile = new DeviceProfile(DeviceProfile.deviceType.ANDROID, DeviceProfile.MobileStatus.MOBILE,
                DeviceProfile.HardwareServices.BLUETOOTHLE, "000000000000");
        globnet = new GlobalNet(this, profile);

        //globnet.registerService(profile);
        globnet.startWifiDirctLoopThread();



        //messagebox handeling
        sendButton = (Button) this.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });
    }

    //adds a message to the list and clears the input field
    private void updateList() {
        Messages.add(MsgBox.getText().toString());
        BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,profile);

        MsgBox.setText("");

    }

    @Override
    protected void onResume() {
       // globnet.startWifiDirectLoopThread();
        super.onResume();
        if(globnet.getP2preceiver() != null &&
                globnet.getP2pIntentFilter() != null)
            this.registerReceiver(globnet.getP2preceiver(), globnet.getP2pIntentFilter());
    }

    @Override
    protected void onPause() {
        //globnet.stopWifiDirectLoopThread();
        super.onPause();
        if(globnet.getP2preceiver() != null)
            this.unregisterReceiver(globnet.getP2preceiver());
    }

    public void addMessage(String message) {
        Messages.add(message);
    }
}
