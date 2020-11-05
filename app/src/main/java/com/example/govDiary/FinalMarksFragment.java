package com.example.govDiary;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.NoConnectionPendingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FinalMarksFragment extends Fragment implements AdapterPeriods.OnClickListener {
    private  ArrayList<MarkedLesson> markedLessons;
    RecyclerView recyclerView;
    String authToken, studentID = "errGettingTokenOrStudentID";
    AdapterFinalMarks adapterFinalMarks;
    View timetableFragmentView;
    ArrayList<Period> periodList;
    String TAG = "FinalMarksFragment";
    //для периодов
    RecyclerView recyclerViewPeriods;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    AdapterPeriods adapterP;
    LinearLayoutManager HorizontalLayout;
    //TODO: при рефреше - если periodNamesList пустой, то получаем его заново, иначе просто получаем заново оценки

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View timetableFragmentView =  inflater.inflate(R.layout.finalmarks_fragment, container, false);
        periodList = new ArrayList<>();
        markedLessons = new ArrayList<>();
        authToken = getActivity().getIntent().getStringExtra("authToken");
        studentID = getActivity().getIntent().getStringExtra("studentID");
        recyclerView = timetableFragmentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // initialisation with id's
        recyclerViewPeriods  = (RecyclerView) timetableFragmentView.findViewById(R.id.recyclerview);
        RecyclerViewLayoutManager
                = new LinearLayoutManager(
                getContext());
        recyclerViewPeriods.setLayoutManager(
                RecyclerViewLayoutManager);
        adapterP = new AdapterPeriods(periodList, this);
        HorizontalLayout
                = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerViewPeriods.setLayoutManager(HorizontalLayout);
        recyclerViewPeriods.setAdapter(adapterP);
        adapterFinalMarks = new AdapterFinalMarks(getContext(), markedLessons);
        recyclerView.setAdapter(adapterFinalMarks);
        adapterFinalMarks.notifyDataSetChanged();
        getPeriods gP = new getPeriods();
        gP.execute();
        return timetableFragmentView;
    }

    @Override
    public void onClick(int position) {
        //Log.d(TAG, String.valueOf(periodNames.inverse().get(periodNamesList.get(position))));
        for (int i = 0; i < periodList.size(); i++) {
            if (i != position) {
                ((CardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
        ((CardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
        markedLessons.clear();
        new getMarks(periodList.get(position).startDate, periodList.get(position).endDate).execute();
        adapterFinalMarks.notifyDataSetChanged();
        //getMarks gM = new getMarks(periodNames.inverse().get(periodNamesList.get(position)));
        //gM.execute();
    }

    private class getPeriods extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=false&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null);
                if (response == null) throw new NoConnectionPendingException();
                JSONArray periods = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
                String yearStart = "", yearEnd = "";
                for (int i = 0; i < periods.length(); i++) {
                    String name = periods.getJSONObject(i).getString("fullname");
                    String startDate = periods.getJSONObject(i).getString("start");
                    String endDate = periods.getJSONObject(i).getString("end");
                    periodList.add(new Period(name, startDate, endDate));
                    Log.d(TAG, "Adding period to periodList: " + periodList.get(i).name + "/" + periodList.get(i).startDate + "/" + periodList.get(i).endDate);
                    if(i == periods.length() - 1){
                        yearEnd = periodList.get(i).endDate;
                    }
                    else if(i == 0){
                        yearStart = periodList.get(i).startDate;
                    }

                }
                //generating year period:
                periodList.add(new Period("Год", yearStart, yearEnd));
                Log.d(TAG, "doInBackground: students json period list size: " + periodList.size());

            }
            catch(Exception e){
                e.printStackTrace();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Произошла ошибка при загрузке данных. Повторите позже.",Snackbar.LENGTH_SHORT).show();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapterP.notifyDataSetChanged();
                }
            });

            return null;

        }
    }


    private class getMarks extends AsyncTask<String, Integer, Void> {
        String startDate, endDate;

        public getMarks(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Void doInBackground(String... strings) {
            Response response = Api.sendRequest("https://edu.gounn.ru/apiv3/getmarks?student=" + studentID + "&days=" + startDate + "-" + endDate + "&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null);
            try{
                if(response == null) throw new ConnectException();
                JSONArray marks = null;
                try {
                     marks = (new JSONObject(response.body().string())).getJSONObject("response").getJSONObject("result").getJSONObject("students").getJSONObject(studentID).getJSONArray("lessons");
                }
                catch(Exception e){
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < marks.length(); i++) {
                    JSONObject mark = marks.getJSONObject(i);
                    String name = mark.getString("name");
                    Double average = mark.getDouble("average");
                    JSONArray marksJsonList = mark.getJSONArray("marks");
                    ArrayList<Mark> tMarklist = new ArrayList<>();
                    //marks fori
                    Log.d(TAG, "doInBackgroundMarkGetting: " + marksJsonList.toString());
                    for (int j = 0; j < marksJsonList.length(); j++) {
                        String markStr = marksJsonList.getJSONObject(j).getString("value");
                        String comment = marksJsonList.getJSONObject(j).getString("comment");
                        String lessonComment = marksJsonList.getJSONObject(j).getString("lesson_comment");
                        String date = marksJsonList.getJSONObject(j).getString("date");
                        String mType = "";
                        double weight = 1.0;
                        if(marksJsonList.getJSONObject(j).has("mType")) mType = marksJsonList.getJSONObject(j).getJSONObject("mType").getString("type");
                        if(marksJsonList.getJSONObject(j).has("weight")) weight = marksJsonList.getJSONObject(j).getDouble("weight");
                        tMarklist.add(new Mark(markStr, weight, date, mType, comment, lessonComment));
                    }
                    //adding info to list
                    markedLessons.add(new MarkedLesson(name, average, tMarklist));
                }

                Log.d(TAG, "markedLessonsGettingFinished(size): " + markedLessons.size());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterFinalMarks.notifyDataSetChanged();
                    }
                });


            }
            catch(IllegalArgumentException e){
                e.printStackTrace();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Похоже, еще не выставлена ни одна оценка.",Snackbar.LENGTH_SHORT).show();
            }
            catch(Exception e){
                e.printStackTrace();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Произошла ошибка при загрузке данных. Повторите позже.",Snackbar.LENGTH_SHORT).show();
            }
            return null;

        }

    }

    private int getUchYear(){
        int y = Calendar.getInstance().get(Calendar.YEAR);
        if(!(Calendar.getInstance().get(Calendar.MONTH) >= Calendar.SEPTEMBER)) y -= 1;
        return y;
    }

}
