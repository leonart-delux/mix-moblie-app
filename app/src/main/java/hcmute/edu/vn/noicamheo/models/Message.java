package hcmute.edu.vn.noicamheo.models;

import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String content;
    private Date timestamp;
    private boolean isOutgoing;

    public Message() {
        // Required empty constructor
    }

    public Message(String id, String senderId, String senderName, String recipientId, 
                  String content, Date timestamp, boolean isOutgoing) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
        this.isOutgoing = isOutgoing;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    public void setOutgoing(boolean outgoing) {
        isOutgoing = outgoing;
    }
}
