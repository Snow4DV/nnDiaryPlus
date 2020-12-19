package com.example.govDiary;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetFactory(getApplicationContext(),intent);
    }

    class WidgetFactory implements RemoteViewsFactory{
        private Context context;
        private int appWidgetID;
        private ArrayList<Lesson> items = new ArrayList<>();
        SharedPreferences pref;
        SharedPreferences prefWidget;
        String dateString;
        String studentID;
        String TAG = "WIDGET_ELDIARY";
        String authToken;

        public WidgetFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            pref = context.getSharedPreferences("LogData",Context.MODE_PRIVATE);
            prefWidget = context.getSharedPreferences("widgetPrefs",Context.MODE_PRIVATE);
            Calendar curCalendar = Calendar.getInstance(Locale.FRANCE);
            Log.d(TAG, "WidgetFactory: appwidgetid:" + appWidgetID);
            Log.d(TAG, "WidgetFactory: checking if it2 has bool " + prefWidget.contains(appWidgetID + "td"));
            if(!prefWidget.getBoolean( (appWidgetID) + "td", false)){
                Log.d(TAG, "WidgetFactory: tomorrow is chosen. increasing the date. appwidgetid:" + appWidgetID);
                curCalendar.add(Calendar.DATE, 1);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            dateString = simpleDateFormat.format(curCalendar.getTime());
            studentID = pref.getString("studentID", "");
            authToken = pref.getString("authToken", "");
    }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            if(!pref.getString("loginStatus", "d").equals("success")) {
                items.clear();
                return;
            }
            ArrayList<Lesson> itemsBackup = items;
            Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getdiary?student=" + studentID + "&days=" + dateString + "-" + dateString + "&rings=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu&unkn=aa", null, context, true);
            String responseString = "";
            boolean doneSuccessfully = false;
            try {
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                items.clear();
                if (response == null) throw new ConnectException();
                JSONObject jsStud = (new JSONObject((responseString))).getJSONObject("response").getJSONObject("result").getJSONObject("students").getJSONObject(studentID).getJSONObject("days").getJSONObject(dateString).getJSONObject("items");
                Iterator<String> keys = jsStud.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    try{
                        String.valueOf(k);
                    }
                    catch(Exception ex){
                        continue;
                    }
                    JSONObject js = jsStud.getJSONObject(k);
                    String key = dateString;
                    Log.d(TAG, "doInBackground: keys of js: " + key);
                    String lessonName = js.getString("name");
                    String teacher = js.getString("teacher");
                    String lessonNumber = js.getString("num");
                    String homeworkString = "null";
                    if (js.has("homework")) {
                        JSONObject homework = js.getJSONObject("homework");
                        //iterating through hw

                        for (Iterator<String> iter2 = homework.keys(); iter2.hasNext(); ) {
                            if (homeworkString.equals("null")) homeworkString = "";
                            String hwKey = iter2.next();

                            boolean ifIndividual = homework.getJSONObject(hwKey).getBoolean("individual");
                            if (!ifIndividual) {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + ", ";
                                Log.d(TAG, "doInBackground: got hw " + homeworkString);
                            } else {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + " - индивидуальное задание" + ", ";
                                Log.d(TAG, "doInBackground: got individual hw " + homeworkString);
                            }
                        }
                        if (!homeworkString.equals("null")) {
                            Log.d(TAG, "doInBackground: resulting hw string is " + homeworkString);
                            homeworkString = homeworkString.substring(0, homeworkString.length() - 2);
                        }
                    }
                    LinkedHashMap<String, String> filesMap = null;
                    if (js.has("files")) {
                        filesMap = new LinkedHashMap<>();
                        JSONArray files = js.getJSONArray("files");
                        for (int i = 0; i < files.length(); i++) {
                            filesMap.put(files.getJSONObject(i).getString("filename"), files.getJSONObject(i).getString("link"));
                        }
                    }
                    //iterating through marks

                    String markStr = "null";
                    if (js.has("assessments")) {
                        JSONArray marks = js.getJSONArray("assessments");
                        for (int i = 0; i < marks.length(); i++) {
                            String color = null;
                            String controlType = null;
                            int weight;
                            if (marks.getJSONObject(i).has("color"))
                                color = marks.getJSONObject(i).getString("color");
                            if (marks.getJSONObject(i).has("control_type"))
                                controlType = marks.getJSONObject(i).getString("control_type");
                            if (marks.getJSONObject(i).has("weight"))
                                weight = marks.getJSONObject(i).getInt("weight");
                            String comment = marks.getJSONObject(i).getString("comment");
                            String value = marks.getJSONObject(i).getString("value");
                            markStr = "";
                            if (color != null) {
                                markStr += "<font color=" + color + ">" + value + "</font>, ";
                            } else {
                                markStr += value + ", ";
                            }
                        }
                        if (!markStr.equals("null")) {
                            markStr = markStr.substring(0, markStr.length() - 2);
                        }
                    }

                    //adding to items
                    items.add(new Lesson(lessonName, teacher, homeworkString, Integer.parseInt(lessonNumber), markStr, filesMap));
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, HomeworkWidget.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lessonsLayout);
                    Log.d("WIDGET_ELDIARY", "fetchHWitemslength: " + items.size());
                }
                return;
            }
            catch (Exception e) {
                Log.e("WIDGET_ELDIARY", "fetchHwERROR: ", e);
                Log.e("WIDGET_ELDIARY", "fetchHwERROR: " + responseString);
                Log.e("TAG", "getDiary: " + "JSON/IO ex");
                items = itemsBackup;
                return;
            }

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            Log.d(TAG, "getCount: returning " + items.size());
            return items.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_lesson);
            views.setTextViewText(R.id.lessonName, items.get(i).number + ". " + items.get(i).name);
            if(items.get(i).files.size() != 0) {
                views.setViewVisibility(R.id.fileAttached, View.VISIBLE);
            }
            else{
                views.setViewVisibility(R.id.fileAttached, View.GONE);
            }
            if(items.get(i).homework.equals("null") || !prefWidget.getBoolean( (appWidgetID) + "hw", true)){
                views.setViewVisibility(R.id.lessonHomeWork, View.GONE);

            }
            else{
                views.setViewVisibility(R.id.lessonHomeWork, View.VISIBLE);
                views.setTextViewText(R.id.lessonHomeWork, items.get(i).homework);
            }

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            //return new RemoteViews(context.getPackageName(), R.layout.widget_loading);
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }





    }
}
