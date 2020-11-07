package com.example.govDiary;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DiaryPagerAdapter extends FragmentPagerAdapter  {


    String authToken, studentID;
    Calendar initDate;
    int amount = 0;

    public DiaryPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
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
    public int getCount() {
        return amount;
    }

    @Override
    public Fragment getItem(int position) {
        return getFragmentBasedOnPosition(position);
    }

    private Fragment getFragmentBasedOnPosition(int position) {
        Log.d(TAG, "getting diary Fragment on " + position + ",prev date is " + initDate.getTime().toString());
        String d = dateCalc(initDate, position);
        Log.d(TAG, "getting diary Fragment on " + position + ",date is " + d);
        return DiaryListFragment.newDiaryFragmentInstance(authToken, studentID, d + "-" + d);
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
