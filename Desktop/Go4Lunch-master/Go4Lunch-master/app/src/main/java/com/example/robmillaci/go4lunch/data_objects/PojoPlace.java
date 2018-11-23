package com.example.robmillaci.go4lunch.data_objects;

import com.example.robmillaci.go4lunch.web_service.HtmlParser;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import static com.example.robmillaci.go4lunch.activities.MainActivity.GOOGLE_MAPS_FRAGMENT;
import static com.example.robmillaci.go4lunch.activities.MainActivity.RESTAURANT_LIST_FRAGMENT;

/**
 * This class is used to convert {@link com.google.android.gms.location.places.Place} into plain old java object Places
 * This allows us to close the {@link com.google.android.gms.location.places.PlaceBufferResponse} to prevent memory leaks
 * It also allows us to make the minimum amount of requests for Google places
 */
public class PojoPlace implements Serializable {
    public static final String PLACE_SERIALIZABLE_KEY = "PojoPlace"; //the key for storing and retrieving PojoPlace objects
    public static final String PLACE_ID_KEY = "placeID"; //the key for storing the place ID
    public static final String PLACE_NAME_KEY = "placeName"; //the key for storing the place name

    private final String name;
    private final String address;
    private final String website;
    private final String phoneNumber;
    private final float rating;
    private final String id;
    private final double latitude;
    private final double longitude;
    private String placeType;
    private boolean placeParsed = false;

    /**
     * Constructor for PojoPlace. If the calling fragment RESTAURANT_LIST_FRAGMENT or GOOGLE_MAPS_FRAGMENT we will parse the place type by calling {@link HtmlParser}<br>
     * From these fragments we can deal with the slight timing delay it takes to parse the HTML as it doesnt affect UX<br>
     * Other fragments that call this constructor cannot deal with the timing delay as it causes visual stuttering. Therefore for these instances the place type is
     * parsed on a separate thread.
     *
     * @param name              name of the place
     * @param address           address of the place
     * @param website           website of the place
     * @param phoneNumber       phone number of the place
     * @param placeType         the place type
     * @param rating            the rating of the place
     * @param id                the id of the place
     * @param latitude          the latitude of the place
     * @param longitude         the longitude of the place
     * @param callingFragmentId the fragment that called the creation of this place
     */

    public PojoPlace(String name, String address, String website, String phoneNumber, String placeType,
                     float rating, String id, double latitude, double longitude, String callingFragmentId) {
        this.name = name;
        this.address = address;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.rating = rating;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeType = placeType;
        if (callingFragmentId.equals(RESTAURANT_LIST_FRAGMENT) || callingFragmentId.equals(GOOGLE_MAPS_FRAGMENT)) {
            parsePlaceType();
        }

    }

    /**
     * see {@link HtmlParser#execute(Object[])} and {@link HtmlParser}
     */
    private void parsePlaceType() {
        String[] placeId = new String[]{this.id};
        try {
            this.placeType = new HtmlParser().execute(placeId).get();
            this.placeParsed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPlaceType() {
        return placeType;
    }

    public String getId() {
        return id;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public float getRating() {
        return rating;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public void setPlaceParsed(boolean placeParsed) {
        this.placeParsed = placeParsed;
    }

    public boolean isPlaceParsed() {
        return placeParsed;
    }

}
