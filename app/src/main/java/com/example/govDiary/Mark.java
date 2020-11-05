package com.example.govDiary;

public class Mark {
    String mark;
    String date;
    String markType;
    String comment;
    String lessonComment;
    double weight;

    public Mark(String mark, double weight, String date, String markType, String comment, String lessonComment) {
        this.mark = mark;
        this.comment = comment;
        this.markType = markType;
        this.date = date;
        this.weight = weight;
        this.lessonComment = lessonComment;
    }
}
