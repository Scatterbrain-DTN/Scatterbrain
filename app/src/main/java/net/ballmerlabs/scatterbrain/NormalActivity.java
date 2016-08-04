package net.ballmerlabs.scatterbrain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

public class NormalActivity extends AppCompatActivity {

    private EditText MsgBox;
    private ListView messageTimeline;
    private ArrayAdapter<String> Messages;
    private Button sendButton;
    private GlobalNet globnet;
    private DeviceProfile profile;
    private TextView peersView;
    private NetTrunk trunk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        messageTimeline = (ListView) this.findViewById(R.id.timeline);

        MsgBox = (EditText) this.findViewById(R.id.editText);
        Messages = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        messageTimeline.setAdapter(Messages);

        //messagebox handeling
        sendButton = (Button) this.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });

        trunk = new NetTrunk(this);
    }

    //adds a message to the list and clears the input field
    private void updateList() {
        Messages.add(MsgBox.getText().toString());
       // BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,profile);

        MsgBox.setText("");

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

    public void addMessage(String message) {
        Messages.add(message);
    }
}
