package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * AsyncTask for creating a connection to the server started by the
 * ServerAsyncTask. This is simple and does not break up large messages
 * yet.
 */
public class ClientAsyncTask extends WifiAsyncTask{

    public final String TAG = "ClientAsyncTask";
    public Context context;

    public ClientAsyncTask(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String host = (String) params[0];
        int port = (Integer) params[1];
        byte[] toTransmit = (byte[]) params[2];
        int len;
        Socket socket = new Socket();

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            OutputStream outputStream  = socket.getOutputStream();
            outputStream.write(toTransmit);

            outputStream.close();
        }
        catch(IOException e) {
            Log.e(TAG,"IOException when creating socket");
        }

        return null;
    }
}
