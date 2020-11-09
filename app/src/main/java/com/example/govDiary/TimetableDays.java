package com.example.govDiary;

import java.util.ArrayList;

public class TimetableDays {
    ArrayList<TimetableLesson> lessons;
    String name;
    String range;
    public TimetableDays(String name, String range, ArrayList<TimetableLesson> lessons){
        this.name = name;
        this.range = range;
        this.lessons = lessons;
    }
}
