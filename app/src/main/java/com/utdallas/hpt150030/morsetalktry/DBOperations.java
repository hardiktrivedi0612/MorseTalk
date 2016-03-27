package com.utdallas.hpt150030.morsetalktry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roshan on 26/03/16.
 */
public class DBOperations
{
    private final String TAG = "DBOperations";
    private DatabaseSkeleton dbHelper;
    private String[] DB_COLUMNS = { DatabaseSkeleton.MSG_ID, DatabaseSkeleton.USER, DatabaseSkeleton.MESSAGE, DatabaseSkeleton.TIME, DatabaseSkeleton.SENT_BY };
    private SQLiteDatabase database;

    public DBOperations(Context context) {
        dbHelper = new DatabaseSkeleton(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Message> getAllMessages(String otherUser)
    {
        try {
            open();
            List<Message> messages = new ArrayList<Message>();
            Cursor cursor = database.query(DatabaseSkeleton.DB_NAME, DB_COLUMNS, DatabaseSkeleton.USER+"=?", new String[] { otherUser }, null, null, DatabaseSkeleton.MSG_ID+" DESC", "8");
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Message message = parseMessage(cursor);
                messages.add(message);
                cursor.moveToNext();
            }
            cursor.close();
            close();
            return messages;
        }
        catch (SQLException e) {
            Log.d(TAG,e.toString());
        }
        return null;
    }

    public List<String> getAllChats()
    {
        try {
            open();
            List<String> chats = new ArrayList<String>();
            Cursor cursor = database.query(true, DatabaseSkeleton.DB_NAME, new String[]{DatabaseSkeleton.USER}, null, null, null, null, DatabaseSkeleton.MSG_ID + " DESC", null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String user = cursor.getString(0);
                chats.add(user);
                cursor.moveToNext();
            }
            cursor.close();
            close();
            return chats;
        } catch (SQLException e) {
            Log.d(TAG, e.toString());
        }
        return null;
    }

    private Message parseMessage(Cursor cursor)
    {
        Message msg = new Message();
        msg.setMsgId(cursor.getInt(0));
        msg.setUser(cursor.getString(1));
        msg.setMessage(cursor.getString(2));
        msg.setTime(cursor.getString(3));
        msg.setSentBy(cursor.getString(4));

        return msg;
    }

    public void insertMessage(Message msg)
    {
        try {
            open();

            ContentValues values = new ContentValues();

            values.put(DatabaseSkeleton.USER, msg.getUser());
            values.put(DatabaseSkeleton.MESSAGE, msg.getMessage());
            values.put(DatabaseSkeleton.TIME, msg.getTime());
            values.put(DatabaseSkeleton.SENT_BY, msg.getSentBy());

            long msgId = database.insert(DatabaseSkeleton.DB_NAME, null, values);
            close();
        }catch (SQLException e) {
            Log.d(TAG,e.toString());
        }
    }
}
