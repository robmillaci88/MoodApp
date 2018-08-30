package com.example.rob.newsapp;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

public class ParseApplication {
    private static final String TAG = "ParseApplication";
    private ArrayList<FeedItem> newsItems;

    public ParseApplication() {
        this.newsItems = new ArrayList<>();
    }

    public ArrayList<FeedItem> getNewsItems() {
        return newsItems;
    }

    public boolean parse(String xmlData) {
        FeedItem currentRecord = null;
        boolean inTitle = false;
        String textValue = "";
        boolean status = true;

        try {
            XmlPullParserFactory xppFactor = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = xppFactor.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();


            while (eventType != xpp.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            inTitle = true;
                            currentRecord = new FeedItem();
                        }
                        break;


                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        Log.d(TAG, "£££££££££££££££££££££££££££££££££££££££££££££££TEXT VALUE " + textValue);
                        break;


                    case XmlPullParser.END_TAG:
                        if (inTitle) {

                            if ("item".equalsIgnoreCase(tagName)) {
                                newsItems.add(currentRecord);
                                inTitle = false;
                            } else if ("title".equalsIgnoreCase(tagName)) {
                                currentRecord.setTitle(textValue);
                            } else if ("description".equalsIgnoreCase(tagName)) {
                                currentRecord.setDescription(textValue);
                            } else if ("link".equalsIgnoreCase(tagName)) {
                                currentRecord.setNewsItemURL(new URL(textValue));
                            } else if ("media:thumbnail".equalsIgnoreCase(tagName)) {
                                String imgUrl = xpp.getAttributeValue(null, "url");
                                currentRecord.setImageURL(imgUrl);
                            }

                        }

                        break;


                    default:
                        //do nothing
                }

                eventType = xpp.next();

            }


        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;

    }

}
