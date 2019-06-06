package com.example.robmillaci.MoodTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * Class to store the current days final mMood and sNote
 * Controls a static arraylist containing up to 7 days worth of mMood and notes to display in history
 * Also controls a static arraylist containing 28 days (4 weeks) worth of moods for the History graph class
 * This class is instantiated once a day at midnight and the Mood object is stored in both sMoodHistoryArrayL and sTotalMoodHistoryArrayL
 */
    class Mood {
    private String mNote; //private String class variable to hold the note
    private int mMood; //private int class variable to hold the mood
    private Date mDate; //private Date class variable containing the date of creation of the class

    static List<Mood> sMoodHistoryArrayL;// static List of moods to facilitate storing of the last 7 days worth of mood objects created
    static List<Mood> sTotalMoodHistoryArrayL;// static List of moods to facilitate storing of the last 28 days worth of mood objects created

    public Mood(String note, int mood, Date date) { //class constructor
        this.mNote = note;
        this.mMood = mood;
        this.mDate = date;
        if (sMoodHistoryArrayL ==null){ //to avoid potential null pointer exceptions when arraylist.size() is called, if the array is null, it is assigned a new Arraylist
            sMoodHistoryArrayL = new ArrayList<>();
        }
        if (sTotalMoodHistoryArrayL ==null){ //to avoid potential null pointer exceptions when arraylist.size() is called, if the array is null, it is assigned a new Arraylist
            sTotalMoodHistoryArrayL = new ArrayList<>();
        }
        addMoodHistory(); //once the class has been instantiated the mood object is added to the Arraylists for the history and total history
        MainActivity.clear(); //resets the mood and notes for the start of a new day
    }

    //Ensures the mood history ArrayList doesn't hold more than 7 days worth of Mood objects, if the array list size is 7 or greater
    //the oldest (first entry) is remove
    //Also does the same for Total history except this can hold 28 days worth of mood data
    private void addMoodHistory() {
        try {
            if (sMoodHistoryArrayL.size() < 7) {
                sMoodHistoryArrayL.add(this);
            } else {
                sMoodHistoryArrayL.remove(0);
                sMoodHistoryArrayL.add(this);
            }

            if (sTotalMoodHistoryArrayL.size() < 28) {
                sTotalMoodHistoryArrayL.add(this);
            } else {
                sTotalMoodHistoryArrayL.remove(0);
                sTotalMoodHistoryArrayL.add(this);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    //Getters and setters to access data objects in this class
    public static List<Mood> getsMoodHistoryArrayL() {
        return sMoodHistoryArrayL;
    }

    public static List<Mood> getsTotalMoodHistoryArrayL() {
        return sTotalMoodHistoryArrayL;
    }

    public String getNote() {
        return mNote;
    }

    public int getMood() {
        return mMood;
    }

    public Date getDate() {
        return mDate;
    }

}
