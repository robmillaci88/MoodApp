package com.example.robmillaci.go4lunch.data_objects.places_details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Used in parsing the data retrieved from {@link com.example.robmillaci.go4lunch.web_service.GetDataService#getDetails(String, String, String)}
 */
public class Result {
    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    public OpeningHours getOpeningHours() {
        return openingHours;
    }
}
