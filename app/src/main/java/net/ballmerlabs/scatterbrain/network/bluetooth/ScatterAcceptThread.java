package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Listens for incoming bluetooth connections
 * without paring/security
 */
public class ScatterAcceptThread extends Thread {
    private BluetoothServerSocket mmServerSocket = null;
    private NetTrunk trunk;
    public ScatterAcceptThread(NetTrunk trunk, BluetoothAdapter adapter) {
        trunk.blman.acceptThreadRunning = true;
        this.trunk = trunk;
        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(
                    trunk.blman.NAME, trunk.blman.UID);
        } catch (IOException e) {
            Log.e(trunk.blman.TAG, "IOException when starting bluetooth listener");
        }

        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        Log.v(trunk.blman.TAG,"Started accept thread" );
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
                trunk.blman.onSucessfulAccept(socket);
            } catch (IOException e) {
                break;
            }


        }
        trunk.blman.acceptThreadRunning = false;
    }


    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {


        }
        trunk.blman.acceptThreadRunning = false;
    }
}
