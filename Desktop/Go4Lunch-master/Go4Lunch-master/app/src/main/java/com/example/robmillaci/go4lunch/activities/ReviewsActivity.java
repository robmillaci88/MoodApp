package com.example.robmillaci.go4lunch.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.ReviewsAdapter;
import com.example.robmillaci.go4lunch.data_objects.reviews_objects.Reviews;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;
import com.example.robmillaci.go4lunch.web_service.GetDataService;
import com.example.robmillaci.go4lunch.web_service.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.robmillaci.go4lunch.data_objects.PojoPlace.PLACE_ID_KEY;
import static com.example.robmillaci.go4lunch.data_objects.PojoPlace.PLACE_NAME_KEY;

/**
 * This class is responsible for displaying reviews for a specific place
 * Users {@link #getReviews(String)} to retrieve the reviews
 */
public class ReviewsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //enables the home button

        mRecyclerView = findViewById(R.id.reviewsRecyclerView); //the recycler view to hold the reviews

        String placeId = getIntent().getStringExtra(PLACE_ID_KEY); //retrieves the placeID passed into the intent to create this activity
        String placeName = getIntent().getStringExtra(PLACE_NAME_KEY); //retrieves the placeName passed into the intent to create this activity

        setTitle(getString(R.string.reviews_for) + " " + placeName); //sets the activities title

        if (placeId != null) {
            getReviews(placeId);
        } else {
            Toast.makeText(this, R.string.error_getting_reviews, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Using retrofit, get the place reviews. See {@link GetDataService}
     *
     * @param placeId the ID of the place to retrieve reviews for
     */
    private void getReviews(String placeId) {
        GetDataService service = ServiceGenerator.getRetrofitInstance().create(GetDataService.class);
        Call<Reviews> call = service.getReviews(placeId, "reviews", ServiceGenerator.getGoogleAPIKey());
        call.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(@NonNull Call<Reviews> call, @NonNull Response<Reviews> response) {
                Reviews reviewsResult = response.body();
                if (reviewsResult != null) {
                    ReviewsAdapter mAdapter = new ReviewsAdapter(reviewsResult.getResult().getReviews());
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                    RecyclerViewMods.setAnimation(mRecyclerView);
                    mRecyclerView.addItemDecoration(RecyclerViewMods.decorate());
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Reviews> call, @NonNull Throwable t) {
                onErrorGettingReviews();
            }
        });
    }

    //the response if no reviews were found
    private void onErrorGettingReviews() {
        Toast.makeText(getApplicationContext(), R.string.no_reviews_found, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}


