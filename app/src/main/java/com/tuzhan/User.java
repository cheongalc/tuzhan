package com.tuzhan;


/**
 * Created by Dhaulagiri on 6/12/2017.
 */

public class User {

    String displayname;
    String email;
    String dpURL;
    String userId;

    public User(){}

    public User(String displayname, String email, String userId, String dpURL){

        this.displayname = displayname;
        this.email = email;
        this.userId = userId;
        this.dpURL = dpURL;

    }

}
