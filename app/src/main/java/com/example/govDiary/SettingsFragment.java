package com.example.govDiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class SettingsFragment extends DialogFragment {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Switch autoLoginSwitch, notificationSwitch;
    final String TAG = "SettingsFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View settingsView = inflater.inflate(R.layout.settings_fragment, container, false);
        getDialog().setTitle("Настройки");
        autoLoginSwitch = settingsView.findViewById(R.id.switchAutologin);
        notificationSwitch = settingsView.findViewById(R.id.switchNotification);
        pref = getContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        //autoLoginSwitch.setChecked(pref.getBoolean("autoLogin", true));
        if(pref.getString("loginStatus", "").equals("success")){
            autoLoginSwitch.setChecked(true);
        }
        else{
            autoLoginSwitch.setChecked(false);
        }
        notificationSwitch.setChecked(pref.getBoolean("notification", true));
        autoLoginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putString("loginStatus", "success");
                    editor.apply();
                    Log.d(TAG, "onCheckedChangedAL: " + isChecked);
                }
                else{
                    Log.d(TAG, "onCheckedChangedAL: " + isChecked);
                    editor.putString("loginStatus", "fail");
                    editor.apply();
                }
            }
        });
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MarksCheckService.class, 15, TimeUnit.MINUTES).addTag("NOTIFJOBAVERSMARKS").build();
                    WorkManager.getInstance().enqueueUniquePeriodicWork("notificationJobMarksAvers", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
                    editor.putBoolean("notifService", true);
                    editor.apply();
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                }
                else{
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                    editor.putBoolean("notifService", false);
                    editor.apply();
                    WorkManager.getInstance().cancelAllWorkByTag("NOTIFJOBAVERSMARKS");
                }
            }
        });
        return settingsView;
    }
}
