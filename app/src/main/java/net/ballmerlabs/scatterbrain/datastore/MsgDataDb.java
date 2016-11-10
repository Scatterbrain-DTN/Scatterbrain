package net.ballmerlabs.scatterbrain.datastore;

import android.provider.BaseColumns;

/**
 * Created by gnu3ra on 11/3/15.
 */
public final class MsgDataDb {

    public MsgDataDb() {}

    public static abstract class MessageQueue implements BaseColumns {
        public static final String TABLE_NAME = "MessageQueue";
        public static final String COLUMN_NAME_SUBJECT = "subject";
        public static final String COLUMN_NAME_CONTENTS = "contents";
        public static final String COLUMN_NAME_TTL = "ttl";
        public static final String COLUMN_NAME_REPLYTO = "replyto";
        public static final String COLUMN_NAME_UUID = "uuid";
        public static final String COLUMN_NAME_RECIPIENT = "recip";
        public static final String COLUMN_NAME_FROM = "from";
        public static final String COLUMN_NAME_SIG = "sig";
        public static final String COLUMN_NAME_FLAGS = "flags";
        public static final String COLUMN_NAME_RANK = "rank";

    }

    public static final String TEXT_TYPE = " TEXT";
    public static final String INT_TYPE = "INTEGER";
    public static final String BOOL_TYPE = "BOOLEAN";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE "  + MessageQueue.TABLE_NAME + " (" +
                    MessageQueue._ID + "INTEGER PRIMARY KEY," +
                    MessageQueue.COLUMN_NAME_UUID + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_SUBJECT + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_TTL + INT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_REPLYTO + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_CONTENTS + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_FROM + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_SIG + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_FLAGS + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_RANK  + INT_TYPE + " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageQueue.TABLE_NAME;

}
