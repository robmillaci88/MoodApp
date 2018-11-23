package com.example.robmillaci.go4lunch.web_service;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created and returns GoogleApiClient
 */
public class GoogleAPIServices implements GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient getClient() {
        GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient
                .Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API);
        return googleApiClientBuilder.build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_LONG).show();
    }

}
