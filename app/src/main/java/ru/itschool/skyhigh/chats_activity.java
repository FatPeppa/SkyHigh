package ru.itschool.skyhigh;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.sql.*;

import static ru.itschool.skyhigh.MainActivity.getStringResponse;
import static ru.itschool.skyhigh.dsadsaContract.COLUMN_CHAT_ID;
import static ru.itschool.skyhigh.dsadsaContract.COLUMN_MESSAGE;
import static ru.itschool.skyhigh.dsadsaContract.TABLE_NAME;

public class chats_activity extends AppCompatActivity {
    private static final String vk_url = "https://api.vk.com/method";
    private static String token;
    private ListView list_of_chats;
    RelativeLayout thisLayout;
    ArrayList<ChatItem> chatArr;
    Button updateButton;
    String[] testStringArr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_chats_activity);

        thisLayout = findViewById(R.id.activity_chats_root);
        updateButton = findViewById(R.id.updateButton);

        token = getIntent().getStringExtra("token");

        list_of_chats = findViewById(R.id.list_of_chats);

        chatArr = new ArrayList<ChatItem>();

        try {
            testStringArr = new getServer_new().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<ChatItem> exampleArrayList = (ArrayList<ChatItem>) getIntent().getSerializableExtra("ArrayList");

        if (exampleArrayList != null) {
            chatArr = exampleArrayList;
        } else {
            try {
                chatArr = new TryToGetArrayOfChats(token).execute().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        displayAllChats(chatArr);

        updateButton.setOnClickListener(v -> {
            ArrayList<ChatItem> arrayList = new ArrayList<>();

            arrayList = tryF(chatArr, testStringArr);

            Log.i("Hu", "no trouble with arrayList methods");

            Intent refresh = new Intent(getIntent());
            refresh.putExtra("NewArrayList", (Serializable) arrayList);
            startActivity(refresh);
            finish();
        });

        list_of_chats.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatItem item = (ChatItem) list_of_chats.getItemAtPosition(position);

                long Conversation_ID = item.getLastConversation_ID();

                show_AddingBanWordWindow(chatArr, Conversation_ID, testStringArr);
            }
        });
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

    private void displayAllChats(ArrayList<ChatItem> chatArr) {
        ListView chats = findViewById(R.id.list_of_chats);

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

    public Boolean check_delete(long chat_id, String message) {

        // cur = db.query(TABLE_NAME, new String[]{COLUMN_CHAT_ID, COLUMN_MESSAGE}, null, null, null, null, null);
        //         String mess;
        //         while(cur.moveToNext()) {
        //             mess = cur.getString(cur.getColumnIndex(COLUMN_MESSAGE));
        //             int chat_id = cur.getInt(cur.getColumnIndex(COLUMN_CHAT_ID));
        //             if(chat_id)
        //         }
        //         String mess = cur.getString(cur.getColumnIndex(COLUMN_MESSAGE));
        //         cur.close();
        dsadsaDbHelper helper = new dsadsaDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cur = db.query(TABLE_NAME, new String[]{COLUMN_CHAT_ID, COLUMN_MESSAGE}, null, null, null, null, null);
        while(cur.moveToNext()) {
            if(cur.getLong(cur.getColumnIndex(COLUMN_CHAT_ID)) == chat_id) {
                if(message.toLowerCase().contains(cur.getString(cur.getColumnIndex(COLUMN_MESSAGE)).toLowerCase())) {
                    cur.close();
                    return true;
                }
            }
        }
        cur.close();
        return false;
    }

    private class TryToUpdateArrayOfChats extends AsyncTask<String, Void, ArrayList<ChatItem>> {
        String token;
        ArrayList<ChatItem> chatItems_arrayList;
        JSONArray updates;

        public TryToUpdateArrayOfChats (String token, ArrayList<ChatItem> chatItems_arrayList, JSONArray updates) {
            this.token = token;
            this.chatItems_arrayList = chatItems_arrayList;
            this.updates = updates;
        }

        protected ArrayList<ChatItem> doInBackground(String... urls) {
            JSONObject response1;

            try {

                for (int i = 0; i < updates.length(); i++) {
                    ChatItem item = new ChatItem();

                    JSONArray update = updates.getJSONArray(i);
                    if (update.getInt(0) == 4) {
                        long chat_id = update.getLong(3);
                        long message_id = update.getLong(1);
                        item.setLastConversation_ID(chat_id);

                        JSONObject infoMessage = getById(token, message_id);
                        JSONArray attachments = infoMessage.getJSONArray("items").getJSONObject(0).getJSONArray("attachments");

                        if (chat_id > 2000000000) {
                            JSONObject chat = getChat(token, chat_id);
                            item.setChatName( chat.getString("title"));
                            item.setChat_Type("chat");

                        } else if(chat_id < 0) {
                            chat_id = -chat_id;
                            item.setChat_Type("group");

                            URL newUrl = new URL(vk_url + "/groups.getById?group_id=" + chat_id + "&fields=photo_50&access_token=" + token + "&v=5.138");
                            HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();
                            JSONObject object = new JSONObject(getStringResponse(con));
                            JSONObject resObj = object.getJSONArray("response").getJSONObject(0);

                            item.setChatName(message_format(resObj.getString("name"), 2));
                        } else {
                            JSONObject profile = infoMessage.getJSONArray("profiles").getJSONObject(0);
                            item.setChat_Type("user");
                            item.setChatName(profile.getString("first_name") + " " +profile.getString("last_name"));
                        }

                        boolean sign = false;
                        for (int j = 0; j < attachments.length(); j++) {
                            if (!getAttach(attachments.getJSONObject(j).getString("type")).equals("")
                                    || !getAttach(attachments.getJSONObject(j).getString("type")).isEmpty()) {
                                item.setText_lastMessage(getAttach(attachments.getJSONObject(j).getString("type")));

                                sign = true;
                            }
                        }

                        try {
                            if(check_delete(chat_id, update.getString(5))) {
                                deleteMessage(message_id);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!sign) {
                            item.setText_lastMessage(update.getString(5));
                        }

                        long unixSeconds = System.currentTimeMillis() / 1000L;
                        Date date = new java.util.Date(unixSeconds);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm");
                        String formattedDate = sdf.format(date);
                        item.setLastMessageTime(formattedDate);

                        if (hasLastConversation_ID(chatItems_arrayList, item.getLastConversation_ID())) {
                            int indexOfLastElement = indexOfChatItem_sameLastConversation_ID(chatItems_arrayList, item.getLastConversation_ID());

                            /*if (chatItems_arrayList.size() == indexOfLastElement) {
                                chatItems_arrayList.add(new ChatItem());
                            }*/
                            if (indexOfLastElement != 0) {
                                for (int j = indexOfLastElement; j > 0; j--) {
                                    chatItems_arrayList.set(j, chatItems_arrayList.get(j - 1));
                                }
                            }

                            chatItems_arrayList.set(0, item);
                        } else {
                            chatItems_arrayList.add(new ChatItem());

                            for (int j = chatItems_arrayList.size() - 1; j > 0; j--) {
                                chatItems_arrayList.set(j, chatItems_arrayList.get(j - 1));
                            }

                            chatItems_arrayList.set(0, item);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


            return chatItems_arrayList;
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
                URL url = new URL(this.url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                JSONObject obj = new JSONObject(getStringResponse(con).toString());
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
        String response = getStringResponse(con);
        JSONObject resp = new JSONObject(response);

        return resp.getJSONObject("response");
    }

    public static JSONObject sendLongPoll(String server, String key, String ts) throws IOException, JSONException {
        URL URLServer = new URL("https://" + server + "?key=" + key + "&act=a_check&wait=25&ts=" + ts + "&version=3");
        HttpURLConnection connection = (HttpURLConnection) URLServer.openConnection();
        String a = getStringResponse(connection);

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

            //Установка типа последней беседы
            item.setChat_Type("test_ChatType");

            //Установка названия беседы/имени пользователя последнего диалога - пока не работает корректно
            item.setChatName("test_ChatName");

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

        ArrayList<JSONObject> lastConversations = getArrayOfLastConversations();
        for (int i = 0; i < lastConversations.size(); i++) {
            JSONObject last_Conversation = lastConversations.get(i);
            JSONObject peer = last_Conversation.getJSONObject("peer");
            long id = peer.getLong("id");
            array.get(i).setLastConversation_ID(id);

            String type = peer.getString("type");
            array.get(i).setChat_Type(type);
            //array.get(i).setChatName(String.valueOf(id));

            switch (type) {
                case "user":
                    URL newUrl = new URL(vk_url + "/users.get?user_ids=" + id + "&fields=photo_50&access_token=" + token + "&v=5.138");
                    HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();
                    JSONObject object = new JSONObject(getStringResponse(con));
                    Log.i("Cont", object.toString());

                    JSONObject resObj = new JSONObject();

                    try {
                        JSONArray resObjArr = object.getJSONArray("response");
                        resObj = resObjArr.getJSONObject(0);

                    } catch (Exception e) {
                        JSONObject resObjArr = object.getJSONObject("response");
                        resObj = resObjArr.getJSONObject("response");
                    }


                    array.get(i).setChatName(resObj.getString("first_name") + " " + resObj.getString("last_name"));

                    break;
                case "chat":
                    //newUrl = new URL(vk_url + "/messages.getChat?chat_id=" + id + "&fields=photo_50&access_token=" + token + "&v=5.138");
                    newUrl = new URL(vk_url + "/messages.getConversationsById?peer_ids=" + id + "&access_token=" + token + "&v=5.138");
                    con = (HttpURLConnection) newUrl.openConnection();
                    object = new JSONObject(getStringResponse(con));
                    resObj = object.getJSONObject("response");

                    JSONObject obj = resObj.getJSONArray("items").getJSONObject(0).getJSONObject("chat_settings");


                    array.get(i).setChatName(message_format(obj.getString("title"), 2));

                    break;
                case "group":
                    newUrl = new URL(vk_url + "/groups.getById?group_id=" + -id + "&fields=photo_50&access_token=" + token + "&v=5.138");
                    con = (HttpURLConnection) newUrl.openConnection();
                    object = new JSONObject(getStringResponse(con));
                    resObj = object.getJSONArray("response").getJSONObject(0);


                    array.get(i).setChatName(message_format(resObj.getString("name"), 2));

                    break;
                default:
                    array.get(i).setChatName("Unknown formatted name");
                    break;
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return array;
    }

    // Получение последних сообщений из всех чатов
    public static ArrayList<JSONObject> getArrayOfLastMessage() throws IOException, JSONException {
        ArrayList<JSONObject> array = new ArrayList<>();
        URL url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&count=200&v=5.138");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        JSONObject object = new JSONObject(getStringResponse(con));
        JSONObject resObj = object.getJSONObject("response");

        int index = 0;
        while (!resObj.getJSONArray("items").isNull(index)) {
            JSONObject last_message = resObj.getJSONArray("items").getJSONObject(index).getJSONObject("last_message");
            array.add(last_message);

            index++;
        }

        url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&offset=200&count=200&v=5.138");
        con = (HttpURLConnection) url.openConnection();

        object = new JSONObject(getStringResponse(con).toString());
        resObj = object.getJSONObject("response");

        int i = 0;
        while (!resObj.getJSONArray("items").isNull(i)) {
            JSONObject last_message = resObj.getJSONArray("items").getJSONObject(i).getJSONObject("last_message");
            array.add(last_message);

            i++;
        }

        return array;
    }

    //Получения ArrayList из последних Conversations
    public static ArrayList<JSONObject> getArrayOfLastConversations() throws IOException, JSONException {
        ArrayList<JSONObject> array = new ArrayList<>();
        URL url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&count=200&v=5.138");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        JSONObject object = new JSONObject(getStringResponse(con));
        JSONObject resObj = object.getJSONObject("response");

        int index = 0;
        while (!resObj.getJSONArray("items").isNull(index)) {
            JSONObject last_conversation = resObj.getJSONArray("items").getJSONObject(index).getJSONObject("conversation");
            array.add(last_conversation);

            index++;
        }

        url = new URL(vk_url + "/messages.getConversations?access_token=" + token + "&offset=200&count=200&v=5.138");
        con = (HttpURLConnection) url.openConnection();

        object = new JSONObject(getStringResponse(con));
        resObj = object.getJSONObject("response");

        int i = 0;
        while (!resObj.getJSONArray("items").isNull(i)) {
            JSONObject last_conversation = resObj.getJSONArray("items").getJSONObject(i).getJSONObject("conversation");
            array.add(last_conversation);

            i++;
        }

        return array;
    }

    //Редактирование текста
    private static String message_format(String text, int format_id) {
        switch (format_id) {
            case 1:
                if (text.length() >= 28 ) {
                    return text = String_cut(text.trim(), 0, 20) + "...";
                } else {
                    return text;
                }
            case 2:
                if (text.length() >= 15 ) {
                    return text = String_cut(text.trim(), 0, 15) + "...";
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

    public static JSONObject getById(String token, Long message_id) throws IOException, JSONException {
        URL getById = new URL(vk_url + "/messages.getById?access_token=" + token + "&message_ids=" + Long.toString(message_id) + "&extended=1&v=5.131");
        HttpURLConnection getResp = (HttpURLConnection) getById.openConnection();
        String resp = getStringResponse(getResp);
        return new JSONObject(resp).getJSONObject("response");
    }

    public static JSONObject getChat(String token, Long chat_id) throws IOException, JSONException {
        if (chat_id - 2000000000 > 0) {
            chat_id = chat_id - 2000000000;
        }
        URL getChat = new URL(vk_url + "/messages.getChat?access_token=" + token + "&chat_id=" + Long.toString(chat_id) + "&v=5.130");
        HttpURLConnection connection = (HttpURLConnection) getChat.openConnection();
        return new JSONObject(getStringResponse(connection)).getJSONObject("response");
    }

    public static String getAttach(String attach) {
        switch(attach) {
            case "photo": {
                return "Фото";
            }
            case "video": {
                return "Видео";
            }
            case "audio": {
                return "Аудио";
            }
            case "doc": {
                return "Документ";
            }
            case "wall": {
                return "Запись на стене";
            }
            case "wall_reply": {
                return "Комментарий к записи на стене";
            }
            case "sticker": {
                return "Стикер";
            }
            case "link": {
                return "Ссылка";
            }
            case "gift": {
                return "Подарок";
            }
            case "market_album": {
                return "Подборка товаров";
            }
            case "market": {
                return "Товар";
            }
            default: {
                return "";
            }
        }
    }

    public static boolean hasLastConversation_ID(ArrayList<ChatItem> arrayList, long id){
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).getLastConversation_ID() == id) {
                return true;
            }
        }

        return false;
    }

    public static int indexOfChatItem_sameLastConversation_ID(ArrayList<ChatItem> arrayList, long id) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).getLastConversation_ID() == id) {
                return i;
            }
        }

        return -1;
    }

    public static boolean sameArrayLists(ArrayList<ChatItem> arrayList1, ArrayList<ChatItem> arrayList2) {
        boolean sign = true;

        for (int i = 0; i < arrayList1.size(); i++) {
            if (arrayList1.get(i) == arrayList2.get(i)) {
                sign = false;
            }
        }

        return sign;
    }

    private void reload(ArrayList<ChatItem> chatArr)
    {
        Intent intent = getIntent();
        intent.putExtra("NewArrayList", chatArr);

        overridePendingTransition(0, 0);//4
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);//5
        finish();//6
        overridePendingTransition(0, 0);//7
        startActivity(intent);//8
    }

    private static class getServer_new extends AsyncTask<String, Void, String[]> {
        public getServer_new () {}

        protected String[] doInBackground(String... urls) {
            JSONObject response1 = new JSONObject();
            String[] array = new String[3];

            try {
                response1 = getLongPollServer();
                String server = response1.getString("server");
                String key = response1.getString("key");
                String ts = Long.toString(response1.getLong("ts"));

                array[0] = server; array[1] = key; array[2] = ts;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return array;
        }

        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
        }
    }

    private static class sendPool_to_Server_new extends AsyncTask<String, Void, JSONObject> {
        String[] array;
        public sendPool_to_Server_new (String[] array) {
            this.array = array;
        }

        protected JSONObject doInBackground(String... urls) {
            JSONObject response = new JSONObject();
            String server = this.array[0];
            String key = this.array[1];
            String ts = this.array[2];

            try {
                response = sendLongPoll(server, key, ts);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }

    private Thread timer = new Thread(new Runnable() {
        @Override
        public void run() {
            try{
                Thread.sleep(5000);
                Log.i("Timer", "TimerFinished");
            }
            catch(Exception e){
                Log.e("Timer","Error");
            }
        }
    });

    public ArrayList<ChatItem> tryF(ArrayList<ChatItem> chatArr, String[] serverData) {
        ArrayList<ChatItem> arrayList = new ArrayList<>();

        try {
            String server = serverData[0];
            String key = serverData[1];
            String ts = serverData[2];

            //с этой части необходимо повторять
            JSONObject response = new sendPool_to_Server_new(serverData).execute().get();

            JSONArray updates = response.getJSONArray("updates");
            ts = Long.toString(response.getLong("ts"));

            arrayList = new TryToUpdateArrayOfChats(token, chatArr, updates).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public static void deleteMessage(long message_id) throws IOException {
        URL url = new URL(vk_url + "/messages.delete?access_token=" + token + "&message_ids=" + Long.toString(message_id) + "&v=5.131");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
    }

    private void show_AddingBanWordWindow(ArrayList<ChatItem> chatArr, long chat_ID, String[] testStringArr) {


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Удалить сообщения по слову");
        dialog.setMessage("Введите слово, сообщения с которым нужно удалить");

        LayoutInflater inflater = LayoutInflater.from(this);
        View adding_banned_word_window = inflater.inflate(R.layout.adding_banned_word_window, null);
        dialog.setView(adding_banned_word_window);

        final MaterialEditText banned_word = adding_banned_word_window.findViewById(R.id.banned_word_textPlace);

        dialog.setNegativeButton("Отменить", (dialogInterface, which) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Удалить", (dialogInterface, which) -> {
            if (TextUtils.isEmpty(banned_word.getText().toString())) {
                Snackbar.make(thisLayout, "Введите удаляемое слово", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (banned_word.getText().toString().length() < 3) {
                Snackbar.make(thisLayout, "Введите удаляемое слово", Snackbar.LENGTH_SHORT).show();
                return;
            }

            try {
                // cur = db.query(TABLE_NAME, new String[]{COLUMN_CHAT_ID, COLUMN_MESSAGE}, null, null, null, null, null);
                //         String mess;
                //         while(cur.moveToNext()) {
                //             mess = cur.getString(cur.getColumnIndex(COLUMN_MESSAGE));
                //             int chat_id = cur.getInt(cur.getColumnIndex(COLUMN_CHAT_ID));
                //             if(chat_id)
                //         }
                //         String mess = cur.getString(cur.getColumnIndex(COLUMN_MESSAGE));
                //         cur.close();
                dsadsaDbHelper helper = new dsadsaDbHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_CHAT_ID, chat_ID);
                cv.put(COLUMN_MESSAGE, banned_word.getText().toString());
                db.insert(TABLE_NAME, null, cv);
                ArrayList<ChatItem> arrayList = new ArrayList<>();

                arrayList = tryF(chatArr, testStringArr);

                Log.i("Hu", "no trouble with adding word");

                Intent refresh = new Intent(getIntent());
                refresh.putExtra("NewArrayList", (Serializable) arrayList);
                startActivity(refresh);
                finish();

            } catch (Exception e) {
                Snackbar.make(thisLayout, "Adding banned word error", Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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