package com.example.robmillaci.go4lunch.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.activities.RestaurantActivity;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.GooglePlacesAutoComplete;
import com.example.robmillaci.go4lunch.web_service.GoogleAPIServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.robmillaci.go4lunch.activities.MainActivity.GOOGLE_MAPS_FRAGMENT;
import static com.example.robmillaci.go4lunch.activities.RestaurantActivity.MARKER_SELECTED;
import static com.example.robmillaci.go4lunch.activities.RestaurantActivity.MARKER_UNSELECTED;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * This fragment is responsible for creating and displaying a MapView, the users location and any nearby places to eat<br>
 * Also contains a search view and google Autocomplete API<br>
 * Contains HashMaps of {@link PojoPlace} and {@link Marker}
 */
public class GoogleMapsFragment extends Fragment implements
        GoogleMap.OnMarkerClickListener,
        FirebaseHelper.firebaseDataCallback, Filterable {

    private static HashMap<String, PojoPlace> mPlaces; //Map of places
    private static HashMap<String, Marker> allMarkers; //Map of all markers
    private static LatLng currentlocationLatLon; //the users current location latitude and longitude
    private static GoogleMap mGoogleMap; //Googlemap to be displayed in the mapview

    private HashMap<String, PojoPlace> originalPlaces; //Map to keep track of the original places, used in filtering
    private HashMap<String, PojoPlace> filteredPlaces; //Map to keep track of filtered places
    private ArrayList<Marker> selectedMarkers; //List to hold all selected markers

    private FirebaseHelper firebaseHelper; //Reference to FirebaseHelper class
    private GoogleApiClient mGoogleApiClient; //The google API client
    private FusedLocationProviderClient mFusedLocationClient; //Fused location client for users location and updates
    private CameraPosition cameraPosition; //The position of the googlemaps camera
    private LocationCallback mLocationCallback; //the callback for location changes
    private PlaceLikelihoodBufferResponse likelyPlaces; //Likely places returned by google placeDetectionClient
    private MapView mMapView; //the mapView

    private ImageView searchingImg; //The image displaed when the app is searching for places
    private TextView searchingText; //The text displayed when the app is searching for places
    private ProgressBar searchingProgressBar; //The progress bar displayed when the app is searching for places

    private int relevantPlacesCount = 0; //The number of relevant places found

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(getString(R.string.maps_fragment_title));
        firebaseHelper = new FirebaseHelper(GoogleMapsFragment.this);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.googlemaps_fragment_view, container, false); //inflate and return this view
        mMapView = fragmentView.findViewById(R.id.mapView);
        setHasOptionsMenu(true); //Report that this fragment would like to participate in populating the options menu by receiving a call to onCreateOptionsMenu and related methods.
        return fragmentView;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //initialize the maps and views
        allMarkers = new HashMap<>();
        mPlaces = new HashMap<>();
        originalPlaces = new HashMap<>();
        selectedMarkers = new ArrayList<>();

        searchingImg = view.findViewById(R.id.searchingImg);
        searchingProgressBar = view.findViewById(R.id.searchProgressbar);
        searchingText = view.findViewById(R.id.searchingText);

        //Create and connect to the GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleAPIServices().getClient();
            mGoogleApiClient.connect();
        }
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        //Get the FusedLocation client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        //Create the callback for location changes and act according
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.i("LocationChanges", "Location: " + location.getLatitude() + " " + location.getLongitude());
                }
            }
        };

        mMapView.onCreate(savedInstanceState);


        //Get the map Asynchronously
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    mGoogleMap = googleMap;
                    mGoogleMap.setMyLocationEnabled(true);

                    //define an onMarkerClickListener
                    mGoogleMap.setOnMarkerClickListener(GoogleMapsFragment.this);
                    //get the users last known locations
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        //set the currentLocation to the returned users last known location
                                        currentlocationLatLon = new LatLng(location.getLatitude(), location.getLongitude());

                                        //get the current users added friends
                                        firebaseHelper.getAddedUsers();

                                        //get the relevant places - see the method doc for more information
                                        getGooglePlaces();

                                        //Set a location request to happy ever 2 minutes
                                        LocationRequest locationRequest = new LocationRequest();
                                        locationRequest.setInterval(120000);
                                        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
                                    }
                                }
                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), R.string.could_not_get_location, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (SecurityException e) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
                }
                mMapView.onResume();
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        this.getActivity().getMenuInflater().inflate(R.menu.google_fragment_menu, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //This fragment will have 2 menu items. One will search all markers on the map using the filter
        //The other menu item calls an instance of GooglePlacesAutocomplete search
        MenuItem searchViewMenuItem = menu.findItem(R.id.action_search);
        @SuppressWarnings("deprecation") SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchViewMenuItem);
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = mSearchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.search_marker);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getFilter().filter(newText);
                return false;
            }
        });

        MenuItem item = menu.findItem(R.id.placesSearch);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GooglePlacesAutoComplete().createAutoCompleteSearch(GoogleMapsFragment.this);
                return true;
            }
        });
        super.onPrepareOptionsMenu(menu);
    }


    private void getGooglePlaces() {
        //noinspection deprecation
        PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(getApplicationContext(), null);

        @SuppressLint("MissingPermission") Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                likelyPlaces = task.getResult(); //get the results of the mPlaceDetectionClient.getCurrentPlace

                if (likelyPlaces != null) {
                    //for each placeLikelihood returned in the task results, get the place type  and check against conditions
                    //We are checking for the following place types
                    //TYPE_RESTAURANT, TYPE_BAR, TYPE_CAFE, TYPE_FOOD
                    for (final PlaceLikelihood placeLikelihood : likelyPlaces) {
                        List<Integer> placeType = placeLikelihood.getPlace().getPlaceTypes();
                        if (placeType.size() > 0) {
                            if (placeType.contains(com.google.android.gms.location.places.Place.TYPE_RESTAURANT) ||
                                    placeType.contains(com.google.android.gms.location.places.Place.TYPE_BAR) ||
                                    placeType.contains(com.google.android.gms.location.places.Place.TYPE_CAFE) ||
                                    placeType.contains(com.google.android.gms.location.places.Place.TYPE_FOOD)) {

                                relevantPlacesCount++; //increment the relevant places found count

                                findPlacesResponse(false); //No error so we can respond by just hiding the UI components displayed while searching. Also animates the camera

                                addMarkersToMap(placeLikelihood.getPlace(), false); //Add the relevant places to the map
                                mGoogleMap.setOnMarkerClickListener(GoogleMapsFragment.this); //sets the onMarkerClick listener
                            }
                        } else {
                            findPlacesResponse(true); //we have no placeTypes returned, therefore show the user a message, hide UI elements for searching and animate camera
                        }
                    }
                    if (relevantPlacesCount == 0) {
                        findPlacesResponse(true); //we have found no relevant places even though we have returned results. Act the same was as if we have found no placeTypes
                    }
                }
            }
        });
    }


    //Method called in response to gettingGooglePlaces
    private void findPlacesResponse(boolean error) {
        if (error) {
            Toast.makeText(getApplicationContext(), R.string.no_places_found, Toast.LENGTH_LONG).show();
        }
        searchingImg.setVisibility(View.GONE);
        searchingProgressBar.setVisibility(View.GONE);
        searchingText.setVisibility(View.GONE);
        cameraPosition = new CameraPosition.Builder().target(currentlocationLatLon).zoom(15).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    //Passing in an ArrayList of placeId's, this method will return a GooglePlace based on the ID
    public void getPlaceById(ArrayList<String> placeId, final IgooglePlacescallback googlePlacescallback) {
        final ArrayList<com.google.android.gms.location.places.Place> placeArrayList = new ArrayList<>();
        final int[] count = {placeId.size()};

        if (placeId.size() == 0) { //if this method is passed no data in the placeID arraylist, callback with no data
            googlePlacescallback.gotplaces(placeArrayList, null);
        } else {
            for (int i = 0; i < placeId.size(); i++) { //for each placeId in the arrayList, loop through each one and get the Place by the ID
                //noinspection deprecation
                Places.getGeoDataClient(getApplicationContext(), null).getPlaceById(placeId.get(i)).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            PlaceBufferResponse places = task.getResult();
                            com.google.android.gms.location.places.Place myPlace = places != null ? places.get(0) : null; //get the place from the PlaceBufferResponse
                            if (myPlace != null) {
                                placeArrayList.add(myPlace); //if the place is not null, add the place to the placeArrayList
                                count[0]--; //reduce the count of the placeID's passed to this method by 1. Because we cannot return from onComplete, we need a way of triggering the callback when we are finished

                                if (count[0] == 0) { //if the count is 0 we have gone through each PlaceID and can now callback
                                    if (googlePlacescallback != null) {
                                        googlePlacescallback.gotplaces(placeArrayList, places); //callback to the calling class
                                    } else {
                                        gotplaces(placeArrayList, places); //callback to this class
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.could_not_download_place, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }


    //on destroy, ensure the buffer is closed
    @Override
    public void onDestroy() {
        if (likelyPlaces != null) {
            likelyPlaces.release();
            likelyPlaces.close();
        }
        super.onDestroy();
    }


    @Override
    public void onPause() { //ensure that we disconnect the googleAPIclient and the FusedLocationClient when the app is paused as we don't want background activity
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) { //clicking on a marker get the specific place from the list of PojoPlaces. This place is passed into the intent and the restaurant activity is called
        Intent restaurantDetailPage = new Intent(getApplicationContext(), RestaurantActivity.class);

        PojoPlace p = mPlaces.get(marker.getTitle());
        restaurantDetailPage.putExtra(PojoPlace.PLACE_SERIALIZABLE_KEY, p);

        getApplicationContext().startActivity(restaurantDetailPage);

        return true;
    }


    /**
     * Callback from {@link FirebaseHelper#getAddedUsers()}<br>
     * For each of the returned users, get the users selected place to display on the map
     *
     * @param users      the list of added users (friends)
     * @param viewHolder if this is called from a RecyclerViewAdaptor class, this will be the viewholder to be updated
     */
    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
        for (String s : users) {
            if (!"".equals(s))
                firebaseHelper.getSelectedPlace(s, null);
        }
    }


    /**
     * Callback from {@link FirebaseHelper#getSelectedPlace(String, AddedUsersAdapter.MyviewHolder)}<br>
     * Gets the place by the place ID
     *
     * @param myviewHolder if this is called from a RecyclerViewAdaptor class, this will be the viewholder to be updated
     * @param place        the place name
     * @param placeId      the place id
     */
    @Override
    public void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String place, String placeId) {
        ArrayList<String> placeIdArray = new ArrayList<>();
        if (!"".equals(placeId)) placeIdArray.add(placeId);

        if (placeIdArray.size() > 0) {
            getPlaceById(placeIdArray, null);
        }
    }


    /**
     * Callback from {@link #getPlaceById(ArrayList, IgooglePlacescallback)}
     *
     * @param places              the list of the returned places
     * @param placeBufferResponse the placeBufferResponse so we can close it once we are finished
     */
    public void gotplaces(ArrayList<com.google.android.gms.location.places.Place> places, PlaceBufferResponse placeBufferResponse) {
        for (com.google.android.gms.location.places.Place p : places) {
            addMarkersToMap(p, true);
        }
        //close the buffer to prevent memory leaks
        placeBufferResponse.close();
    }


    /**
     * For the passed googlePlace, this method adds a marker to the map, it also converts the googlePlace to a {@link PojoPlace} so we can parse the place detail type
     * and safely close the placeBufferReponse
     *
     * @param place          the place to be added to the map and converted to {@link PojoPlace}
     * @param selectedMarker determins wether this is a selected marker or not (to define the drawable set for the marker)
     */
    @SuppressWarnings("ConstantConditions")
    private void addMarkersToMap(final com.google.android.gms.location.places.Place place, boolean selectedMarker) {
        final Marker marker;
        MarkerOptions mOptions = new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getName().toString());

        if (selectedMarker) {
            mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
            marker = mGoogleMap.addMarker(mOptions);
            marker.setTag(MARKER_SELECTED);
            selectedMarkers.add(marker);
        } else {
            mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
            marker = mGoogleMap.addMarker(mOptions);
            marker.setTag(MARKER_UNSELECTED);
        }

        allMarkers.put(marker.getTitle(), marker);

        final String name = place.getName().toString();
        final String address = place.getAddress().toString();
        final String webSite = place.getWebsiteUri().toString();
        final String phoneNumber = place.getPhoneNumber().toString();
        final float rating = place.getRating();
        final String id = place.getId();
        final double latitude = place.getLatLng().latitude;
        final double longitude = place.getLatLng().longitude;

        //creating the POJO places required HTML parsing which can slow down the main UI execution causing UX problems.
        //Handle the creation on a background thread to fix this issue and smooth out UX.
        new Thread(new Runnable() {
            @Override
            public void run() {
                PojoPlace POJOplace = new PojoPlace(name, address, webSite, phoneNumber, "", rating, id, latitude, longitude, GOOGLE_MAPS_FRAGMENT);

                mPlaces.put(POJOplace.getName(), POJOplace);
                originalPlaces.put(POJOplace.getName(), POJOplace);
            }
        }).start();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredPlaces = originalPlaces; //if the constraint is "" , we set the filteresPlaces Map to the original
                } else {
                    HashMap<String, PojoPlace> queryfilteredList = new HashMap<>();
                    for (String key : originalPlaces.keySet()) { //loop through the key set of the originalPlaces map
                        //filter condition if the places name, address or place type contains the searched string
                        //noinspection ConstantConditions
                        if (originalPlaces.get(key).getName().toLowerCase().contains(charString.toLowerCase()) ||
                                originalPlaces.get(key).getAddress().toLowerCase().contains(charString.toLowerCase()) ||
                                originalPlaces.get(key).getPlaceType().toLowerCase().contains(charString.toLowerCase())) {
                            queryfilteredList.put(key, originalPlaces.get(key));
                        }
                    }
                    filteredPlaces = queryfilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredPlaces;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mPlaces = (HashMap<String, PojoPlace>) results.values;
                mGoogleMap.clear();
                if (mPlaces != null) {
                    for (String s : mPlaces.keySet()) {
                        Marker originalMarker = getSpecificMarker(s);

                        if (originalMarker != null) {
                            if (selectedMarkers.contains(originalMarker)) {
                                MarkerOptions mOptions = new MarkerOptions()
                                        .position(mPlaces.get(s).getLocation())
                                        .title(mPlaces.get(s).getName())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                                Marker marker = mGoogleMap.addMarker(mOptions);
                                marker.setTag(MARKER_SELECTED);
                            } else {
                                MarkerOptions mOptions = new MarkerOptions()
                                        .position(mPlaces.get(s).getLocation())
                                        .title(mPlaces.get(s).getName())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
                                Marker marker = mGoogleMap.addMarker(mOptions);
                                marker.setTag(RestaurantActivity.MARKER_UNSELECTED);

                            }
                        }
                    }
                }
            }
        };
    }


    //Response called using GoogleAutoComplete search. If results is OK, we add the place to the map and animate the camera to the found places location
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GooglePlacesAutoComplete.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                com.google.android.gms.location.places.Place place = PlaceAutocomplete.getPlace(this.getActivity(), data);

                addMarkersToMap(place, false);

                cameraPosition = new CameraPosition.Builder().target(place.getLatLng()).zoom(15).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this.getActivity(), data);
                Log.d("placeAutoCompleteError", status.getStatusMessage());

            } else //noinspection StatementWithEmptyBody
                if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
        }
    }


    public static HashMap<String, PojoPlace> getmPlaces() {
        return mPlaces;
    }

    public static LatLng getCurrentlocation() {
        return currentlocationLatLon;
    }

    public static Marker getSpecificMarker(String markerTitle) {
        return allMarkers.get(markerTitle);
    }

    public static GoogleMap getmGoogleMap() {
        return mGoogleMap;
    }

    public static HashMap<String, Marker> getAllMarkers() {
        return allMarkers;
    }

    //unused interface methods
    @Override
    public void datadownloadedcallback(ArrayList arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList arrayList) {
    }

    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {

    }

    @Override
    public void isItLikedCallback(boolean response) {

    }

    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {

    }

}

interface IgooglePlacescallback {
    void gotplaces(ArrayList<com.google.android.gms.location.places.Place> places, PlaceBufferResponse placeBufferResponse);
}






