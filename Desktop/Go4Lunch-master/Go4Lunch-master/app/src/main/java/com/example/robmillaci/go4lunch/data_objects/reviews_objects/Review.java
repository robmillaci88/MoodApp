package com.example.robmillaci.go4lunch.data_objects.reviews_objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Used in parsing the data retrieved from {@link com.example.robmillaci.go4lunch.web_service.GetDataService#getReviews(String, String, String)}
 */
public class Review {

    @SerializedName("author_name")
    @Expose
    private String authorName;
    @SerializedName("profile_photo_url")
    @Expose
    private String profilePhotoUrl;
    @SerializedName("rating")
    @Expose
    private Integer rating;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("time")
    @Expose
    private Integer time;

    public String getAuthorName() {
        return authorName;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public Integer getRating() {
        return rating;
    }

    public String getText() {
        return text;
    }

    public Integer getTime() {
        return time;
    }


}
