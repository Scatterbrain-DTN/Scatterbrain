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
                Log.v(trunk.blman.TAG, "Accepted a connection");
                trunk.blman.onSucessfulAccept(socket);
                onAccept(socket);
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


    public void onAccept(BluetoothSocket socket) {
        try {
            InputStream i = socket.getInputStream();
            OutputStream o = socket.getOutputStream();
            BluetoothDevice d = socket.getRemoteDevice();
            trunk.mainService.noticeNotify("Senpai NOTICED YOU!!", "There is a senpai in your area somewhere");
            AdvertisePacket outpacket = GlobalNet.encodeAdvertise(trunk.profile);
            o.write(outpacket.getContents());
            byte[] buffer = new byte[50];
            i.read(buffer);
            AdvertisePacket inpacket;
            if(buffer != null) {
                inpacket = GlobalNet.decodeAdvertise(buffer);
                if(!inpacket.isInvalid()) {
                    trunk.mainService.updateUiOnDevicesFound();
                }
            }
            socket.close();
        }
        catch(IOException c) {

        }
    }
}
