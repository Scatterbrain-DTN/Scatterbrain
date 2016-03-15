package net.ballmerlabs.scatterbrain.network;

/**
 * Created by gnu3ra on 1/29/16.
 */
public interface RecievedCallback<T> {

    public void onSuccess(T t, String recieved);
    public void onFail(T t, String recieved);
}
