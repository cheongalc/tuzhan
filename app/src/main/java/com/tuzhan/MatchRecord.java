package com.tuzhan;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

/**
 * Created by chenchangheng on 7/12/17.
 */

public class MatchRecord extends DatabaseModel {

    private static final String DEBUG_TAG = "MATCHRECORD";

    String id;
    String topic;
    List<Integer> cardIds;
    Integer scoreSelf, scoreOpp;
    Double timeSelf, timeOpp;
    List<String> entriesSelf, entriesOpp;
    List<Integer> scoresSelf, scoresOpp;
    String oppEmail, winnerEmail;

    // initialize a match record with partial data immediately when this user completes the match
    public MatchRecord(String id, String topic, List<Integer> cardIds, String oppEmail, Integer scoreSelf, Double timeSelf, List<String> entriesSelf, List<Integer> scoresSelf){
        this.id = id;
        this.topic = topic;
        this.cardIds = cardIds;
        this.oppEmail = oppEmail;
        this.scoreSelf = scoreSelf;
        this.timeSelf = timeSelf;
        this.entriesSelf = entriesSelf;
        this.scoresSelf = scoresSelf;
        this.tableName = "MATCHES";
    }

    // initialize a match record with complete data when both users finishes the game
    public MatchRecord(String id, String topic, List<Integer> cardIds, String oppEmail, Integer scoreSelf, Integer scoreOpp, Double timeSelf, Double timeOpp, List<String> entriesSelf, List<String> entriesOpp, List<Integer> scoresSelf,  List<Integer> scoresOpp){
        this.id = id;
        this.topic = topic;
        this.cardIds = cardIds;
        this.oppEmail = oppEmail;
        this.scoreSelf = scoreSelf;
        this.scoreOpp = scoreOpp;
        this.timeSelf = timeSelf;
        this.timeOpp = timeOpp;
        this.entriesSelf = entriesSelf;
        this.entriesOpp = entriesOpp;
        this.scoresSelf = scoresSelf;
        this.scoresOpp = scoresOpp;
        this.tableName = "MATCHES";
    }

    public MatchRecord(Cursor dbCursor){
        this.id = dbCursor.getString(0);
        this.topic = dbCursor.getString(1);
        this.cardIds = Utils.splitToInts(dbCursor.getString(2));
        this.oppEmail = dbCursor.getString(3);
        this.scoreSelf = dbCursor.getInt(5);
        this.timeSelf = dbCursor.getDouble(7);
        this.entriesSelf = Utils.split(dbCursor.getString(9));
        this.scoresSelf = Utils.splitToInts(dbCursor.getString(11));
        if(!dbCursor.isNull(6)){
            this.winnerEmail = dbCursor.getString(4);
            this.scoreOpp = dbCursor.getInt(6);
            this.timeOpp = dbCursor.getDouble(8);
            this.entriesOpp = Utils.split(dbCursor.getString(10));
            this.scoresOpp = Utils.splitToInts(dbCursor.getString(12));
        }
    }



    @Override
    public String selector() {
        return "id='"+id+"'";
    }


    @Override
    public ContentValues composeUpdateValues() {
        ContentValues values2Update = new ContentValues();
        values2Update.put("id", id);
        values2Update.put("topic", topic);
        values2Update.put("cardIds", Utils.concatenate(cardIds));
        values2Update.put("emailOpp", oppEmail);
        values2Update.put("scoreSelf", scoreSelf);
        values2Update.put("timeSelf", timeSelf);
        values2Update.put("entriesSelf", Utils.concatenate(entriesSelf));
        values2Update.put("scoresSelf", Utils.concatenate(scoresSelf));
        if(scoreOpp != null){
            values2Update.put("scoreOpp", scoreOpp);
            values2Update.put("entriesOpp", Utils.concatenate(entriesOpp));
            values2Update.put("timeOpp", timeOpp);
            values2Update.put("scoresOpp", Utils.concatenate(scoresOpp));
            values2Update.put("winnerEmail", winnerEmail);
        }
        return values2Update;
    }
}
