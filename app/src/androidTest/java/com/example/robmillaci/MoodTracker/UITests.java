package com.example.robmillaci.MoodTracker;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;

import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class UITests {

    /**
     * Ensure you all disable help fragment animation before running
     * they can fail due to Espresso and animations not functioning correctly
     * {@link HelpFragment#onCreateView}
     */

    /**
     * This class contains all the UI tests for the app.
     */

    //Test rule creating a test instance of MainActivity class
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);


    //Set up to be run before each test to ensure the state is ready for testing
    @Before
    public void setup() {
        //this removes the help fragment if it is showing
        boolean f = mActivityRule.getActivity().getSupportFragmentManager().getFragments().isEmpty();
        if (!f) {
            Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        }

        //Remove any mood history if it exists - If the test requires mood history this is explicity generated within the test that requires the history
        if (Mood.sMoodHistoryArrayL != null) { //ensure the Mood mood History arrays are cleared before each test
            Mood.sMoodHistoryArrayL.clear();
        }
        if (Mood.sTotalMoodHistoryArrayL != null) { //ensure the Mood mood History arrays are cleared before each test
            Mood.sTotalMoodHistoryArrayL.clear();
        }
    }


    @Test
    //Check the views within MainActivity are displayed on opening of the app
    public void AinstantiateViews() {
        ViewInteraction moodimageView = Espresso.onView(ViewMatchers.withId(R.id.moodImageView));
        ViewInteraction moodLayout = Espresso.onView(ViewMatchers.withId(R.id.moodLayout));

        moodimageView.check(matches(isEnabled()));
        moodLayout.check(matches(isEnabled()));

    }


    @Test
    //Check that the help fragment is shown if the app is run for the first time
    public void BhelpOnStart() {
        boolean f = mActivityRule.getActivity().getSupportFragmentManager().getFragments().isEmpty();
        if (mActivityRule.getActivity().mFirstRun) {
            assertTrue(!f);
        }

    }


    @Test
    //Tests the outcome of a user swiping up. This test stores the tag of the image before the swipe up, performs 2 swipes and then verifies the tag
    //after is different
    public void CSwipeUp() throws InterruptedException {
        //check that the image is changed when the user swipes up
        int tagBefore = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());

        Thread.sleep(1000);

        int tagAfter = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        assertTrue(tagBefore != tagAfter);
    }


    @Test
    //This test checks the outcome of a user swiping up to the maximum mood value and checks that the image doesn't change when swipe up is performed on the maximum mood
    public void DSwipeUpMax() throws InterruptedException {
        //swipe up to the max mood (very happy)
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());
        Thread.sleep(1000);

        //obtain the tag of the maximum mood
        int tagBefore = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        //swipe up from the maximum mood
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeUp());
        Thread.sleep(1000);
        int tagAfter = (int) mActivityRule.getActivity().mMoodImageView.getTag();


        //does the maximum mood tag change - for this test to pass the tagBefore and tagAfter must equal
        assertTrue(tagBefore == tagAfter);

    }

    @Test
    //Tests the outcome of a user swiping down. This test stores the tag of the image before the swipe down, performs 2 swipes down and then verifies the tag
    //after is different
    public void ESwipeDown() throws InterruptedException {
        //check that the image is changed when the user swipes down
        int tagBefore = (int) mActivityRule.getActivity().mMoodImageView.getTag();
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Thread.sleep(1000);
        int tagAfter = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        //this test passes if the tag before is NOT equal to the tag after the image changes
        assertTrue(tagBefore != tagAfter);
    }

    @Test
    //This test checks the outcome of a user swiping down to the minimum mood value and checks that the image doesn't change when swipe dowm is performed on the minimum mood
    public void FSwipeDownMax() throws InterruptedException { //check that the image doesnt change when swipe down is performed on the minimum mood
        //swipe down to the min mood (very bad mood)
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Thread.sleep(1000);

        int tagBefore = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Espresso.onView(ViewMatchers.withId(R.id.moodImageView)).perform(ViewActions.swipeDown());
        Thread.sleep(1000);

        int tagAfter = (int) mActivityRule.getActivity().mMoodImageView.getTag();

        assertTrue(tagBefore == tagAfter);
    }

    @Test
    //check the fabmenu1 (notes) button opens a 'add note' dialog
    public void GaddNote() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(ViewMatchers.withId(R.id.fabmenu1)).perform(click());

        Espresso.onView(withId(R.id.dialogView)).check(matches(isDisplayed()));
    }


    @Test
    //check that the history activity is launched when the fabmenu2 is clicked
    public void HhistorySevenDaysNoData() throws InterruptedException {

        //if the help fragment is showing, first remove it
        boolean f = mActivityRule.getActivity().getSupportFragmentManager().getFragments().isEmpty();
        if (!f) {
            Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        }

        //perform the click on the FAB
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Intents.init();
        Thread.sleep(1000);

        //If no history is present, check that a toast is displayed
        if (Mood.getsMoodHistoryArrayL() == null || Mood.getsMoodHistoryArrayL().size() == 0) {
            Espresso.onView(ViewMatchers.withId(R.id.fabmenu2)).perform(click());
            Espresso.onView(withText(R.string.no_history)).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));

        } else {

            //if history is present (will be when the test HhistorySevenDaysWithData is run) check that the history class is launched
            Espresso.onView(ViewMatchers.withId(R.id.fabmenu2)).perform(click());
            intended(hasComponent(History.class.getName()));

            //now testing the recycler view items - testing that the expected number of values in the recycler view is as expected based on the number of moods added
            Espresso.onView(withId(R.id.moodRecyclerView)).check(new RecyclerViewItemCountAssertion(3));
        }

        Intents.release();
    }


    @Test
    public void HhistorySevenDaysWithData() { //same as test above but now with data present to test the recycler view and the History class

        Mood a = new Mood("testMood", 1, new Date());
        Mood b = new Mood("testMood", 1, new Date());
        Mood c = new Mood("testMood", 1, new Date());


        try {
            HhistorySevenDaysNoData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    //Test that adding a note to the mood is saved
    public void addNoteSaved() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(ViewMatchers.withId(R.id.fabmenu1)).perform(click());
        Thread.sleep(1000);

        Espresso.onView(withId(R.id.dialogView)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.notesText)).perform(ViewActions.typeText("Test note"));
        Espresso.onView(withText("OK")).inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        //re-open the dialog box and check the saved note above is present
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(ViewMatchers.withId(R.id.fabmenu1)).perform(click());
        Thread.sleep(1000);

        Espresso.onView(withId(R.id.dialogView)).check(matches(isDisplayed()));

        String textString = mActivityRule.getActivity().mNotesText.getText().toString();

        assertTrue(textString.equals("Test note"));

    }

    @Test
    //Test that a long press on the recycler view causes the notes dialog to be displayed
    public void longPressRecyclerView() throws InterruptedException {

        Mood a = new Mood("testMood", 1, new Date());
        Mood b = new Mood("testMood", 1, new Date());
        Mood c = new Mood("testMood", 1, new Date());

        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(ViewMatchers.withId(R.id.fabmenu2)).perform(click());
        Thread.sleep(1000);

        Espresso.onView(withId(R.id.moodRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()))
                .inRoot(isDialog()).check(matches(isDisplayed()));

    }

    @Test
    //Checks that the History seven days of data doesnt exceed 5 numbers of items when presented with more than 5 moods
    public void HistorySevenDaysDataMax() throws InterruptedException {
        //add 10 moods
        Mood a = new Mood("testMood", 0, new Date());
        Mood b = new Mood("testMood", 1, new Date());
        Mood c = new Mood("testMood", 3, new Date());
        Mood d = new Mood("testMood", 4, new Date());
        Mood e = new Mood("testMood", 0, new Date());
        Mood f = new Mood("testMood", 1, new Date());
        Mood g = new Mood("testMood", 2, new Date());
        Mood h = new Mood("testMood", 3, new Date());
        Mood i = new Mood("testMood", 4, new Date());
        Mood j = new Mood("testMood", 0, new Date());

        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);

        Espresso.onView(ViewMatchers.withId(R.id.fabmenu2)).perform(click());

        //now testing the recycler view items - testing that the recycler view holds only 7 Moods even though we have added 10 above
        Espresso.onView(withId(R.id.moodRecyclerView)).check(new RecyclerViewItemCountAssertion(7));

    }

    @Test
    public void IhistoryGraphShown() throws InterruptedException { //checks IF there is history, that the history graph is shown. disable graph animation before testing

        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(ViewMatchers.withId(R.id.fabmenu2)).perform(click());

        if (Mood.getsMoodHistoryArrayL() == null || Mood.getsMoodHistoryArrayL().size() == 0) {
            Espresso.onView(withText(R.string.no_history)).inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));

            //Do the test again with mood data. This also tests that the history graph isnt null on creation of the HistoryGraph class
            Mood a = new Mood("testMood", 0, new Date());
            Mood b = new Mood("testMood", 1, new Date());
            Mood c = new Mood("testMood", 3, new Date());
            Mood d = new Mood("testMood", 4, new Date());

            IhistoryGraphShown();

        } else {
            Intents.init();
            Espresso.onView(ViewMatchers.withId(R.id.histGraph)).perform(click());
            Thread.sleep(1000);
            intended(hasComponent(HistoryGraph.class.getName()));
            Intents.release();
        }


    }

    @Test
    //check that the help fragment is shown when the menu item 'help' is clicked. Animations need to be disabled first
    public void JhelpFragmentShown() throws InterruptedException {

        Espresso.onView(ViewMatchers.withId(R.id.help)).perform(click());

        Thread.sleep(1000);
        boolean f = mActivityRule.getActivity().getSupportFragmentManager().getFragments().isEmpty();

        assertTrue(!f);
    }

    @Test
    //check that the about dialog is shown when the menu item 'About' is clicked
    public void KaboutDiagShown() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

        Espresso.onView(ViewMatchers.withText("About")).perform(click());

        Espresso.onView(ViewMatchers.withText("About 'Mood Tracker'")).check(matches(isDisplayed()));
    }


    @Test
    //check that the alarm has been scheduled after onCreate() of main activity has run
    public void AlarmScheduled (){
        assertTrue(Schedule.alarmManager != null );
    }

}



