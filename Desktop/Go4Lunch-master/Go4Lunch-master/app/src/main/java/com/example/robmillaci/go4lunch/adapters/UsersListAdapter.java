package com.example.robmillaci.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.robmillaci.go4lunch.firebase.FirebaseHelper.DATABASE_ADDED_USERS_FIELD;

/**
 * This class is responsible for creating the adaptor to display all users of this app
 */
public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.MyviewHolder> implements
        FirebaseHelper.firebaseDataCallback,
        Filterable {

    private ArrayList<Object> mUsersArrayList; //Arraylist containing all the users
    private ArrayList<Object> filteredUsersList; //Arraylist to hold the filtered users
    private final ArrayList<Object> originalArray; //Arraylist to keep a copy of the origional list of users - used when filtering
    private String addedUIds; // the users 'added friends'
    private final Context mContext; //the context of the calling class


    public UsersListAdapter(ArrayList<Object> usersArrayList, Context context) {
        mUsersArrayList = usersArrayList;
        originalArray = usersArrayList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public UsersListAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_recycler_view, parent, false); //inflate and return the view
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersListAdapter.MyviewHolder holder, final int position) {
        final Users user = (Users) mUsersArrayList.get(position); //get the user related to this position
        final String name = user.getUsername(); //get the users name
        final String email = user.getUserEmail(); //get the users email
        final String uId = user.getUserID(); //get the users ID
        final String pic = user.getUserPic() == null ? "" : user.getUserPic(); //get the users picture

        holder.uniqueId = uId; //get the unique id of the user in this position

        FirebaseHelper firebaseHelper = new FirebaseHelper(this, holder);
        firebaseHelper.getAddedUsers();  //get the current users addedUsers. The results are called back to this classes finishedGettingUsers method

        holder.userEmail.setText(email); //set the users email
        holder.username.setText(name); //set the users name


        //load the users picture into the holder view
        if (!pic.equals("")) {
            Picasso.get().load(pic).into(holder.userPicture);
        } else {
            holder.userPicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }

        //set the addFriend on click listener. First retrieve the list of currently added users and then pass this to the addFriend method in FireBaseHelper class
        //Updates the UI to reflect the adding of a user and displays a Toast message to confirm 'Friend added'
        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResults = task.getResult();
                            if (taskResults != null) {
                                List<DocumentSnapshot> documents = taskResults.getDocuments();
                                DocumentSnapshot d = documents.get(0);
                                try {
                                    addedUIds = d.get(DATABASE_ADDED_USERS_FIELD) == null ? "" : (String) d.get(DATABASE_ADDED_USERS_FIELD); //get the added users UIDs from the database
                                } catch (Exception e) {
                                    addedUIds = "";
                                }

                                FirebaseHelper.addFriend(uId, addedUIds);

                                holder.addFriend.setImageResource(R.drawable.checked);
                                holder.addFriend.setClickable(false);
                                Toast.makeText(mContext, R.string.friend_added, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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
                    filteredUsersList = originalArray; //if no text is entered in the search box, set the filteredUserList to the original
                } else {
                    ArrayList<Object> queryfilteredList = new ArrayList<>(); //create a new arraylist to hold the filtered results
                    for (Object row : originalArray) { //loop through each object in the originalArray
                        // here we are looking for a match on the uses username or email
                        if (((Users) row).getUsername().toLowerCase().contains(charString.toLowerCase())
                                || ((Users) row).getUserEmail().toLowerCase().contains(charString.toLowerCase())) {
                            queryfilteredList.add(row); //if the condition is med, add the user to the filtered array list
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
                mUsersArrayList = (ArrayList<Object>) results.values;
                notifyDataSetChanged();

            }
        };
    }


    /**
     * called after {@link FirebaseHelper#getAddedUsers()}
     * If the viewholders current user's ID is within the returned users Array, this user has been added and so we make UI changes to reflect this
     *
     * @param users      the current users added users (friends)
     * @param viewHolder the viewholder that the UI changes are applied to
     */
    @Override
    public void finishedGettingUsers(String[] users, MyviewHolder viewHolder) {
        for (String user : users) {
            if (viewHolder.uniqueId.equals(user)) {
                viewHolder.addFriend.setImageResource(R.drawable.checked);
                viewHolder.addFriend.setClickable(false);
            }
        }
    }


    public static class MyviewHolder extends RecyclerView.ViewHolder {

        final TextView username;
        final TextView userEmail;
        final ImageView userPicture;
        final ImageView addFriend;
        String uniqueId;

        private MyviewHolder(View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.uname);
            this.userEmail = itemView.findViewById(R.id.uEmail);
            this.userPicture = itemView.findViewById(R.id.profPic);
            this.addFriend = itemView.findViewById(R.id.addFriend);
            this.uniqueId = "";
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
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
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
