package net.ballmerlabs.scatterbrain.network;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import net.ballmerlabs.scatterbrain.ChatboxFragment;
import net.ballmerlabs.scatterbrain.MainActivity;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.BLE.BLEPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.BLE.BluetoothSpewer;
import net.ballmerlabs.scatterbrain.network.BLE.LeNotSupportedException;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Global network management framework
 */
public class GlobalNet {
    private BluetoothSpewer bleman;
    private ArrayList<BLEPacket> packetqueue;
    public String err;
    private boolean broken;
    private boolean armed;
    private Activity main;
    private DeviceProfile prof;
    private Thread blethread;
    public final String TAG = "GlobNet";
    public GlobalNet(Activity mainActivity, DeviceProfile me) {
        broken = false;
        armed = false;
        err = "";
        bleman = null;
        packetqueue = new ArrayList<>();
        main = mainActivity;
        prof = me;
    }

    public boolean isReady() {
        return (!broken) && armed;
    }

    /* appends a packet to the queue */
    public void appendPacket(BLEPacket p) {
        packetqueue.add(p);
    }

    public BLEPacket dequeuePacket() {
        if(packetqueue.size() > 0) {
            BLEPacket result = packetqueue.get(0);
            packetqueue.remove(0);
            return result;
        }
        else
            return null;

    }

    public void sendBlePacket(BLEPacket s) {
        bleman.transmitMesage(s.getContents());
    }

    public void initBLE() {
        try {
            bleman = new BluetoothSpewer(main, prof, this);
        } catch (LeNotSupportedException le) {
            err = "Bluetooth LE is not supported";
            broken = true;
        }

        blethread =  new Thread(new Runnable() {
           @Override
            public void run() {
               bleman.startScan();
               ArrayAdapter<String> Messages;
               Messages = new ArrayAdapter<>(main,android.R.layout.simple_list_item_1);
               boolean go = true;
               while(go) {
                    BLEPacket in = dequeuePacket();
                    if(in != null && !in.invalid ) {
                        if(in.getHeader() == 0)
                            Messages.add("[Advertise packet]");
                        else if(in.getHeader() == 1)
                            Messages.add(new String(((BlockDataPacket)in).body));
                    }
                   try {
                       wait(100);
                   }
                   catch(InterruptedException e) {
                       Log.d(TAG,"BLE Packet Handler thread interrupted for some odd reason");

                   }
               }
           }
        });
        blethread.start();
    }
}
