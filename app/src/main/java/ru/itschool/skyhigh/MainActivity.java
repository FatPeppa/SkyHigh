package ru.itschool.skyhigh;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    public static final String vk_url = "https://api.vk.com/method";

    Button btnSignIn, btnRegister;
    RelativeLayout root;
    String token = "empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        root = findViewById(R.id.root_element);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
            }
        });
    }

    private void showSignInWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Войти");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);

        final MaterialEditText login = sign_in_window.findViewById(R.id.loginField);
        final MaterialEditText password = sign_in_window.findViewById(R.id.passwordField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (TextUtils.isEmpty(login.getText().toString())) {
                    Snackbar.make(root, "Введите ваш номер телефона", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.getText().toString().length() < 4) {
                    Snackbar.make(root, "Введите ваш пароль", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                try {
                    token = new TryToGetToken(token, login.getText().toString(), password.getText().toString())
                            .execute(("https://oauth.vk.com/token?grant_type=password&client_id=2274003&client_secret=hHbZxrka2uZ6jB1inYsH&scope=501202911&username="
                                    + URLEncoder.encode(login.getText().toString())
                                    + "&password=" + URLEncoder.encode(password.getText().toString()))).get();
                } catch (Exception e) {
                    Snackbar.make(root, "Catching token error: wrong method", Snackbar.LENGTH_SHORT).show();
                }

                if (token.equals("error") ) {
                    Snackbar.make(root, "Catching token error: Invalid token", Snackbar.LENGTH_SHORT).show();
                } else if (token.equals("empty")) {
                    Snackbar.make(root, "Catching token error: Token Empty", Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, messages_activity.class);
                    intent.putExtra("token", token);
                    startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    private class TryToGetToken extends AsyncTask<String, Void, String> {
        String token;
        String login, password;

        public TryToGetToken (String token, String login, String password) {
            this.token = token;
            this.login = login;
            this.password = password;
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

    public static String getToken(String login, String password, String link) throws IOException {

        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        try {
            JSONObject response = new JSONObject(getStringResponse(con));
            String token = response.getString("access_token");
            return token;
        } catch (IOException | JSONException e) {
            return "error";
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
}