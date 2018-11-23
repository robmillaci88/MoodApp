package com.example.robmillaci.go4lunch.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.activities.ChatActivity;
import com.example.robmillaci.go4lunch.activities.RestaurantActivity;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;
import com.example.robmillaci.go4lunch.web_service.HtmlParser;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.robmillaci.go4lunch.activities.MainActivity.USER_LIST_FRAGMENT;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * This class is responsible for creating the adaptor to display added users
 *
 */
public class AddedUsersAdapter extends RecyclerView.Adapter<AddedUsersAdapter.MyviewHolder> implements Filterable, FirebaseHelper.firebaseDataCallback {
    private ArrayList<Users> mUsersArrayList; //The list of added users
    private ArrayList<Users> filteredUsersList; //the list to hold the filter results
    private final ArrayList<Users> originalArray; //ArrayList to keep track of the original users array. Used when filtering
    private final Context mContext; //The context of the calling activity
    private String addedUIds; //The added users ID


    public AddedUsersAdapter(ArrayList<Users> usersArrayList, Context context) {
        mUsersArrayList = usersArrayList;
        mContext = context;
        originalArray = usersArrayList;
    }


    @NonNull
    @Override
    public AddedUsersAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate and return the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_recycler_view, parent, false);
        return new AddedUsersAdapter.MyviewHolder(view);
    }



    @Override
    public void onBindViewHolder(
            @NonNull final AddedUsersAdapter.MyviewHolder holder, final int position) {

        final Users user = mUsersArrayList.get(holder.getAdapterPosition()); //The user to be displayed this the relevant position
        holder.username.setText(user.getUsername()); //set the username
        holder.userEmail.setText(user.getUserEmail()); //set the user email

        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.getSelectedPlace(user.getUserID(), holder); //get the users selected place to display where they are eating. Callback to gotSelectedPlaces

        firebaseHelper.checkNewNotifications(user.getUserID(), holder); //Checks wether the user has new notifications from the displayed user

        try {
            Picasso.get().load(user.getUserPic()).into(holder.userPicture); //load the users picture into the view
        } catch (Exception e) {
            holder.userPicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }



        /*
         Sets the onclickListener for the chat button <br>
         Passes the id of the user receiving the messages as well as their name and picture. Also passes the picture of the user sending the messages
        */
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra(ChatActivity.CHATTING_TO_ID, user.getUserID());
                chatIntent.putExtra(ChatActivity.CHATTING_TO, user.getUsername());
                chatIntent.putExtra(ChatActivity.CHATTING_TO_PIC, user.getUserPic());
                chatIntent.putExtra(ChatActivity.CURRENT_USER_PIC, FirebaseHelper.getmCurrentUserPicUrl());
                FirebaseHelper.removeChatNotification(user.getUserID());

                holder.chat.setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat));
                mContext.startActivity(chatIntent);
            }
        });




        /*
        Sets the onClickListener for the deleteFriend button. Creates an alert dialog to confirm deletion
         */
        holder.deleteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(mContext);
                confirmDialog.setTitle(R.string.delete_confirmation);
                confirmDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUser(holder); //calls deleteUser if dialog positive results button is clicked
                    }
                });
                confirmDialog.setNegativeButton(R.string.cancel, null);
                confirmDialog.show(); //show the dialog
            }
        });
    }



    /**
     * Calls {@link FirebaseHelper#getCurrentUserData()} to get the current users data and remove the ID of the user to be deleted from the friends list
     * @param holder the holder to remove when deleteUser is called. Removing the user from the recyclerview
     */
    private void deleteUser(final RecyclerView.ViewHolder holder) {
        FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot taskResults = task.getResult();
                    assert taskResults != null;
                    List<DocumentSnapshot> documents = taskResults.getDocuments();
                    DocumentSnapshot d = documents.get(0); //get the document snapshow from the task results

                    try {
                        addedUIds = (String) d.get("addedUsers"); //retrieve the String that relates to this users 'addedUsers'
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String idToRemove = mUsersArrayList.get(holder.getAdapterPosition()).getUserID(); //get the ID of the user to remove from the database
                    String[] currentAddedUsers = addedUIds.split(","); //Create a string array of all added users ID's splitting by ','
                    StringBuilder newAddedUsers = new StringBuilder();
                    int count = 0;
                    for (String s : currentAddedUsers) { //loop through all added users
                        count++;
                        if (count == 1 & !s.equals(idToRemove)) { //if this is the first iteration and the ID is not being removed, append the users ID to the newAddedUsers stringbuilder
                            newAddedUsers.append(s);
                        } else if (!s.equals(idToRemove)) { //if this is NOT the first iteration and the Id is not being removed, append a , to generated CSV
                            newAddedUsers.append(",").append(s);
                        }
                    }

                    if (newAddedUsers.toString().equals("")) { //if the newAddedUsers do not contain any values, we can remove this field from the database
                        FirebaseHelper.deleteField("addedUsers");
                    } else {
                        if (newAddedUsers.toString().substring(0, 1).equals(",")) {
                            //ensure there are no leading comma characters
                            newAddedUsers = newAddedUsers.replace(0, 1, "");
                        }
                        FirebaseHelper.updateField("addedUsers", newAddedUsers.toString()); //Update the database with the new added users after removing the deleted user
                    }
                    mUsersArrayList.remove(mUsersArrayList.get(holder.getAdapterPosition())); //remove the deleted user from the arraylist
                    holder.itemView.startAnimation(RecyclerViewMods.createRemoveAnimation(AddedUsersAdapter.this, mContext));
                }
            }
        });
    }




    @Override
    public int getItemCount() {
        return mUsersArrayList.size();
    }



    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredUsersList = originalArray;
                } else {
                    ArrayList<Users> queryfilteredList = new ArrayList<>();
                    for (Users row : originalArray) {
                        // name match condition. if the username or user email matches the filter constraint
                        if (row.getUsername().toLowerCase().contains(charString.toLowerCase()) || row.getUserEmail().toLowerCase().contains(charString.toLowerCase())) {
                            queryfilteredList.add(row);
                        }
                    }

                    filteredUsersList = queryfilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredUsersList;
                return filterResults;

            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mUsersArrayList = (ArrayList<Users>) results.values;
                notifyDataSetChanged();

            }
        };
    }

    @Override
    public void datadownloadedcallback(ArrayList arrayList) {
        //for this class, the callback is used to check if there are new notifications for this user using Firebasehelper.checkNotifications();
        //if there are new notifications, the chat icon is changed to reflect this
        if ((boolean) arrayList.get(0)) {
            ((MyviewHolder) arrayList.get(1)).chat.setImageDrawable(mContext.getResources().getDrawable(R.drawable.new_chat));
        }
    }

    @Override
    public void finishedGettingPlace(final MyviewHolder myviewHolder, String s, String placeId) {
        if (s.equals("null")) {
            myviewHolder.eatingAt.setText(String.format("%s %s", myviewHolder.username.getText(), mContext.getString(R.string.hasnt_decided_yet)));
        } else {
            myviewHolder.eatingAt.setText(String.format(mContext.getString(R.string.eating_at), myviewHolder.username.getText(), s));
            Places.getGeoDataClient(mContext).getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        final PlaceBufferResponse response = task.getResult();
                        assert response != null;
                        for (final com.google.android.gms.location.places.Place foundPlace : response) {
                            myviewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onClick(View v) {
                                    String name,address,website,phoneNumber,id;
                                    double latitude,longitude;
                                    float rating = 0;

                                    try {
                                        name = foundPlace.getName().toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        name = mContext.getString(R.string.no_name_found);
                                    }
                                    try {
                                        address = foundPlace.getAddress().toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        address = mContext.getString(R.string.no_address_found);
                                    }
                                    try {
                                        website = foundPlace.getWebsiteUri().toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        website = mContext.getString(R.string.no_website_found);
                                    }
                                    try {
                                        phoneNumber = foundPlace.getPhoneNumber().toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        phoneNumber = mContext.getString(R.string.no_phone_number);
                                    }
                                    try {
                                        rating = foundPlace.getRating();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        id = foundPlace.getId();
                                    } catch (Exception e) {
                                        id = "0";
                                        e.printStackTrace();
                                    }
                                    try {
                                        latitude = foundPlace.getLatLng().latitude;
                                    } catch (Exception e) {
                                        latitude = 0.00;
                                        e.printStackTrace();
                                    }
                                    try {
                                        longitude = foundPlace.getLatLng().longitude;
                                    } catch (Exception e) {
                                        longitude = 0.00;
                                        e.printStackTrace();
                                    }

                                    String[] placeId = new String[]{foundPlace.getId()};
                                    String placeType = "";
                                    try {
                                        placeType = new HtmlParser().execute(placeId[0]).get(); //using the placeID , parse the detailed place type
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    Intent restaurantDetailPage = new Intent(getApplicationContext(), RestaurantActivity.class);

                                    restaurantDetailPage.putExtra(PojoPlace.PLACE_SERIALIZABLE_KEY,
                                            new PojoPlace(name, address, website, phoneNumber, placeType, rating, id, latitude, longitude,USER_LIST_FRAGMENT));

                                    getApplicationContext().startActivity(restaurantDetailPage);
                                }
                            });
                        }
                    }
                }
            });
        }
    }



    public static class MyviewHolder extends RecyclerView.ViewHolder {

        final TextView username;
        final TextView userEmail;
        final ImageView userPicture;
        final ImageView deleteFriend;
        final TextView eatingAt;
        final ImageView chat;


        private MyviewHolder(View itemView) {

            super(itemView);
            this.username = itemView.findViewById(R.id.uname);
            this.userEmail = itemView.findViewById(R.id.uEmail);
            this.userPicture = itemView.findViewById(R.id.profPic);
            this.deleteFriend = itemView.findViewById(R.id.deleteFriend);
            this.eatingAt = itemView.findViewById(R.id.eatingAt);
            this.chat = itemView.findViewById(R.id.chatIcon);

        }


    }



    //Unused interface methods
    @Override
    public void workUsersDataCallback(ArrayList arrayList) {
    }

    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
    }

    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
    }

    @Override
    public void isItLikedCallback(boolean response) {

    }

    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {

    }



}
