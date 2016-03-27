package com.utdallas.hpt150030.morsetalktry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roshan on 26/03/16.
 */
public class DatabaseSkeleton extends SQLiteOpenHelper {

    public static final String DB_NAME = "morse_talk_chat_db";
    private static final int DATABASE_VERSION = 1;
    public static final String USER = "_user";
    public static final String MESSAGE = "_message";
    public static final String TIME = "_time";
    public static final String MSG_ID = "_msg_id";
    public static final String SENT_BY = "_sent_by";

    private static final String DATABASE_CREATE = "create table " + DB_NAME
            + "(" + MSG_ID + " integer primary key autoincrement, "
            + USER + " text not null,"
            + MESSAGE + " text not null,"
            + TIME + " text not null,"
            + SENT_BY + " text not null);";

    public DatabaseSkeleton(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
        onCreate(db);
    }
}
