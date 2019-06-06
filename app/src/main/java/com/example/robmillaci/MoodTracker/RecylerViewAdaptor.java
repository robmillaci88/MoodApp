package com.example.robmillaci.MoodTracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * This class controls the data in the recycler view in the History class
 * It also contains a static inner class 'MyViewHolder' which is responsible for assigning the view objects inflated to their ID's
 * The MyViewHolder objects created are pass to the RecyclerViewAdaptors 'onBindViewHolder' method which controls the data appearing in each Reclyer view entry
 */
class RecylerViewAdaptor extends RecyclerView.Adapter<RecylerViewAdaptor.MyviewHolder> {
    private List moods; //private class member to hold a list of moods
    private Context mContext; //private class member to store the context
    private boolean mIsLongPressed; //private boolean to determine if the user is performing a long press or not
    private AlertDialog mNotepad; //private alert dialog which is used after long press to display an alert pop up to the user so they can view their entire note

    RecylerViewAdaptor(Context context, List moods) {
        this.moods = moods;
        this.mContext = context;
    }


    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //Creates the holder that the view will be inflated into
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recycler_view, parent, false);

        return new MyviewHolder(view);//returns a new object of static inner class to the 'OnBindViewHolder method
    }

    @Override
    public void onBindViewHolder(@NonNull final MyviewHolder holder, int position) { //this method deals with setting the view properties from the view object returned from 'OnCreateViewHolder'
        final Mood mood = (Mood) moods.get(position); //access the Mood object in the moods arraylist at a certain position
        int moodInt = mood.getMood(); //gets the Mood stored in the mood object from the arraylist
        Date date = mood.getDate(); //gets the date of the mood from the Mood object stored in the arraylist
        String noteText = mood.getNote(); //gets the note of the mood from the Mood object stored in the arraylist

        switch (moodInt) { //switch to determine which mood was obtained and perform neccessary UI changes
            case 0:
                holder.moodPic.setBackgroundResource(R.drawable.very_bad_mood);
                holder.moodColourBar.setBackgroundColor(mContext.getResources().getColor(R.color.pastelRed));
                break;
            case 1:
                holder.moodPic.setBackgroundResource(R.drawable.bad_mood);
                holder.moodColourBar.setBackgroundColor(mContext.getResources().getColor(R.color.pastelOrange));
                break;
            case 2:
                holder.moodPic.setBackgroundResource(R.drawable.decent_mood);
                holder.moodColourBar.setBackgroundColor(mContext.getResources().getColor(R.color.pastelBlue));
                break;
            case 3:
                holder.moodPic.setBackgroundResource(R.drawable.good_mood);
                holder.moodColourBar.setBackgroundColor(mContext.getResources().getColor(R.color.pastelYellow));
                break;
            case 4:
                holder.moodPic.setBackgroundResource(R.drawable.great_mood);
                holder.moodColourBar.setBackgroundColor(mContext.getResources().getColor(R.color.pastelGreen));
                break;
        }
        holder.moodDate.setText(android.text.format.DateFormat.format("dd-MM-yyyy", date));
        holder.notesText.setText(noteText);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() { //sets on long click listener which responds by displaying an Alert dialog containing the full note saved for the Mood object
            @Override
            public boolean onLongClick(View v) {
                View notepadView = LayoutInflater.from(mContext).inflate(R.layout.pop_up_note, null);
                AlertDialog.Builder notepad = new AlertDialog.Builder(mContext, R.style.dialogInvisible);
                notepad.setView(notepadView);

                mNotepad = notepad.create();
                mNotepad.getWindow().setBackgroundDrawable(new ColorDrawable(0));


                if (!mNotepad.isShowing()) {
                    mNotepad.show();
                }
                TextView popupText = notepadView.findViewById(R.id.popup_note_text);

                String note = holder.notesText.getText().toString();
                if (note.equals("")) {
                    popupText.setText(R.string.no_note_save);
                } else {
                    popupText.setText(note);
                }
                //setting mIsLongPressed here so that we know a user has long pressed the view holder. This then means the
                //onTouchListener below can perform the actions associated with when the user removes their finger
                mIsLongPressed = true;
                return true;
            }

        });

        //set an on touch listener to the view holder, this wont do anything until both the following conditions are met
        //1. The touch event was ACTION_UP (the user released their finger)
        //2. mIsLongPressed is TRUE
        //if both of these conditions are satisfied then the alert dialog is dismissed and mIsLongPressed is reverted to false
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                //We are interested here when the finger is released over the dialog
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //If any view is currently being pressed
                    if (mIsLongPressed) {
                        //do something when the button is released
                        mNotepad.dismiss();
                        mNotepad.dismiss();
                        mIsLongPressed = false;
                    }
                }
                return true;
            }
        });
    }


    @Override
    public int getItemCount() { //returns the size of the mood array list that the onBindViewHolder uses

        return moods.size();
    }


    /**
     * Static inner class MyViewHolder contains the views within each view holder
     * The constructor of this class only assigns the class members to their ID's
     * allowing onBindViewHolder method to set the relevant parameters for the views
     */
    static class MyviewHolder extends RecyclerView.ViewHolder {
        ImageView moodColourBar;
        ImageView moodPic;
        TextView moodDate;
        TextView notesText;

        private MyviewHolder(View itemView) {

            super(itemView);
            this.moodColourBar = itemView.findViewById(R.id.mood_color_bar);
            this.moodPic = itemView.findViewById(R.id.moodPic);
            this.moodDate = itemView.findViewById(R.id.moodDate);
            this.notesText = itemView.findViewById(R.id.notesTextView);
        }
    }

}
