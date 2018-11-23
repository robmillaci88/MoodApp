package com.example.robmillaci.go4lunch.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.RestaurantListAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;

import java.util.ArrayList;

import static com.example.robmillaci.go4lunch.activities.MainActivity.LIKED_RESTAURANT_FRAGMENT;

/**
 * This fragment is responsible for creating and displaying the users liked restaurants<br>
 * Builds the likedRestaurants using {@link #buildLikedRestaurants()} which calles {@link FirebaseHelper#getLikedRestaurants()}
 */
public class LikedRestaurantsFragment extends Fragment implements FirebaseHelper.firebaseDataCallback, IgooglePlacescallback {
    private RecyclerView likedRecyclerView;
    private RestaurantListAdapter mAdaptor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(getString(R.string.liked_restaurant_title));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.liked_restaurant_fragment, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        likedRecyclerView = view.findViewById(R.id.likedRecyclerView);
        setHasOptionsMenu(true);
        buildLikedRestaurants(); //see class docs
    }

    private void buildLikedRestaurants() {
        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.getLikedRestaurants(); //see class docs
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdaptor.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdaptor != null) {
                    mAdaptor.getFilter().filter(newText);
                }
                return false;
            }
        });


    }


    /**
     * Callback from {@link FirebaseHelper#getLikedRestaurants()}
     *
     * @param places returned liked place ID's
     */
    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {
        //now get the places by their ID returned
        new GoogleMapsFragment().getPlaceById(places, LikedRestaurantsFragment.this);
    }


    /**
     * Callback from {@link GoogleMapsFragment#getPlaceById(ArrayList, IgooglePlacescallback)}
     * Converts the googlePlaces return inti PojoPlaces
     * * @param places the list of the returned places
     *
     * @param placesBuffer the placeBufferResponse so we can close it once we are finished
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void gotplaces(ArrayList<Place> places, PlaceBufferResponse placesBuffer) {
        ArrayList<PojoPlace> Pojoplaces = new ArrayList<>();
        for (Place p : places) {
            Pojoplaces.add(new PojoPlace(p.getName().toString(), p.getAddress().toString(),
                    p.getWebsiteUri().toString(), p.getPhoneNumber().toString(),
                    "", p.getRating(), p.getId(), p.getLatLng().latitude, p.getLatLng().longitude, LIKED_RESTAURANT_FRAGMENT));
        }
        placesBuffer.close();

        mAdaptor = new RestaurantListAdapter(Pojoplaces, GoogleMapsFragment.getCurrentlocation(), placesBuffer, getContext());
        likedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerViewMods.setAnimation(likedRecyclerView);
        likedRecyclerView.setAdapter(mAdaptor);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mAdaptor != null) {
            buildLikedRestaurants();
        }
    }


    //unused interface methods
    @Override
    public void datadownloadedcallback(ArrayList arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList arrayList) {
    }

    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
    }

    @Override
    public void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId) {
    }

    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {

    }

    @Override
    public void isItLikedCallback(boolean response) {
    }
}
