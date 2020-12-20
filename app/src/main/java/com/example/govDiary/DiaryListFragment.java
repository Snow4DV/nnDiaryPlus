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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DiaryListFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterDiary adapter;
    String authToken, studentID; // getting these from intent
    String lastDays = "";
    String paramDays;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean loadingFinished = false;
    ArrayList<Lesson> items;

    DatePickerDialog.OnDateSetListener onDateSetListener;
    private AsyncTask<String, Integer, Void> getDairyAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dairyFragmentView =  inflater.inflate(R.layout.diary_list, container, false);
        pref = getContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
        editor = pref.edit();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        authToken = getArguments().getString("authToken");
        studentID = getArguments().getString("studentID");
        paramDays = getArguments().getString("days");
        getDairyAsyncTask = new getDairy(getArguments().getString("days"), getArguments().getString("daysMinus7"), getArguments().getString("daysPlus7"), getArguments().getStringArrayList("daysList")).execute();
        //datePickerTimeline = dairyFragmentView.findViewById(R.id.datePickerTimeline); //TODO
        //TODO: datePickerTimeline.setActiveDate(cal);
        //datePickerTimeline.setInitialDate(2020,5,10);
        // Set a date Selected Listener
        items = new ArrayList<>();
        recyclerView = dairyFragmentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterDiary(getContext(), items);
        recyclerView.setAdapter(adapter);
        initColors();
        swipeRefreshLayout = dairyFragmentView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDairyAsyncTask = new getDairy(getArguments().getString("days"), getArguments().getString("daysMinus7"), getArguments().getString("daysPlus7"), getArguments().getStringArrayList("daysList"), true).execute();
            }
        });
        getDairyAsyncTask = new getDairy(getArguments().getString("days"), getArguments().getString("daysMinus7"), getArguments().getString("daysPlus7"), getArguments().getStringArrayList("daysList")).execute();
        swipeRefreshLayout.setRefreshing(true);
        return dairyFragmentView;
    }

    @Override
    public void onPause() {
        if(getDairyAsyncTask != null)
            getDairyAsyncTask.cancel(true);
        super.onPause();
    }

    @Override
    public void onStop() {
        if(getDairyAsyncTask != null)
            getDairyAsyncTask.cancel(true);
        super.onStop();
    }


    private class getDairy extends AsyncTask<String, Integer, Void> {
        String day, daysMin7, daysPlus7;
        Response response;
        ArrayList<String> daysL;
        JSONObject js;
        boolean ifReloading = false;
        public getDairy(String d, String dMinus7, String dPlus7, ArrayList<String> daysList) {
            super();
            daysL = daysList;
            day = d;
            daysMin7 = dMinus7;
            daysPlus7 = dPlus7;
        }
        public getDairy(String d, String dMinus7, String dPlus7, ArrayList<String> daysList, boolean ifReloading) {
            super();
            daysL = daysList;
            this.ifReloading = ifReloading;
            day = d;
            daysMin7 = dMinus7;
            daysPlus7 = dPlus7;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                if (((JournalActivity) getActivity()).daysJSON.containsKey(day) && !ifReloading) {
                    js = ((JournalActivity) getActivity()).daysJSON.get(day);
                } else {
                    response = Api.sendRequest("https://edu.gounn.ru/apiv3/getdiary?student=" + studentID + "&days=" + daysMin7 + "-" + daysPlus7 + "&rings=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);

                    if (response == null) throw new ConnectException();
                    JSONObject jsStud = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result");
                    for (String dayString : daysL) {
                        try {
                            ((JournalActivity) getActivity()).daysJSON.put(dayString, jsStud.getJSONObject("students").getJSONObject(studentID).getJSONObject("days").getJSONObject(dayString).getJSONObject("items"));
                        } catch (Exception ex) {

                        }
                    }
                    if ((response.code() == 200)) {
                        //Toast.makeText(getContext(), "В этот день уроки отсутствуют.", Toast.LENGTH_SHORT).show();
                    }
                    if (jsStud.getString("students").equals("null"))
                        throw new IllegalArgumentException();
                    js = jsStud.getJSONObject("students").getJSONObject(studentID).getJSONObject("days").getJSONObject(day).getJSONObject("items");
                }
                return null;
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                items.clear();

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
                            if(homeworkString.equals("null")) homeworkString = "";
                            String hwKey = iter2.next();

                            boolean ifIndividual = homework.getJSONObject(hwKey).getBoolean("individual");
                            if (!ifIndividual) {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + ", ";
                                Log.d(TAG, "doInBackground: got hw " + homeworkString);
                            } else {
                                homeworkString += homework.getJSONObject(hwKey).getString("value") + " - индивидуальное задание" + ", ";
                                Log.d(TAG, "doInBackground: got individual hw " + homeworkString);
                            }
                        }
                        if (!homeworkString.equals("null")) {
                            Log.d(TAG, "doInBackground: resulting hw string is " + homeworkString);
                            homeworkString = homeworkString.substring(0, homeworkString.length() - 2);
                        }
                    }
                    LinkedHashMap<String, String> filesMap = null;
                    if(js.getJSONObject(key).has("files")) {
                        filesMap = new LinkedHashMap<>();
                        JSONArray files = js.getJSONObject(key).getJSONArray("files");
                        for (int i = 0; i < files.length(); i++) {
                            filesMap.put(files.getJSONObject(i).getString("filename"), files.getJSONObject(i).getString("link"));
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
                    items.add(new Lesson(lessonName, teacher, homeworkString, Integer.parseInt(lessonNumber), markStr, filesMap));

                }

            } catch (IllegalArgumentException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: response=null");

            }
            catch (Exception e) {
                e.printStackTrace();
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

            }

            editor.apply();
            loadingFinished = true;
            lastDays = day;
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

    public static DiaryListFragment newDiaryFragmentInstance(String authToken, String studentID, String days, String daysBefore7Days, String daysAfter7Days, ArrayList<String> dates){
        Bundle bundle = new Bundle();
        bundle.putString("authToken", authToken);
        bundle.putString("studentID", studentID);
        bundle.putString("days", days);
        bundle.putStringArrayList("daysList", dates);
        bundle.putString("daysMinus7", daysBefore7Days);
        bundle.putString("daysPlus7", daysAfter7Days);
        DiaryListFragment diaryListFragment = new DiaryListFragment();
        diaryListFragment.setArguments(bundle);
        return diaryListFragment;
    }

    private void initColors() {
        if(getResources().getString(R.string.mode).equals("Night")){
            //TODO:datePickerTimeline.setMonthTextColor(getResources().getColor(R.color.primary));
            //TODO:datePickerTimeline.setDayTextColor(getResources().getColor(R.color.white));
            //TODO:datePickerTimeline.setDateTextColor(getResources().getColor(R.color.white));
        }
    }


}
