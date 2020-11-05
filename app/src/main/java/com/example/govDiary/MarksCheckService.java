package com.example.govDiary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MarksCheckService extends Worker {
    SharedPreferences pref;
    HashMap<Integer, String> subjects;
    String id2f, loginf, IPf, passf;
    String classidf, student;
    String subjectsUnformatted;
    int itemCounter = 0;
    String TAG = "marksCheckService";
    SharedPreferences.Editor editor;

    public MarksCheckService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        subjects = new HashMap<>();
        pref = getApplicationContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        id2f = pref.getString("id2", "errorGettingIntent");
        loginf = pref.getString("login", "errLogin");
        IPf = pref.getString("IP", "errIP");
        classidf = pref.getString("classidf", "errClass");
        subjectsUnformatted = pref.getString("subjects", "[]");
        student = pref.getString("id", "studentIdErr");
        passf = pref.getString("passwordSHA1", "errPassSHA1");

        getMarks gM = new getMarks();
        gM.execute();



// notificationId is a unique int for each notification that you must define
        Log.d("TAG", "checkservice");
        return Result.success();
    }

    private void createNotificationExpandable(String t){

        // Intent being called when clicking on the notification
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(getApplicationContext().getString(R.string.app_name));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        // Create notification
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "my_channel_id";
        CharSequence channelName = "My Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), "newMark")
                    .setContentTitle("Новая оценка!")
                    .setSmallIcon(R.drawable.ic_info)
                    .setChannelId(channelId)
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(t))
                    .build();

            notificationManager.notify(655, notification);
        } else {

            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Новая оценка!")
                    .setContentText(t)
                    .setSmallIcon(R.drawable.ic_info)
                    .build();

            notificationManager.notify(888, notification);
        }
    }
    private void createNotification(String t){

        // Intent being called when clicking on the notification
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction(getApplicationContext().getString(R.string.app_name));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        // Create notification
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "my_channel_id";
        CharSequence channelName = "My Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), "newMark")
                    .setContentTitle("Новая оценка!")
                    .setContentText(t)
                    .setSmallIcon(R.drawable.ic_info)
                    .setChannelId(channelId)
                    .build();

            notificationManager.notify(655, notification);
        } else {

            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Новая оценка!")
                    .setContentText(t)
                    .setSmallIcon(R.drawable.ic_info)
                    .build();

            notificationManager.notify(888, notification);
        }
    }

    private class getMarks extends AsyncTask<String, Integer, Void> {


        @Override
        protected Void doInBackground(String... strings) {

            //составляем запрос на сервер за списком всех оценок (не годовых\четвертных)
            String serverAnswer;
            String url = "http://" + IPf + "/act/GET_STUDENT_JOURNAL_DATA ";

            OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
            cookieHelper.setCookie(url, "ys-userId", "n%3A" + id2f);
            cookieHelper.setCookie(url, "ys-user", "s%3A" + StringEscapeUtils.escapeJava(loginf).replace("\\", "%"));
            cookieHelper.setCookie(url, "ys-password", "s%3A" + passf);

            OkHttpClient client = new OkHttpClient.Builder()  //??? DELETE PROXY
                    .cookieJar(cookieHelper.cookieJar())
                    .build();
            RequestBody formbody = new FormBody.Builder()
                    .add("cls", classidf)
                    .add("parallelClasses", "")
                    .add("student", student)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formbody)
                    .build();

            try {
                //обработка предметов
                JSONArray subj = new JSONArray(subjectsUnformatted);
                for (int i = 0; i < subj.length(); i++) {
                    subjects.put(subj.getJSONArray(i).getInt(0), subj.getJSONArray(i).getString(1));
                }
                //получение оценок
                Response response3 = client.newCall(request).execute();
                serverAnswer = response3.body().string();
                serverAnswer = "[" + serverAnswer.substring(2, serverAnswer.length() - 1) + "]";
                serverAnswer = serverAnswer.replace("\n", "").replace("new Date(", "\"")
                        .replace("),", "\",");
                JSONArray myMarks = new JSONArray(serverAnswer);
                String text = "новых оценок нет";
                if(pref.contains("previousMarks")){
                    text = "";
                    JSONArray myOldMarks = new JSONArray(pref.getString("previousMarks", ""));
                    if(myOldMarks.length() < myMarks.length()){
                        for (int i = myMarks.length() - 1; i > myOldMarks.length() - 1; i--) {
                            itemCounter++;
                            text += myMarks.getJSONArray(i).getString(2) + " - " + subjects.get(myMarks.getJSONArray(i).getInt(4)) + ", ";
                        }
                    }
                    Log.d(TAG, "previousServerAnswer: " + myOldMarks.length());
                }

                try {
                    if(!text.equals("новых оценок нет")) {
                        text = text.substring(0, text.length() - 2);
                        if(itemCounter >1) {
                            createNotificationExpandable(text);
                        }
                        else{
                            createNotification(text);
                        }

                    }
                }
                catch(StringIndexOutOfBoundsException e){
                    Log.e(TAG, "String is null");
                }
                editor.putString("previousMarks", serverAnswer);

                Log.d(TAG, "serverAnswer: " + serverAnswer);
                //editor.putString("previousMarks", "");
                editor.commit();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
