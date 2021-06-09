package ru.itschool.skyhigh;

import java.sql.*;

public class Filter {
    public static final String database_name = "filter.db";
    public static Connection conn;
    public static Statement statmt;

    Filter() throws Exception {
        connect();
        createDB();
    }

    public static void connect() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite:" + database_name);
    }

    public static void createDB() throws Exception {
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists 'filter' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'chat_id' INTEGER, 'message' INT);");
    }

    public static Boolean check_delete(long chat_id, String message) throws Exception {
        ResultSet result;
        result = statmt.executeQuery("SELECT * FROM filter WHERE chat_id = " + Long.toString(chat_id));
        String banWord;
        while (result.next()) {
            banWord = result.getString("message");
            if (message.toLowerCase().contains(banWord.toLowerCase())) return true;
        }
        return false;
    }

    public static void add_ban_word(long chat_id, String banWord) throws Exception {
        statmt.execute("INSERT INTO 'filter' ('chat_id', 'message') VALUES (" + Long.toString(chat_id) + ", '" + banWord + "'); ");
    }
}
