package com.example.govDiary;

import androidx.annotation.Nullable;

public class Mark  implements Comparable<Mark>{
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

    @Override
    public int compareTo(Mark m) {
        if(m.date.equals(date) && m.mark.equals(mark)){
            return 1;
        }
        else return 0;

    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Mark))
            return false;
        Mark m = (Mark) obj;
        if(m.mark.equals(mark) && m.date.equals(date)) return true;
        return false;
    }
}
