package net.ballmerlabs.scatterbrain.datastore;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.ballmerlabs.scatterbrain.ScatterLogManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gnu3ra on 11/3/15.
 * <p/>
 * Imnplemnets a sort of queue for messages. Messages are added when recieved and can be
 * retrieved all at once for burst transmission. Mesages are deleted when older than a certain
 * age.
 */
public class LeDataStore {
    private SQLiteDatabase db;
    private MsgDbHelper helper;
    private int dataTrimLimit;
    private final String TAG = "DataStore";
    private Service mainService;
    private Cursor c;
    public boolean connected;
    public final String[] names = {
            MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS,
            MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT,
            MsgDataDb.MessageQueue.COLUMN_NAME_TTL,
            MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO,
            MsgDataDb.MessageQueue.COLUMN_NAME_LUID,
            MsgDataDb.MessageQueue.COLUMN_NAME_SIG,
            MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS,
            MsgDataDb.MessageQueue.COLUMN_NAME_RANK};

    public LeDataStore(Service mainService, int trim) {
        dataTrimLimit = trim;
        this.mainService = mainService;

    }

    public void flushDb() {
        db.execSQL("DELETE FROM " + MsgDataDb.MessageQueue.TABLE_NAME);
    }

    public void connect() {
        helper = new MsgDbHelper(mainService.getApplicationContext());
        db = helper.getWritableDatabase();
        connected = true;
    }
    public void disconnect() {
        db.close();
        connected = false;
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
    public void enqueueMessage(String subject, String contents, int ttl, String replyto, String luid,
                               String recipient, String from, String flags, String sig, int rank) {
        ScatterLogManager.e(TAG, "Enqueued a message to the datastore.");
        ContentValues values = new ContentValues();
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS, contents);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT, subject);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_TTL, ttl);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO, replyto);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_LUID, luid);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SIG, sig);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS, flags);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_RANK, rank);

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
        ScatterLogManager.e(TAG, "Trimming message queue. Too long.");
        String del = "DELETE FROM " + MsgDataDb.MessageQueue.TABLE_NAME +
                " WHERE ROWID IN (SELECT ROWID FROM "
                + MsgDataDb.MessageQueue.TABLE_NAME +
                " ORDER BY ROWID DESC LIMIT -1 OFFSET " + limit + ")\n";
        db.execSQL(del);
    }



    /*
     * (Hopefully) returns an array list of Message objects with all the data
     * in the datastore in it.
     */

    public ArrayList<Message> getMessages() {

        final String SEP = ", ";
        Cursor cu = db.rawQuery("SELECT "+
                        MsgDataDb.MessageQueue.COLUMN_NAME_SUBJECT + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_CONTENTS + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_TTL + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_REPLYTO + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_LUID + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_SIG + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS +
                " FROM " + MsgDataDb.MessageQueue.TABLE_NAME, null);

        ArrayList<Message> finalresult = new ArrayList<Message>();
        cu.moveToFirst();
        //check here for overrun problems
        while (!cu.isAfterLast()) {
            String subject = cu.getString(0);
            String contents = cu.getString(1);
            int ttl = cu.getInt(2);
            String replyto = cu.getString(3);
            String luid = cu.getString(4);
            String flags = cu.getString(5);
            String sig = cu.getString(6);

            cu.moveToNext();
            finalresult.add(new Message(subject, contents, ttl, replyto, luid,
                   flags,  sig));
        }

        return finalresult;

    }


    /*
     * Gets n rows from the datastore in a random order. For use when there is no time to transmit
     * the entire datastore.
     */
    public ArrayList<HashMap> getTopRandomMessages(int count) {
        Cursor cu = db.rawQuery("SELECT * FROM" + MsgDataDb.MessageQueue.TABLE_NAME
                + "ORDER BY RANDOM() LIMIT" + count + "1", null);


        ArrayList<HashMap> finalresult = new ArrayList<HashMap>();
        HashMap<String, String> result;
        cu.moveToFirst();
        //check here for overrun problems
        while (!cu.isAfterLast()) {
            result = new HashMap<String, String>();
            result.clear();
            for (int i = 0; i < cu.getColumnCount(); i++) {
                result.put(names[i], cu.getString(i));
            }
            finalresult.add(result);
        }

        return finalresult;

    }


}
