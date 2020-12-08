package com.example.govDiary;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.govDiary.Api;
import com.example.govDiary.JournalActivity;
import com.example.govDiary.Lesson;
import com.example.govDiary.R;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Implementation of App Widget functionality.
 */
public class HomeworkWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences pref = context.getSharedPreferences("widgetPrefs",Context.MODE_PRIVATE);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.homework_widget);
        Calendar curCal = Calendar.getInstance();
        String titleText = "";
        Log.d(TAG, "updateAppWidget: appwidgetid is " + appWidgetId);
        Log.d(TAG, "WidgetFactory: checking if it1 has bool " + pref.contains(appWidgetId + "td"));
        if(!pref.getBoolean( appWidgetId + "td", false)){
            curCal.add(Calendar.DATE, 1);
        }
        switch (curCal.get(Calendar.DAY_OF_WEEK)){
            case 2:
                titleText += "Пн, ";
                break;
            case 3:
                titleText+= "Вт, ";
                        break;
            case 4:
                titleText +="Ср, ";
                break;
            case 5:
                titleText+="Чт, ";
                break;
            case 6:
                titleText +="Пт, ";
                break;
            case 7:
                titleText+="Сб, ";
                break;
            case 1:
                titleText+="Вскр, ";
        }
        titleText+=curCal.get(Calendar.DAY_OF_MONTH)+"."+ (curCal.get(Calendar.MONTH) + 1);
        views.setTextViewText(R.id.appwidget_text, titleText);
        Intent serviceIntent = new Intent(context, WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.listLessons, serviceIntent);
        views.setEmptyView(R.id.listLessons, R.id.emptyTextView);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }





}

