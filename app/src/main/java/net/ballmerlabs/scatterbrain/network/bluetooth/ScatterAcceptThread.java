package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.MainTrunk;
import net.ballmerlabs.scatterbrain.R;
import net.ballmerlabs.scatterbrain.network.AdvertisePacket;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.Exchanger;

import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * Listens for incoming bluetooth connections
 * without paring/security
 */
public class ScatterAcceptThread extends Thread {
    private BluetoothServerSocket mmServerSocket = null;
    private NetTrunk trunk;
    private BluetoothAdapter adapter;

    public ScatterAcceptThread(NetTrunk trunk, BluetoothAdapter adapter) {
        this.adapter = adapter;
        this.trunk = trunk;
    }

    @Override
    public void run() {
        ScatterLogManager.v(trunk.blman.TAG,"Started accept thread" );
        BluetoothSocket socket;
        trunk.blman.acceptThreadRunning = true;
        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(
                    trunk.blman.NAME, trunk.blman.UID);
        } catch (IOException e) {
            ScatterLogManager.e(trunk.blman.TAG, "IOException when starting bluetooth listener");
        }

        mmServerSocket = tmp;
        boolean go = true;
        socket = null;
        //noinspection ConstantConditions
        while (go) {
            try {
                socket = mmServerSocket.accept();
                ScatterLogManager.v(trunk.blman.TAG, "Accepted a connection");
                trunk.blman.onSuccessfulConnect(socket);
            } catch (IOException e) {
                    try {
                        if(socket != null)
                            socket.close();
                    }
                    catch (Exception ex) {
                        ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(ex.getStackTrace()));
                    }
                    //TODO: find a way to break out of loop on standby.
            } catch (Exception e) {
                ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(e.getStackTrace()));
            }


        }
    }


    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            ScatterLogManager.e(trunk.blman.TAG, Arrays.toString(e.getStackTrace()));


        }
        trunk.blman.acceptThreadRunning = false;
    }

}
