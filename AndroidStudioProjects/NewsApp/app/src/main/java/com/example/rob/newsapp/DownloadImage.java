package com.example.rob.newsapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private String imageURL;
    private Bitmap image;
    private static final String TAG = "DownloadImage";

    @Override
    protected Bitmap doInBackground(String... strings) {
        String pathtoFile = strings[0];
        Bitmap bitmap = null;
        try {
            InputStream in = new URL(pathtoFile).openStream();
            bitmap = BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.image = bitmap;
        return bitmap;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        this.image = bitmap;
    }
}
