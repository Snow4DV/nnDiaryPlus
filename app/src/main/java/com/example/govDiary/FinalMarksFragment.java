package com.example.govDiary;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.riversun.okhttp3.OkHttp3CookieHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.NoConnectionPendingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<Period> periodList;
    public int lastSelectedPeriod = -1;
    String lastStartDate = "", lastEndDate = "";
    boolean firstLoadingFinished = false;
    String TAG = "FinalMarksFragment";
    //для периодов
    RecyclerView recyclerViewPeriods;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    AdapterPeriods adapterP;
    LinearLayoutManager HorizontalLayout;
    private AsyncTask<String, Integer, Void> getMarksAsyncTasks;
    private getPeriods getPeriodsAsyncTask;
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
        swipeRefreshLayout = timetableFragmentView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(firstLoadingFinished && !lastStartDate.equals("") && !lastEndDate.equals("")){
                    getMarksAsyncTasks = new getMarks(lastStartDate, lastEndDate).execute();
                }
            }
        });
        getPeriodsAsyncTask = new getPeriods();
        getPeriodsAsyncTask.execute();
        return timetableFragmentView;
    }

    @Override
    public void onClick(int position) {
        try {
            //Log.d(TAG, String.valueOf(periodNames.inverse().get(periodNamesList.get(position))));
            for (int i = 0; i < periodList.size(); i++) {
                if (i != position) {
                    ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.periodButtonColor));
                    ((TextView) (recyclerViewPeriods.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.textview))).setTextColor(getResources().getColor(R.color.periodButtonTextColor));
                    ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.cardview))).setStrokeColor(getResources().getColor(R.color.periodButtonStrokeColor));
                }
            }
            ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.periodButtonColorActive));
            ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.cardview))).setStrokeColor(getResources().getColor(R.color.periodButtonStrokeColorActive));
            ((TextView) (recyclerViewPeriods.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.textview))).setTextColor(getResources().getColor(R.color.periodButtonTextColorActive));
            markedLessons.clear();
            lastSelectedPeriod = position;
            new getMarks(periodList.get(position).startDate, periodList.get(position).endDate).execute();
            adapterFinalMarks.notifyDataSetChanged();
            //getMarks gM = new getMarks(periodNames.inverse().get(periodNamesList.get(position)));
            //gM.execute();

        }
        catch(NullPointerException e){
            Log.e(TAG, "onClick: nullpointer");
        }
    }

    private class getPeriods extends AsyncTask<String, Integer, Void> {  //TODO: YEAR FINAL ASSESSMENTS FIX!!!!!!!!!
        Response response;
        String responseString;
        @Override
        protected Void doInBackground(String... strings) {
            response = Api.sendRequest("https://edu.gounn.ru/apiv3/getperiods?weeks=false&show_disabled=true&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
            try {
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                swipeRefreshLayout.setRefreshing(true);
                if (response == null) throw new NoConnectionPendingException();
                JSONArray periods = (new JSONObject(responseString)).getJSONObject("response").getJSONObject("result").getJSONArray("students").getJSONObject(0).getJSONArray("periods");
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
            if(!firstLoadingFinished){
                try {
                    String firstDateAfterFoundStart = null;
                    String firstDateAfterFoundEnd = null;
                    int lastCounterFound = 0;
                for (int i = 0; i < periodList.size(); i++) {

                        Date startingDate = new SimpleDateFormat("yyyyMMdd").parse(periodList.get(i).startDate);
                        Date endingDate = new SimpleDateFormat("yyyyMMdd").parse(periodList.get(i).endDate);
                        Date curDate = Calendar.getInstance().getTime();
                    if(firstDateAfterFoundStart == null && curDate.after(startingDate)){
                        firstDateAfterFoundStart = periodList.get(i).startDate;
                        firstDateAfterFoundEnd = periodList.get(i).endDate;
                        lastCounterFound = i;
                    }
                        if(curDate.after(startingDate) && curDate.before(endingDate)){
                           firstDateAfterFoundStart = null;
                            getMarksAsyncTasks = new getMarks(periodList.get(i).startDate, periodList.get(i).endDate).execute();
                            Log.d(TAG, "position period: " + i);
                            final int counter = i;
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(counter).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.periodButtonColorActive));
                                    ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(counter).itemView.findViewById(R.id.cardview))).setStrokeColor(getResources().getColor(R.color.periodButtonStrokeColorActive));
                                    ((TextView) (recyclerViewPeriods.findViewHolderForAdapterPosition(counter).itemView.findViewById(R.id.textview))).setTextColor(getResources().getColor(R.color.periodButtonTextColorActive));
                                }
                            }, 50);
                            break;
                        }

                }

                    if(firstDateAfterFoundStart != null){
                        getMarksAsyncTasks = new getMarks(firstDateAfterFoundStart, firstDateAfterFoundEnd).execute();
                        final int lastCounter = lastCounterFound;
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(lastCounter).itemView.findViewById(R.id.cardview))).setCardBackgroundColor(getResources().getColor(R.color.periodButtonColorActive));
                                ((MaterialCardView) (recyclerViewPeriods.findViewHolderForAdapterPosition(lastCounter).itemView.findViewById(R.id.cardview))).setStrokeColor(getResources().getColor(R.color.periodButtonStrokeColorActive));
                                ((TextView) (recyclerViewPeriods.findViewHolderForAdapterPosition(lastCounter).itemView.findViewById(R.id.textview))).setTextColor(getResources().getColor(R.color.periodButtonTextColorActive));
                            }
                        }, 50);

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            firstLoadingFinished = true;
            super.onPostExecute(aVoid);
        }
    }


    private class getMarks extends AsyncTask<String, Integer, Void> {
        String startDate, endDate;
        Response response;
        String responseString;
        Response responseFinalMarksAssessments;
        String responseFinalMarksAssessmentsString;

        public getMarks(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            lastStartDate = startDate;
            lastEndDate = endDate;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            swipeRefreshLayout.setRefreshing(true);
            markedLessons.clear();
            try{
                //Getting all finalmarks - bad implementation because of the diary's api - they separated the marks and finalmarks :|
                HashMap<String, String> finalMarks = new HashMap<>();

                try {
                    JSONArray finalMarksAssessments = (new JSONObject(responseFinalMarksAssessmentsString)).getJSONObject("response").getJSONObject("result")
                            .getJSONObject("students").getJSONObject(studentID).getJSONArray("items");
                    for (int i = 0; i < finalMarksAssessments.length(); i++) {
                        String subjName = finalMarksAssessments.getJSONObject(i).getString("name");
                        JSONArray assessments = finalMarksAssessments.getJSONObject(i).getJSONArray("assessments");
                        for (int j = 0; j < assessments.length(); j++) {
                            String value = assessments.getJSONObject(j).getString("value");
                            int periodNum = j;
                            if (j > (periodList.size() - 1))
                                periodNum = periodList.size() - 1;
                            if (value != null && periodNum == lastSelectedPeriod) { //Checking if the mark is from current period
                                finalMarks.put(subjName, value);
                            }
                        }
                    }
                }
                catch(Exception ex){
                    Log.d(TAG, "doInBackground: final marks are empty or err happened when getting it.");
                }
                //Getting the marks
                if(response == null) throw new ConnectException();
                JSONArray marks = null;
                try {
                    marks = (new JSONObject(responseString)).getJSONObject("response").getJSONObject("result").getJSONObject("students").getJSONObject(studentID).getJSONArray("lessons");
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
                        Boolean counted = marksJsonList.getJSONObject(j).getBoolean("count");
                        String lessonComment = marksJsonList.getJSONObject(j).getString("lesson_comment");
                        String date = marksJsonList.getJSONObject(j).getString("date");
                        String mType = "";
                        double weight = 1.0;
                        if(marksJsonList.getJSONObject(j).has("mtype")) mType = marksJsonList.getJSONObject(j).getJSONObject("mtype").getString("type");
                        if(marksJsonList.getJSONObject(j).has("weight")) weight = marksJsonList.getJSONObject(j).getDouble("weight");
                        if(!counted) weight = 0.0;
                        tMarklist.add(new Mark(markStr, weight, date, mType, comment, lessonComment));
                    }
                    //adding info to list
                    if(!finalMarks.containsKey(name))
                        markedLessons.add(new MarkedLesson(name, average, tMarklist));
                    else markedLessons.add(new MarkedLesson(name, finalMarks.get(name), average, tMarklist));
                }

                Log.d(TAG, "markedLessonsGettingFinished(size): " + markedLessons.size());
                adapterFinalMarks.notifyDataSetChanged();
                lastStartDate = startDate;
                lastEndDate = endDate;


            }
            catch(IllegalArgumentException e){
                e.printStackTrace();
                try {
                    Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Похоже, еще не выставлена ни одна оценка.", Snackbar.LENGTH_SHORT).show();
                }
                catch(NullPointerException ex){
                    Log.d(TAG, "onPostExecute: tried to snackbar, but fragment is dead already");
                }
            }
            catch(Exception e){
                e.printStackTrace();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Произошла ошибка при загрузке данных. Повторите позже.",Snackbar.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            response = Api.sendRequest("https://edu.gounn.ru/apiv3/getmarks?student=" + studentID + "&days=" + startDate + "-" + endDate + "&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
            try {
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            responseFinalMarksAssessments = Api.sendRequest("https://edu.gounn.ru/apiv3/getfinalassessments?devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authToken + "&vendor=edu", null, getContext(), false);
            try {
                responseFinalMarksAssessmentsString = responseFinalMarksAssessments.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

    }

    @Override
    public void onPause() {
        if(getMarksAsyncTasks != null)
        getMarksAsyncTasks.cancel(true);
        if(getPeriodsAsyncTask != null)
        getPeriodsAsyncTask.cancel(true);
        super.onPause();
    }
}
