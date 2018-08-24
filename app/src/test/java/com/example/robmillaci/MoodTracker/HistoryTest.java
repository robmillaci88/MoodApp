package com.example.robmillaci.MoodTracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class HistoryTest {
    History h;

    @Before
    public void setUp() throws Exception {
        h = new History();
        h.createAdaptor();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void onCreate() {
        assertNotNull(h.mAdaptor);
    }
}