package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import net.ballmerlabs.scatterbrain.MainTrunk;

import java.io.IOException;

/**
 * Listens for incoming bluetooth connections
 * without paring/security
 */
public class ScatterAcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket = null;
    public ScatterAcceptThread(MainTrunk trunk) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = trunk.blman.getAdapter().listenUsingInsecureRfcommWithServiceRecord(
                    trunk.blman.NAME, trunk.blman.UID);
        }
        catch(IOException e) {
            Log.e(trunk.blman.TAG, "IOException when starting bluetooth listener");
        }

    }
    @Override
    public void run() {
        BluetoothSocket socket = null;
        while(true) {
            try {
                socket = mmServerSocket.accept();
            }
            catch(IOException e) {
                break;
            }
        }
    }


    public void cancel() {
        try {
            mmServerSocket.close();
        }
        catch(IOException e) {

        }
    }
}
