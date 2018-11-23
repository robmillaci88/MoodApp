package com.example.robmillaci.go4lunch.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.activities.RestaurantActivity;
import com.example.robmillaci.go4lunch.activities.ReviewsActivity;
import com.example.robmillaci.go4lunch.custom_views.CustomTextView;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.data_objects.places_details.RawData;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.IphotoDownloadedCallback;
import com.example.robmillaci.go4lunch.utils.PhotoDownloader;
import com.example.robmillaci.go4lunch.web_service.GetDataService;
import com.example.robmillaci.go4lunch.web_service.HtmlParser;
import com.example.robmillaci.go4lunch.web_service.IhtmlParser;
import com.example.robmillaci.go4lunch.web_service.ServiceGenerator;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

import static com.example.robmillaci.go4lunch.data_objects.PojoPlace.PLACE_SERIALIZABLE_KEY;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * This class is responsible for creating the adaptor to display chat data to users
 * Contains a list of {@link PojoPlace}
 */
public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.MyviewHolder> implements IphotoDownloadedCallback,
        FirebaseHelper.firebaseDataCallback,
        IhtmlParser,
        Filterable {

    private final LatLng mCurrentLocation; //the current location of the user, used to determin the distance to the restaurant
    private final Context mContext;

    private ArrayList<PojoPlace> mPojoPlaceArray; //List of places (restaurants) to display to the user
    private ArrayList<PojoPlace> filteredArray; //List to hold the filtered places
    private final ArrayList<PojoPlace> origionalArray; //List to keep track of the origional place array

    private static final int NO_USERS = 0;


    /**
     * Two constructors, one when we use {@link com.google.android.gms.location.places.PlaceBufferResponse} in order to ensure the buffer is closed to prevent memory leaks
     *
     * @param pojoPlaces          the list of pojoPlaces
     * @param currentLocation the current users location
     * @param placesBuffer    the placeBuffer to be closed
     * @param context         the calling class context
     */

    public RestaurantListAdapter(ArrayList<PojoPlace> pojoPlaces, LatLng currentLocation, PlaceBufferResponse placesBuffer, Context context) {
        this.mCurrentLocation = currentLocation;
        mPojoPlaceArray = pojoPlaces;
        origionalArray = pojoPlaces;
        this.mContext = context;
        if (placesBuffer != null && !placesBuffer.isClosed()) {
            placesBuffer.close();
        }
    }

    public RestaurantListAdapter(ArrayList<PojoPlace> pojoPlaces, LatLng currentLocation, Context context) {
        this.mCurrentLocation = currentLocation;
        this.mPojoPlaceArray = pojoPlaces;
        this.origionalArray = pojoPlaces;
        this.mContext = context;
    }


    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurantlistview, parent, false);
        return new MyviewHolder(view);//returns a new object of static inner class to the 'OnBindViewHolder method
    }


    @Override
    public void onBindViewHolder(@NonNull final MyviewHolder holder, final int position) {
        final PojoPlace pojoPlace = mPojoPlaceArray.get(holder.getAdapterPosition()); //the PojoPlace related to the adapters position
        holder.rest_title.setText(pojoPlace.getName()); //Set the pojoPlace title
        holder.phoneNumber.setText(pojoPlace.getPhoneNumber()); //set the pojoPlace phone number
        holder.ratingBar.setRating(pojoPlace.getRating()); //set the pojoPlace rating
        holder.vicinity.setText(String.format("%s - %s", pojoPlace.getPlaceType()
                , pojoPlace.getAddress())); //set the pojoPlace type, additional information for pojoPlace type is retrieved below and updated once complete

        if (!pojoPlace.isPlaceParsed()) { //if the pojoPlace is already parsed to determine its detailed pojoPlace type, the handle is skipped
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //using the pojoPlace ID parse the HTML from https://www.google.com/maps/search/ to determine the places detailed type
                    new HtmlParser(RestaurantListAdapter.this, holder).execute(pojoPlace.getId());
                }
            }, 500); //wait half a second, this is because the callback of HTMLParser calls notifyDataSetChange which will cause issues with animations
            //if run too early
        }

        //see getAdditionalPlaceData method
        getAdditionalPlaceData(pojoPlace.getId(), holder);


        Uri websiteUrl = Uri.parse(pojoPlace.getWebsite());
        if (websiteUrl != null) {
            holder.website.setText(pojoPlace.getWebsite()); //set the places website
        }


        //now determine the distance from the current users location to the location of the pojoPlace
        double placeLat = pojoPlace.getLocation().latitude;
        double placeLong = pojoPlace.getLocation().longitude;

        try {
            Location locA = new Location("point A");
            locA.setLatitude(mCurrentLocation.latitude);
            locA.setLongitude(mCurrentLocation.longitude);

            Location locB = new Location("point B");
            locB.setLatitude(placeLat);
            locB.setLongitude(placeLong);

            float distance = locA.distanceTo(locB);
            int distanceInt = (int) distance;

            holder.distance.setText(String.format("%sm", String.valueOf(distanceInt)));
        } catch (Exception e) {
            e.printStackTrace();
            holder.distance.setText(R.string.not_available);
        }


        //get the photo of the pojoPlace
        PhotoDownloader photoDownloader = new PhotoDownloader(this, holder);
        photoDownloader.getPhotos(pojoPlace.getId(), Places.getGeoDataClient(getApplicationContext()));


        //set the onclick listener for the reviews button
        holder.reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviewActivity = new Intent(mContext, ReviewsActivity.class);
                reviewActivity.putExtra(PojoPlace.PLACE_ID_KEY, pojoPlace.getId());
                reviewActivity.putExtra(PojoPlace.PLACE_NAME_KEY, pojoPlace.getName());
                mContext.startActivity(reviewActivity);
            }
        });


        //set the onclick listener for the recyclerviews item. This will call the RestaurantActivity for the specific pojoPlace
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent restaurantDetailPage = new Intent(getApplicationContext(), RestaurantActivity.class);
                PojoPlace p = mPojoPlaceArray.get(holder.getAdapterPosition());

                restaurantDetailPage.putExtra(PLACE_SERIALIZABLE_KEY, p);
                getApplicationContext().startActivity(restaurantDetailPage);
            }
        });


        //Get the users that are eating at this pojoPlace in order to update the recycler view and display to the user all other users eating here
        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.getUsersEatingHere(pojoPlace.getId(), holder);
    }


    /**
     * Gets additional place data not available from Google Places SDK
     * see {@link com.example.robmillaci.go4lunch.web_service.GetDataService}
     *
     * @param placeId the id of the place from which additional data is requested
     * @param holder  the recyclerview holder to be updated in the onResponse of this method
     */
    private void getAdditionalPlaceData(String placeId, final MyviewHolder holder) {
        GetDataService service = ServiceGenerator.getRetrofitInstance().create(GetDataService.class);
        retrofit2.Call<RawData> call = service.getDetails(placeId, "opening_hours", ServiceGenerator.getGoogleAPIKey());
        call.enqueue(new Callback<RawData>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<RawData> call, @NonNull Response<RawData> response) {
                final RawData extraData = response.body();

                if (extraData != null) {
                    try {
                        //determines wether the place is open or closed and acts accordingly
                        if (extraData.getResult().getOpeningHours().getOpenNow()) {
                            holder.openNow.setText(R.string.open_now);
                            holder.openNow.setTextColor(Color.GREEN);
                        } else {
                            holder.openNow.setText(R.string.closed_now);
                            holder.openNow.setTextColor(Color.RED);
                        }

                        //Sets the onclick listener for the opening hours button
                        //This shows a dialog that presents the user with the opening hours for the specific place
                        holder.openingHours.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> weekdayText = extraData.getResult().getOpeningHours().getWeekdayText(); //the returned opening details for this place

                                @SuppressLint("InflateParams") final AlertDialog.Builder openingTimesDialogBuilder = new AlertDialog
                                        .Builder(mContext);

                                @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(mContext)
                                        .inflate(R.layout.opening_times_dialog_view, null, false); //inflate the view for this dialog

                                openingTimesDialogBuilder.setPositiveButton(R.string.ok, null);

                                openingTimesDialogBuilder.setView(dialogView); //set the view for this dialog
                                AlertDialog openingTimesDialog = openingTimesDialogBuilder.create(); //create the AlertDialog from the builder

                                if (weekdayText != null && openingTimesDialog.getWindow() != null) {
                                    //set the dialog background to transparent
                                    openingTimesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    //show the dialog
                                    openingTimesDialog.show();

                                    CustomTextView monday = dialogView.findViewById(R.id.mondayTextView);
                                    CustomTextView tuesday = dialogView.findViewById(R.id.tuesdayTextView);
                                    CustomTextView wednesday = dialogView.findViewById(R.id.wednesdayTextView);
                                    CustomTextView thursday = dialogView.findViewById(R.id.thursdayTextView);
                                    CustomTextView friday = dialogView.findViewById(R.id.fridayTextView);
                                    CustomTextView saturday = dialogView.findViewById(R.id.saturdayTextView);
                                    CustomTextView sunday = dialogView.findViewById(R.id.sundayTextView);

                                    try {
                                        monday.setText(weekdayText.get(0)); //set the text for monday opening hours
                                        tuesday.setText(weekdayText.get(1)); //set the text for tuesday opening hours
                                        wednesday.setText(weekdayText.get(2)); //set the text for wednesday opening hours
                                        thursday.setText(weekdayText.get(3)); //set the text for thursday opening hours
                                        friday.setText(weekdayText.get(4)); //set the text for friday opening hours
                                        saturday.setText(weekdayText.get(5)); //set the text for saturday opening hours
                                        sunday.setText(weekdayText.get(6)); //set the text for sundau opening hours
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), R.string.error_getting_times, Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.no_time_data, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        holder.openNow.setText(R.string.no_opening_info);
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<RawData> call, @NonNull Throwable t) {
                Log.d("gettingextradata", "onFailure: " + t.getMessage());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mPojoPlaceArray.size();
    }


    /**
     * callback from PhotoDownloader. See {@link PhotoDownloader#getPhotos(String, GeoDataClient)}
     *
     * @param photo  the returned photo to load into the holder
     * @param holder the holder at a specific position in the recyclerview
     */
    @Override
    public void photoReady(Bitmap photo, RecyclerView.ViewHolder holder) {
        if (photo != null) {
            ((MyviewHolder) holder).placeImage.setImageBitmap(photo);
        } else {
            ((MyviewHolder) holder).placeImage.setImageResource(R.drawable.restaurant_placeholder);
        }
    }

    //return a new filter used to filter the restaurant list
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredArray = origionalArray; //if no text is entered into the search box, the filtered array = the origional place array
                } else {
                    ArrayList<PojoPlace> queryfilteredList = new ArrayList<>(); //create a new Arraylist to hold the filtered results
                    for (PojoPlace row : origionalArray) { //loop through the origional array adding the places that match the filter criteria to the filteredArray
                        //Filter criteria matching by looking at the place name and place address
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getAddress().toLowerCase().contains(charString.toLowerCase())) {
                            queryfilteredList.add(row);
                        }
                    }

                    filteredArray = queryfilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredArray;
                return filterResults;

            }

            //set the place array to equal the results of the filter and then notifyDataSetChanged()
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mPojoPlaceArray = (ArrayList<PojoPlace>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    /**
     * The callback from HTMLParser. See {@link HtmlParser}
     * This is run on a separate thread and hence we have to use {@link Activity#runOnUiThread(Runnable)} to make view changes
     *
     * @param returnedObjects the objects returned from {@link HtmlParser}
     */
    @Override
    public void parseComplete(Object... returnedObjects) {
        try {
            Object[] returnedViewHolderObject = (Object[]) returnedObjects[1]; //the Object[] returned
            MyviewHolder holder = (MyviewHolder) returnedViewHolderObject[0]; //the viewholder object returned
            mPojoPlaceArray.get(holder.getAdapterPosition()).setPlaceType((String) returnedObjects[0]); //the place type parsed and returned
            mPojoPlaceArray.get(holder.getAdapterPosition()).setPlaceParsed(true); //set the place as parsed
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Activity) mContext).runOnUiThread(new Runnable() { //run notifyDataSetChange() on the UI thread in order to make view changes
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });


    }

    static class MyviewHolder extends RecyclerView.ViewHolder {
        final ImageView placeImage;
        final TextView rest_title;
        final TextView vicinity;
        final TextView phoneNumber;
        final RatingBar ratingBar;
        final TextView distance;
        final TextView website;
        final TextView openNow;
        final ImageView openingHours;
        final ImageView reviews;
        final TextView numberOfEaters;

        private MyviewHolder(View itemView) {

            super(itemView);
            this.placeImage = itemView.findViewById(R.id.placeImage);
            this.rest_title = itemView.findViewById(R.id.rest_title);
            this.vicinity = itemView.findViewById(R.id.vicinity);
            this.phoneNumber = itemView.findViewById(R.id.phoneNumber);
            this.ratingBar = itemView.findViewById(R.id.ratingBar);
            this.distance = itemView.findViewById(R.id.distance);
            this.website = itemView.findViewById(R.id.website);
            this.openNow = itemView.findViewById(R.id.openNow);
            this.openingHours = itemView.findViewById(R.id.openinghours);
            this.reviews = itemView.findViewById(R.id.reviews);
            this.numberOfEaters = itemView.findViewById(R.id.usersEatingHere);
        }
    }


    /**
     * callback method from {@link FirebaseHelper#getUsersEatingHere(String, RecyclerView.ViewHolder)}
     * @param users the list of users eating at the specific restaurant
     * @param v the viewHolder to be updated
     */
    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
        if (users != null) {
            ((MyviewHolder) v).numberOfEaters.setText(String.format(mContext.getString(R.string.friends), users.size()));
        } else {
            ((MyviewHolder) v).numberOfEaters.setText(String.format(mContext.getString(R.string.friends), NO_USERS));
        }
    }



    //unused interface methods

    @Override
    public void datadownloadedcallback(ArrayList<Object> arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList<Users> arrayList) {
    }

    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
    }

    @Override
    public void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId) {
    }

    @Override
    public void isItLikedCallback(boolean response) {
    }

    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {
    }

}
