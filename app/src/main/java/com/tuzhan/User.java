package com.tuzhan;


import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by Dhaulagiri on 6/12/2017.
 */

public class User extends DatabaseModel implements Serializable {

    String displayname;
    String email;
    String dpURL;
    String userId;
    int won_rounds;
    int played_rounds;

    public User(){
        //required empty constructor
    }

    //constructor for any arbitrary self
    public User(String displayname, String email, String userId, String dpURL){

        this.tableName = "USERS";
        this.displayname = displayname;
        this.email = email;
        this.userId = userId;
        this.dpURL = dpURL;

    }

    //special constructor for current self
    public User(String displayname, String email, String userId, String dpURL, int won_rounds, int played_rounds){

        this.tableName = "USERS";
        this.displayname = displayname;
        this.email = email;
        this.userId = userId;
        this.dpURL = dpURL;
        this.won_rounds = won_rounds;
        this.played_rounds = played_rounds;

    }

    // initialize arbitrary self object from database cursor
    public User(Cursor cursor){
        this.userId = cursor.getString(0);
        this.dpURL = cursor.getString(1);
        this.displayname = cursor.getString(2);
        this.email = cursor.getString(3);
    }

    // overrides for superclass DatabaseModel
    @Override
    public String selector() {
        return "id='" + userId + "'";
    }

    @Override
    public ContentValues composeUpdateValues() {
        ContentValues values2Update = new ContentValues();
        values2Update.put("id", userId);
        values2Update.put("dpURL", dpURL);
        values2Update.put("dpName", displayname);
        values2Update.put("email", email);
        return values2Update;
    }

}
