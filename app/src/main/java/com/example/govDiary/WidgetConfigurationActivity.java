package com.example.govDiary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.Toast;

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
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listLessons);
        finish();
    }



}