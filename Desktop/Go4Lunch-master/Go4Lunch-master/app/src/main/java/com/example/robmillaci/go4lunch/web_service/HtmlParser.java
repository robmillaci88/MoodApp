package com.example.robmillaci.go4lunch.web_service;

import android.os.AsyncTask;

import org.jsoup.Jsoup;

import java.io.IOException;

/**
 * Parses additional place type data from www.google.com/maps/search
 */
public class HtmlParser extends AsyncTask<String, Void, String> {
    final private static String baseUrl = "https://www.google.com/maps/search/?api=1&query=Google&query_place_id=";
    final private IhtmlParser mCallback;
    private Object returnObjects;

    public HtmlParser() {
        this.mCallback = null;
    }

    public HtmlParser(IhtmlParser callback, Object... objects) {
        this.mCallback = callback;
        this.returnObjects = objects;
    }

    @Override
    protected String doInBackground(String... strings) {
        //create the URL
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        sb.append(strings[0]);
        String doc = "";
        int startIndex = 0;
        int endIndex = 0;

        try {
            //Connect the to URL and get the elements by attribute "itemprop" with value "description"
            doc = Jsoup.connect(sb.toString()).get().getElementsByAttributeValue("itemprop", "description").get(0).toString();
            startIndex = doc.indexOf("·") + 1; //the start index to split the returned data
            endIndex = doc.indexOf("·", startIndex); //the end index to split the returned data
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mCallback != null) {
            mCallback.parseComplete(doc.substring(startIndex, endIndex).trim(), returnObjects); //return the parsed string as well as any other objects passed for return
        }
        return doc.substring(startIndex, endIndex).trim(); //if no callback is supplied, return the parsed string with the calling class using .get()
    }
}


