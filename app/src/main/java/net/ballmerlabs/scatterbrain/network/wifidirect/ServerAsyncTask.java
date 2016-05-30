package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class to send text/sms messages over scatterbrain wifi p2p
 */
public class ServerAsyncTask extends WifiAsyncTask {

    public final int SERVERPORT = 8222;
    public final String TAG = "ServerAsyncTask";

    public ServerAsyncTask(Context context, ArrayAdapter<String> messageList) {
        super(context);
    }

    @Override
    protected Object[] doInBackground(Object[] params) {
        ArrayList<Byte> received = new ArrayList<>();
        InputStream inputStream = null;
        try {
            ServerSocket serverSocket = new ServerSocket(8222);
            Socket client = serverSocket.accept();

           inputStream = client.getInputStream();

            int i;
            while((i= inputStream.read()) != -1) {
                received.add((byte)i );
            }

            serverSocket.close();

        }
        catch(IOException e) {
            Log.e(TAG, "IOException when transfering a message");
            return null;
        }

        return received.toArray();
    }
}
