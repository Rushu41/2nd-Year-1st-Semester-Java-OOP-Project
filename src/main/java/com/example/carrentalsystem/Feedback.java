package com.example.carrentalsystem;

public class Feedback {

    private String username;
    private String feedback;
    private String reply;

    public Feedback(String username, String feedback, String reply) {
        this.username = username;
        this.feedback = feedback;
        this.reply = reply;
    }

    public String getUsername() {
        return username;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}