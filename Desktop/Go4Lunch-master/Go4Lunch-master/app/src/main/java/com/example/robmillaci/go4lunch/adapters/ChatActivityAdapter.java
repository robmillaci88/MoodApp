package com.example.robmillaci.go4lunch.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.data_objects.chat_objects.ChatObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 * This class is responsible for creating the adaptor to display chat data to users
 * Contains a list of {@link ChatObject}
 */
public class ChatActivityAdapter extends RecyclerView.Adapter<ChatActivityAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECIEVED = 2;

    private final ArrayList<ChatObject> mChatData; //Arraylist holding ChatObjects
    private final String mCurrentUserPic; //the current users picture url
    private final String mChattingToPic; //the picture url of the users receiving the messages

    private Animation sentAnimation;
    private Animation recievedAnimation;
    private final boolean mUpdating; //boolean to determine if we updating using notifyDataSetChange()


    public ChatActivityAdapter(ArrayList<ChatObject> chatData, boolean updating, String currentUserPic, String chattingToPic) {
        mChatData = chatData;
        mCurrentUserPic = currentUserPic;
        mChattingToPic = chattingToPic;
        mUpdating = updating;

        createAnimations(); //Creates the animations used when displaying the chat messages
    }



    private void createAnimations() {
        sentAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        sentAnimation.setDuration(500);

        recievedAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        recievedAnimation.setDuration(500);
    }



    /*
    Inflates one of 2 views.
    If a message is being recieved, inflate chat_message_view_recieved
    If a message is being sent, inflate chat_message_view
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == VIEW_TYPE_MESSAGE_RECIEVED){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_view_recieved, parent,false);
            return new MyViewHolder(v);
        }else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_view, parent,false);
            return new MyViewHolder(v);
        }
    }




    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatObject currentChatObject = mChatData.get(position);
        String chatMessage = currentChatObject.getMessageBody();
        String time = currentChatObject.getDateString();

        holder.timeStamp.setText(time); //set the time stamp of the message
        holder.messageBody.setText(chatMessage); //set the message body

        //Depending on wether the message is sent or recieves, load the specific users picture into the imageview
        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_RECIEVED:
                Picasso.get().load(mChattingToPic).into(holder.userPic);
                break;

            case VIEW_TYPE_MESSAGE_SENT:
                Picasso.get().load(mCurrentUserPic).into(holder.userPic);
                break;
        }


      if (mUpdating) { // if we are using notifyDataChange()
            if (position  == 0 && holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
                holder.itemView.startAnimation(sentAnimation);
            }else if(position == 0 && holder.getItemViewType() == VIEW_TYPE_MESSAGE_RECIEVED){
                holder.itemView.startAnimation(recievedAnimation);
          }
        }

    }


    //Return wether the chat object is a sender object or a reciever object and return the viewType
    @Override
    public int getItemViewType(int position) {
        ChatObject currentChatObject = mChatData.get(position);

        if (currentChatObject.isSender()){
            return VIEW_TYPE_MESSAGE_RECIEVED;
        }else {
            return VIEW_TYPE_MESSAGE_SENT;
        }
    }

    @Override
    public int getItemCount() {
        return mChatData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView messageBody;
        final TextView timeStamp;
        final ImageView userPic;

        private MyViewHolder(View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.messageBody);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            userPic = itemView.findViewById(R.id.chatPic);
        }
    }

}
