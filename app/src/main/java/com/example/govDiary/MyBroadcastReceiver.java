package com.example.govDiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyBroadcastReceiver extends BroadcastReceiver {
    SharedPreferences pref;

    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("LogData", Context.MODE_PRIVATE);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && pref.getBoolean("notifService", false)) {
            WorkManager.getInstance().cancelAllWorkByTag("NOTIFJOBAVERSMARKS");
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MarksCheckService.class, 15, TimeUnit.MINUTES).addTag("NOTIFJOBAVERSMARKS").build();
            WorkManager.getInstance().enqueueUniquePeriodicWork("notificationJobMarksAvers", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
            Log.d("AversBootReceiver", "onReceive: periodicWorkAdded");
        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d("AversBootReceiver", "onReceive: periodicWork WASNT added");
        }
    }
}