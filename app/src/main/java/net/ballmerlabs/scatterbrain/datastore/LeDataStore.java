package net.ballmerlabs.scatterbrain.datastore;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gnu3ra on 11/3/15.
 *
 * Imnplemnets a sort of queue for messages. Messages are added when recieved and can be
 * retrieved all at once for burst transmission. Mesages are deleted when older than a certain
 * age.
 */
public class LeDataStore {
    private SQLiteDatabase db;
    private MsgDbHelper  helper;
    private int dataTrimLimit;
    private final String TAG = "DataStore";
    private Cursor c;
    public final String[] names = {MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS,
    MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT,
    MsgDataDb.MessageQueue.COLUMN_NAME_TTL,
    MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO,
    MsgDataDb.MessageQueue.COLUMN_NAME_UUID,
    MsgDataDb.MessageQueue.COLUMN_NAME_RECIPIENT,
    MsgDataDb.MessageQueue.COLUMN_NAME_SIG};

    public LeDataStore(Activity mainActivity, int trim) {
        dataTrimLimit = trim;
        helper = new MsgDbHelper(mainActivity.getApplicationContext());
        db = helper.getWritableDatabase();
    }


    public void setDataTrimLimit(int val) {
        dataTrimLimit = val;
    }

    public int getDataTrimLimit() {
        return dataTrimLimit;
    }






    /*
     * sticks a message into the datastore at the front?.
     */
    public void enqueueMessage(String subject, String contents, int ttl, String replyto, String uuid,
                               String recipient, String sig) {
        Log.e(TAG, "Enqueued a message to the datastore.");
        ContentValues values = new ContentValues();
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS, contents);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT, subject);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_TTL, ttl);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO,replyto);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_UUID, uuid);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_RECIPIENT, recipient);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SIG,sig);

        long newRowId;
        newRowId = db.insert(MsgDataDb.MessageQueue.TABLE_NAME,
                null,
                values);

        trimDatastore(dataTrimLimit);

    }


    /*
     * this is called to trim the datastore and leave only the x newest entires
     * Makes messages 'die out' after a while
     */
    public void trimDatastore(int limit) {
        Log.e(TAG, "Trimming message queue. Too long.");
        String del = "DELETE FROM " + MsgDataDb.MessageQueue.TABLE_NAME +
                " WHERE ROWID IN (SELECT ROWID FROM "
                + MsgDataDb.MessageQueue.TABLE_NAME +
                " ORDER BY ROWID DESC LIMIT -1 OFFSET " + limit + ")\n";
        db.execSQL(del);
    }



    /*
     * (Hopefully) returns an array list of string to string maps with all the data
     * in the datastore in it.
     */

    public ArrayList<HashMap> getMessages() {

        String[] cols =  {
                MsgDataDb.MessageQueue._ID,
        MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS,
                MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT,
                MsgDataDb.MessageQueue.COLUMN_NAME_TTL,
                MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO,
                MsgDataDb.MessageQueue.COLUMN_NAME_UUID,
                MsgDataDb.MessageQueue.COLUMN_NAME_RECIPIENT,
                MsgDataDb.MessageQueue.COLUMN_NAME_SIG};
        Cursor cu = db.query(MsgDataDb.MessageQueue.TABLE_NAME,
                cols,
                null,
                null,
                null,
                null,
                null
                );

        ArrayList<HashMap> finalresult = new ArrayList<HashMap>();
        HashMap<String,String> result;
        cu.moveToFirst();
        //check here for overrun problems
        while(!cu.isAfterLast()) {
            result = new HashMap<String, String>();
            result.clear();
            for(int i  =0; i<cu.getColumnCount() ; i++) {
                result.put(names[i], cu.getString(i));
            }
            finalresult.add(result);
        }

        return finalresult;

    }




}
