package com.example.govDiary;

public class TimetableLesson {
    String lessonName;
    String lessonNumber;
    String room;
    String teacher;
    String group;

    public TimetableLesson(String lessonName, String lessonNumber, String room, String teacher, String group) {
        this.lessonName = lessonName;
        this.lessonNumber = lessonNumber;
        this.room = room;
        this.teacher = teacher;
        this.group = group;
    }
}
