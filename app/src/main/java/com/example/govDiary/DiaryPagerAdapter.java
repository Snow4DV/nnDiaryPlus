package com.example.govDiary;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DiaryPagerAdapter extends FragmentStateAdapter {


    String authToken, studentID;
    Calendar initDate;
    int amount = 0;


    public DiaryPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void setTokenAndId(String authToken, String studentID){
        this.studentID = studentID;
        this.authToken = authToken;
    }
    public void setInitDateAndDaysAmount(Calendar d, int amount){
        this.amount = amount;
        Log.d(TAG, "setInitDateAndDaysAmount: called");
        this.initDate = d;
    }
    @Override
    public int getItemCount() {
        return amount;
    }



    @Override
    public Fragment createFragment(int position) {
        return getFragmentBasedOnPosition(position);
    }

    private Fragment getFragmentBasedOnPosition(int position) {
        Log.d(TAG, "getting diary Fragment on " + position + ",prev date is " + initDate.getTime().toString());
        String d = dateCalc(initDate, position);
        Log.d(TAG, "getting diary Fragment on " + position + ",date is " + d);
        ArrayList<String> dates = new ArrayList<>();
        for (int i = position - 7; i < position + 7; i++) {
            dates.add(dateCalc(initDate, i));
        }
        return DiaryListFragment.newDiaryFragmentInstance(authToken, studentID, d, dateCalc(initDate, position - 7), dateCalc(initDate, position + 7), dates);
    }

    private String dateCalc(Calendar cal, int pos){
        Date tDate = cal.getTime();
        Calendar tCal = Calendar.getInstance();
        tCal.setTime(tDate);
        tCal.add(Calendar.DATE, pos);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(tCal.getTime());
    }

}
