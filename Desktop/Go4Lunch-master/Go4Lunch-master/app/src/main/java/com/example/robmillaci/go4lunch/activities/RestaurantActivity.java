package com.example.robmillaci.go4lunch.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.RestaurantActivityAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.example.robmillaci.go4lunch.utils.IphotoDownloadedCallback;
import com.example.robmillaci.go4lunch.utils.PhotoDownloader;
import com.example.robmillaci.go4lunch.web_service.HtmlParser;
import com.example.robmillaci.go4lunch.web_service.IhtmlParser;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.example.robmillaci.go4lunch.data_objects.PojoPlace.PLACE_SERIALIZABLE_KEY;
import static com.example.robmillaci.go4lunch.firebase.FirebaseHelper.DATABASE_SELECTED_RESTAURANT_FIELD;
import static com.example.robmillaci.go4lunch.firebase.FirebaseHelper.DATABASE_SELECTED_RESTAURANT_ID_FIELD;

/**
 * This activity is responsible for displaying the details of a specific place
 */
public class RestaurantActivity extends AppCompatActivity implements IphotoDownloadedCallback,
        FirebaseHelper.firebaseDataCallback, IhtmlParser {

    public static final String MARKER_UNSELECTED = "notSelected"; //Tag for marker when the marker is 'unselected' as place the user is eating
    public static final String MARKER_SELECTED = "selected"; //Tag for marker when the marker is 'selected' as place the user is eating

    private String phoneNumber; //The phone number of the place
    private String webaddress; //the web url of the place
    private String placeID; //the ID of the place

    private ImageView image; //The image of the place
    private FloatingActionButton selectedFab; //The action button to 'select' this place
    private TabLayout.Tab starLikeTab; //The tab to 'like' this place
    private RecyclerView peopleEatingRecyclerView; //Recycler view displaying the list of all friends 'eating' here

    private PojoPlace mPojoPlace; //The place this activity is displaying data for
    private Marker mMarker; //The marker that relates to this place
    private LatLng location; //The location (LatLng) of this place

    private FirebaseHelper firebaseHelper; //instance of firebase helper used in this class to retrieve the data
    private PhotoDownloader photoDownloader; //instance of PhotoDownloader in order to download the photo for this place


    /**
     * Instantiates {@link FirebaseHelper} and {@link PhotoDownloader}<br>
     * Create the recyclerview as well as all relevant views for this Activity<br>
     * Retrieves the specific googleMaps marker and stores a reference so we can 'select' or 'unselect' the marker {@link GoogleMapsFragment#getSpecificMarker(String)}<br>
     * Some data is unavailable with the GooglePlaces SDK. {@link #getAdditionalPlaceData()} retrieves this extra required data.<br>
     * Also handle the events of clicking on {@link #selectedFab} and the events of clicking the tabs<br>
     *
     * @param savedInstanceState saveInstance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        firebaseHelper = new FirebaseHelper(this);
        photoDownloader = new PhotoDownloader(this, null);

        peopleEatingRecyclerView = findViewById(R.id.peopleEatingRecyclerView); //RecyclerView displaying the users eating at this place
        ImageView backbtn = findViewById(R.id.backbtn);
        TextView name = findViewById(R.id.restaurant_name); //The place name

        image = findViewById(R.id.restaurant_image); //The place image
        RatingBar rating = findViewById(R.id.ratingBar); //The place rating bar
        TabLayout tabs = findViewById(R.id.action_tabs); //The tabs for this activity
        selectedFab = findViewById(R.id.restaurant_selected); //The tab to selected this restaurant as the 'eating at' place
        starLikeTab = tabs.getTabAt(1); //The tab to 'like' this place

        Bundle data = getIntent().getExtras();
        if (data != null) mPojoPlace = (PojoPlace) data.getSerializable(PLACE_SERIALIZABLE_KEY); //Restore the place passed to this activity in the calling intent

        if (mPojoPlace != null) {
            mMarker = GoogleMapsFragment.getSpecificMarker(mPojoPlace.getName()); //Retrieve the specific marker for this place

            // if the marker is null, create a new marker for this place
            if (mMarker == null) {
                MarkerOptions mOptions = new MarkerOptions()
                        .position(mPojoPlace.getLocation())
                        .title(mPojoPlace.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));

                Marker marker = GoogleMapsFragment.getmGoogleMap().addMarker(mOptions);

                GoogleMapsFragment.getAllMarkers().put(mOptions.getTitle(), marker);
                marker.setTag(MARKER_UNSELECTED);
                mMarker = marker;
            }

            // Set the name, rating, web url, placeId, phonenumber and location
            name.setText(mPojoPlace.getName());
            rating.setRating(mPojoPlace.getRating());
            webaddress = mPojoPlace.getWebsite();
            placeID = mPojoPlace.getId();
            phoneNumber = mPojoPlace.getPhoneNumber();
            location = mPojoPlace.getLocation();
        }


        getAdditionalPlaceData(); //see method comments


        // get the marker tag and adjust the UI as appropriate
        String markerTag;
        try {
            //noinspection ConstantConditions
            markerTag = mMarker.getTag().toString();
        } catch (Exception e) {
            markerTag = MARKER_UNSELECTED;
        }

        if (markerTag.equals(MARKER_SELECTED)) {
            selectedFab.setImageResource(R.drawable.checked);
        } else {
            selectedFab.setImageResource(R.drawable.add_restaurant);
        }

        selectedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMarker.getTag().toString().equals(MARKER_UNSELECTED)) {
                    /*
                    if the marker is unselected, set the tag to 'selected'
                    Change the marker drawable to green (to display it is selected)
                    Update the database with the new selected place by calling firebasehelper.getSelectedPlace()
                     */

                    selectedFab.setImageResource(R.drawable.checked);
                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                    mMarker.setTag(MARKER_SELECTED);

                    firebaseHelper.getSelectedPlace(FirebaseHelper.getmCurrentUserId(), null); //method calls back to 'finishedGettingPlace'

                } else if (mMarker.getTag().toString().equals(MARKER_SELECTED)) {
                    /*
                    if the marker is selected, set the tag to 'unselected'
                    Change the marker drawable to orange (to display it is not selected)
                    Update the database, removing the selected place by calling firebasehelper.deleteField
                     */

                    selectedFab.setImageResource(R.drawable.add_restaurant);
                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
                    mMarker.setTag(MARKER_UNSELECTED);
                    FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_FIELD);
                    FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_ID_FIELD);
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        call(); //start a call intent
                        break;

                    case 1:
                        like(tab); //like the restaurant
                        break;

                    case 2:
                        web(); //start a browser intent navigating to the places website
                        break;

                    case 3:
                        navigate(); //start a google maps navigation activity
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }


    /**
     * Retrieves additional place data that is not available with GooglePlacesAPI SDK <br>
     * {@link com.example.robmillaci.go4lunch.web_service.HtmlParser} extracts the place detailed 'type' from  https://www.google.com/maps/search/ <br>
     * {@link com.example.robmillaci.go4lunch.utils.PhotoDownloader} downloads and returns the place image bitmap <br>
     * {@link com.example.robmillaci.go4lunch.firebase.FirebaseHelper#getUsersEatingHere(String, RecyclerView.ViewHolder)} returns the users eating at this place <br>
     * {@link com.example.robmillaci.go4lunch.firebase.FirebaseHelper#isItLiked(String)} returns wether this place is 'liked' by the user <br>
     */
    private void getAdditionalPlaceData() {
        String[] placeId = new String[]{placeID};
        new HtmlParser(this).execute(placeId[0]);

        photoDownloader.getPhotos(placeID, Places.getGeoDataClient(this));

        firebaseHelper.getUsersEatingHere(mPojoPlace.getId(), null);
        firebaseHelper.isItLiked(mPojoPlace.getId());
    }


    /**
     * Builds a URI for google maps navigation passing the destination as the lat and long of this place<br>
     * Then start the googleMaps navigation activity
     */
    private void navigate() {
        Uri.Builder directionsBuilder = new Uri.Builder()
                .scheme("https")
                .authority("www.google.com")
                .appendPath("maps")
                .appendPath("dir")
                .appendPath("")
                .appendQueryParameter("api", "1")
                .appendQueryParameter("destination", location.latitude + "," + location.longitude);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, directionsBuilder.build()));
        } catch (ActivityNotFoundException ex) {

            Toast.makeText(this, R.string.install_google_maps, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Create a intent passing the webaddress - starting this intent opens the browser and navigates the user to the places website
     */
    private void web() {
        if (webaddress != null && !webaddress.equals("")) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            webIntent.setData(Uri.parse(webaddress));
            startActivity(webIntent);
        } else Toast.makeText(this, R.string.no_website_found, Toast.LENGTH_SHORT).show();
    }


    /**
     * Performs UI changes related to liking a restaurant, then updates the database either adding or removing a liked place
     *
     * @param tab the tab clicked
     */
    private void like(TabLayout.Tab tab) {
        if (tab.getTag() == null) {
            tab.setTag(MARKER_UNSELECTED);
        } else {
            switch (tab.getTag().toString()) {
                case MARKER_UNSELECTED:
                    tab.setIcon(R.drawable.start_selected);
                    FirebaseHelper.likeRestaurant(mPojoPlace.getId());
                    tab.setTag(MARKER_SELECTED);
                    break;

                case MARKER_SELECTED:
                    tab.setIcon(R.drawable.star);
                    FirebaseHelper.removeLikeRestaurant(mPojoPlace.getId());
                    tab.setTag(MARKER_UNSELECTED);
                    break;
            }
        }
    }


    /**
     * Starts an ACTION_DIAL intent to allow the user to call the relevant place
     */
    private void call() {
        if (phoneNumber != null && !phoneNumber.equals("")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_SHORT).show();
    }


    /**
     * Displays the users of the App that are eating at this place
     *
     * @param users the users eating at this place
     * @param v     used when this callback is called in a recyclerview class so that holder changes can be performed based on the results. Null is returned otherwise
     */
    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
        RestaurantActivityAdapter mAdaptor = new RestaurantActivityAdapter(users, this);
        peopleEatingRecyclerView.setLayoutManager(new LinearLayoutManager(RestaurantActivity.this));
        peopleEatingRecyclerView.setAdapter(mAdaptor);
    }



    /**
     * Updates the database with the newly selected place
     *
     * @param myviewHolder the viewholder returned for updating if this method calls back to a RecyclerView class
     * @param s            the returned place name
     * @param placeId      the returned placeId
     */
    @Override
    public void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId) {
        FirebaseHelper firebasehelper = new FirebaseHelper(RestaurantActivity.this);
        firebasehelper.addSelectedMarker(mMarker.getTitle(), placeID, s);
    }



    /**
     * callback from {@link PhotoDownloader} setting the downloaded image of this place
     *
     * @param photo  the places photo to be displayed
     * @param holder the RecyclerView.ViewHolder to be updated if this callback is in a RecyclerView class. Null is returned otherwise
     */
    @Override
    public void photoReady(Bitmap photo, RecyclerView.ViewHolder holder) {
        if (photo != null) {
            image.setImageBitmap(photo);
        } else {
            image.setImageResource(R.drawable.emptyplate);
        }
    }


    /**
     * Callback from {@link FirebaseHelper#isItLiked(String)} to determine if this place is liked by the user or not
     * Updates the UI based on the response
     *
     * @param response true if this place is liked, otherwise false
     */
    @Override
    public void isItLikedCallback(boolean response) {
        if (response) {
            starLikeTab.setTag(MARKER_SELECTED);
            starLikeTab.setIcon(R.drawable.start_selected);
        } else {
            starLikeTab.setTag(MARKER_UNSELECTED);
            starLikeTab.setIcon(R.drawable.star);
        }
    }


    /**
     * callback from {@link com.example.robmillaci.go4lunch.web_service.HtmlParser} to update the UI with the place detaild type
     * @param value the returned objects from this method
     */
    @Override
    public void parseComplete(Object... value) {
        TextView address = findViewById(R.id.restaurant_address);
        address.setText(String.format("%s - %s", value[0], mPojoPlace.getAddress()));
    }


    //Unused interface methods
    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {
    }

    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
    }

    @Override
    public void datadownloadedcallback(ArrayList arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList arrayList) {
    }
}



