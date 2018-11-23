package com.example.robmillaci.go4lunch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

/**
 * This class created the first activity presented to a new user<br>
 * It deals with authenticating the user via Facebook or Google and handles the login event
 */
public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    private static final String GOOGLE_ID_TOKEN = "476591839693-6ukp9b83561afq71vmbglq0i6f4v0ig2.apps.googleusercontent.com";

    private static FirebaseAuth mAuth; //The entry point of the Firebase Authentication SDK

    static String loggedInUser; //the logged in user
    static String loggedInEmail; //the logged in users email
    static String loggedInPic; //the logged in users picture

    private CallbackManager callbackManager; //the loggin callback
    private GoogleSignInClient mGoogleSignInClient; //the GoogleSignInClient


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LauncherTheme);
        setContentView(R.layout.activity_start);
        FacebookSdk.sdkInitialize(getApplicationContext()); //initialized the facebook SDK
        AppEventsLogger.activateApp(this); //allows logging of various types of events back to Facebook.

        mAuth = FirebaseAuth.getInstance(); //Returns an instance of this class corresponding to the default FirebaseApp instance

        callbackManager = CallbackManager.Factory.create(); //creates an instance of callbackManager

        final LoginButton facebookLoginBtn = findViewById(R.id.facebook_login_btn); //default facebook login button
        facebookLoginBtn.setReadPermissions("email", "public_profile"); //the read permissions

        final Button facebookLogin = findViewById(R.id.facebook_login);
        //custom facebook login button. Visually different from the default button but will call the default buttons onClick method
        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLoginBtn.callOnClick();
            }
        });

        //register the callback and define the actions for onSuccess, onCancel and onError
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel: called");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG, "onError: " + exception);
                        Toast.makeText(getApplicationContext(),"Authentication error",Toast.LENGTH_LONG).show();
                    }
                });



        //build the google sign in options, passing in the ID token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_ID_TOKEN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); //get the GoogleSignIn client passing in the sign in options


        Button googleLogin = findViewById(R.id.google_login); //the google login button
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(); //call the google sign in method
            }
        });
    }


    //Create a signInIntent using the Google sign in client to get the sign in intent
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }


    //Handles the results of the Google sign in intent and then handle the results using handleSignInResults(task)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;

            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    //Get the results of the sign in intent and if successful, authenticate the users with fireback
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            if (account!=null) {
                firebaseAuthWithGoogle(account);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_LONG).show();
        }
    }

    //Handle the facebook access token and try to sign in with the Auth credentials
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser(); //get the current user

                            if (user != null) {
                                FirebaseUserMetadata metadata = user.getMetadata();
                                assert metadata != null;
                                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                                    // The user is new so add the user to the database and display welcome message
                                    addUserToDB(user);
                                    Toast.makeText(StartActivity.this, getString(R.string.welcome) + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                } else {
                                    //The user is not a new user, dont re-add to the database but display a welcome back message
                                    Toast.makeText(StartActivity.this, getString(R.string.welcome_back) + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                }
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Toast.makeText(StartActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    }
                });
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                FirebaseUserMetadata metadata = user.getMetadata();
                                //noinspection ConstantConditions
                                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                                    // The user is new
                                    addUserToDB(user);
                                } else {
                                    Toast.makeText(StartActivity.this, getString(R.string.welcome_back) + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                }
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                Toast.makeText(StartActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    @SuppressWarnings("ConstantConditions")
    /*
     * Once the user has been authenticated, logged in and added to the database. launch the MainActivity
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent launchMain = new Intent(this, MainActivity.class);
            loggedInUser = mAuth.getCurrentUser().getDisplayName();
            loggedInEmail = mAuth.getCurrentUser().getEmail();
            loggedInPic = mAuth.getCurrentUser().getPhotoUrl().toString();

            startActivity(launchMain);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    /**
     * Add the user to the database if they are a new user. Adding username, useremail,picture and unique ID<br>
     * Also gets the users device token and stores this in the user record in the database
     * @param fbUser the user to be added
     */
    private void addUserToDB(final FirebaseUser fbUser) {
        final FirebaseFirestore mFirebaseDatabase;
        mFirebaseDatabase = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();

        data.put("username", fbUser.getDisplayName());
        data.put("userEmail", fbUser.getEmail());
        //noinspection ConstantConditions
        data.put("picture", (fbUser.getPhotoUrl().toString()));
        data.put("uniqueID", fbUser.getUid());
        mFirebaseDatabase.collection("users").document(fbUser.getUid()).set(data);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    @SuppressWarnings("ConstantConditions") String idToken = task.getResult().getToken();
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", idToken);
                    mFirebaseDatabase.collection("users").document(fbUser.getUid()).update(data);
                    addUserTestData(data, mFirebaseDatabase);
                }
            }
        });
    }


    /*
      Test users added to the application
     */
    private void addUserTestData(Map<String, Object> data, FirebaseFirestore mFirebaseDatabase) {
        data.clear();
        data.put("username", "Kirsten New");
        data.put("userEmail", "Kirsten.new@hotmail.com");
        data.put("picture", ("https://images.pexels.com/photos/762020/pexels-photo-762020.jpeg?auto=compress&cs=tinysrgb&h=350"));
        data.put("uniqueID", "QFWSVSRGJMWEOIMWOFIMWEF");

        mFirebaseDatabase.collection("users").document("QFWSVSRGJMWEOIMWOFIMWEF").set(data);

        data.clear();
        data.put("username", "Dena Millaci");
        data.put("userEmail", "Dena.Millaci@hotmail.com");
        data.put("picture", ("https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&h=350"));
        data.put("uniqueID", "sdfagegbaerbq34gwrvrv");

        mFirebaseDatabase.collection("users").document("sdfagegbaerbq34gwrvrv").set(data);


        data.clear();
        data.put("username", "Phil Taylor");
        data.put("userEmail", "P.Taylor@hotmail.com");
        data.put("picture", ("https://previews.123rf.com/images/ostill/ostill1505/ostill150500232/39958303-one-man-mature-handsome-man-toothy-smile-portrait-studio-white-background.jpg"));
        data.put("uniqueID", "sdfsdfsfwefqw3r23r2352353fsv");

        mFirebaseDatabase.collection("users").document("sdfsdfsfwefqw3r23r2352353fsv").set(data);


        data.clear();
        data.put("username", "James Brown");
        data.put("userEmail", "Brown.J@hotmail.com");
        data.put("picture", ("https://vignette.wikia.nocookie.net/creepypasta/images/6/6c/7268969-Portrait-of-happy-smiling-man-isolated-on-white-Stock-Photo-face.jpg/revision/latest?cb=20161109170616"));
        data.put("uniqueID", "bnvbnmvmbvnmbvnmgfhjgfym");

        mFirebaseDatabase.collection("users").document("bnvbnmvmbvnmbvnmgfhjgfym").set(data);


        data.clear();
        data.put("username", "Paul Franklin");
        data.put("userEmail", "P.F@hotmail.com");
        data.put("picture", ("https://media.gettyimages.com/photos/closeup-of-smiling-man-on-white-background-picture-id482907157?b=1&k=6&m=482907157&s=612x612&w=0&h=Z8AYyf6JCW0dQY5e5SgG9wLPoPMTVur7HapjohqpWFc="));
        data.put("uniqueID", "rtyrtytrytrycbcbergbdrsg");

        mFirebaseDatabase.collection("users").document("rtyrtytrytrycbcbergbdrsg").set(data);


        data.clear();
        data.put("username", "Jenny Morse");
        data.put("userEmail", "JMorse@hotmail.com");
        data.put("picture", ("https://images.pexels.com/photos/372042/pexels-photo-372042.jpeg?auto=compress&cs=tinysrgb&h=350"));
        data.put("uniqueID", "rtyrtytrytrycbcbergbdrsg");

        mFirebaseDatabase.collection("users").document("tytytytytytytytytytytytytytytyt").set(data);


        data.clear();
        data.put("username", "Dorothy Taylor");
        data.put("userEmail", "DottyT@hotmail.com");
        data.put("picture", ("https://thumb1.shutterstock.com/mosaic_250/2967241/776697943/stock-photo-pretty-smiling-joyfully-female-with-fair-hair-dressed-casually-looking-with-satisfaction-at-776697943.jpg"));
        data.put("uniqueID", "xcvxcvxcvxcvxcv1213213213sdfsdf");

        mFirebaseDatabase.collection("users").document("bbbbbvvvvvvvvcccccc45454cdd").set(data);
    }

}




