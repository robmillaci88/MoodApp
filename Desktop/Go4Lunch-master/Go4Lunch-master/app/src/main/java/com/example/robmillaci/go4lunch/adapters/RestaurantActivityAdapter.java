package com.example.robmillaci.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * This class is responsible for creating the adaptor to display users eating at a specific restaurant
 *
 */
public class RestaurantActivityAdapter extends RecyclerView.Adapter<RestaurantActivityAdapter.MyViewHolder> {
    private final ArrayList mEaters; //Arraylist of the users eating here
    private final Context mContext;

    public RestaurantActivityAdapter(ArrayList eaters, Context context) {
        this.mEaters = eaters;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_eaters, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Users user = (Users) mEaters.get(position); //get the user at this position

        holder.name.setText(String.format("%s%s", user.getUsername(), mContext.getString(R.string.is_joining))); //set the users name that is eating here
        try {
            Picasso.get().load(user.getUserPic()).into(holder.profilePic);
        } catch (Exception e) {
            //ignore - placeholder image is automatically set
        }
    }

    @Override
    public int getItemCount() {
        return mEaters.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView profilePic;
        final TextView name;

        MyViewHolder(View itemView) {
            super(itemView);
            this.profilePic = itemView.findViewById(R.id.profPic);
            this.name = itemView.findViewById(R.id.userText);
        }
    }
}
