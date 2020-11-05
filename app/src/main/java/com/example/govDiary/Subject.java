package com.example.govDiary;

public class Subject {
    public int id, group;
    public String name;
    public Subject(int id, int group, String name){
        this.id = id;
        this.group = group;
        this.name = name;
    }

    public int getGroup(){
        return group;
    }
}
