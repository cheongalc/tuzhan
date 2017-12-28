package com.tuzhan;


import java.io.Serializable;

/**
 * Created by Dhaulagiri on 6/12/2017.
 */

public class User implements Serializable {

    String displayname;
    String email;
    String dpURL;
    String userId;
    int won_rounds;
    int played_rounds;

    public User(){
        //required empty constructor
    }

    //constructor for any arbitrary user
    public User(String displayname, String email, String userId, String dpURL){

        this.displayname = displayname;
        this.email = email;
        this.userId = userId;
        this.dpURL = dpURL;

    }

    //special constructor for current user
    public User(String displayname, String email, String userId, String dpURL, int won_rounds, int played_rounds){

        this.displayname = displayname;
        this.email = email;
        this.userId = userId;
        this.dpURL = dpURL;
        this.won_rounds = won_rounds;
        this.played_rounds = played_rounds;

    }

}
