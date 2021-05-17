package ru.itschool.skyhigh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class chats_activity extends AppCompatActivity {
    private static final String vk_url = "https://api.vk.com/method";
    private static String token;
    //private List<Message> chats_last_messages;


    EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_activity);

        token = getIntent().getStringExtra("token");

        displayAllChats();
    }

    private void displayAllChats() {
        ListView chats = findViewById(R.id.list_of_chats);

        //Array chatArr =

        ArrayAdapter<ChatItem> chatsAdapter
                = new ArrayAdapter<ChatItem>(this, R.layout.chat_item, chatArr);


    }

    private class TryToGetArrayOfChats extends AsyncTask<String, Void, ArrayList> {
        String token;

        public TryToGetArrayOfChats (String token) {
            this.token = token;

        }

        protected String doInBackground(String... urls) {
            String link = urls[0];
            String myToken = null;

            try {
                myToken = getToken(login, password, link);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return myToken;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public static String getStringResponse(HttpURLConnection con) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    public static ArrayList<ChatItem> getChats(String token) throws Exception {
        ArrayList<ChatItem> chatItemList = new ArrayList();

        URL url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&count=200&v=5.138");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        JSONObject response = new JSONObject(getStringResponse(con));

        int index = 0;
        while (!response.getJSONArray("items").isNull(index)) {
            JSONObject last_message = response.getJSONArray("items")
                    .getJSONObject(index).getJSONObject("last_message");

            if (!last_message.getString("text").equals("")) {
                ChatItem item = new ChatItem(,
                        ,message_format(last_message.getString("text"), 1)
                        );
                chatItemList.add(message_format(last_message.getString("text"), 1));

            } else if (last_message.getJSONArray("attachments").isNull(0) && last_message.getJSONArray("fwd_messages").isNull(0)) {
                System.out.println("Сервисное сообщение index: " + index);
                chatItemList.add("Сервисное сообщение index: ")
            } else if (last_message.getJSONArray("attachments").isNull(0) && !last_message.getJSONArray("fwd_messages").isNull(0)) {
                System.out.println("Пересланное сообщение: " + index);

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("wall")) {
                System.out.println("Запись на стене. index: " + index);

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("audio_message")) {
                System.out.println("Аудиозапись. index: " + index);

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("photo")) {
                System.out.println("Фотография. index: " + index);

            } else {
                System.out.println("Последнее сообщение этого чата не принадлежит заданному формату index: " + index);
            }
            System.out.println();

            index++;
        }

        return null;
    }

    private static String message_format(String text, int format_id) {
        switch (format_id) {
            case 1:
                if (text.length() >= 20 ) {
                    return text = String_cut(text.trim(), 0, 20) + "...";
                } else {
                    return text;
                }
        }

        return null;
    }

    private static String String_cut(String text, int index_start, int index_end) {
        char[] dst = new char[index_end - index_start];
        String str;

        text.getChars(index_start, index_end, dst, 0);
        str = "" + dst[0];

        for (int i = 1; i < dst.length - 1; i++) {
            str += dst[i];
        }
        return str;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        chats_activity.token = token;
    }

}