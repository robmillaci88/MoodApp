package com.example.robmillaci.MoodTracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

/***
 * BroadcastReceiver class that is used in the AlarmManager. On receipt of the broadcast, a new
 * object of Mood is created containing the note and mood for that day
 */
public class Schedule extends BroadcastReceiver {
    static AlarmManager alarmManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        //get the date of creation of mood object (this will be the previous day as this method will be called at midnight
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE,-1);
        Date d = c.getTime();

        //Create a new instance of Mood class passing the sNote and the current image (mood) for the day
         new Mood(MainActivity.getsNote(),MainActivity.getsCurrentImage(), d);


    }

    static void scheduleAlarm(Context mcontext){

        // Create a calendar object that is 0 hours 0 minutes and 0 seconds of the next day which allows us to get the time in milliseconds until midnight
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long scheduleTime = c.getTimeInMillis();

        // Create an Intent and set the class that will execute when the Alarm triggers. Here we have
        // specified Schedule class in the Intent. The onReceive() method of this class will execute when the broadcast from the alarm is received.
        Intent intentAlarm = new Intent(mcontext, Schedule.class);

        // Get the Alarm Service.
         alarmManager = (AlarmManager) mcontext.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm for the specified timeUntilMidnight passing a pending intent with the Schedule.class intent (reciever)
        // Set the reocurance of the alarm to be 24 hours 1000*60*60*24

        long alarmRecurranceTime = 1000*60*60*24;

        alarmManager.setRepeating(AlarmManager.RTC, scheduleTime, alarmRecurranceTime,PendingIntent.getBroadcast(mcontext, 1, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT));



    }
}