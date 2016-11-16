package net.ballmerlabs.scatterbrain.datastore;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;

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
        ScatterLogManager.v(TAG, "Connected to datastore");
        helper = new MsgDbHelper(mainService.getApplicationContext());
        db = helper.getWritableDatabase();
        connected = true;
    }
    public void disconnect() {
        ScatterLogManager.v(TAG, "Disconnected from datastore");
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
                                 String flags, String sig, int rank) {
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


    /* Very temporary method for writing a blockdata stanza to datastore */
    public void enqueueMessage(BlockDataPacket bd) {
        enqueueMessage("SenpaiDetector",
                Base64.encodeToString(bd.body,Base64.DEFAULT),
                -1, "none", Base64.encodeToString(bd.senderluid, Base64.DEFAULT), "none", "none",-1);
    }

    public BlockDataPacket messageToBlockData(Message m) {
        BlockDataPacket result = new BlockDataPacket(Base64.decode(m.contents, Base64.DEFAULT),true,
                null,Base64.decode(m.luid,Base64.DEFAULT));
        return result;
    }


    /*
     * this is called to trim the datastore and leave only the x newest entires
     * Makes messages 'die out' after a while
     */
    public void trimDatastore(int limit) {
        ScatterLogManager.v(TAG, "Trimming message queue. Too long.");
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

        ScatterLogManager.v(TAG, "Mass dumping all messages from datastore");

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
    public ArrayList<BlockDataPacket> getTopRandomMessages(int count) {
        Cursor cu = db.rawQuery("SELECT * FROM " + MsgDataDb.MessageQueue.TABLE_NAME
                + " ORDER BY RANDOM() LIMIT " + count, null);


        ScatterLogManager.v(TAG, "Attempting to retrieve a random packet from datastore");
        ArrayList<BlockDataPacket> result = new ArrayList<>(count);
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
            BlockDataPacket resultbdpacket = new BlockDataPacket(Base64.decode(contents,Base64.DEFAULT), false, null,
                    Base64.decode(luid,Base64.DEFAULT));
            if(resultbdpacket.isInvalid())
                ScatterLogManager.e(TAG, "Decoded an invalid blockdata packet in random. Continuing anway. Godspeed.");
            result.add(resultbdpacket);
            cu.moveToNext();
        }

        return result;

    }


}
