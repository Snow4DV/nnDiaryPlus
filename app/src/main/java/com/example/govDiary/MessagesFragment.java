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
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.govDiary.Api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
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

public class MessagesFragment extends Fragment {
    String authToken, studentID; // getting these from intent
    Context context;
    String usersObject;
    ArrayList<Message> messages;
    AsyncTask<String, Integer, Void> gm;
    ListView listView;
    MessagesListAdapter messagesListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO: if periods couldn't be got make swiperefreshlistener active
        messages = new ArrayList<>();
        View messagesView =  inflater.inflate(R.layout.messages_list_fragment, container, false);
        context = getContext();
        swipeRefreshLayout = messagesView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                gm = (new getMessages(authToken)).execute();
            }
        });
        authToken = getActivity().getIntent().getStringExtra("authToken");
        studentID = getActivity().getIntent().getStringExtra("studentID");
        //datePickerTimeline.setInitialDate(2020,5,10);
        //usersObject = getArguments().getString("usersObject"); //"users_to" or "users_from"
         messagesListAdapter = new MessagesListAdapter(getActivity(), R.layout.message_item, messages, getActivity(), authToken);
        listView = messagesView.findViewById(R.id.listMsg);
        listView.setAdapter(messagesListAdapter);
        gm = (new getMessages(authToken)).execute();
        return messagesView;
    }

    public void setUsersObject(String usersObject){
        this.usersObject = usersObject;
    }

    private class getMessages extends AsyncTask<String, Integer, Void> {
        String authT;
        String folderName = "inbox";
        String servAnsw;
        Response sentMessagesResponse;
        getMessages(String auth){
            authT = auth;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                messages.clear();
                if (sentMessagesResponse == null) throw new NoConnectionPendingException();

                JSONArray messagesJSON = (new JSONObject(servAnsw)).getJSONObject("response").getJSONObject("result")
                        .getJSONArray("messages");
                for (int i = 0; i < messagesJSON.length(); i++) {
                    String msgDate = messagesJSON.getJSONObject(i).getString("date");
                    String shortText = messagesJSON.getJSONObject(i).getString("short_text");
                    String subject = messagesJSON.getJSONObject(i).getString("subject");
                    String id = messagesJSON.getJSONObject(i).getString("id");
                    boolean unread = messagesJSON.getJSONObject(i).getBoolean("unread");
                    boolean withFiles = messagesJSON.getJSONObject(i).getBoolean("with_files");
                    LinkedHashMap<String, String> users = new LinkedHashMap<>(); //<ID, Name string>
                    if(usersObject.equals("users_to")){
                        JSONArray usersToJson = messagesJSON.getJSONObject(i).getJSONArray(usersObject);
                        for (int j = 0; j < usersToJson.length(); j++) {
                            users.put(usersToJson.getJSONObject(j).getString("name"), usersToJson.getJSONObject(j).getString("lastname") + " " + usersToJson.getJSONObject(j).getString("firstname").substring(0, 1) + ". " + usersToJson.getJSONObject(j).getString("middlename").substring(0, 1) + ".");
                        }
                    }
                    else{
                        users.put(messagesJSON.getJSONObject(i).getJSONObject(usersObject).getString("name"), messagesJSON.getJSONObject(i).getJSONObject(usersObject).getString("lastname") + " " + messagesJSON.getJSONObject(i).getJSONObject(usersObject).getString("firstname").substring(0, 1) + ". " + messagesJSON.getJSONObject(i).getJSONObject(usersObject).getString("middlename").substring(0, 1) + ".");
                    }
                    messages.add(new Message(msgDate, shortText, subject, id, users, usersObject.equals("users_to"), withFiles, unread));
                }
                if(getActivity() == null) {
                    this.cancel(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                    Toast.makeText(getContext(), "Произошла ошибка при загрузке данных. Повторите позже.", Toast.LENGTH_SHORT).show();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            messagesListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            if(usersObject.equals("users_to")) folderName = "sent";
            sentMessagesResponse = Api.sendRequest("https://edu.gounn.ru/apiv3/getmessages?folder=" + folderName + "&unreadonly=no&devkey=d9ca53f1e47e9d2b9493d35e2a5e36&out_format=json&auth_token=" + authT + "&vendor=edu", null, getContext(), false);
            try {
                servAnsw = sentMessagesResponse.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }


    }

    public void refreshMessages(){
        swipeRefreshLayout.setRefreshing(true);
        gm = (new getMessages(authToken)).execute();
    }


}
