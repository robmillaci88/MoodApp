package com.example.robmillaci.go4lunch.data_objects.chat_objects;

/**
 * This class is reponsible for create ChatObjects user in {@link com.example.robmillaci.go4lunch.adapters.ChatActivityAdapter}
 */
public class ChatObject {
    private final String dateString; //the date the message was sent
    private final String messageBody; //the message body
    private final boolean sender; //true = this message was sent by the sender, false this message was sent by the receiver
    private static final String SEPARATOR_VALUE = "-"; //the value that separates the message composers name from the message body
    private static final int SEPARATOR_OFFSET = 1; //offset applied to the index value of the SEPARATOR_VALUE

    public ChatObject(String dateString, String messageBody, boolean sender) {
        this.dateString = dateString;
        this.messageBody = messageBody.substring((messageBody.indexOf(SEPARATOR_VALUE)+ SEPARATOR_OFFSET),messageBody.length()).trim();
        this.sender = sender;
    }

    public String getDateString() {
        return dateString;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public boolean isSender() {
        return sender;
    }
}
