package com.example.govDiary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.example.govDiary.Api;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    EditText logText, passText, IPtext;
    private static final int MIN_TIME_INTERVAL_BETWEEN_BACK_CLICKS = 2000; // # milliseconds, desired time passed between two back presses.
    private long backPressedTime;

    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    String logf, passf;
    ACProgressFlower loading;
    CheckBox autoLogin;
    Boolean exceptionCaught = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         pref = getApplicationContext().getSharedPreferences("LogData",Context.MODE_PRIVATE); // saving data of application
         editor = pref.edit();
         loading = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Подключение...")
                .fadeColor(Color.DKGRAY).build();
        setContentView(R.layout.activity_login);
        autoLogin = findViewById(R.id.checkBox);
        logText = ((TextInputLayout)findViewById(R.id.logET)).getEditText();
        passText = ((TextInputLayout)findViewById(R.id.passET)).getEditText();
        logText.setText(pref.getString("login", ""));
    }

    public void loginButton(View view) {
        logf = logText.getText().toString();
        passf = passText.getText().toString();
        loading.show();
        editor.putString("login", logf);
        editor.commit();
        tryLogin tl = new tryLogin();
        tl.execute();
    }

    private class tryLogin extends AsyncTask<String, Integer, Void> {
        protected Void doInBackground(String... urls) {
            exceptionCaught = false;
            RequestBody formBody = new FormBody.Builder()
                    .add("login", logf)
                    .add("password", passf)
                    .build();
            Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/auth?devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token&vendor=edu", formBody);
            if(response == null){
                editor.putString("loginStatus", "fail");
                editor.commit();
                snackbarExecute("Проблемы с интернет-соединением");
                loading.dismiss();
            }
            else{
                try {
                    if(response.code() == 400) {
                        snackbarExecute("Неверная пара логин-пароль");
                        loading.dismiss();
                    }
                    else{
                        String serverAnswer = response.body().string();
                        Log.d("LoginActivity", "ServerAnswer: " + serverAnswer);
                        JSONObject js = new JSONObject(serverAnswer);
                        JSONObject jsonAuth = js.getJSONObject("response").getJSONObject("result");
                        String token = jsonAuth.getString("token");
                        String timeToExpire = jsonAuth.getString("expires");
                        Intent intent = new Intent(LoginActivity.this, JournalActivity.class);
                        intent.putExtra("authToken", token);
                        intent.putExtra("timeToExpire", timeToExpire);
                        //получаем "правила" и фио юзера
                        Response responseRules = Api.sendRequest("https://edu.gounn.ru/apiv3/getrules?devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + token + "&vendor=edu", null);
                        String rulesJsonPlainString = responseRules.body().string();
                        JSONObject jsRules = new JSONObject(rulesJsonPlainString);
                        Log.d("LoginActivity", "ServerAnswerRulesResult: " + jsRules.getJSONObject("response").getJSONObject("result").toString());
                        String lName = jsRules.getJSONObject("response").getJSONObject("result").getString("lastname");
                        String mName = jsRules.getJSONObject("response").getJSONObject("result").getString("firstname");
                        String fName = jsRules.getJSONObject("response").getJSONObject("result").getString("middlename");
                        String studentID = jsRules.getJSONObject("response").getJSONObject("result").getString("id");
                        Log.d("LoginActivity", "Token received: " + token);
                        intent.putExtra("name", lName + " " + mName + " " + fName);
                        editor.putString("authToken", token);
                        editor.putString("studentID", studentID);
                        editor.putString("rulesJson", rulesJsonPlainString);
                        editor.putString("timeToExpire", timeToExpire);
                        editor.putString("name", lName + " " + mName + " " + fName);
                        //putting these to intent
                        intent.putExtra("authToken", token);
                        intent.putExtra("studentID", studentID);
                        Log.d("LoginActivity", "FIO received: " + lName + " " + mName + " " + fName);
                        //получаем название школы
                        String schoolName = jsRules.getJSONObject("response").getJSONObject("result").getJSONObject("relations").getJSONArray("schools").getJSONObject(0).getString("title");
                        String schoolClass = "";
                        Iterator<String> iter = jsRules.getJSONObject("response").getJSONObject("result").getJSONObject("relations").getJSONObject("groups").keys();
                        for(;iter.hasNext();) {
                            schoolClass = iter.next();

                        }
                        editor.putString("schoolName", schoolName);
                        editor.putString("schoolClass", schoolClass);
                        Log.d("LoginActivity", "School name received: " + schoolName + "/studentid:" + studentID);
                        //коммитим
                        editor.commit();
                        //переходим к основному активити
                        loading.dismiss();
                        snackbarExecute("Вход успешно выполнен");
                        if(autoLogin.isChecked()){
                            editor.putString("loginStatus", "success");
                            editor.commit();
                            startActivity(intent);
                        }
                        else {
                            editor.putString("loginStatus", "fail");
                            editor.commit();
                            startActivity(intent);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    loading.dismiss();
                    snackbarExecute("Произошла ошибка при подключении");
                }
            }







            return null;
        }

        }
    @Override
    public void onBackPressed()
    {
        if (backPressedTime + MIN_TIME_INTERVAL_BETWEEN_BACK_CLICKS > System.currentTimeMillis()) {
            finishAffinity();
            return;
        }
        else {
            Toast.makeText(this, "Нажмите \"назад\" дважды, чтобы выйти", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    private void snackbarExecute(String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(android.R.id.content),s,Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    }


