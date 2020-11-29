package com.example.govDiary;

import java.util.ArrayList;

public class MarkedLesson {
        String name;
        Double averageMark = 0.0;
        ArrayList<Mark> marks;
        boolean ifMarkIsFinal = false;
        String finalMark = "";

    public MarkedLesson(String name, Double averageMark, ArrayList<Mark> marks) {
        this.name = name;
        this.averageMark = averageMark;
        this.marks = marks;
    }
    public MarkedLesson(String name, String finalMark, Double averageMark, ArrayList<Mark> marks) {
        this.name = name;
        ifMarkIsFinal = true;
        this.averageMark = averageMark;
        this.finalMark = finalMark;
        this.marks = marks;
    }
}
