package com.example.rob.newsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String feedUrl = "http://feeds.skynews.com/feeds/rss/home.xml";
    private ListView listMovies;
    private TextView mainMenuTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listMovies = (ListView) findViewById(R.id.xmlList);
        mainMenuTitle = findViewById(R.id.appMainTitle);
        mainMenuTitle.setText("Home");
        downloadURL(feedUrl);

    }


    private void downloadURL(String feedUrl) {
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl);
    }


    private class DownloadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String rssData = downloadXML(strings[0]);
            return rssData;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplication parseApplications = new ParseApplication();
            parseApplications.parse(s);

            final FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this,R.layout.item_view,parseApplications.getNewsItems());
            listMovies.setAdapter(feedAdapter);
            listMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                   FeedItem selectedItem = feedAdapter.getNewsItems().get(i);
                   URL url = selectedItem.getNewsItemURL();
                    Uri uri = Uri.parse(url.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

        }

        private String downloadXML(String url) {
            StringBuilder xmlResults = new StringBuilder();

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                int response = connection.getResponseCode();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputbuffer = new char[700];

                while (true) {
                    charsRead = br.read(inputbuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResults.append(String.copyValueOf(inputbuffer, 0, charsRead));
                    }
                }
                br.close();
                return xmlResults.toString();


            } catch (MalformedURLException e) {
                Log.d("MainActivity", "downloadXML: error downloading invalid URL");
            } catch (IOException e) {
                Log.d(TAG, "downloadXML: IOException");
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       int id = item.getItemId();
       switch (id){
           case R.id.mnuhome:
               feedUrl = "http://feeds.skynews.com/feeds/rss/home.xml";
               mainMenuTitle.setText("Home");
               break;

           case R.id.mnuuk:
               feedUrl = "http://feeds.skynews.com/feeds/rss/uk.xml";
               mainMenuTitle.setText("UK News");
               break;

           case R.id.mnuworld:
               feedUrl = "http://feeds.skynews.com/feeds/rss/world.xml";
               mainMenuTitle.setText("World News");
               break;

           case R.id.mnuus:
               feedUrl = "http://feeds.skynews.com/feeds/rss/us.xml";
               mainMenuTitle.setText("US News");
               break;

           case R.id.mnubusiness:
               feedUrl = "http://feeds.skynews.com/feeds/rss/business.xml";
               mainMenuTitle.setText("Business News");
               break;

           case R.id.mnupolitics:
               feedUrl = "http://feeds.skynews.com/feeds/rss/politics.xml";
               mainMenuTitle.setText("Political News");
               break;

           case R.id.mnutech:
               feedUrl = "http://feeds.skynews.com/feeds/rss/technology.xml";
               mainMenuTitle.setText("Tech News");
               break;

           case R.id.mnuentertainment:
               feedUrl = "http://feeds.skynews.com/feeds/rss/entertainment.xml";
               mainMenuTitle.setText("Entertainment");
               break;

           case R.id.mnustrangeNews:
               feedUrl = "http://feeds.skynews.com/feeds/rss/strange.xml";
               mainMenuTitle.setText("Strange News");
               break;


       }

       downloadURL(feedUrl);
       return true;
    }


}