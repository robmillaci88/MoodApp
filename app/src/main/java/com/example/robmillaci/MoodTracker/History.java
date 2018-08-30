package com.example.robmillaci.MoodTracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/*
 * This activity is used to display and handle all events to do with the history screen.
 * It defines a custom recycler view adaptor (class: RecyclerViewAdaptor) and a menu containing a menu item
 * 'home' and a menu item 'history graph'
 */
    public class History extends AppCompatActivity {
    RecylerViewAdaptor mAdaptor;//the adaptor for the history list view
    RecyclerView historyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds a home button to the support action bar
        setTitle("Mood History"); //set the title of the activity

        //create and set the recycler view adaptor. The recycler view adaptor constructor requires an array list for the mood history
        historyView = findViewById(R.id.moodRecyclerView);
        historyView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        historyView.setAdapter(createAdaptor());

    }

        RecylerViewAdaptor createAdaptor(){
        mAdaptor = new RecylerViewAdaptor(this,Mood.getsMoodHistoryArrayL());
        return mAdaptor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.history_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.histGraph:
                //The intent that begins to History graph activity.
                Intent intent = new Intent(this,HistoryGraph.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
