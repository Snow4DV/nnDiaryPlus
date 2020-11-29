package com.example.govDiary;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.govDiary.Api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.vivekkaushik.datepicker.DatePickerTimeline;
import com.vivekkaushik.datepicker.OnDateSelectedListener;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.channels.NoConnectionPendingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_DAY;

public class DairyFragment extends Fragment {
    String authToken, studentID; // getting these from intent
    String lastDays = "";
    Date yearStartDate, yearEndDate;
    private Date initDate;
    Context context;
    ProgressBar progressBar;
    Boolean periodsLoaded = false;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    DiaryPagerAdapter diaryPagerAdapter;
    ViewPager2 viewPager;
    boolean loadingFinished = false;
    ArrayList<Lesson> items;

    Calendar dateAndTime;
    DatePickerTimeline datePickerTimeline;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    private AsyncTask<String, Integer, Void> dairyAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO: if periods couldn't be got make swiperefreshlistener active
        View dairyFragmentView =  inflater.inflate(R.layout.dairy, container, false);
        context = getContext();
        progressBar = dairyFragmentView.findViewById(R.id.progress_circular);
        viewPager = dairyFragmentView.findViewById(R.id.viewPager);
        pref = getContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        dateAndTime = Calendar.getInstance();
        onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            if(periodsLoaded) {
                dateAndTime.set(Calendar.YEAR, year);
                dateAndTime.set(Calendar.MONTH, monthOfYear);
                dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                //adapter.notifyDataSetChanged();
                //TODO:new getDairy(year + "" + String.format("%02d", monthOfYear + 1) + "" + String.format("%02d", dayOfMonth) + "-" +year   + "" + String.format("%02d", monthOfYear + 1) + "" + String.format("%02d", dayOfMonth)).execute();
                datePickerTimeline.setActiveDate(dateAndTime);
                Log.d(TAG, "onCreateView: onDateSetListener: setting active date to " + dateAndTime.getTime().toString());
                Calendar newCal = Calendar.getInstance();
                newCal.set(year,monthOfYear,dayOfMonth, 0, 0);
                Calendar initCal = Calendar.getInstance();
                initCal.setTime(initDate);
                initCal.set(Calendar.HOUR, 0);
                initCal.set(Calendar.MINUTE, 0);
                initCal.set(Calendar.MILLISECOND, 0);
                //Toast.makeText(getContext(), "selected: " + ((newCal.getTimeInMillis() - initCal.getTimeInMillis())/MILLIS_PER_DAY), Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem((int) ((newCal.getTimeInMillis() - initCal.getTimeInMillis())/MILLIS_PER_DAY));
            }
        };
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        authToken = getActivity().getIntent().getStringExtra("authToken");
        studentID = getActivity().getIntent().getStringExtra("studentID");
        datePickerTimeline = dairyFragmentView.findViewById(R.id.datePickerTimeline);
        Calendar cal = Calendar.getInstance();
        datePickerTimeline.setActiveDate(cal);
        //datePickerTimeline.setInitialDate(2020,5,10);
        // Set a date Selected Listener
        //TODO: Check if periods loaded!!!

        items = new ArrayList<>();
        initColors();

        //TODO:new getDairy(year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day) + "-" + year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day)).execute();
        dairyAsyncTask = new getPeriods(authToken).execute();
        return dairyFragmentView;
    }

    public void goToDate() {
    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onDateSetListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH));
    try {
        datePickerDialog.getDatePicker().setMinDate(initDate.getTime());
        datePickerDialog.getDatePicker().setMaxDate(yearEndDate.getTime());
        datePickerDialog.show();
    }
    catch (NullPointerException e) {
        Toast.makeText(getContext(), "Подождите завершения загрузки", Toast.LENGTH_SHORT).show();
    }
    }

    @Override
    public void onPause() {
        if(dairyAsyncTask != null)
        dairyAsyncTask.cancel(true);
        super.onPause();
    }

    public void setDateToDatePicker(Calendar date){
        Log.d(TAG, "setDateToDatePicker: got " + date.getTime().toString());
        datePickerTimeline.setActiveDate(date);
    }

    private void initColors() {
        if(getResources().getString(R.string.mode).equals("Night")){
            datePickerTimeline.setMonthTextColor(getResources().getColor(R.color.primary));
            datePickerTimeline.setDayTextColor(getResources().getColor(R.color.white));
            datePickerTimeline.setDateTextColor(getResources().getColor(R.color.white));
        }
    }

    private class getPeriods extends AsyncTask<String, Integer, Void> {
        String authT;
        getPeriods(String auth){
            authT = auth;
        }
        @Override
        protected Void doInBackground(String... strings) {
            Boolean gotData=false;
            while(!gotData) {
                try {

                    Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=false&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authT + "&vendor=edu", null, getContext(), false);
                    if (response == null) throw new NoConnectionPendingException();
                    JSONArray periods = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
                    String yearStart = "", yearEnd = "";
                    for (int i = 0; i < periods.length(); i++) {
                        String startDate = periods.getJSONObject(i).getString("start");
                        String endDate = periods.getJSONObject(i).getString("end");
                        if (i == periods.length() - 1) {
                            yearEnd = endDate;
                        } else if (i == 0) {
                            yearStart = startDate;
                        }

                    }
                    yearStartDate = new SimpleDateFormat("yyyyMMdd").parse(yearStart);
                    yearEndDate = new SimpleDateFormat("yyyyMMdd").parse(yearEnd);
                    Log.d(TAG, "doInBackground: got last day: " + yearEndDate.toString() + "and first date " + yearStartDate.toString());
                    int days = (int) TimeUnit.DAYS.convert(yearEndDate.getTime() - yearStartDate.getTime(), TimeUnit.MILLISECONDS);
                    int currentDay = (int) TimeUnit.DAYS.convert(Calendar.getInstance().getTime().getTime() - yearStartDate.getTime(), TimeUnit.MILLISECONDS);
                    periodsLoaded = true;
                    if(getActivity() == null) {
                        this.cancel(true);
                        break;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                    diaryPagerAdapter = new DiaryPagerAdapter(((AppCompatActivity)context).getSupportFragmentManager(), getLifecycle());
                    diaryPagerAdapter.setTokenAndId(authToken, studentID);
                    Calendar initCal = Calendar.getInstance();
                    initDate = yearStartDate;
                    initCal.setTime(yearStartDate);
                    diaryPagerAdapter.setInitDateAndDaysAmount(initCal, days + 1);

                    ((AppCompatActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            datePickerTimeline.setDatesAmount(days + 1);
                            viewPager.setAdapter(diaryPagerAdapter);
                            viewPager.setCurrentItem(currentDay, false);
                            viewPager.setOffscreenPageLimit(3);
                           viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                               @Override
                               public void onPageSelected(int position) {
                                   //There are no pointers in java idk why did it worked in other way so i did this. Sorry:
                                   Calendar tCal = Calendar.getInstance();
                                   tCal.setTime(initDate);
                                   tCal.add(Calendar.DATE, position);
                                   Log.d(TAG, "onPageSelected: " + "date on screen is " + tCal.getTime().toString() + ",init cal is " + yearStartDate.toString());
                                   setDateToDatePicker(tCal);
                                   super.onPageSelected(position);
                               }
                           });
                            Calendar initCalc = Calendar.getInstance();
                            initCalc.setTime(initDate);
                            datePickerTimeline.setInitialDate(initCalc.get(Calendar.YEAR), initCalc.get(Calendar.MONTH), initCalc.get(Calendar.DAY_OF_MONTH));

                            Log.d(TAG, "run: curDay:" + currentDay + ",days:" + days);
                            datePickerTimeline.setOnDateSelectedListener(new OnDateSelectedListener() {
                                @Override
                                public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                                    if (periodsLoaded) {
                                        Log.d("TAG", "onDateSelected: " + year + "/" + month + "/" + day + "/" + dayOfWeek);
                                        if (!items.isEmpty())
                                            items.clear(); //bugfix for inconsistency detected
                                        Calendar newCal = Calendar.getInstance();
                                        newCal.set(year, month, day, 0, 0);
                                        Calendar initCal = Calendar.getInstance();
                                        initCal.setTime(initDate);
                                        initCal.set(Calendar.HOUR, 0);
                                        initCal.set(Calendar.MINUTE, 0);
                                        initCal.set(Calendar.MILLISECOND, 0);
                                        //Toast.makeText(getContext(), "selected: " + ((newCal.getTimeInMillis() - initCal.getTimeInMillis())/MILLIS_PER_DAY), Toast.LENGTH_SHORT).show();
                                        viewPager.setCurrentItem((int) ((newCal.getTimeInMillis() - initCal.getTimeInMillis()) / MILLIS_PER_DAY));
                                    } else {
                                        Toast.makeText(getContext(), "Подождите, пока завершится загрузка", Toast.LENGTH_SHORT).show();
                                        Calendar cal = Calendar.getInstance();
                                        setDateToDatePicker(cal);
                                        //TODO: back to prev date
                                    }
                                }

                                @Override
                                public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {
                                    Log.d(TAG, "onDisabledDateSelected: Disabled Date Selected, doing nothing");
                                }
                            });

                        }
                    });
                     gotData = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                        Toast.makeText(getContext(), "Произошла ошибка при загрузке данных. Повторите позже.", Toast.LENGTH_SHORT).show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
            return null;

        }


    }


}
