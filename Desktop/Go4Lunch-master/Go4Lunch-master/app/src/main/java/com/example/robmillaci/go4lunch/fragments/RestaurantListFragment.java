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
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.RestaurantListAdapter;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;

import java.util.ArrayList;
import java.util.HashMap;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * This fragment is responsible for creating the restaurant list fragment displaying all places added to the map
 */
public class RestaurantListFragment extends Fragment {
    private RestaurantListAdapter mAdaptor;//the adaptor for the history list view


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(getApplicationContext().getString(R.string.main_title));

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restaurant_list_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        HashMap<String, PojoPlace> placesMap = GoogleMapsFragment.getmPlaces(); //get all the places added to the map
        ArrayList<PojoPlace> POJOplaces = new ArrayList<>();

        for (String s  : placesMap.keySet()){ //convert the places Map to an arraylist
            PojoPlace p = placesMap.get(s);
            POJOplaces.add(p);
        }

        RecyclerView restaurantList = view.findViewById(R.id.restaurant_list_recycler_view);
        ImageView noResultsFound = view.findViewById(R.id.no_results_found);

        if (POJOplaces.size() == 0) {
            noResultsFound.setVisibility(View.VISIBLE); //if there are no places, display "no results found" to the user
        } else {
            noResultsFound.setVisibility(View.GONE); //else remove "no results found"
        }
        restaurantList.setBackgroundColor(getResources().getColor(R.color.white));

        mAdaptor = new RestaurantListAdapter(POJOplaces, GoogleMapsFragment.getCurrentlocation(),RestaurantListFragment.this.getContext());
        restaurantList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        RecyclerViewMods.setAnimation(restaurantList);
        restaurantList.setAdapter(mAdaptor);
    }
}
