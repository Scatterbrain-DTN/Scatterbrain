package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import net.ballmerlabs.scatterbrain.utils.ScatterLogManager;
/**
 * Class to send text/sms messages over scatterbrain wifi p2p
 */
@SuppressWarnings("FieldCanBeLocal")
class ServerAsyncTask extends WifiAsyncTask {

    @SuppressWarnings("unused")
    public final int SERVERPORT = 8222;
    private final String TAG = "ServerAsyncTask";

    @SuppressWarnings({"unused", "UnusedParameters"})
    public ServerAsyncTask(Context context, ArrayAdapter<String> messageList) {
        super(context);
    }

    @Override
    protected Object[] doInBackground(Object[] params) {
        ArrayList<Byte> received = new ArrayList<>();
        InputStream inputStream;
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
            ScatterLogManager.e(TAG, "IOException when transfering a message");
            return null;
        }

        return received.toArray();
    }
}
