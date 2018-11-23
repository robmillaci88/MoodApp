package com.example.robmillaci.go4lunch.firebase;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.data_objects.chat_objects.ChatObject;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private static final String mCurrentUserId;
    private static final String mCurrentUserPicUrl;
    private static int count;
    private static final DocumentReference userDoc;

    static final String DATABASE_TOKEN_PATH = "token";
    private static final String DATABASE_COLLECTION_PATH = "users";
    private static final String DATABASE_CHAT_COLLECTION = "chatdata";
    private static final String DATABASE_CHAT_NOTIFICATIONS = "chatNotifications";
    private static final String DATABASE_NAME_FIELD = "username";
    private static final String DATABASE_EMAIL_FIELD = "userEmail";
    private static final String DATABASE_PICTURE_FIELD = "picture";
    private static final String DATABASE_UNIQUE_ID_FIELD = "uniqueID";
    public static final String DATABASE_ADDED_USERS_FIELD = "addedUsers";
    public static final String DATABASE_SELECTED_RESTAURANT_FIELD = "selectedRestaurant";
    public static final String DATABASE_SELECTED_RESTAURANT_ID_FIELD = "selectedPlaceID";
    private static final String DATABASE_LIKED_RESTAURANTS_FIELD = "likedPlaces";
    private static final String NEW_MESSAGE_FIELD = "New message";

    private firebaseDataCallback mFirebaseDataCallback;
    private chatData mChatDataCallback;

    private UsersListAdapter.MyviewHolder mViewHolder;
    private ArrayList<Users> usersObjects = new ArrayList<>();


    public FirebaseHelper(firebaseDataCallback mCallback) {
        mFirebaseDataCallback = mCallback;
    }

    public FirebaseHelper(chatData mCallback) {
        mChatDataCallback = mCallback;
    }

    public FirebaseHelper(firebaseDataCallback mCallback, UsersListAdapter.MyviewHolder viewholder) {
        mFirebaseDataCallback = mCallback;
        this.mViewHolder = viewholder;
    }

    static {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        mCurrentUserId = currentUser.getUid();
        //noinspection ConstantConditions
        mCurrentUserPicUrl = currentUser.getPhotoUrl().toString();
        userDoc = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(FirebaseHelper.getmCurrentUserId());
    }

    private static Query getQueryDocSnapshot(String userId) {
        return FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).whereEqualTo(DATABASE_UNIQUE_ID_FIELD, userId);
    }

    public static Query getCurrentUserData() {
        return getQueryDocSnapshot(mCurrentUserId);
    }


    public void getMyWorkUsers() {
        FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> addedUsersArray = new ArrayList<>();
                if (task.isSuccessful()) {
                    try {
                        QuerySnapshot taskResults = task.getResult();
                        @SuppressWarnings("ConstantConditions") List<DocumentSnapshot> documents = taskResults.getDocuments();
                        @SuppressWarnings("ConstantConditions") String[] addedUsers = documents.get(0).get(DATABASE_ADDED_USERS_FIELD).toString().split(",");

                        //noinspection unchecked
                        addedUsersArray.addAll(Arrays.asList(addedUsers));
                    } catch (Exception e) {
                        e.printStackTrace();
                        createUserObjects(null);
                    }
                }
                createUserObjects(addedUsersArray);
            }
        });
    }

    private void createUserObjects(ArrayList<String> addedUsersArray) {
        if (addedUsersArray == null) {
            mFirebaseDataCallback.workUsersDataCallback(null);
        } else {
            usersObjects = new ArrayList<>();
            count = addedUsersArray.size();
            for (String s : addedUsersArray) {
                Query docRef = FirebaseHelper.getQueryDocSnapshot(s);
                docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResults = task.getResult();
                            List<DocumentSnapshot> documents = taskResults.getDocuments();
                            String userName = "";
                            String email = "";
                            String id = "";
                            String picture = "";
                            try {
                                DocumentSnapshot d = documents.get(0);
                                userName = (String) d.get(DATABASE_NAME_FIELD);
                                email = (String) d.get(DATABASE_EMAIL_FIELD);
                                id = (String) d.get(DATABASE_UNIQUE_ID_FIELD);
                                picture = (String) d.get(DATABASE_PICTURE_FIELD);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!userName.equals("") && !email.equals("") && !id.equals("")) {
                                Users user = new Users(userName, id, email, picture);
                                usersObjects.add(user);
                                count--;

                                if (count == 0) {
                                    mFirebaseDataCallback.workUsersDataCallback(usersObjects);
                                }
                            } else {
                                mFirebaseDataCallback.workUsersDataCallback(null);
                            }
                        }
                    }
                });
            }

        }

    }

    public void getAllUsers() {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot taskResults = task.getResult();

                    if (taskResults != null) {
                        List<DocumentSnapshot> documents = taskResults.getDocuments();

                        ArrayList<Object> usersArrayList = new ArrayList<>();

                        for (DocumentSnapshot d : documents) {
                            String name = (String) d.get(DATABASE_NAME_FIELD);
                            String email = (String) d.get(DATABASE_EMAIL_FIELD);
                            String picture = (String) d.get(DATABASE_PICTURE_FIELD);
                            String uniqueID = (String) d.get(DATABASE_UNIQUE_ID_FIELD);

                            Users user = new Users(name, uniqueID, email, picture);
                            usersArrayList.add(user);
                        }
                        mFirebaseDataCallback.datadownloadedcallback(usersArrayList);
                    }
                }
            }
        });
    }

    public void getAddedUsers() {
        FirebaseHelper.getCurrentUserData().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            QuerySnapshot taskResults = task.getResult();
                            @SuppressWarnings("ConstantConditions") List<DocumentSnapshot> documents = taskResults.getDocuments();
                            DocumentSnapshot d = documents.get(0);
                            String addedUIDs = (String) d.get(DATABASE_ADDED_USERS_FIELD);
                            assert addedUIDs != null;
                            String[] addedUsersUniqueID = addedUIDs.split(",");
                            mFirebaseDataCallback.finishedGettingUsers(addedUsersUniqueID, mViewHolder);

                        } catch (Exception e) {
                            mFirebaseDataCallback.finishedGettingUsers(new String[]{""}, mViewHolder);
                        }
                    }
                });

    }

    public static void deleteField(String fieldToDelete) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldToDelete, FieldValue.delete());
        userDoc.update(updates);
    }

    public static void updateField(String field, String values) {
        userDoc.update(field, values);
    }


    public void addSelectedMarker(String markerId, String placeId, String oldplaceId) {
        try {
            FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_FIELD);
            FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_ID_FIELD);
            Marker m = GoogleMapsFragment.getSpecificMarker(oldplaceId);
            m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
            m.setTag("notSelected");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(DATABASE_SELECTED_RESTAURANT_FIELD, markerId);
        updates.put(DATABASE_SELECTED_RESTAURANT_ID_FIELD, placeId);
        userDoc.update(updates);

    }

    public static void likeRestaurant(final String placeID) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                StringBuilder sb = new StringBuilder();

                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    sb.append(placeID);
                    try {
                        //noinspection ConstantConditions
                        String likedPlaces = taskResults.get("likedPlaces").toString();
                        String[] likedPlacesArray = likedPlaces.split(",");

                        for (String s : likedPlacesArray) {
                            sb.append(",");
                            sb.append(s);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(DATABASE_LIKED_RESTAURANTS_FIELD, sb.toString());
                    userDoc.update(updates);
                }
            }
        });
    }

    public void isItLiked(final String id) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        @SuppressWarnings("ConstantConditions") String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString();
                        String[] likedPlacesArray = likedPlaces.split(",");
                        for (String s : likedPlacesArray) {
                            if (s.equals(id)) {
                                mFirebaseDataCallback.isItLikedCallback(true);
                                return;
                            }
                        }

                    } catch (Exception e) {
                        mFirebaseDataCallback.isItLikedCallback(false);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void removeLikeRestaurant(final String id) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                StringBuilder sb = new StringBuilder();

                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        @SuppressWarnings("ConstantConditions") String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString();
                        String[] likedPlacesArray = likedPlaces.split(",");

                        int count = 0;
                        for (String s : likedPlacesArray) {
                            if (!s.equals(id)) {
                                if (count == 0) {
                                    sb.append(s);
                                    count++;
                                } else {
                                    sb.append(",");
                                    sb.append(s);
                                }
                            }
                        }

                        if (sb.length() > 0) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put(DATABASE_LIKED_RESTAURANTS_FIELD, sb.toString());
                            userDoc.update(updates);
                        } else {
                            userDoc.update(DATABASE_LIKED_RESTAURANTS_FIELD, FieldValue.delete());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public void getLikedRestaurants() {
        final ArrayList<String> places = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        //noinspection ConstantConditions
                        String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString();
                        String[] likedPlacesArray = likedPlaces.split(",");

                        places.addAll(Arrays.asList(likedPlacesArray));

                        mFirebaseDataCallback.finishedGettingLikedRestaurants(places);

                    } catch (Exception e) {
                        mFirebaseDataCallback.finishedGettingLikedRestaurants(places);
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public void getSelectedPlace(String userId,
                                 final AddedUsersAdapter.MyviewHolder holder) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        if (taskResults.get(DATABASE_SELECTED_RESTAURANT_FIELD) != null) {
                            String place = taskResults.get(DATABASE_SELECTED_RESTAURANT_FIELD).toString();
                            String placeId = taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString();
                            mFirebaseDataCallback.finishedGettingPlace(holder, place, placeId);
                        } else {
                            mFirebaseDataCallback.finishedGettingPlace(holder, "null", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mFirebaseDataCallback.finishedGettingPlace(holder, "null", "");
                    }
                }
            }
        });
    }


    public void getUsersEatingHere(final String id, final RecyclerView.ViewHolder v) {
        final ArrayList<Users> foundUsers = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot taskResults = task.getResult();

                    if (taskResults != null) {
                        List<DocumentSnapshot> documents = taskResults.getDocuments();

                        int count = documents.size();
                        for (DocumentSnapshot d : documents) {
                            count--;
                            try {
                                String selectedPlaceId = (String) d.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD);
                                //noinspection ConstantConditions
                                if (selectedPlaceId != null && selectedPlaceId.equals(id)) {
                                    String name = (String) d.get(DATABASE_NAME_FIELD);
                                    String email = (String) d.get(DATABASE_EMAIL_FIELD);
                                    String picture = (String) d.get(DATABASE_PICTURE_FIELD);
                                    String uniqueID = (String) d.get(DATABASE_UNIQUE_ID_FIELD);
                                    foundUsers.add(new Users(name, uniqueID, email, picture));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (count == 0) {
                                mFirebaseDataCallback.finishedGettingEaters(foundUsers, v);
                            }
                        }
                    }
                }
            }
        });

    }

    public void addChatData(final Map<String, Object> chatData, final String chattingTo) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_COLLECTION)
                .document(chattingTo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();
                if (taskResults != null) {
                    Map<String, Object> userChatData = taskResults.getData();
                    if (userChatData == null) {
                        dbRef.collection(DATABASE_CHAT_COLLECTION).document(chattingTo).set(chatData);
                        mChatDataCallback.refreshData();
                    } else {
                        userChatData.putAll(chatData);
                        dbRef.collection(DATABASE_CHAT_COLLECTION).document(chattingTo).set(userChatData);
                        mChatDataCallback.refreshData();
                    }
                }
            }
        });
    }

    public void getCurrentUserChatData(final String userId, final String chattingToId) {

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(userId).collection(DATABASE_CHAT_COLLECTION)
                .document(chattingToId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                if (taskResults != null) {
                    Map<String, Object> userChatData = taskResults.getData();

                    ArrayList<ChatObject> chatObjects = new ArrayList<>();
                    if (userChatData != null) {
                        Object[] keySet = userChatData.keySet().toArray();

                        for (int i = 0; i < userChatData.size(); i++) {
                            String key = (String) keySet[i];
                            String messageBody = (String) userChatData.get(key);
                            chatObjects.add(new ChatObject(key, messageBody, false));
                        }
                    }
                    getChattingUserData(chattingToId, userId, chatObjects);
                }
            }
        });
    }

    private void getChattingUserData(String chattingToId, String userId, final ArrayList<ChatObject> userChatData) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(chattingToId).collection(DATABASE_CHAT_COLLECTION)
                .document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                if (taskResults != null) {
                    Map<String, Object> chattingToUserData = taskResults.getData();

                    ArrayList<ChatObject> ChattingTochatObjects = new ArrayList<>();
                    if (chattingToUserData != null) {
                        Object[] keySet = chattingToUserData.keySet().toArray();

                        for (int i = 0; i < userChatData.size(); i++) {
                            String key = (String) keySet[i];
                            String messageBody = (String) chattingToUserData.get(key);
                            ChattingTochatObjects.add(new ChatObject(key, messageBody, true));
                        }
                    }
                    mChatDataCallback.gotChatData(userChatData, ChattingTochatObjects);
                }
            }
        });
    }


    public static void newMessage(final String messageFromUserId) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(messageFromUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    if (taskResults != null) {
                        Map<String, Object> notificationData = taskResults.getData();
                        if (notificationData == null) {
                            notificationData = new HashMap<>();
                            notificationData.put(NEW_MESSAGE_FIELD, 1);
                            dbRef.collection(DATABASE_CHAT_NOTIFICATIONS).document(messageFromUserId).set(notificationData);
                        } else {
                            notificationData.put(NEW_MESSAGE_FIELD, 1);
                            dbRef.collection(DATABASE_CHAT_NOTIFICATIONS).document(messageFromUserId).set(notificationData);
                        }
                    }
                }
            }
        });
    }

    public void checkNewNotifications(final String userID, final RecyclerView.ViewHolder viewHolder) {

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().getData() != null) {
                        //we have a new notification from this user
                        ArrayList<Object> response = new ArrayList<>();
                        response.add(true);
                        response.add(viewHolder);
                        mFirebaseDataCallback.datadownloadedcallback(response);

                    }
                }
            }
        });
    }

    public static void addFriend(String userId, String friendId) {
        final StringBuilder sb = new StringBuilder();
        if ("".equals(friendId)) {
            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).update(DATABASE_ADDED_USERS_FIELD, userId);
        } else {
            sb.append(friendId);
            sb.append(",").append(userId);
            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).update(DATABASE_ADDED_USERS_FIELD, sb.toString());
        }
    }


    public static void removeChatNotification(String chattingToUserId) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(chattingToUserId).delete();
    }

    public static String getmCurrentUserId() {
        return mCurrentUserId;
    }

    public static String getmCurrentUserPicUrl() {
        return mCurrentUserPicUrl;
    }


    public interface firebaseDataCallback {
        void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v);

        void datadownloadedcallback(ArrayList<Object> arrayList);

        void workUsersDataCallback(ArrayList<Users> arrayList);

        void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder);

        void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId);

        void isItLikedCallback(boolean response);

        void finishedGettingLikedRestaurants(ArrayList<String> places);
    }

    public interface chatData {
        void gotChatData(ArrayList<ChatObject> messagesSent, ArrayList<ChatObject> messagesRecieved);

        void refreshData();
    }
}



