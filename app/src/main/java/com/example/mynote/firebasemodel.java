package com.example.mynote;

import com.google.firebase.Timestamp;


public class firebasemodel {

    private String title;
    private String content;
    private Timestamp editTime;
    private Timestamp creationTime; // Added new field for creation time
    private int colorIndex;

    public firebasemodel() { }

    public firebasemodel(String title, String content, Timestamp editTime) {
        this.title = title;
        this.content = content;
        this.editTime = editTime;
        // creationTime is not set in this constructor, assuming it's handled separately
    }

    public String getTitle() {
        return title;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getEditTime() {
        return editTime;
    }

    public void setEditTime(Timestamp editTime) {
        this.editTime = editTime;
    }

    // Getter and Setter for creationTime
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }
}