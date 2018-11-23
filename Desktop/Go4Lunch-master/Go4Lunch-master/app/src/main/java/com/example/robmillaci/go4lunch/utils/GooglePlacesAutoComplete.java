package com.example.robmillaci.go4lunch.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

/**
 * Responsible for creating the GooglePlaceAutoComplete search
 * see {@link PlaceAutocomplete}
 */
public class GooglePlacesAutoComplete extends Activity {
    public static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public void createAutoCompleteSearch(GoogleMapsFragment mFragment) {
        //noinspection TryWithIdenticalCatches
        try {
            @SuppressWarnings("ConstantConditions") Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(mFragment.getActivity());
            mFragment.startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, R.string.google_services_error, Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, R.string.google_services_not_available, Toast.LENGTH_SHORT).show();
        }

    }
}
