package com.example.robmillaci.go4lunch.data_objects.places_details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Used in parsing the data retrieved from {@link com.example.robmillaci.go4lunch.web_service.GetDataService#getDetails(String, String, String)}
 */
public class OpeningHours {

    @SerializedName("open_now")
    @Expose
    private Boolean openNow;
    @SerializedName("weekday_text")
    @Expose
    private final List<String> weekdayText = null;

    public Boolean getOpenNow() {
        return openNow;
    }

    public List<String> getWeekdayText() {
        return weekdayText;
    }


}
