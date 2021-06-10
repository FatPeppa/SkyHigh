package ru.itschool.skyhigh;

import android.database.sqlite.SQLiteOpenHelper;


import android.provider.BaseColumns;

public final class dsadsaContract implements BaseColumns {
    private dsadsaContract(){}

    public static final String TABLE_NAME = "filter";
    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_MESSAGE = "message";

}
