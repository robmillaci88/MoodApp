package com.example.robmillaci.go4lunch.data_objects.reviews_objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Used in parsing the data retrieved from {@link com.example.robmillaci.go4lunch.web_service.GetDataService#getReviews(String, String, String)}
 */
public class Reviews {
    @SerializedName("result")
    @Expose
    private Result result;

    public Result getResult() {
        return result;
    }

}
