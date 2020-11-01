package com.waterworks.close;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;


public class User {
    public String username;
    public GeoPoint location;
    public ArrayList<String> friends;
    public ArrayList<String> chats;
    public boolean online;
    public String dob;
    public int age;

    public User(){

    }

    public User(String username, GeoPoint location, ArrayList<String> friends, ArrayList<String> chats, boolean online, String dob) {
        this.username = username;
        this.location = location;
        this.friends = friends;
        this.chats = chats;
        this.online = online;
        this.dob = dob;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public ArrayList<String> getChats() {
        return chats;
    }

    public void setChats(ArrayList<String> chats) {
        this.chats = chats;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}