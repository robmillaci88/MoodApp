package com.example.robmillaci.go4lunch.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.data_objects.reviews_objects.Review;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

/**
 * This class is responsible for creating the review adaptor to display the review results to the user
 * Contains a list of review objects. See {@link com.example.robmillaci.go4lunch.data_objects.reviews_objects.Review}
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.MyViewHolder> {
    private final List<Review> reviewsList; //the list of review objects

    public ReviewsAdapter(List<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_recycler_view, null);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Review reviewItem = reviewsList.get(position); //Retrieve the review object at this position

        holder.reviewBody.setText(reviewItem.getText()); //set the review body text
        holder.mRatingBar.setRating(reviewItem.getRating()); //set the review rating
        holder.reviewerName.setText(reviewItem.getAuthorName()); //set the reviewer's name

        long reviewTime = reviewItem.getTime(); //get the review time int
        long newReviewTime = reviewTime * 1000; //convert the review time into milliseconds

        Date date = new Date(newReviewTime); //create a date passing the milliseconds of the review time

        holder.reviewTime.setText(date.toString()); //set the review time view

        Picasso.get().load(reviewItem.getProfilePhotoUrl()).into(holder.profPic); //load the reviewers picture into the profile picture view
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView reviewBody;
        final TextView reviewerName;
        final RatingBar mRatingBar;
        final ImageView profPic;
        final TextView reviewTime;

        private MyViewHolder(View itemView) {
            super(itemView);
            reviewBody = itemView.findViewById(R.id.reviewbody);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            reviewerName = itemView.findViewById(R.id.reviewerName);
            profPic = itemView.findViewById(R.id.profPic);
            reviewTime = itemView.findViewById(R.id.reviewTime);
        }
    }
}
