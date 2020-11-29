package com.example.govDiary;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Lesson {
  public String name, topic, homework, mark;
  public int number;
  public LinkedHashMap<String, String> files;

  Lesson(String gname, String gtopic, String ghomework, int gnumber, String gmark, LinkedHashMap<String, String> files){
    name = gname;
    this.files = files;
    topic = gtopic;
    homework = ghomework;
    number = gnumber;
    mark = gmark;
  }

}
