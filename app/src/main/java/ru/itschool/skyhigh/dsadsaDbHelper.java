package ru.itschool.skyhigh;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static ru.itschool.skyhigh.dsadsaContract.*;

public class dsadsaDbHelper extends SQLiteOpenHelper {
    static String DATABASE_NAME = "filter.db";
    static int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CHAT_ID + " INTEGER, "
            + COLUMN_MESSAGE + " TEXT); ";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public dsadsaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }
}