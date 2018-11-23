package com.example.robmillaci.go4lunch.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.ChatActivityAdapter;
import com.example.robmillaci.go4lunch.data_objects.chat_objects.ChatObject;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This activity is responsible for handling the chat service this app provides
 */
public class ChatActivity extends AppCompatActivity implements FirebaseHelper.chatData {
    private RecyclerView chatMessage; //The recycler view to display sent and recieved messages
    private EditText chatMessageEditText;//The edit text to compose the message to be sent

    private ChatActivityAdapter mChatActivityAdapter; //the adapter to link the activity with the recyclerview list

    public static final String CHATTING_TO = "ChattingToName"; //Intent bundle key for the username receiving the messages
    public static final String CHATTING_TO_ID = "chattingTo"; //Intent bundle key for the userID receiving the messages
    public static final String CHATTING_TO_PIC = "chattingToPicture"; //Intent bundle key for the user picture receiving the messages
    public static final String CURRENT_USER_PIC = "userPic"; //Intent bundle key for the user picture sending messages
    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"; //The pattern to format any retrieved date values

    /*
     private variables to hold the retrieved bundle values using the keys above
     */
    private String chattingToId;
    private String chattingToName;
    private String chattingToPicture;
    private String currentUserPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the home button to the action bar to navigate back from this activity
        }

        Button sendMessage = findViewById(R.id.sendMessage); //assigns the send message button


        chatMessage = findViewById(R.id.chatMessageRecyclerView); //assigns the chatMessage variable to the recyclerview
        chatMessageEditText = findViewById(R.id.chatMessage); //assigns the chatMessageEditText variable to the chat edit text


        if (getIntent().getExtras() != null) {
            chattingToId = getIntent().getExtras().getString(CHATTING_TO_ID); //the id of the user recieving the messages
            chattingToName = getIntent().getExtras().getString(CHATTING_TO); //the name of the user recieving the messages
            chattingToPicture = getIntent().getExtras().getString(CHATTING_TO_PIC); //the picture of the user recieving the messages
            currentUserPicture = getIntent().getExtras().getString(CURRENT_USER_PIC); //the picture of the user sending the messages
        }

        setTitle(String.format(getString(R.string.chattingTo), chattingToName)); //sets this activities title appending the name of the user receiving the messages

        getChatData(); //see method comments

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chatMessageEditText.getText().toString().equals("")) {
                    Map<String, Object> chatData = new HashMap<>();

                    Date date = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

                    chatData.put(dateFormat.format(date), StartActivity.loggedInUser + " - " + chatMessageEditText.getText().toString());

                    FirebaseHelper firebaseHelper = new FirebaseHelper(ChatActivity.this);
                    firebaseHelper.addChatData(chatData, chattingToId);

                    chatMessageEditText.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, R.string.please_enter_message_first, Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    /**
     * Retrieves any previous chat data between the two users using
     * {@link FirebaseHelper} class
     */
    private void getChatData() {
        FirebaseHelper firebaseHelper = new FirebaseHelper(ChatActivity.this);
        firebaseHelper.getCurrentUserChatData(FirebaseHelper.getmCurrentUserId(), chattingToId);
    }


    /**
     * Callback from {@link com.example.robmillaci.go4lunch.firebase.FirebaseHelper#getCurrentUserChatData}<br>
     * this is called once we have retrieved chat history<br>
     *
     * @param messagesSent     - The list of messages sent to the message receiver<br>
     * @param messagesRecieved - The listo of messages recieved from the message receiver<br>
     *                         <br>
     *                         Add all messagesSent and messagesRecieved to an array list and then sorted by date in descending order<br>
     *                         Once the arraylist has been sorted, create the Recyclerview adapter and set<br>
     */
    @Override
    public void gotChatData(ArrayList<ChatObject> messagesSent, ArrayList<ChatObject> messagesRecieved) {
        ArrayList<ChatObject> sortedArray = new ArrayList<>();

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        sortedArray.addAll(messagesSent); //add all the messages sent to the sortedArray list
        sortedArray.addAll(messagesRecieved); //add all the messages recieved to the sortedArray list

        Collections.sort(sortedArray, new Comparator<ChatObject>() { //sort all the stored messages in descending date order
            @Override
            public int compare(ChatObject o1, ChatObject o2) {
                try {
                    if (dateFormat.parse(o1.getDateString()).before(dateFormat.parse(o2.getDateString()))) {
                        return 1;
                    } else if (dateFormat.parse(o1.getDateString()).equals(dateFormat.parse(o2.getDateString()))) {
                        return 0;
                    } else {
                        return -1;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });

        if (mChatActivityAdapter == null) {
            mChatActivityAdapter = new ChatActivityAdapter(sortedArray, false, currentUserPicture, chattingToPicture);
            chatMessage.setLayoutManager(new LinearLayoutManager(this));
            chatMessage.setAdapter(mChatActivityAdapter);
        } else {
            mChatActivityAdapter = new ChatActivityAdapter(sortedArray, true, currentUserPicture, chattingToPicture);
            chatMessage.setAdapter(mChatActivityAdapter);
            mChatActivityAdapter.notifyDataSetChanged();

        }
    }

    /**
     * If the home button is pressed, call {@link #onBackPressed()}<br>
     *
     * @param item the menu item clicked
     */
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
     * Callback from {@link com.example.robmillaci.go4lunch.firebase.FirebaseHelper#addChatData(Map, String)}<br>
     * this is called when we send new chat data and need to refresh the recyclerview to display the new messages
     */
    @Override
    public void refreshData() {
        getChatData();
    }

    // TODO: 23/11/2018 check if we actually need the below code
//
//    @Override
//    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        outState.putBoolean("returnFromChat", true);
//        super.onSaveInstanceState(outState, outPersistentState);
//    }
}
