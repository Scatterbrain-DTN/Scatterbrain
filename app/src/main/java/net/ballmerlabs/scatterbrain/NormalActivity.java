package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

@SuppressWarnings({"MismatchedReadAndWriteOfArray", "unused"})
public class NormalActivity extends AppCompatActivity {

    private EditText MsgBox;
    private MessageBoxAdapter Messages;
    @SuppressWarnings("unused")
    private GlobalNet globnet;
    @SuppressWarnings("unused")
    private DeviceProfile profile;
    @SuppressWarnings("unused")
    private TextView peersView;
    @SuppressWarnings("unused")
    private ScatterRoutingService service;
    private Button fileChooseButton;
    private boolean scatterBound = false;
    private ScatterRoutingService mService;
    public static boolean active = false;
    private final String TAG = "MessagingActivity";

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            mService.registerMessageArrayAdapter(Messages);

            //add some previously received messages.
            for(BlockDataPacket b : mService.dataStore.getTopMessages()) {
                Messages.data.add(new DispMessage(new String(b.body),
                        Base64.encodeToString(b.senderluid, Base64.DEFAULT)));
            }
            ScatterLogManager.v(TAG, "Bound to routing service");
            scatterBound = true;



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ScatterLogManager.e(TAG, "Disconnected from routing service");
            scatterBound = false;
        }
    };

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        ListView messageTimeline = (ListView) this.findViewById(R.id.timeline);

        fileChooseButton = (Button) this.findViewById(R.id.filebutton);

        final DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        final Activity t = this;
        final FilePickerDialog dialog = new FilePickerDialog(NormalActivity.this, properties);
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if(isExternalStorageReadable() && isExternalStorageWritable()) {
                    if(files.length == 1) {
                        File f = new File(files[0]);


                        byte[] hash = null;
                        if(service != null) {
                            try {
                                FileInputStream i = new FileInputStream(f);
                                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                                byte[] buffer = new byte[1024];
                                int bytes_recieved;
                                int offset = 0;
                                while((bytes_recieved = i.read(buffer)) != -1) {
                                    digest.update(buffer, offset, bytes_recieved);
                                    offset += bytes_recieved;
                                }
                                hash = digest.digest();
                                i.skip(0);
                                BlockDataPacket bd = new BlockDataPacket(i,f.length(),service.luid);
                                service.getBluetoothManager().sendStreamToBroadcast(bd.getContents(),
                                        i, f.length());
                                if(hash != null) {
                                    Messages.data.add(new DispMessage(BlockDataPacket.bytesToHex(hash),
                                            "Sent file"));
                                    Messages.notifyDataSetChanged();
                                }
                            } catch (Exception e) {

                            }

                        }
                    } else {
                        //TODO: handle user selecting more than one filexrdfrdxszre2q11fhygtre      gy6rewq qwer6ue4r5f6joiutrewa   q
                    }

                }
            }
        });
        dialog.setTitle("File to send");

        final Activity  current = this;
        fileChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    dialog.show();
            }
        });

        MsgBox = (EditText) this.findViewById(R.id.editText);
        Messages = new MessageBoxAdapter(this);

        messageTimeline.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messageTimeline.setStackFromBottom(true);
        messageTimeline.setAdapter(Messages);

        //messagebox handeling
        Button sendButton = (Button) this.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });

        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);


    }

    //adds a message to the list and clears the input field
    private void updateList() {

        if(scatterBound) {
            BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true, mService.luid);
            Messages.data.add(new DispMessage(new String(bd.body),
                    Base64.encodeToString(bd.senderluid, Base64.DEFAULT)));
            // BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,profile);

            if(!bd.isInvalid()) {
                    mService.dataStore.enqueueMessageNoDuplicate(bd);
                    ScatterLogManager.v(TAG, "Updating list");
                    if (mService.getBluetoothManager() != null) {
                        mService.getBluetoothManager().sendRawToBroadcast(bd.getContents());
                    }
            }

            else {
                ScatterLogManager.e(TAG, "Packet was corrupt from the start");
            }

        }
        MsgBox.setText("");

    }


    @Override
    protected void onStart() {
        super.onStart();
        NormalActivity.active = true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        NormalActivity.active = false;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onResume() {
        // trunk.globnet.startWifiDirectLoopThread();
        super.onResume();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
    }

    @SuppressWarnings("unused")
    public void addMessage(String message, byte[] luid) {
        Messages.data.add(  new DispMessage(message,
                Base64.encodeToString(luid, Base64.DEFAULT)));
    }
}