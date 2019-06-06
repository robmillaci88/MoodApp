package com.example.robmillaci.MoodTracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class MoodTest {
    Mood mood;

    @Before
    public void setUp() throws Exception {
        mood = new Mood("test", 4, new Date()); //test instantiation of the class
    }

    @After
    public void tearDown() throws Exception {
        mood = null;
    }

    @Test
    //test that the mood history isn't null on creation of the Mood class
    public void getsMoodHistory() {
        assertNotNull(Mood.getsMoodHistoryArrayL());

    }

    //test that the total mood history isnt null on creation of the Mood class
    @Test
    public void getsTotalMoodHistory() {
        assertNotNull(Mood.getsTotalMoodHistoryArrayL());

    }


}