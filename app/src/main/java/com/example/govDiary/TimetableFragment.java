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
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.util.ArrayList;
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
    ProgressDialog prog;
    private TreeMap<Integer, TreeMultimap<Integer,String>> lessonsAdapterMap = new TreeMap<>();
    ArrayList<TimetableLesson> timetableLessons;
    final Handler handler = new Handler();
    String IPf, id2f, classidf, student, passf, logf;
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
        logf = getActivity().getIntent().getStringExtra("login");
        passf = getActivity().getIntent().getStringExtra("password");
        //загрузка
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //((JournalActivity) getActivity()).getProgressBar().setVisibility(View.VISIBLE);
            }
        });

        getTimetable gT = new getTimetable();
        gT.execute();
        return view;
    }

    private class getTimetable extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String serverAnswer;
            String url = "http://" + IPf + "/act/GET_TIMETABLE";

            OkHttp3CookieHelper cookieHelper = new OkHttp3CookieHelper();
            cookieHelper.setCookie(url, "ys-userId", "n%3A" + id2f);
            cookieHelper.setCookie(url, "ys-user", "s%3A" + StringEscapeUtils.escapeJava(logf).replace("\\", "%"));
            cookieHelper.setCookie(url, "ys-password", "s%3A" + passf);

            OkHttpClient client =  new OkHttpClient.Builder()
                    .cookieJar(cookieHelper.cookieJar())
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                //получение расписания
                Response response = client.newCall(request).execute();
                serverAnswer = response.body().string();
                serverAnswer = "[" + serverAnswer.substring(2, serverAnswer.length() - 1) + "]";
                serverAnswer = serverAnswer.replace("\n", "");
                JSONArray timetable = new JSONArray(serverAnswer);
                int suitableLessons = 0;
                for (int i = 0; i < timetable.length(); i++) {
                    if(timetable.getJSONArray(i).getInt(1) == Integer.parseInt(classidf)) {
                        timetableLessons.add(new TimetableLesson(subjects.get(timetable.getJSONArray(i).getInt(2)), timetable.getJSONArray(i).getInt(3), timetable.getJSONArray(i).getInt(5),
                                timetable.getJSONArray(i).getInt(6), timetable.getJSONArray(i).getString(8)));
                        suitableLessons++;
                    }
                }
                for (int i = 0; i < timetableLessons.size(); i++) {
                    String roomNum = timetableLessons.get(i).roomNumber;
                    if(roomNum.equals("")) roomNum = "неизв.";
                    if(lessonsAdapterMap.containsKey(timetableLessons.get(i).weekday)){
                        if(timetableLessons.get(i).groupid == 1) lessonsAdapterMap.get(timetableLessons.get(i).weekday).put(timetableLessons.get(i).lessonNumber,"<b>" + timetableLessons.get(i).lessonNumber + " урок" + "</b>" +" - " + timetableLessons.get(i).lessonName + " (" + roomNum + ")<br>");
                        else lessonsAdapterMap.get(timetableLessons.get(i).weekday).put(timetableLessons.get(i).lessonNumber, "<b>" + timetableLessons.get(i).lessonNumber + " урок, гр. " + timetableLessons.get(i).groupid + "</b>"  + " - " + timetableLessons.get(i).lessonName + " (" + roomNum + ")<br>");
                    }
                    else{
                    TreeMultimap<Integer, String> tempMap = TreeMultimap.create();
                    if(timetableLessons.get(i).groupid == 1) tempMap.put(timetableLessons.get(i).lessonNumber,"<b>" + timetableLessons.get(i).lessonNumber + " урок" + "</b>" +" - " + timetableLessons.get(i).lessonName + " (" + roomNum + ")<br>");
                    else tempMap.put(timetableLessons.get(i).lessonNumber, "<b>" + timetableLessons.get(i).lessonNumber + " урок, гр. " + timetableLessons.get(i).groupid + "</b>"  + " - " + timetableLessons.get(i).lessonName + " (" + roomNum + ")<br>");
                    lessonsAdapterMap.put(timetableLessons.get(i).weekday, tempMap);
                    }
                }
                Log.d("TAG", "suitableLessons: " + suitableLessons);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        adapterTimetable.notifyDataSetChanged();
                    }
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //((JournalActivity) getActivity()).getProgressBar().setVisibility(View.GONE);
                }
            });


            return null;

        }
    }

}
