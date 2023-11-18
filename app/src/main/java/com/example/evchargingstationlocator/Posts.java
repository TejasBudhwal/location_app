package com.example.evchargingstationlocator;

import android.net.Uri;

public class Posts
{
    public String publisher, location, message, imageName, postImage;
    public String date, time;
    public Posts()
    {

    }

    @Override
    public String toString() {
        return "Posts{" +
                "publisher='" + publisher + '\'' +
                ", location='" + location + '\'' +
                ", message='" + message + '\'' +
                ", fullName='" + imageName + '\'' +
                ", postImage='" + postImage + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date1) {
        this.date = date1;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time1) {
        this.time = time1;
    }

}
