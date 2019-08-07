package io.xpush.chat.persist;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by luffy on 2015-07-05.
 */
public class MessageTable {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_ID = "id";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_COUNT = "count";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TYPE = "type";
    public static final String KEY_METADATA = "metadata";
    public static final String KEY_UPDATED = "received";
    public static String SQLITE_TABLE;

    public static final String[] ALL_PROJECTION = {
        MessageTable.KEY_CHANNEL,
        MessageTable.KEY_ID,
        MessageTable.KEY_SENDER ,
        MessageTable.KEY_IMAGE ,
        MessageTable.KEY_COUNT ,
        MessageTable.KEY_MESSAGE ,
        MessageTable.KEY_TYPE ,
        MessageTable.KEY_METADATA ,
        MessageTable.KEY_UPDATED };

    public static void onCreate(SQLiteDatabase db, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        String DATABASE_CREATE =
                "CREATE TABLE if not exists " + tableName + " (" +
                        KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                        KEY_CHANNEL + ", " +
                        KEY_ID + "," +
                        KEY_SENDER + "," +
                        KEY_IMAGE + "," +
                        KEY_COUNT + " integer ," +
                        KEY_MESSAGE + "," +
                        KEY_TYPE + " integer , " +
                        KEY_METADATA + " ,"+
                        KEY_UPDATED + " integer );";

        db.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, String tableName) {
        SQLITE_TABLE = tableName;

        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db, tableName);
    }
}
