package com.example.robmillaci.MoodTracker;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static com.example.robmillaci.MoodTracker.Mood.sMoodHistoryArrayL;
import static com.example.robmillaci.MoodTracker.Mood.sTotalMoodHistoryArrayL;
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    ArrayList<Integer> mMoodsArrayL = new ArrayList<>(); //Arraylist to store the 5 different Moods
    GestureDetector mGestureDetector; //Gesture detector that is called from a touch event and determines wether the user swiped up or down

    boolean mFabShowing = false; //boolean to determine if the floating action menu is open or not
    boolean mFirstRun = true; // boolean to determine if the app is being run for the first time to show the help fragment
    boolean mFragmentIsShowing = false; // Determines if the help fragment is showing to block users being able to create another at the same time

    static String sNote = ""; //String to hold the value of the sNote
    static int sCurrentImage; //the current image set as the mood

    AlertDialog mNotesDialog; // Alert dialog to display the notes in
    ImageView mMoodImageView; //The image view to display the mood picture
    ConstraintLayout mMoodLayout; //The parent layout of the mood tracker
    EditText mNotesText; //Edit text that holds the notes string value
    com.github.clans.fab.FloatingActionButton mFab; //Custom API for floating action button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Restore share preferences*********************************************************************************************
        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        sNote = prefs.getString("sNote", "");
        mFirstRun = prefs.getBoolean("mFirstRun", true);
        sCurrentImage = prefs.getInt("mood", 3);

        //create a new Gson instance and get any saved mood history
        Gson gson = new Gson();
        String json = prefs.getString("sMoodHistoryArrayL", null); //String to hold the retrieved JSon data
        Type type = new TypeToken<List<Mood>>() {
        }.getType();
        sMoodHistoryArrayL = gson.fromJson(json, type);

        String totalMoodHist = prefs.getString("totalmoodHistory", null); //String to hold the retrieves JSon data
        sTotalMoodHistoryArrayL = gson.fromJson(totalMoodHist, type);
        //end of restore shared preferences**************************************************************************************

        //display the first run "how to" fragment to show users how to use the app
        if (mFirstRun) {
            HelpFragment.createHelpFragment(this);
        } else {
            //remove any existing fragments from fragment manager
            for (android.support.v4.app.Fragment fragment : getSupportFragmentManager().getFragments())
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        //gesture detector used to detect user actions on the screen
        mGestureDetector = new GestureDetector(this, this);

        //assign the views
        mMoodImageView = findViewById(R.id.moodImageView);
        mMoodLayout = findViewById(R.id.moodLayout);

        //populate the mMoodsArrayL array list with the mood images int values to facilitate moving through different mMoodsArrayL on screen swipe
        mMoodsArrayL.add(R.drawable.very_bad_mood);
        mMoodsArrayL.add(R.drawable.bad_mood);
        mMoodsArrayL.add(R.drawable.decent_mood);
        mMoodsArrayL.add(R.drawable.good_mood);
        mMoodsArrayL.add(R.drawable.great_mood);

        Schedule.scheduleAlarm(this); //schedule the alarm to broadcast to the reciever class (Schedule.class) in order to save the daily mood at midnight

        //Set up floating button menu item and set visibility to invisible to hide until user interacts with the FOB
        final com.github.clans.fab.FloatingActionMenu fabMenu = findViewById(R.id.fabmenu1);
        fabMenu.setVisibility(View.INVISIBLE);
        final com.github.clans.fab.FloatingActionMenu fabMenu2 = findViewById(R.id.fabmenu2);
        fabMenu2.setVisibility(View.INVISIBLE);

        //add on click listeners to the FAB menu items - history FAB menu button
        fabMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder historyDialog = new AlertDialog.Builder(MainActivity.this, R.style.dialogInvisible);

                historyDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() { //add on click listener to the button
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNotesText = ((AlertDialog) dialog).findViewById(R.id.notesText);
                        if (mNotesText != null) { //if not null check to avoid nullPointerException running getEditableText().
                            sNote = mNotesText.getEditableText().toString();
                        }
                        Toast.makeText(MainActivity.this, R.string.note_saved, Toast.LENGTH_LONG).show();
                        //to close the menu items
                    }
                });

                LayoutInflater inflater = getLayoutInflater();  //Get a reference to the view that is inflated by the alert dialog so we can make changes

                @SuppressLint("InflateParams") View alertView = inflater.inflate(R.layout.dialog_history, null);

                //sets the view of the alert dialog, create the alert dialog and show on screen
                historyDialog.setView(alertView)
                        .setNegativeButton("Cancel", null);

                mNotesDialog = historyDialog.create();
                mNotesDialog.show();

                //Checks that the specified object reference is not null and set the dialog background to be invisible
                Objects.requireNonNull(mNotesDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));

                //get access to the positive and negative alert dialog buttons so changes can be made
                Button btnPositive = mNotesDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = mNotesDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                //re-arrange the OK and Cancel button so that they are evenly distributed and centered accross the dialog
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);

                //set the dialog image to represent the current mood
                ImageView dialogImageView = alertView.findViewById(R.id.dialogMoodImage);
                dialogImageView.setBackgroundResource(mMoodsArrayL.get(getsCurrentImage()));

                mFab.callOnClick(); //rehide the FAB menu items after click

                //get access to the edit text in the dialog view and set the value to any previously entered sNote.
                EditText notesInput = alertView.findViewById(R.id.notesText);
                notesInput.setText(sNote);
                notesInput.setSelection(notesInput.getText().length()); //ensure the edit text cursor is placed at the end of any pre existing text
            }
        });
        fabMenu2.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //set on click listener to history FAB menu
                Toast message = Toast.makeText(MainActivity.this, R.string.no_history, Toast.LENGTH_LONG);
                try {
                    if (Mood.getsMoodHistoryArrayL().size() > 0) { //if the mood histroy array list is not empty start the History class activity
                        Intent intent = new Intent(MainActivity.this, History.class);
                        startActivity(intent);
                        mFab.callOnClick(); //call on click of the FAB menu so the menu items are hidden
                    } else {
                        message.show(); // shows the toast message
                        mFab.callOnClick(); // call on click to close the menu items
                    }
                } catch (Exception e) {
                    message.show(); //if a null pointer exists due to moodHistory not being assigned yet, show the no history toast message
                    mFab.callOnClick(); // call on click to close the menu items
                }
            }
        });

        //Create animations for FOB menu items and assign animation listener
        final Animation animationEnd = new TranslateAnimation(0, -10000, 0, 0);
        animationEnd.setDuration(1000);
        animationEnd.setFillAfter(true);

        animationEnd.setAnimationListener(new Animation.AnimationListener() { //sets an animation listener which will hide the fab menus after the animation completes
            @Override
            public void onAnimationStart(Animation animation) {

            } // this method isnt used in this app but needs to be in the code

            @Override
            public void onAnimationEnd(Animation animation) {
                fabMenu.setVisibility(View.INVISIBLE);
                fabMenu2.setVisibility(View.INVISIBLE);
             }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        final Animation animation = new RotateAnimation(0, 360, 200, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            } // this method isnt used in this app but needs to be in the code

            @Override
            public void onAnimationEnd(Animation animation) {
                fabMenu.clearAnimation();
                fabMenu2.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mFab = findViewById(R.id.fab);    //Assign the floating action button and set on click listener which will display or hide FOB menu items. Also starts the animation of the menu items
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mFabShowing) {
                    fabMenu.startAnimation(animation);
                    fabMenu2.startAnimation(animation);

                    fabMenu.setVisibility(View.VISIBLE);
                    fabMenu2.setVisibility(View.VISIBLE);
                    mFabShowing = true;
                } else {
                    fabMenu.startAnimation(animationEnd);
                    fabMenu2.startAnimation(animationEnd);
                    mFabShowing = false;
                }
            }
        });
        selectColour(sCurrentImage); //changes the background colour to match the current image displayed on screen
        mMoodImageView.setImageResource(mMoodsArrayL.get(sCurrentImage)); //changes the image view to display the current mood selected
        mMoodImageView.setTag(mMoodsArrayL.get(sCurrentImage));
    }

    //method called when user swipes up. If the current image is 4, toast is displayed. If current image is not 4, 1 is added to the current image value and the image + color is updated
    public void selectImageUp() {
        Animation animation = new RotateAnimation(0, 360);
        animation.setDuration(1000);
        animation.setRepeatCount(0);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Handler handler = new Handler(); // delay for 0.5 seconds for a better visual effect
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMoodImageView.setImageResource(mMoodsArrayL.get(sCurrentImage));
                        mMoodImageView.setTag(mMoodsArrayL.get(sCurrentImage));
                        selectColour(sCurrentImage);
                    }
                }, 500);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (sCurrentImage == 4) {
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(1000);
            rotate.setInterpolator(new LinearInterpolator());
          mMoodImageView.startAnimation(rotate);
            Toast.makeText(this, "You couldn't be more happier!", Toast.LENGTH_LONG).show();
        } else {
            sCurrentImage += 1;
            mMoodImageView.startAnimation(animation);
        }
    }
    //method called when user swipes down. If the current image is 0, toast is displayed.If current image is not 0, 1 is taken from the current image value and the image + color is updated
    public void selectImageDown() {
        Animation animation = new RotateAnimation(360, 0);
        animation.setDuration(1000);
        animation.setRepeatCount(0);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() { //a delay is set to wait before setting the image view and the color for a better user experience
                        mMoodImageView.setImageResource(mMoodsArrayL.get(sCurrentImage));
                        mMoodImageView.setTag(mMoodsArrayL.get(sCurrentImage));
                        selectColour(sCurrentImage);
                    }
                }, 500);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (sCurrentImage == 0) { //detects whether the user is trying to set a mood greater than the max mood and deals with the choice
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(1000);
            rotate.setInterpolator(new LinearInterpolator());
            mMoodImageView.startAnimation(rotate);
            Toast.makeText(this, "You cant get any sadder. Do something to cheer yourself up :)", Toast.LENGTH_LONG).show();
        } else {
            sCurrentImage -= 1;
            mMoodImageView.startAnimation(animation);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //method to create options menu and inflate the menu layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //detects which menu item is selected and performs necessary actions based on the choice
        switch (item.getItemId()) {
            case R.id.help:
                if (!mFragmentIsShowing) {
                    HelpFragment.createHelpFragment(this);
                }
                break;
            case R.id.about:
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View aboutDiagView = inflater.inflate(R.layout.about_dialog, null);

                AlertDialog.Builder aboutDialog = new AlertDialog.Builder(MainActivity.this, R.style.MyDialog).setView(aboutDiagView);
                aboutDialog.setView(aboutDiagView)
                        .setPositiveButton("OK", null)
                        .create()
                        .show();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override //detects a touch even and returns as a gesture detected to detect swipes
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
    //These methods arent used in this application ************************************************************************************************************
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    } // this method isnt used in this app but needs to be in the code
    @Override
    public void onShowPress(MotionEvent e) {
    }// this method isnt used in this app but needs to be in the code
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }// this method isnt used in this app but needs to be in the code
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }// this method isnt used in this app but needs to be in the code
    @Override
    public void onLongPress(MotionEvent e) {
    }// this method isnt used in this app but needs to be in the code
    //*********************************************************************************************************************************************************

    @Override
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float X, float Y) { //method determines if the user swipes up or down by the use of Y co-ordinates of the gesture
        if (motionEvent1.getY() - motionEvent2.getY() > 50) {
            selectImageUp();
            return true;
        }
        if (motionEvent2.getY() - motionEvent1.getY() > 50) {
            selectImageDown();
            return true;
        }
        return false;
    }

    public void selectColour(int colorIdentifier) { //Method to set background colour based on the mood selected
        switch (colorIdentifier) {
            case 0:
                mMoodLayout.setBackgroundColor(getResources().getColor(R.color.pastelRed));
                break;
            case 1:
                mMoodLayout.setBackgroundColor(getResources().getColor(R.color.pastelOrange));
                break;
            case 2:
                mMoodLayout.setBackgroundColor(getResources().getColor(R.color.pastelBlue));
                break;
            case 3:
                mMoodLayout.setBackgroundColor(getResources().getColor(R.color.pastelYellow));
                break;
            case 4:
                mMoodLayout.setBackgroundColor(getResources().getColor(R.color.pastelGreen));
                break;
        }
    }

    //save existing state for when app is recreated - this method will save the current mood, if its first run or not and the notes into shared preferences
    @SuppressLint("ApplySharedPref")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentIsShowing = false;

        SharedPreferences.Editor sharedEditor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
        sharedEditor.putInt("mood", sCurrentImage).putBoolean("mFirstRun", mFirstRun).putString("sNote", getsNote());
        //the mood history arraylsit and total mood history array list are also saved by first converting the arrays and their objects into JSon format and storing this as a string
        Gson gson = new Gson();
        String json =gson.toJson(sMoodHistoryArrayL);
        sharedEditor.putString("sMoodHistoryArrayL", json);

        String totalMoodHist = gson.toJson(sTotalMoodHistoryArrayL);
        sharedEditor.putString("totalmoodHistory", totalMoodHist).commit();
    }


    public static void clear() { //method used to reset notes which is called at midnight to prepare the app for the next day
        sNote = "";
    }

    static int getsCurrentImage() { //getter for the static class member sCurrentImage
        return sCurrentImage;
    }
    static String getsNote() {//getter for the static class member sNotes;
        return sNote;
    }
}