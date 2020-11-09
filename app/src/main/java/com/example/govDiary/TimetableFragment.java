package com.example.govDiary;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.TreeMultimap;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.NoConnectionPendingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TreeMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimetableFragment extends Fragment {
    RecyclerView recyclerView;
    HashMap<Integer, String> subjects;
    AdapterTimetable adapterTimetable;
    ACProgressFlower loading;
    private TreeMap<Integer, TreeMultimap<Integer, String>> lessonsAdapterMap = new TreeMap<>();
    ArrayList<TimetableLesson> timetableLessons;
    String weekStart, weekEnd;
    String authToken, studentID, form;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_fragment, container, false);
        timetableLessons = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loading = new ACProgressFlower.Builder(getContext())
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Загрузка...")
                .fadeColor(Color.DKGRAY).build();
        adapterTimetable = new AdapterTimetable(getContext(), lessonsAdapterMap);
        recyclerView.setAdapter(adapterTimetable);
        authToken = getActivity().getIntent().getStringExtra("authToken");
        studentID = getActivity().getIntent().getStringExtra("studentID");
        form = getActivity().getIntent().getStringExtra("form");
        //загрузка
        Calendar c1 = Calendar.getInstance();

        //first day of week
        c1.set(Calendar.DAY_OF_WEEK, 1);

        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH) + 1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);

        //last day of week
        c1.set(Calendar.DAY_OF_WEEK, 7);

        int year7 = c1.get(Calendar.YEAR);
        int month7 = c1.get(Calendar.MONTH) + 1;
        int day7 = c1.get(Calendar.DAY_OF_MONTH);
        weekStart = year1 + "" + String.format("%02d", month1) + "" + String.format("%02d", day1);
        weekEnd = year7 + "" + String.format("%02d", month7) + "" + String.format("%02d", day7);
        (new getTimetable()).execute();
        return view;
    }

    private class getTimetable extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=true&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
                String rString = response.body().string();
                //at first getting index of current period
                JSONArray periods = (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
                int periodNum = 0;
                for (int i = 0; i < periods.length(); i++) {
                    String start = periods.getJSONObject(i).getString("start");
                    String end = periods.getJSONObject(i).getString("end");
                    Date startDate = new SimpleDateFormat("yyyyMMdd").parse(start);
                    Date endDate = new SimpleDateFormat("yyyyMMdd").parse(end);
                    if(Calendar.getInstance().getTime().after(startDate) && Calendar.getInstance().getTime().before(endDate)){
                        periodNum = i;
                        break;
                    }
                }
                //getting current week
                Log.d("TimetableFragment", "doInBackground: " + (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(periodNum).toString());
                JSONArray weeks = (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods").getJSONObject(periodNum).getJSONArray("weeks");
                String currentWeekStart = "";
                String currentWeekEnd = "";
                Calendar curDateStart = Calendar.getInstance();
                String curTitle = "";
                Calendar curDateEnd = Calendar.getInstance();
                curDateStart.set(Calendar.HOUR_OF_DAY, 0);
                curDateEnd.set(Calendar.HOUR_OF_DAY, 0);
                if (response == null) throw new NoConnectionPendingException();
                for (int i = 0; i < weeks.length(); i++) {
                    String start = weeks.getJSONObject(i).getString("start");
                    String end = weeks.getJSONObject(i).getString("end");
                    String title = weeks.getJSONObject(i).getString("title");
                    Date startDate = new SimpleDateFormat("yyyyMMdd").parse(start);
                    Date endDate = new SimpleDateFormat("yyyyMMdd").parse(end);
                    Date curDate = Calendar.getInstance().getTime();
                    if(curDate.before(endDate)){
                        currentWeekEnd = end;
                        curTitle = title;
                        currentWeekStart = start;
                        Log.d("TimetableFragment", "doInBackground: chosen current date is " + currentWeekStart + "/" + currentWeekEnd);
                        curDateStart.setTime(startDate);
                        curDateEnd.setTime(endDate);
                        break;
                    }
                }
                Response responseSchedule = Api.sendRequest("https://edu.gounn.ru/apiv3/getschedule?student=" + studentID  + "&days=" + currentWeekStart + "-" + currentWeekEnd + "&class=" + form + "&rings=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
                String responseScheduleStr = responseSchedule.body().string();
                ArrayList<TimetableDays> timetableDays = new ArrayList<>();
                for(; curDateStart.compareTo(curDateEnd)<=0; curDateStart.add(Calendar.DATE, 1)) {
                    try {
                        String curDate = (new SimpleDateFormat("yyyyMMdd")).format(curDateStart.getTime());
                        Log.d("Timetable fragment", "this week day:" + curDate);
                        JSONObject day = (new JSONObject(responseScheduleStr)).getJSONObject("response").getJSONObject("result").getJSONObject("days").getJSONObject(curDate);
                        JSONArray items = day.getJSONArray("items");
                        ArrayList<TimetableLesson> lessons = new ArrayList<>();
                        String title = day.getString("title");
                        for (int i = 0; i < items.length(); i++) {
                            String lessonName = "";
                            String lessonNum = "";
                            String lessonRoom = "";
                            String teacher = "";
                            String group = "";
                            try {
                                lessonName = items.getJSONObject(i).getString("name");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            try {
                                lessonNum = items.getJSONObject(i).getString("num");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            try {
                                lessonRoom = items.getJSONObject(i).getString("room");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            try {
                                teacher = items.getJSONObject(i).getString("teacher");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            try {
                                group = items.getJSONObject(i).getString("grp");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            lessons.add(new TimetableLesson(lessonName, lessonNum, lessonRoom, teacher, group));
                        }
                        timetableDays.add(new TimetableDays(title, curTitle, lessons));
                    }
                    catch(Exception ex){
                        ex.printStackTrace();
                    }

                }
                if(responseSchedule == null) throw new ConnectException();
                responseSchedule.close();
                response.close();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }


            return null;


        }

    }
}
