package com.kelompok5.open_notepad.entity;

import java.sql.Date;

public class Note {
    private int moduleID;
    private String ownerID;
    private String title;
    private String description;
    private String course;
    private String major;
    private Date uploadDate;
    private Double rating;
    private boolean visibility;
    private File file;

    // getter
    public int getModuleID() {
        return moduleID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCourse() {
        return course;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public File getFile() {
        return file;
    }

    public String getMajor() {
        return major;
    }

    public String getOwnerID(String ownerID) {
        return this.ownerID;
    }

    public Double getRating() {
        return rating;
    }

    // setter
    public void setRating(Double rating) {
        this.rating = rating;
    }

    // setter
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }

}
