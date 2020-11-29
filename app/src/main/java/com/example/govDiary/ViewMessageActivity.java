package com.example.govDiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.nio.channels.NoConnectionPendingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import okhttp3.Response;


public class ViewMessageActivity extends AppCompatActivity {
    private static final int PERMISSION_STORAGE_CODE = 1000;
    Toolbar toolbar;
    String id, authToken;
    String userFromTo;
    LinearLayout linearLayout;
    TextView topic, users, date, text;
    ImageView avatar;
    String lastUrlRequest = "";
    private String lastFilenameT = "";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);
        progressBar = findViewById(R.id.progressCircular);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Сообщение");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusbarColor));
        }
        Intent intent = getIntent();
        id = intent.getStringExtra("messageId");
        authToken = intent.getStringExtra("authToken");
        userFromTo = intent.getStringExtra("userFromTo");
        avatar = findViewById(R.id.avatar);
        topic = findViewById(R.id.topic);
        users = findViewById(R.id.users);
        date = findViewById(R.id.date);
        text = findViewById(R.id.textMessage);
        linearLayout = findViewById(R.id.linearLayout);
        (new getMessage()).execute();
    }

    private class getMessage extends AsyncTask<String, Integer, Void> {


        @Override
        protected Void doInBackground(String... strings) {
            try {

                Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getmessageinfo?id=" + id + "&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getApplicationContext(), false); /*
                 //GET /apiv3/getmessageinfo?id=6180&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=089261c48824b50747cc49f9e274681bc374c3eabb30968d37620___84069&vendor=edu HTTP/1.1
               // */
                JSONObject messageJSON = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result").getJSONObject("message");
                String dateS = messageJSON.getString("date");
                String subjectS = messageJSON.getString("subject");
                String textS = messageJSON.getString("text");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        date.setText(dateS);
                        topic.setText(subjectS);
                        text.setText(Html.fromHtml(textS));
                        users.setText(userFromTo);
                        ColorGenerator generator = ColorGenerator.MATERIAL; //generating avatar
                        TextDrawable drawable = TextDrawable.builder()
                                .buildRect(userFromTo.substring(0,1), generator.getColor(userFromTo.toString().split(" ")[0]));
                        avatar.setImageDrawable(drawable);
                    }
                });


                if(messageJSON.has("files")) {
                    JSONArray filesJson = messageJSON.getJSONArray("files");
                    LayoutInflater layoutInflater = getLayoutInflater();
                    for (int i = 0; i < filesJson.length(); i++) {
                        View fileView = layoutInflater.inflate(R.layout.file_item, null);
                        TextView filenameTextview = fileView.findViewById(R.id.fileName);
                        String filenameT = filesJson.getJSONObject(i).getString("filename");
                        filenameTextview.setText(filenameT);
                        final int onclicklistenerI = i;
                        filenameTextview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                lastUrlRequest = "";
                                lastFilenameT = "";
                                try {
                                    lastUrlRequest = filesJson.getJSONObject(onclicklistenerI).getString("link");
                                    lastFilenameT = filenameT;
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                                        requestPermissions(permissions, PERMISSION_STORAGE_CODE);
                                    }
                                    else{
                                        startDownload(lastUrlRequest, lastFilenameT);
                                    }
                                }
                                else{
                                    startDownload(lastUrlRequest, lastFilenameT);
                                }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.addView(fileView);
                            }
                        });

                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
            catch(Exception ex){
                ex.printStackTrace();
            }


            return null;


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_STORAGE_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && !lastUrlRequest.equals("")){
                    startDownload(lastUrlRequest, lastFilenameT);
                }
        }
    }

    private void startDownload(String url, String fileNameT) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }



}