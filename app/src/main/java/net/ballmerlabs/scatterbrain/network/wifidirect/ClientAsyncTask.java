package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
/**
 * AsyncTask for creating a connection to the server started by the
 * ServerAsyncTask. This is simple and does not break up large messages
 * yet.
 */
class ClientAsyncTask extends WifiAsyncTask{

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ClientAsyncTask";

    public ClientAsyncTask(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String host = (String) params[0];
        int port = (Integer) params[1];
        byte[] toTransmit = (byte[]) params[2];
        Socket socket = new Socket();

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            OutputStream outputStream  = socket.getOutputStream();
            outputStream.write(toTransmit);

            outputStream.close();
        }
        catch(IOException e) {
            ScatterLogManager.e(TAG,"IOException when creating socket");
        }
        finally {
            //noinspection ConstantConditions
            if(socket != null) {
                if(socket.isConnected()) {
                    try {
                        socket.close();
                    }
                    catch(IOException e) {
                        //we will swallow this. Sorry 
                    }
                }
            }
        }

        return null;
    }
}
