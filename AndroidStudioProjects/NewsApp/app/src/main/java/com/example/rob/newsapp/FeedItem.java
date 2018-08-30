package com.example.rob.newsapp;

import android.graphics.Bitmap;

import java.net.URL;

public class FeedItem {
    private String title;
    private String description;
    private URL newsItemURL;
    private String imageURL;

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getNewsItemURL() {
        return newsItemURL;
    }

    public void setNewsItemURL(URL newsItemURL) {
        this.newsItemURL = newsItemURL;
    }

    @Override
    public String toString() {
        return
                "title= " + title + '\n' +
                ", description= " + description + '\n';
    }
}
