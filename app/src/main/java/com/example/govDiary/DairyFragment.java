package com.example.govDiary;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.govDiary.Api;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DairyFragment extends Fragment {
    RecyclerView recyclerView;
    Adapter adapter;
    String authToken, studentID; // getting these from intent
    String lastDays = "";
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean loadingFinished = false;
    ArrayList<Lesson> items;
    Calendar dateAndTime;
    DatePickerTimeline datePickerTimeline;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dairyFragmentView =  inflater.inflate(R.layout.dairy, container, false);
        pref = getContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        dateAndTime = Calendar.getInstance();
        onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            adapter.notifyDataSetChanged();
            new getDairy(year + "" + String.format("%02d", monthOfYear + 1) + "" + String.format("%02d", dayOfMonth) + "-" +year   + "" + String.format("%02d", monthOfYear + 1) + "" + String.format("%02d", dayOfMonth)).execute();
            datePickerTimeline.setActiveDate(dateAndTime);
            Log.d(TAG, "onCreateView: onDateSetListener: setting active date to " + dateAndTime.getTime().toString());
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
        datePickerTimeline.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                swipeRefreshLayout.setRefreshing(true);
                Log.d("TAG", "onDateSelected: " + year + "/" + month + "/" + day + "/" + dayOfWeek);
                if (!items.isEmpty())
                    items.clear(); //bugfix for inconsistency detected
                adapter.notifyDataSetChanged();
                new getDairy(year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day) + "-" + year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day)).execute();
            }

            @Override
            public void onDisabledDateSelected(int year, int month, int day, int dayOfWeek, boolean isDisabled) {
                Log.d(TAG, "onDisabledDateSelected: Disabled Date Selected, doing nothing");
            }
        });
        items = new ArrayList<>();
        recyclerView = dairyFragmentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Adapter(getContext(), items);
        recyclerView.setAdapter(adapter);
        initColors();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        swipeRefreshLayout = dairyFragmentView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new getDairy(lastDays).execute();
            }
        });
        new getDairy(year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day) + "-" + year + "" + String.format("%02d", month + 1) + "" + String.format("%02d", day)).execute();
        swipeRefreshLayout.setRefreshing(true);
        return dairyFragmentView;
    }

    public void goToDate() {
        new DatePickerDialog(getContext(), onDateSetListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }



    private class getDairy extends AsyncTask<String, Integer, Void> {
        String days;
        Response response;
        public getDairy(String d) {
            super();
            days = d;
        }
        @Override
        protected Void doInBackground(String... strings) {
            try {
                swipeRefreshLayout.setRefreshing(true);
                items.clear();
                response = Api.sendRequest("https://edu.gounn.ru/apiv3/getdiary?student=" + studentID + "&days=" + days + "&rings=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
                if(response == null) throw new ConnectException();
                JSONObject jsStud = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result");
                if(jsStud.getString("students").equals("null")) throw new IllegalArgumentException();
                JSONObject js = jsStud.getJSONObject("students").getJSONObject(studentID).getJSONObject("days").getJSONObject((days.split("-"))[0]).getJSONObject("items");
                for(Iterator<String> iter = js.keys();iter.hasNext();) {
                    String key = iter.next();
                    Log.d(TAG, "doInBackground: keys of js: " + key);
                    String lessonName = js.getJSONObject(key).getString("name");
                    String teacher = js.getJSONObject(key).getString("teacher");
                    String lessonNumber = js.getJSONObject(key).getString("num");
                    String room = js.getJSONObject(key).getString("room");
                    String homeworkString = "null";
                    if(js.getJSONObject(key).has("homework")) {
                        JSONObject homework = js.getJSONObject(key).getJSONObject("homework");
                        //iterating through hw
                        for (Iterator<String> iter2 = homework.keys(); iter2.hasNext(); ) {
                            String hwKey = iter2.next();
                            homeworkString = "";
                            boolean ifIndividual = homework.getJSONObject(hwKey).getBoolean("individual");
                            Log.d(TAG, "doInBackground: keys of hw: " + hwKey);
                            if (!ifIndividual) {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + ", ";
                            } else {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + " - индивидуальное задание" + ", ";
                            }
                        }
                        if (!homeworkString.equals("null")) {
                            homeworkString = homeworkString.substring(0, homeworkString.length() - 2);
                        }
                    }
                    //iterating through marks

                    String markStr = "null";
                    if(js.getJSONObject(key).has("assessments")) {
                        JSONArray marks = js.getJSONObject(key).getJSONArray("assessments");
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
                    items.add(new Lesson(lessonName, teacher, homeworkString, Integer.parseInt(lessonNumber), markStr));

                }

            } catch (IllegalArgumentException e){
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),"Данные за выходной день не могут быть получены.",Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
            catch (ConnectException e){
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),"Проблемы с интернет-соединением.",Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
            catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),"Произошла ошибка. Попробуйте еще раз.",Snackbar.LENGTH_SHORT).show();
                    }
                });
                Log.e("TAG", "getDiary: " + "JSON/IO ex");
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {

                    adapter.notifyDataSetChanged();
                }
            });
            //loading.dismiss();

           try {
               Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                   public void run() {
                       swipeRefreshLayout.setRefreshing(false);
                   }
               });
           }
           catch(NullPointerException e){
               e.printStackTrace();
               try {
                   Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Snackbar.make(getActivity().findViewById(android.R.id.content), "Произошла ошибка. Попробуйте еще раз.", Snackbar.LENGTH_SHORT).show();
                       }
                   });
               }
               catch(NullPointerException e2){
                   e2.printStackTrace();
               }
           }

            editor.apply();
            loadingFinished = true;
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lastDays = days;
            swipeRefreshLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();
            try {
                response.close();
            }
            catch(NullPointerException e){
                Log.d(TAG, "onPostExecute: no response");
            }
        }
    }

    public boolean ifLoadingFinished(){
        return loadingFinished;
    }

    private void initColors() {
        if(getResources().getString(R.string.mode).equals("Night")){
            datePickerTimeline.setMonthTextColor(getResources().getColor(R.color.primary));
            datePickerTimeline.setDayTextColor(getResources().getColor(R.color.white));
            datePickerTimeline.setDateTextColor(getResources().getColor(R.color.white));
        }
    }


}
