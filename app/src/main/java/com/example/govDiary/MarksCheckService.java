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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.nio.channels.NoConnectionPendingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MarksCheckService extends Worker {
    SharedPreferences pref;

    String TAG = "marksCheckService";
    SharedPreferences.Editor editor;
    String studentID = "";
    String authToken = "";
    String lastStartDate = "";
    String lastEndDate = "";
    ArrayList<Period> periodList;
    LinkedHashMap<String, ArrayList<Mark>> markedLessons;
    private AsyncTask<String, Integer, Void> getMarksAsyncTasks;

    public MarksCheckService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        periodList = new ArrayList<>();
        markedLessons = new LinkedHashMap<>();
        pref = getApplicationContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        studentID = pref.getString("studentID", "errPref");
        authToken = pref.getString("authToken", "errPref");




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

   /*private class getMarks extends AsyncTask<String, Integer, Void> {


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

    */

    private class getPeriods extends AsyncTask<String, Integer, Void> {  //TODO: YEAR FINAL ASSESSMENTS FIX!!!!!!!!!
        Response response;
        String responseString;
        @Override
        protected Void doInBackground(String... strings) {
            response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=false&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, null, false);
            try {
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (response == null) throw new NoConnectionPendingException();
                JSONArray periods = (new JSONObject(responseString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
                String yearStart = "", yearEnd = "";
                for (int i = 0; i < periods.length(); i++) {
                    String name = periods.getJSONObject(i).getString("fullname");
                    String startDate = periods.getJSONObject(i).getString("start");
                    String endDate = periods.getJSONObject(i).getString("end");
                    periodList.add(new Period(name, startDate, endDate));
                    Log.d(TAG, "Adding period to periodList: " + periodList.get(i).name + "/" + periodList.get(i).startDate + "/" + periodList.get(i).endDate);
                    if(i == periods.length() - 1){
                        yearEnd = periodList.get(i).endDate;
                    }
                    else if(i == 0){
                        yearStart = periodList.get(i).startDate;
                    }

                }
                //generating year period:
                periodList.add(new Period("Год", yearStart, yearEnd));
                Log.d(TAG, "doInBackground: students json period list size: " + periodList.size());

            }
            catch(Exception e){
                e.printStackTrace();
            }
                try {
                    String firstDateAfterFoundStart = null;
                    String firstDateAfterFoundEnd = null;
                    int lastCounterFound = 0;
                    for (int i = 0; i < periodList.size(); i++) {

                        Date startingDate = new SimpleDateFormat("yyyyMMdd").parse(periodList.get(i).startDate);
                        Date endingDate = new SimpleDateFormat("yyyyMMdd").parse(periodList.get(i).endDate);
                        Date curDate = Calendar.getInstance().getTime();
                        if(firstDateAfterFoundStart == null && curDate.after(startingDate)){
                            firstDateAfterFoundStart = periodList.get(i).startDate;
                            firstDateAfterFoundEnd = periodList.get(i).endDate;
                            lastCounterFound = i;
                        }
                        if(curDate.after(startingDate) && curDate.before(endingDate)){
                            firstDateAfterFoundStart = null;
                            Log.d(TAG, "position period: " + i);
                            final int counter = i;
                            final Handler handler = new Handler(Looper.getMainLooper());
                            break;
                        }

                    }

                    if(firstDateAfterFoundStart != null){
                        final int lastCounter = lastCounterFound;
                        getMarksAsyncTasks = new getNewMarks(firstDateAfterFoundStart, firstDateAfterFoundEnd).execute();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            super.onPostExecute(aVoid);
        }
    }


    private class getNewMarks extends AsyncTask<String, Integer, Void> {
        String startDate, endDate;
        Response response;
        String responseString;

        public getNewMarks(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                //Getting all finalmarks - bad implementation because of the diary's api - they separated the marks and finalmarks :|
                HashMap<String, String> finalMarks = new HashMap<>();


                //Getting the marks
                if(response == null) throw new ConnectException();
                JSONArray marks = null;
                try {
                    marks = (new JSONObject(responseString)).getJSONObject("response").getJSONObject("result").getJSONObject("students").getJSONObject(studentID).getJSONArray("lessons");
                }
                catch(Exception e){
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < marks.length(); i++) {
                    JSONObject mark = marks.getJSONObject(i);
                    String name = mark.getString("name");
                    Double average = mark.getDouble("average");
                    JSONArray marksJsonList = mark.getJSONArray("marks");
                    ArrayList<Mark> tMarklist = new ArrayList<>();
                    //marks fori
                    Log.d(TAG, "doInBackgroundMarkGetting: " + marksJsonList.toString());
                    for (int j = 0; j < marksJsonList.length(); j++) {
                        String markStr = marksJsonList.getJSONObject(j).getString("value");
                        String comment = marksJsonList.getJSONObject(j).getString("comment");
                        boolean counted = marksJsonList.getJSONObject(j).getBoolean("count");
                        String lessonComment = marksJsonList.getJSONObject(j).getString("lesson_comment");
                        String date = marksJsonList.getJSONObject(j).getString("date");
                        String mType = "";
                        double weight = 1.0;
                        if(marksJsonList.getJSONObject(j).has("mtype")) mType = marksJsonList.getJSONObject(j).getJSONObject("mtype").getString("type");
                        if(marksJsonList.getJSONObject(j).has("weight")) weight = marksJsonList.getJSONObject(j).getDouble("weight");
                        if(!counted) weight = 0.0;
                        tMarklist.add(new Mark(markStr, weight, date, mType, comment, lessonComment));
                    }
                    //adding info to list
                    markedLessons.put(name,tMarklist);
                }
                Gson gson = new Gson();
                Log.d(TAG, "markedLessonsGettingFinished(size): " + markedLessons.size());
                lastStartDate = startDate;
                lastEndDate = endDate;
                //obtaining previous mark list and checking for new marks
                ArrayList<Mark> newMarks = new ArrayList<>();
                LinkedHashMap<String, ArrayList<Mark>> newMarksMap = new LinkedHashMap<>();
                if(pref.contains("lastMarks")){
                    Type listType = new TypeToken<LinkedHashMap<String, ArrayList<Mark>>>(){}.getType();
                    LinkedHashMap<String, ArrayList<Mark>> prevMarkedLessons = gson.fromJson(pref.getString("lastMarks", ""), listType);
                    //Map<String, MapDifference.ValueDifference<ArrayList<Mark>>> difference = Maps.difference(markedLessons, prevMarkedLessons).entriesDiffering();
                    for(Map.Entry<String, ArrayList<Mark>> entry: prevMarkedLessons.entrySet()){ //deleting marks with same date and value (comparator method and equals overrided to do so)
                        if(markedLessons.containsKey(entry.getKey())){
                            ArrayList<Mark> prevMarks = entry.getValue();
                            ArrayList<Mark> curMarks = markedLessons.get(entry.getKey()); //edited to remove the same marks - only new marks are left
                            for (int i = 0; i < prevMarks.size(); i++) {
                                if(curMarks.contains(prevMarks.get(i))){
                                    curMarks.remove(prevMarks.get(i));
                                }
                            }
                            newMarksMap.put(entry.getKey(), curMarks);
                        }
                    }


                }
                for(Map.Entry<String, ArrayList<Mark>> entry: newMarksMap.entrySet()){
                   StringBuilder notif = new StringBuilder();
                   if(entry.getValue().size() == 1){
                       notif = new StringBuilder(entry.getKey() + ": новая оценка - " + entry.getValue().get(0));
                       createNotification(notif.toString());
                   }
                   else if(entry.getValue().size() > 1){
                       ArrayList<Mark> entryMarkList = entry.getValue();
                       notif = new StringBuilder(entry.getKey() + ": новые оценки - ");
                       for (int i = 0; i < entryMarkList.size(); i++) {
                           notif.append(entryMarkList.get(i).mark);
                           if(i != entryMarkList.size() - 1) notif.append(',');
                       }
                   createNotification(notif.toString());
                   }
                }


                //putting last list of marks to sharedpref
                editor.putString("lastMarks", gson.toJson(markedLessons));
                editor.commit();
            }
            catch(IllegalArgumentException e){
                e.printStackTrace();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            response = Api.sendRequest("https://edu.gounn.ru/apiv3/getmarks?student=" + studentID + "&days=" + startDate + "-" + endDate + "&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, null, false);
            try {
                responseString = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

    }


}
