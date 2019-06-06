package com.example.robmillaci.MoodTracker;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/*
 * This class is a subclass of Fragment which deals with the help fragment that is displayed on the first run of the app
 * it contains 2 image views , swipe up and clickimage that are used to show the user how to perform actions
 * 2 animations are also created that are applied to the image views for a better user experience
 */
public class HelpFragment extends Fragment {
    View mFragmentView; //the view will assigned to the inflated fragment_help layout
    ImageView swipeUp;
    ImageView clickimage;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         mFragmentView = inflater.inflate(R.layout.fragment_help, container, false);

         swipeUp = mFragmentView.findViewById(R.id.swipeUp);
         clickimage = mFragmentView.findViewById(R.id.click);


         //comment these lines out prior to testing************************************************
        swipeUp.startAnimation(creatTranslateAnimation(0,0,0,200));
        clickimage.startAnimation(creatTranslateAnimation(-200,0,-200,0));
         //****************************************************************************************

        return mFragmentView;
    }

    //method to create the help fragment and set the onclick listener
    public static void createHelpFragment(MainActivity c) {
        final MainActivity activity = c;
        final ConstraintLayout fragmentContainer = activity.findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        final HelpFragment helpFragment = new HelpFragment();
        FragmentManager manager = c.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragmentContainer, helpFragment).commit();

        activity.mFirstRun = false; //this is no longer the first run of the app so mFirstRun needs to be set to false
        activity.mFragmentIsShowing = true; //the fragment is now showing so mFragmentIsShowing is set to true

        fragmentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //on click listener that closes the fragment so a user can begin setting moods
                FragmentManager manager = activity.getSupportFragmentManager();
                final FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(helpFragment).commit();
                fragmentContainer.setVisibility(View.GONE);
                activity.mFragmentIsShowing = false; // the fragment is now closed so mFragmentIsShowing is set to false
            }
        });
    }

    public Animation creatTranslateAnimation(int fromX, int toX, int fromY, int toY){
        Animation anim = new TranslateAnimation(fromX, toX, fromY, toY);
        anim.setDuration(2000);
        anim.setRepeatCount(50);
        anim.setFillAfter(false);

        return anim;
    }

}