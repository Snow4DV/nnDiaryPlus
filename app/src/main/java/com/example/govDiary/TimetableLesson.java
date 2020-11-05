package com.example.govDiary;

public class TimetableLesson {
    String lessonName;
    int groupid;
    int weekday;
    int lessonNumber;
    String roomNumber;
    String weekdayString;

    public TimetableLesson(String lessonName, int groupid, int weekday, int lessonNumber, String roomNumber) {
        this.lessonName = lessonName;
        this.groupid = groupid;
        this.weekday = weekday;
        this.lessonNumber = lessonNumber;
        this.roomNumber = roomNumber;
        switch(weekday){
            case 1:
                weekdayString = "Понедельник";
                break;
            case 2:
                weekdayString = "Вторник";
                break;
            case 3:
                weekdayString = "Среда";
                break;
            case 4:
                weekdayString = "Четверг";
                break;
            case 5:
                weekdayString = "Пятница";
                break;
            case 6:
                weekdayString = "Суббота";
                break;
        }
    }
}
