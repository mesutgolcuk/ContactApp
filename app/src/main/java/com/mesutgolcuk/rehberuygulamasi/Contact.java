package com.mesutgolcuk.rehberuygulamasi;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Each contact in contact list
 */
public class Contact implements Comparable {

    private String id;
    private String name;
    private ArrayList<String> phoneNumbers;
    private String email;
    private Bitmap photo;
    private Bitmap displayPhoto;
    private ArrayList<String> locations;
    // relation with the user
    private Relation relation;

    public Contact() {
        phoneNumbers = new ArrayList<>();
        locations = new ArrayList<>();
        relation = new Relation();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<String> locations) {
        this.locations = locations;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Bitmap getDisplayPhoto() {
        return displayPhoto;
    }

    public void setDisplayPhoto(Bitmap displayPhoto) {
        this.displayPhoto = displayPhoto;
    }

    public Contact getContact(){
        return this;
    }
    @Override
    public int compareTo(Object another) {
        Contact o = (Contact) another;
        return getName().compareToIgnoreCase(o.getName());
    }

    public void addPhoneNumber(String number){
        phoneNumbers.add(number);
    }
    public void addLocation(String location){
        locations.add(location);
    }
    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;
        if(o instanceof String)
            return name.equalsIgnoreCase((String)o);
        else if(o instanceof Contact)
            return equals(((Contact)o).name);
        return false;
    }
}
