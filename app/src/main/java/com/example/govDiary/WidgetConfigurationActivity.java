package com.example.govDiary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class WidgetConfigurationActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Button buttonToday, buttonTomorrow;
    boolean todayClicked = false;
    private CheckBox checkBoxHW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences logPrefs = getSharedPreferences("LogData", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusbarColor));
        }
        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if(extras != null){
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
        if(!logPrefs.getString("loginStatus", "err").equals("success")) {
            Toast.makeText(this, "Для начала выполните вход в приложении электронного дневника и активируйте автоматический вход", Toast.LENGTH_SHORT).show();
            finish();
        }
        buttonToday = findViewById(R.id.buttonToday);
        buttonTomorrow = findViewById(R.id.buttonTomorrow);
        checkBoxHW = findViewById(R.id.checkBoxHW);
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todayClicked = true;
                configConfirmation();
            }
        });
        buttonTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configConfirmation();
            }
        });
    }


    private void configConfirmation(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("widgetPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(appWidgetId + "td", todayClicked);
        editor.putBoolean(appWidgetId + "hw", checkBoxHW.isChecked());
        editor.commit();
        Log.d("WIDGET_ELDIARY", "configConfirmation:appwidgetid put todayclicked " + todayClicked + " to " + appWidgetId);
        Log.d("WIDGET_ELDIARY", "WidgetFactory: checking if it3 has bool " + prefs.contains(appWidgetId + "td"));
        setResult(RESULT_OK, new Intent());
        RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.homework_widget);
        Intent serviceIntent = new Intent(getApplicationContext(), WidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.listLessons, serviceIntent);
        views.setEmptyView(R.id.listLessons, R.id.emptyTextView);
        // Instruct the widget manager to update the widget
        Calendar curCal = Calendar.getInstance(Locale.FRENCH);
        String titleText = "";
        Log.d(TAG, "updateAppWidget: appwidgetid is " + appWidgetId);
        Log.d(TAG, "WidgetFactory: checking if it1 has bool " + prefs.contains(appWidgetId + "td"));
        if(!prefs.getBoolean( appWidgetId + "td", false)){  //First update should be done after the configration activity finishes. That updates it correctly for the first time
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
        SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm");
        SimpleDateFormat dfDayMonth = new SimpleDateFormat("dd.MM");
        titleText+=dfDayMonth.format(curCal.getTime());
        views.setTextViewText(R.id.update_time, "Обновлено в " + dfTime.format(curCal.getTime()));
        views.setTextViewText(R.id.appwidget_text, titleText);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listLessons);
        finish();
    }



}