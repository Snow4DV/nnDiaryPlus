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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.Objects;
import java.util.TreeMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimetableFragment extends Fragment {
    private static final String TAG = "TimetableFragment";
    RecyclerView recyclerView;
    AdapterTimetable adapterTimetable;
    Response responseSchedule;
    Calendar curDateStart, curDateEnd;
    String curTitle;
    SwipeRefreshLayout swipeRefreshLayout;
    ACProgressFlower loading;
    ArrayList<TimetableDays> timetableDays;
    String weekStart, weekEnd;
    String authToken, studentID, form;
    private AsyncTask<String, Integer, Void> getTimetableAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_fragment, container, false);
        timetableDays = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTimetableAsyncTask = new getTimetable().execute();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loading = new ACProgressFlower.Builder(getContext())
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Загрузка...")
                .fadeColor(Color.DKGRAY).build();
        adapterTimetable = new AdapterTimetable(getActivity(), timetableDays);
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
        getTimetableAsyncTask = new getTimetable().execute();
        return view;
    }

    private class getTimetable extends AsyncTask<String, Integer, Void> {
        String responseScheduleStr;
        @Override
        protected void onPostExecute(Void aVoid) {
            try {

                timetableDays.clear();
                for (; curDateStart.compareTo(curDateEnd) <= 0; curDateStart.add(Calendar.DATE, 1)) {
                    try {
                        String curDate = (new SimpleDateFormat("yyyyMMdd")).format(curDateStart.getTime());
                        Log.d("Timetable fragment", "this week day:" + curDate);
                        JSONObject day = (new JSONObject(responseScheduleStr)).getJSONObject("response").getJSONObject("result").getJSONObject("days").getJSONObject(curDate);
                        JSONArray items = day.getJSONArray("items");
                        ArrayList<TimetableLesson> lessons = new ArrayList<>();
                        String title = day.getString("title"); //Название дня
                        ArrayList<String> nonRepeatingNumsList = new ArrayList<>();

                        for (int i = 0; i < items.length(); i++) {
                            boolean repeatingNum = false;
                            String lessonName = "";
                            String lessonNum = "";
                            String lessonRoom = "";
                            String teacher = "";
                            String group = "";
                            try {
                                lessonName = items.getJSONObject(i).getString("name");
                            } catch (Exception ex) {
                                Log.d("TimetableFragmentThread", "name - skipped(" + i + ")");
                            }
                            try {
                                lessonNum = items.getJSONObject(i).getString("num");
                                if (nonRepeatingNumsList.contains(lessonNum)) {
                                    repeatingNum = true;
                                } else {
                                    nonRepeatingNumsList.add(lessonNum);
                                }

                            } catch (Exception ex) {
                                Log.d("TimetableFragmentThread", "num - skipped(" + i + ")");
                            }
                            try {
                                lessonRoom = items.getJSONObject(i).getString("room");
                            } catch (Exception ex) {
                                Log.d("TimetableFragmentThread", "room - skipped(" + i + ")");
                            }
                            try {
                                teacher = items.getJSONObject(i).getString("teacher");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Log.d("TimetableFragmentThread", "teacherMissing - skipped(" + i + ")");
                            }
                            try {
                                group = items.getJSONObject(i).getString("grp_short");
                            } catch (Exception ex) {
                                Log.d("TimetableFragmentThread", "groupMissing - skipped(" + i + ")");
                            }
                            lessons.add(new TimetableLesson(lessonName, lessonNum, lessonRoom, teacher, group, repeatingNum));
                        }
                        timetableDays.add(new TimetableDays(title, curTitle, lessons));
                        Log.d("TimetableFragmentThread", "added TimetableDay elements: " + timetableDays.size());

                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }

                }
                Log.d("TimetableFragmentThread", "notifying that the timetableDays is : " + timetableDays.size());
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.getRecycledViewPool().clear();
                        adapterTimetable.notifyDataSetChanged();
                    }
                });
                if (responseSchedule == null) throw new ConnectException();

                responseSchedule.close();

                swipeRefreshLayout.setRefreshing(false);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=true&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
                String rString = response.body().string();
                response.close();
                //at first getting index of current period
                JSONArray periods = (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
                int periodNum = 0;
                for (int i = 0; i < periods.length(); i++) {
                    String start = periods.getJSONObject(i).getString("start");
                    String end = periods.getJSONObject(i).getString("end");
                    Date startDate = new SimpleDateFormat("yyyyMMdd").parse(start);
                    Date endDate = new SimpleDateFormat("yyyyMMdd").parse(end);
                    if(Calendar.getInstance().getTime().after(startDate) && Calendar.getInstance().getTime().before(endDate)){  //TODO: IF NOT FOUND- CHOOSE THE CLOSEST DATE!!!!!!!!!!
                        periodNum = i;
                        break;
                    }
                }
                //getting current week
                Log.d("TimetableFragment", "doInBackground: " + (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(periodNum).toString());
                JSONArray weeks = (new JSONObject(rString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods").getJSONObject(periodNum).getJSONArray("weeks");
                String currentWeekStart = "";
                String currentWeekEnd = "";
                curDateStart = Calendar.getInstance();
                curTitle = "";
                curDateEnd = Calendar.getInstance();
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
                responseSchedule = Api.sendRequest("https://edu.gounn.ru/apiv3/getschedule?student=" + studentID  + "&days=" + currentWeekStart + "-" + currentWeekEnd + "&class=" + form + "&rings=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
                responseScheduleStr = responseSchedule.body().string();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }


            return null;


        }

    }

    @Override
    public void onPause() {
        if(getTimetableAsyncTask != null)
        getTimetableAsyncTask.cancel(true);
        super.onPause();
    }
}
