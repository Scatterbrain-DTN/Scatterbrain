package net.ballmerlabs.scatterbrain.datastore;

import android.provider.BaseColumns;

/**
 * Created by gnu3ra on 11/3/15.
 */
@SuppressWarnings("DefaultFileTemplate")
final class MsgDataDb {

    @SuppressWarnings("unused")
    public MsgDataDb() {}

    public static abstract class MessageQueue implements BaseColumns {
        public static final String TABLE_NAME = "MessageQueue";
        public static final String COLUMN_NAME_HASH = "hash";
        public static final String COLUMN_NAME_EXTBODY = "extbody";
        public static final String COLUMN_NAME_BODY = "body";
        public static final String COLUMN_NAME_APPLICATION = "application";
        public static final String COLUMN_NAME_TEXT = "istext";
        public static final String COLUMN_NAME_FILE = "file";
        public static final String COLUMN_NAME_TTL = "ttl";
        public static final String COLUMN_NAME_REPLYLINK = "replylink";
        public static final String COLUMN_NAME_SENDERLUID = "senderluid";
        public static final String COLUMN_NAME_RECEIVERLUID = "receiverluid";
        public static final String COLUMN_NAME_SIG = "sig";
        public static final String COLUMN_NAME_FLAGS = "flags";

    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE "  + MessageQueue.TABLE_NAME + " (" +
                    MessageQueue._ID + "INTEGER PRIMARY KEY," +
                    MessageQueue.COLUMN_NAME_HASH + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_EXTBODY + INT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_BODY + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_APPLICATION + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_TEXT + INT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_FILE + INT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_TTL + INT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_REPLYLINK + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_SENDERLUID + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_RECEIVERLUID + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_SIG + TEXT_TYPE + COMMA_SEP +
                    MessageQueue.COLUMN_NAME_FLAGS + TEXT_TYPE + " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageQueue.TABLE_NAME;

}
