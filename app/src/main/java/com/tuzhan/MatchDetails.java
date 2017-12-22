package com.tuzhan;

/**
 * Created by Dhaulagiri on 8/12/2017.
 */

public class MatchDetails {

    String match_id;
    User opponent;
    String outcome;
    String topic;

    public MatchDetails(){
        //required empty constructor
    }

    MatchDetails(String match_id, User opponent, String outcome, String topic){
        this.match_id = match_id;
        this.opponent = opponent;
        this.outcome = outcome;
        this.topic = topic;
    }

}
