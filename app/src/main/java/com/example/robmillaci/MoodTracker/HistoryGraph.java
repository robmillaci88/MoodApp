package com.example.robmillaci.MoodTracker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * This activity handles the display of the past 28 days of mood history data
 * It uses a custom API 'com.github.PhilJay:MPAndroidChart:v3.0.3' to generate a pie chart
 */
public class HistoryGraph extends AppCompatActivity {
    private int veryBadMoodPercent = 0; //holds the total amount of very bad moods for the past 28 days
    private int badMoodPercent = 0;     //holds the total amount of bad moods for the past 28 days
    private int decentMoodPercent = 0;  //holds the total amount of decent moods for the past 28 days
    private int goodMoodPercent = 0;    //holds the total amount of good moods for the past 28 days
    private int veryGoodMoodPercent = 0;//holds the total amount of very good moods for the past 28 days

    private PieChart pieChart; //the pie chart to display

    private List<Mood> mMoodsList = Mood.getsTotalMoodHistoryArrayL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_graph);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Mood History"); //sets the title of the activity

        calculateMoodPercentage(mMoodsList); //calculates the total mood values to use as a data set for the pie chart
        this.pieChart = findViewById(R.id.piechart_1);
        setPieChart(veryBadMoodPercent, badMoodPercent, decentMoodPercent, goodMoodPercent, veryGoodMoodPercent); //sets the mood values as a dataset in the pie chart

    }

    //loop through the entries in sTotalMoodHistoryArrayL from the Mood class and define the number of different moods found over the last 28 days
    private void calculateMoodPercentage(List<Mood> moodList) {
        for (Mood m : moodList) {
            switch (m.getMood()) {
                case 0:
                    veryBadMoodPercent++;
                    break;
                case 1:
                    badMoodPercent++;
                    break;
                case 2:
                    decentMoodPercent++;
                    break;
                case 3:
                    goodMoodPercent++;
                    break;
                case 4:
                    veryGoodMoodPercent++;
                    break;
            }
        }
    }


    //Method to create the pie chart and change properties to suit requirements
    private void setPieChart(int mood1, int mood2, int mood3, int mood4, int mood5) {
        //Sets the pie chart to use percentages
        pieChart.setUsePercentValues(true);

        //set offests for the pie chart layout
        pieChart.setExtraOffsets(5, 5, 5, 5);

        //set the speed at which the graph decelerates after drag
        pieChart.setDragDecelerationFrictionCoef(0.9f);

        //sets the alpha value of the transparent inner circle
        pieChart.setTransparentCircleRadius(61f);

        //sets the color of the center hole of the graph
        pieChart.setHoleColor(Color.WHITE);

        //sets an animation when drawing the graph
        pieChart.animateY(2000, Easing.EasingOption.EaseInOutCubic);

        // disable the pie chart description label
        pieChart.getDescription().setEnabled(false);

        //prepare the colours for assignment to the graph segments
        int red = getResources().getColor(R.color.pastelRed);
        int orange = getResources().getColor(R.color.pastelOrange);
        int blue = getResources().getColor(R.color.pastelBlue);
        int yellow = getResources().getColor(R.color.pastelYellow);
        int green = getResources().getColor(R.color.pastelGreen);

        //add the colors to an arrayList so they can be accessed in a loop
        ArrayList<Integer> colorsArray = new ArrayList<>();
        colorsArray.add(red);
        colorsArray.add(orange);
        colorsArray.add(blue);
        colorsArray.add(yellow);
        colorsArray.add(green);

        //create a LinkedHashSet that will only hold unique mood values so we can correctly set the pie chart colors. LinkedHashSet will preserve the order of
        //the added elements
        LinkedHashSet<Integer> uniqueMoods = new LinkedHashSet<>();

        //loop through each mood and add the mood int to the hash set, only unique values will be added so we will end up with a hashset of up to 5 moods in order
        for (Mood m : Mood.sTotalMoodHistoryArrayL) {
            uniqueMoods.add(m.getMood());
        }

        //5 local variables so we can assign the values for the colors in the hashSet iteration
        //initialized to 10,11,12,13,14 because these numbers will never exist in the mood list but they need to exist in the sortedArray so
        //the arraylist index does not throw an exception. Also needed so the correct colors are set, essentially 10,11,12,13,14 will be ignored
        //and once the unique mood exists, the initialized value will be overwritten.
        int color1 = 0;
        int color2 = 1;
        int color3 = 2;
        int color4 = 3;
        int color5 = 4;

        //create an iterator to iterate over the uniqueMoods hash set assigning the colors to the values
        try {
            Iterator it = uniqueMoods.iterator();
            color1 = (int) it.next();
            color2 = (int) it.next();
            color3 = (int) it.next();
            color4 = (int) it.next();
            color5 = (int) it.next();

        } catch (Exception e) {
            //expected exception until 5 unique moods exist
        }

        TreeSet<Integer> sortedColorSet = new TreeSet<>();
        sortedColorSet.add(color1);
        sortedColorSet.add(color2);
        sortedColorSet.add(color3);
        sortedColorSet.add(color4);
        sortedColorSet.add(color5);

        //convert the sortedColor tree set into an arraylist so we can easily .get() values required without needing to iterate.
        ArrayList<Integer> sortedColorArray = new ArrayList<>(sortedColorSet);

        //create an array list to hold the Piechart data set
        ArrayList<PieEntry> yValues = new ArrayList<>();
        if (mood1 != 0) yValues.add(new PieEntry(mood1, "Very bad mood"));
        if (mood2 != 0) yValues.add(new PieEntry(mood2, "Bad mood"));
        if (mood3 != 0) yValues.add(new PieEntry(mood3, "Decent mood"));
        if (mood4 != 0) yValues.add(new PieEntry(mood4, "Good mood"));
        if (mood5 != 0) yValues.add(new PieEntry(mood5, "Very good mood"));
        //create the data set from the pie entry array defined above
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(10f);
        dataSet.setSelectionShift(5f);


        //set the pie chart colors based on the result of iterating through sorted unique moods tree set

        switch (sortedColorArray.size()) {
            case 1:
                dataSet.setColors(colorsArray.get(sortedColorArray.get(0)));
                break;

            case 2:
                dataSet.setColors(colorsArray.get(sortedColorArray.get(0)),
                colorsArray.get(sortedColorArray.get(1)));
                break;

            case 3:
                dataSet.setColors(colorsArray.get(sortedColorArray.get(0)),
                colorsArray.get(sortedColorArray.get(1)),
                colorsArray.get(sortedColorArray.get(2)));
                break;

            case 4:
                dataSet.setColors(colorsArray.get(sortedColorArray.get(0)),
                colorsArray.get(sortedColorArray.get(1)),
                colorsArray.get(sortedColorArray.get(2)),
                colorsArray.get(sortedColorArray.get(3)));
                break;

            case 5:
                dataSet.setColors(colorsArray.get(sortedColorArray.get(0)),
                colorsArray.get(sortedColorArray.get(1)),
                colorsArray.get(sortedColorArray.get(2)),
                colorsArray.get(sortedColorArray.get(3)),
                colorsArray.get(sortedColorArray.get(4)));
                break;
        }

        PieData pieData = new PieData(dataSet); //create a new instance of PieData passing the data set


        pieData.setValueTextSize(10f); //set the text sie of the pie chart values


        pieData.setValueTextColor(Color.BLACK); //set the color of the pie chart text


        pieChart.setData(pieData); //set the data to the pie chart


        //PieChart Ends Here
    }
}
