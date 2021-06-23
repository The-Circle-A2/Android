package com.pedro.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Comparable<Message> {
    private int id;
    private String userName;
    private String text;
    private Date date;
    private String imageUrl;

    public Message(int id, String userName, String text, Date date, String imageUrl) {
        this.id = id;
        this.userName = userName;
        this.text = text;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
        String dateProcessed = "Onbekend";

        try {
            dateProcessed = newFormat.format(date);
        }
        catch (Exception e)
        {
//            Log.d(Feature.class.getSimpleName(), e.toString());
        }

        return dateProcessed;
    }

    public Date getDateTime() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int compareTo(Message o) {
        if (getDateTime() == null || o.getDateTime() == null)
            return 0;

        return getDateTime().compareTo(o.getDateTime());
    }
}
