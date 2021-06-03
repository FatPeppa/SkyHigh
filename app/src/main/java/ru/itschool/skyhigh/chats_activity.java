package ru.itschool.skyhigh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class chats_activity extends AppCompatActivity {
    private static final String vk_url = "https://api.vk.com/method";
    private static String token;
    //private List<Message> chats_last_messages;

    EditText text;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_activity);
        prefs = this.getSharedPreferences(
                "com.example.app", Context.MODE_PRIVATE);

        token = getIntent().getStringExtra("token");

        displayAllChats();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //ediText.setText(prefs.getString("tag", ""));
    }

    @Override
    protected void onStop() {
        super.onStop();

        //prefs.edit().putString("tag", editText.getText().toString()).apply();
    }

    private void displayAllChats() {
        ListView chats = findViewById(R.id.list_of_chats);

        ArrayList<ChatItem> chatArr = new ArrayList<>();

        try {
            chatArr = new TryToGetArrayOfChats(token).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        ChatAdapter chatsAdapter = new ChatAdapter(this, chatArr);

        chats.setAdapter(chatsAdapter);
    }


    private class TryToGetArrayOfChats extends AsyncTask<String, Void, ArrayList<ChatItem>> {
        String token;

        public TryToGetArrayOfChats (String token) {
            this.token = token;
        }

        protected ArrayList<ChatItem> doInBackground(String... urls) {
            ArrayList<ChatItem> chatArray = new ArrayList<>();
            try {
                chatArray = getChatItemsFunc();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return chatArray;
        }

        protected void onPostExecute(ArrayList<ChatItem> result) {
            super.onPostExecute(result);
        }
    }

    private class TryToGetResObject extends AsyncTask<String, Void, JSONObject> {
        String token;
        String url;

        public TryToGetResObject (String token, String url) {
            this.token = token;
            this.url = url;
        }

        protected JSONObject doInBackground(String... urls) {
            JSONObject object = null;

            try {
                //URL url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&count=200&v=5.138");
                URL url = new URL(this.url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                JSONObject obj = new JSONObject(MainActivity.getStringResponse(con).toString());
                object = obj.getJSONObject("response");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return object;
        }

        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }

    //Работа с LongPool
    public static JSONObject getLongPollServer() throws IOException, JSONException {
        URL url = new URL(vk_url + "/messages.getLongPollServer?access_token=" + token + "&lp_version=3&v=5.130");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String response = MainActivity.getStringResponse(con);
        JSONObject resp = new JSONObject(response);

        return resp.getJSONObject("response");
    }

    public static JSONObject sendLongPoll(String server, String key, String ts) throws IOException, JSONException {
        URL URLServer = new URL("https://" + server + "?key=" + key + "&act=a_check&wait=25&ts=" + ts + "&version=3");
        HttpURLConnection connection = (HttpURLConnection) URLServer.openConnection();
        String a = MainActivity.getStringResponse(connection);

        return new JSONObject(a);
    }

    // Получение/установка токена
    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        chats_activity.token = token;
    }

    // Получение ArrayList из ChatItem
    public static ArrayList<ChatItem> getChatItemsFunc() throws IOException, JSONException {
        ArrayList<ChatItem> array = new ArrayList<>();
        ArrayList<JSONObject> lastMessages = getArrayOfLastMessage();

        for (int i = 0; i < lastMessages.size(); i++) {
            JSONObject last_message = lastMessages.get(i);

            ChatItem item = new ChatItem();

            //Установка названия беседы/имени пользователя последнего диалога - пока не работает корректно
            Long id = (Long) last_message.getLong("id");
            String strID = id.toString();
            item.setChatName(strID);

            //устновка текста последнего сообщения
            if (!last_message.getString("text").equals("")) {
                item.setText_lastMessage(message_format(last_message.getString("text"), 1));

            } else if (last_message.getJSONArray("attachments").isNull(0) && last_message.getJSONArray("fwd_messages").isNull(0)) {
                item.setText_lastMessage("Сервисное сообщение");

            } else if (last_message.getJSONArray("attachments").isNull(0) && !last_message.getJSONArray("fwd_messages").isNull(0)) {
                item.setText_lastMessage("Пересланное сообщение.");

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("wall")) {
                item.setText_lastMessage("Запись на стене.");

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("audio_message")) {
                item.setText_lastMessage("Аудиозапись.");

            } else if (last_message.getJSONArray("attachments").getJSONObject(0).getString("type").equals("photo")) {
                item.setText_lastMessage("Фотография.");

            } else {
                item.setText_lastMessage("Not this format.");

            }

            //Установка даты + времени последнего сообщения
            long unixSeconds = last_message.getLong("date");
            Date date = new java.util.Date(unixSeconds*1000L);
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm");
            String formattedDate = sdf.format(date);
            item.setLastMessageTime(formattedDate);

            array.add(item);
        }

        return array;
    }

    // Получение последних сообщений из всех чатов
    public static ArrayList<JSONObject> getArrayOfLastMessage() throws IOException, JSONException {
        ArrayList<JSONObject> array = new ArrayList<>();
        URL url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&count=200&v=5.138");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        JSONObject object = new JSONObject(MainActivity.getStringResponse(con).toString());
        JSONObject resObj = object.getJSONObject("response");

        int index = 0;
        while (!resObj.getJSONArray("items").isNull(index)) {
            JSONObject last_message = resObj.getJSONArray("items").getJSONObject(index).getJSONObject("last_message");
            array.add(last_message);
            
            index++;
        }

        url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&offset=200&count=200&v=5.138");
        con = (HttpURLConnection) url.openConnection();

        object = new JSONObject(MainActivity.getStringResponse(con).toString());
        resObj = object.getJSONObject("response");

        int i = 0;
        while (!resObj.getJSONArray("items").isNull(i)) {
            JSONObject last_message = resObj.getJSONArray("items").getJSONObject(i).getJSONObject("last_message");
            array.add(last_message);

            i++;
        }

        return array;
    }

    //Редактирование текста
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
}

class ChatAdapter extends ArrayAdapter<ChatItem> {

    public ChatAdapter(Context context, ArrayList<ChatItem> arr) {
        super(context, R.layout.chat_item, arr);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ChatItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item, null);
        }

        // Заполняем адаптер
        ((TextView) convertView.findViewById(R.id.chatName)).setText(item.getChatName());
        ((TextView) convertView.findViewById(R.id.lastMessageTime)).setText(item.getLastMessageTime());
        ((TextView) convertView.findViewById(R.id.text_lastMessage)).setText(item.getText_lastMessage());

        return convertView;
    }
}


