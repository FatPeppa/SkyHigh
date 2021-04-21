package ru.itschool.skyhigh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class messages_activity extends AppCompatActivity {
    public static final String vk_url = "https://api.vk.com/method";
    public static String token;

    EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_activity);

        token = getIntent().getStringExtra("token");

        text = findViewById(R.id.editText);
        text.setText(token);
    }
}