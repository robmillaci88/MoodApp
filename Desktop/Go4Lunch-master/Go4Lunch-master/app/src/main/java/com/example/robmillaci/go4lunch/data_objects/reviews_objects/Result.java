package com.example.robmillaci.go4lunch.data_objects.reviews_objects;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Used in parsing the data retrieved from {@link com.example.robmillaci.go4lunch.web_service.GetDataService#getReviews(String, String, String)}
 */
public class Result {

    @SerializedName("reviews")
    @Expose
    private List<Review> reviews;

    public List<Review> getReviews() {
        return reviews;
    }

}
