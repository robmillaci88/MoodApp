package com.example.robmillaci.go4lunch.data_objects;

/**
 * Responsible for creating User objects in the application. Data from Firebase is passed into this class to create objects representing the users of the app
 */
public class Users {
    private final String username;
    private final String userID;
    private final String userEmail;
    private final String userPic;

    public Users(String username, String userID, String userEmail, String picture) {
        this.username = username;
        this.userID = userID;
        this.userEmail = userEmail;
        this.userPic = picture;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPic() {
        return userPic;
    }
}
