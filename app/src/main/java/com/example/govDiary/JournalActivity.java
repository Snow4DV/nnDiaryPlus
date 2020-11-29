package com.example.govDiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;


public class JournalActivity extends AppCompatActivity { //TODO: resulting marks, timetable, maybe disable dates
    private static final int MIN_TIME_INTERVAL_BETWEEN_BACK_CLICKS = 2000;
    private long backPressedTime;
    Toolbar toolBar;
    public Context context;
    String studentFIO;
    View mTooltipView;
    Drawer drawer;
    LinkedHashMap<String, JSONObject> daysJSON;
    int runningActivity = 1;
    FragmentTransaction ft2 = null;
    FinalMarksFragment finalMarksFragment;
    TimetableFragment timetableFragment;
    FragmentManager myFragmentManager;
    MessagesTabFragment messagesTabFragment;
    TextView title;
    BottomNavigationView buttomNavigationMenu;
    final static String TAG_1 = "FRAGMENT_1";
    DairyFragment dairyFragment;
    String TAG = "JorunalActivity";
    SharedPreferences pref;

    TextView titleToolbar;
    ImageButton multibutton;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daysJSON = new LinkedHashMap<>();
        pref = getApplicationContext().getSharedPreferences("LogData", Context.MODE_PRIVATE); // saving data of application
        editor = pref.edit();
        setContentView(R.layout.activity_journal);
        buttomNavigationMenu = findViewById(R.id.bottomNavigationView);
        buttomNavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int index;
                switch(item.getItemId()){
                    case R.id.homemenu:
                        index = 1;
                        break;
                    case R.id.diarymenu:
                        index = 1;
                        break;
                    case R.id.timetablemenu:
                        index = 2;
                        break;
                    case R.id.fmarksmenu:
                        index = 3;
                        break;
                    case R.id.messagesmenu:
                        index = 4;
                        break;
                    default:
                        index = 1;
                        break;
                }
                drawer.setSelection(index, false);
                materialDrawerItemClickHolder(index);
                return true;
            }
        });
                context = getApplicationContext();
        multibutton = findViewById(R.id.multibutton);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        myFragmentManager = getSupportFragmentManager();
        dairyFragment = new DairyFragment();
        finalMarksFragment = new FinalMarksFragment();
        toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        toolBar.setBackground(getResources().getDrawable(R.color.colorToolbar));
        titleToolbar = findViewById(R.id.titleToolbar);
        //fio in toolbar
        studentFIO = pref.getString("name", "Ошибка");
        title = findViewById(R.id.title);
        Log.d(TAG, "StudentName: " + studentFIO);
        titleToolbar.setText("Дневник");
        //Getting the drawable icon with letter inside
        ColorGenerator generator = ColorGenerator.MATERIAL;
        TextDrawable drawable = TextDrawable.builder()
                .buildRect(String.valueOf((char)studentFIO.split(" ")[1].charAt(0)), generator.getColor((char)studentFIO.split(" ")[1].charAt(0)));

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.gradient)
                .addProfiles(
                        new ProfileDrawerItem().withName(studentFIO).withEmail(pref.getString("schoolName", "Учебное заведение")).withIcon(drawable)
                )
                .withSelectionListEnabledForSingleProfile(false)
                .build();
        //creating drawer
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Дневник").withIconTintingEnabled(true).withIcon(getDrawable(R.drawable.ic_dairy));
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Расписание").withIconTintingEnabled(true).withIcon(getDrawable(R.drawable.ic_timetable));
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Итоговые оценки").withIconTintingEnabled(true).withIcon(getDrawable(R.drawable.ic_marks));
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("Настройки").withIconTintingEnabled(true).withSelectable(false).withIcon(getDrawable(R.drawable.ic_settings));
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName("Выход из аккаунта").withIconTintingEnabled(true).withSelectable(false).withIcon(getDrawable(R.drawable.ic_logout));
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(4).withName("Сообщения").withIconTintingEnabled(true).withIcon(getDrawable(R.drawable.ic_baseline_message_24));
        drawer = new DrawerBuilder().withActivity(this).withToolbar(toolBar).withAccountHeader(headerResult).addDrawerItems(
                item1,
                item2,
                item3,
                item6,
                new DividerDrawerItem(),
                item4,
                item5
        ) .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch(position){
                    case 1:
                        buttomNavigationMenu.setSelectedItemId(R.id.diarymenu);
                        break;
                    case 2:
                        buttomNavigationMenu.setSelectedItemId(R.id.timetablemenu);
                        break;
                    case 3:
                        buttomNavigationMenu.setSelectedItemId(R.id.fmarksmenu);
                        break;
                    case 4:
                        buttomNavigationMenu.setSelectedItemId(R.id.messagesmenu);
                        break;
                }
               return materialDrawerItemClickHolder(position);
            }
        })
                .withSavedInstance(savedInstanceState)
                .build();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            ft.remove(fragment);
        }
        ft.add(R.id.dairy_fragment, dairyFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.detach(dairyFragment).attach(dairyFragment);
        ft.commit();
        //Тест сервиса
        //PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MarksCheckService.class, 15, TimeUnit.MINUTES).build();
        //WorkManager.getInstance().enqueue(periodicWorkRequest);
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(MarksCheckService.class).build();
        WorkManager.getInstance().enqueue(oneTimeWorkRequest);


        //предупреждение о сырости ПО
        if(!pref.contains("notFirstRun")) {
            AlertDialog alertDialog = new AlertDialog.Builder(JournalActivity.this).create();
            alertDialog.setTitle("Привет!");
            alertDialog.setMessage("Благодарю за загрузку моего ПО. Это одна из первых версий программы, поэтому в ней могут содержатся различные баги и недочеты. Я стараюсь по-максимуму совершенствовать эту прог");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            editor.putString("notFirstRun", "notFirstRun");
                            editor.commit();
                        }
                    });
            alertDialog.show();
        }
        //добавляем переодическую задачу проверки оценок через WorkManager
        if(!pref.contains("notificationServiceAdded")){
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MarksCheckService.class, 15, TimeUnit.MINUTES).addTag("NOTIFJOBAVERSMARKS").build();
            WorkManager.getInstance().enqueueUniquePeriodicWork("notificationJobMarksAvers", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
            editor.putString("notificationServiceAdded", "notificationServiceAdded");
            editor.putBoolean("notifService", true);
            editor.apply();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusbarColor));
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


    public void calendarButton(View view) {
        if(runningActivity == 1){
            dairyFragment.goToDate();
        }
        else if(runningActivity == 3){
            AlertDialog alertDialog = new AlertDialog.Builder(JournalActivity.this).create();
            alertDialog.setTitle("Информация");
            alertDialog.setIcon(R.drawable.ic_info_dark);
            alertDialog.setMessage("Цвет оценки зависит от ее значимости. Зеленая оценка - высокая значимость, желтая - средняя значимость, голубая - низкая значимость, а красная не участвует в подсчете среднего балла.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Хорошо",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else if(runningActivity == 4){
            messagesTabFragment.refreshMessages();
        }
    }


    @Override
    protected void onResume() {
        drawer.setSelection(runningActivity,false);
        Log.d(TAG, "onResume: setting selected pos on drawer to " + runningActivity);
        super.onResume();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mTooltipView != null) {
            ViewGroup parent = (ViewGroup) mTooltipView.getParent();
            if (parent != null) {
                parent.removeView(mTooltipView);
                mTooltipView = null;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setMTooltipView(View mTooltipView) {
        this.mTooltipView = mTooltipView;
    }

    private boolean materialDrawerItemClickHolder(int position){

        Log.d(TAG, "materialDrawerItemClickHolder: fired");
            ft2 = getSupportFragmentManager().beginTransaction();
            if(position == 1 || position == 2 || position == 3 || position == 4) {
                runningActivity = position;
                if (position == 1) {
                    multibutton.setVisibility(View.VISIBLE);
                    multibutton.setImageResource(R.drawable.ic_calendar);
                } else if (position == 3) {
                    multibutton.setVisibility(View.VISIBLE);
                    multibutton.setImageResource((R.drawable.ic_info));
                }
                else if(position == 4){
                    multibutton.setVisibility(View.VISIBLE);
                    multibutton.setImageResource((R.drawable.ic_baseline_refresh_24));
                }
                else multibutton.setVisibility(View.GONE);
            }
            //Toast.makeText(JournalActivity.this, position + "d", Toast.LENGTH_SHORT).show();
            switch(position){
                case 1:
                    titleToolbar.setText("Дневник");
                    if(dairyFragment == null)
                    dairyFragment = new DairyFragment();
                    if(!dairyFragment.isAdded()) {
                        ft2.replace(R.id.dairy_fragment, dairyFragment);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        ft2.commit();
                    }
                    return true;
                case 3:
                    titleToolbar.setText("Итоговые оценки");
                    //if(finalMarksFragment == null)
                    finalMarksFragment = new FinalMarksFragment();
                    if(!finalMarksFragment.isAdded()) {
                        ft2.replace(R.id.dairy_fragment, finalMarksFragment);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        ft2.commit();
                    }
                    return true;
                case 7:
                    editor.putString("loginStatus", "fail");
                    editor.apply();
                    Intent intent = new Intent(JournalActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    titleToolbar.setText("Расписание");
                    if(timetableFragment == null)
                    timetableFragment = new TimetableFragment();
                    if(!timetableFragment.isAdded()) {
                        ft2.replace(R.id.dairy_fragment, timetableFragment);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        ft2.commit();
                    }
                    break;
                case 6:
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft3.remove(prev);
                    }
                    ft3.addToBackStack(null);
                    SettingsFragment sF = new SettingsFragment();
                    //sF.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog); causes dialog not to show. idk why
                    sF.show(ft3, "dialog");
                    break;
                case 4:
                        messagesTabFragment = new MessagesTabFragment();
                    titleToolbar.setText("Сообщения");
                    if(!messagesTabFragment.isAdded()){
                        ft2.replace(R.id.dairy_fragment, messagesTabFragment);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                        ft2.commit();
                    }
                    break;
                default:
                    Toast.makeText(JournalActivity.this, "Функция в разработке или произошла ошибка", Toast.LENGTH_SHORT).show();
                    throw new NullPointerException();

            }
            return true;
    }
}

