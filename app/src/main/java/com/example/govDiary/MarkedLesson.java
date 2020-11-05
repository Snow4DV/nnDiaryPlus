package com.example.govDiary;

import java.util.ArrayList;

public class MarkedLesson {
        String name;
        Double averageMark;
        ArrayList<Mark> marks;

    public MarkedLesson(String name, Double averageMark, ArrayList<Mark> marks) {
        this.name = name;
        this.averageMark = averageMark;
        this.marks = marks;
    }
}
