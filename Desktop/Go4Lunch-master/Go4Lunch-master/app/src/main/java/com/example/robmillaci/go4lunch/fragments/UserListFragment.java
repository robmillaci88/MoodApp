package com.example.robmillaci.go4lunch.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * This class is responsible for creating the list of users in the system
 */
public class UserListFragment extends Fragment implements FirebaseHelper.firebaseDataCallback {
    private final String TAB_SHAREDPREF_KEY = "tabSelected";
    private final int ALL_WORKERS_TAB = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MY_WORK_MATES_TAB = 1;

    private final FirebaseHelper mFirebaseHelper = new FirebaseHelper(this);
    private View mFragmentView;
    private AddedUsersAdapter mAdaptor;//the adaptor for the friends list view
    private UsersListAdapter mAdaptorAll;//the adaptor for ALL users list view
    private RecyclerView mUsersList; // the recyclerview to hold the list of users
    private ProgressBar mAllUsersProgress; //the progress bar to be displayed when searching for all users
    private ProgressBar mMyWorkersProgress; //the progress bar to be displayed when searching for added users
    private TabLayout mTabLayout; //The tab layout to switch between all users and added users


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(getString(R.string.friends_list));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //create the search view and define the filter
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mAdaptor == null && mAdaptorAll != null) {
                    mAdaptorAll.getFilter().filter(query);
                } else if (mAdaptor != null && mAdaptorAll == null) {
                    mAdaptor.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdaptor == null && mAdaptorAll != null) {
                    mAdaptorAll.getFilter().filter(newText);
                } else if (mAdaptor != null && mAdaptorAll == null) {
                    mAdaptor.getFilter().filter(newText);
                }
                return false;
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.friends_list_fragment, container, false);
        setHasOptionsMenu(true);
        return mFragmentView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAllUsersProgress = view.findViewById(R.id.allUsersProgress);
        mAllUsersProgress.setVisibility(View.INVISIBLE);
        mMyWorkersProgress = view.findViewById(R.id.myWorkerProgress2);
        mMyWorkersProgress.setVisibility(View.INVISIBLE);

        //noinspection ConstantConditions
        mUsersList = view.findViewById(R.id.usersListRecyclerView);
        mUsersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mTabLayout = view.findViewById(R.id.usersTab);
        //noinspection ConstantConditions
        mAllUsersProgress.setVisibility(View.VISIBLE);
        mAllUsersProgress.setProgress(1);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getMyWorkUsers();//see method comments
                        break;

                    case 1:
                        getallUsers(); //see method comments
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }


    //displays the searching progress bar and calls getAllusers()
    private void getallUsers() {
        mMyWorkersProgress.setVisibility(View.INVISIBLE);
        mAllUsersProgress.setVisibility(View.VISIBLE);
        mAllUsersProgress.setProgress(1);
        mFirebaseHelper.getAllUsers();
    }


    //displays the searching progress bar and calls getMyWorkUsers()
    private void getMyWorkUsers() {
        mAllUsersProgress.setVisibility(View.INVISIBLE);
        mMyWorkersProgress.setVisibility(View.VISIBLE);
        mMyWorkersProgress.setProgress(1);
        mFirebaseHelper.getMyWorkUsers();
    }


    /**
     * Callback from {@link FirebaseHelper#getAllUsers()}
     *
     * @param arrayList the returned results
     */
    @Override
    public void datadownloadedcallback(ArrayList<Object> arrayList) {
        mAllUsersProgress.setVisibility(View.INVISIBLE);
        //noinspection unchecked
        mAdaptor = null;
        mAdaptorAll = new UsersListAdapter(arrayList, getApplicationContext()); //create the adapter for all users
        RecyclerViewMods.setAnimation(mUsersList); //set the animation to the recycler view to enhance UX
        mUsersList.setAdapter(mAdaptorAll); //set the adapter

        if (arrayList == null) {
            mFragmentView.setBackgroundResource(R.drawable.nousersfound); //if the returned results are null, display to the user that no users have been found
        } else {
            mFragmentView.setBackgroundResource(0);
        }
    }


    /**
     * callback from {@link FirebaseHelper#getAddedUsers()}
     *
     * @param arrayList the returned results
     */
    @Override
    public void workUsersDataCallback(ArrayList<Users> arrayList) {
        mMyWorkersProgress.setVisibility(View.INVISIBLE);
        mAdaptorAll = null;

        if (arrayList == null) {
            mUsersList.setAdapter(mAdaptor); //if the returned results is null, we want to clear any items in the recyclerview and set the background to no friends found
            mFragmentView.setBackgroundResource(R.drawable.no_friends_found);
        } else {
            //noinspection ConstantConditions
            mAdaptor = new AddedUsersAdapter(arrayList, this.getActivity().getWindow().getContext()); //create the adapter
            RecyclerViewMods.setAnimation(mUsersList); //set the animations
            mUsersList.setAdapter(mAdaptor); //set the adapter
            mFragmentView.setBackgroundResource(0);//remove the background
        }
    }


    //save the tab the user last selected so we can restore the same tab
    public void onPause() {
        super.onPause();
        @SuppressWarnings("ConstantConditions")
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();

        spEditor.putInt(TAB_SHAREDPREF_KEY, mAdaptor == null ? MY_WORK_MATES_TAB : ALL_WORKERS_TAB);
        spEditor.apply();
    }


    //retrieve the tab the user last selected and restore the data
    @Override
    public void onResume() {
        @SuppressWarnings("ConstantConditions")
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        int tabSelectedId = sp.getInt(TAB_SHAREDPREF_KEY, ALL_WORKERS_TAB);
        TabLayout.Tab tab = mTabLayout.getTabAt(tabSelectedId);
        if (tab != null) {
            tab.select();
        }
        super.onResume();
    }


    //unused interface methods
    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
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
