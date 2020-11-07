package com.example.govDiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    private static final int MIN_TIME_INTERVAL_BETWEEN_BACK_CLICKS = 2000; // # milliseconds, desired time passed between two back presses.
    private long backPressedTime;
    String authToken;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getApplicationContext().getSharedPreferences("LogData", Context.MODE_PRIVATE); // saving data of application
        editor = pref.edit();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
            if(pref.getString("loginStatus", "").equals("success")){
                authToken = pref.getString("authToken", "");

                Intent intent = new Intent(MainActivity.this, JournalActivity.class);
                Log.d("MainActivityAuth", "auth succeed");
                intent.putExtra("authToken", authToken);
                intent.putExtra("studentID", pref.getString("studentID", "errGettingStudIdFromPref"));
                startActivity(intent);


            }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("authToken", authToken);
            intent.putExtra("studentID", pref.getString("studentID", "errGettingStudIdFromPref"));
            startActivity(intent);
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







