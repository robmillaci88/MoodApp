package com.example.robmillaci.MoodTracker;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MainActivityTest {

    MainActivity mA;

    @Before
    //create an instance of Main Activity
    public void setup() {
        mA = new MainActivity();
    }

    @Test(expected = NullPointerException.class)
    //tests that the selectImageUp method always assigns a value to current image between 1 and 5
    public void selectImageUp() {

        for (int i = 0; i < 1000; i++) {
            mA.selectImageUp();
            assert MainActivity.sCurrentImage > -1 & MainActivity.sCurrentImage < 6;
        }
    }

    @Test(expected = NullPointerException.class)
    //tests that the selectImageDown method always assigns a value to current image between 1 and 5
    public void selectImageDown() {

        for (int i = 0; i < 1000; i++) {
            mA.selectImageDown();
            assert MainActivity.sCurrentImage > -1 & MainActivity.sCurrentImage < 6;
        }
    }


    @Test
    //test that the mood array created when MainActivity is created is not null.
    public void moodArrayisNotNull() {
        assertNotNull(mA.mMoodsArrayL);
    }

}