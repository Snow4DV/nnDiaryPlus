package com.example.govDiary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Message {
    String msgDate, shortText, subject, id;
    LinkedHashMap<String, String> users;
    boolean ifSent, withFiles, unread;

    public Message(String msgDate, String shortText, String subject, String id, LinkedHashMap<String, String> users, boolean ifSent, boolean withFiles, boolean unread) {
        this.msgDate = msgDate;
        this.shortText = shortText;
        this.subject = subject;
        this.id = id;
        this.users = users;
        this.ifSent = ifSent;
        this.withFiles = withFiles;
        this.unread = unread;
    }
}
