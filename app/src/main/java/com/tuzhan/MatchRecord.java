package com.tuzhan;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenchangheng on 7/12/17.
 */

public class MatchRecord extends DB_DataModel {

    String id;
    List<Integer> cardIds;
    Integer scoreSelf, scoreOpp;
    Double timeSelf, timeOpp;
    List<String> entriesSelf, entriesOpp;
    List<Integer> scoresSelf, scoresOpp;
    String oppEmail, winnerEmail;

    static ListView prevMatchesListView;

    // initialize a match record with partial data immediately when this user completes the match
    public MatchRecord(String id, List<Integer> cardIds, String oppEmail, Integer scoreSelf, Double timeSelf, List<String> entriesSelf, List<Integer> scoresSelf, ListView prevMatchesListView){
        this.id = id;
        this.cardIds = cardIds;
        this.oppEmail = oppEmail;
        this.scoreSelf = scoreSelf;
        this.timeSelf = timeSelf;
        this.entriesSelf = entriesSelf;
        this.scoresSelf = scoresSelf;
        this.prevMatchesListView = prevMatchesListView;
        uploadToFB();
    }

    public MatchRecord(Cursor dbCursor){
        this.id = dbCursor.getString(0);
        this.cardIds = Utils.splitToInts(dbCursor.getString(1));
        this.oppEmail = dbCursor.getString(2);
        this.scoreSelf = dbCursor.getInt(4);
        this.timeSelf = dbCursor.getDouble(6);
        this.entriesSelf = Utils.split(dbCursor.getString(8));
        this.scoresSelf = Utils.splitToInts(dbCursor.getString(10));
        if(!dbCursor.isNull(5)){
            this.winnerEmail = dbCursor.getString(3);
            this.scoreOpp = dbCursor.getInt(5);
            this.timeOpp = dbCursor.getDouble(7);
            this.entriesOpp = Utils.split(dbCursor.getString(9));
            this.scoresOpp = Utils.splitToInts(dbCursor.getString(11));
        }
        else startListening();
    }

    // TODO: incomplete implementation
    private void uploadToFB(){
        DataSource.shared.rootRef.child("Matches").child(id).child(Utils.getUserEmail()).setValue(new HashMap<String, Object>() {{
            put("score", scoreSelf);
            put("time", timeSelf);
            put("entries", Utils.concatenate(entriesSelf));
            put("scores", Utils.concatenate(scoresSelf));
        }});
        // will not be using above implementation, instead send get request to node.js server for update
        // after request completes, call startListening() to begin listening for match updates
    }

    // TODO: incomplete implementation
    private void startListening(){
        DataSource.shared.rootRef.child("Matches").child(id).child(oppEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update local db match info & detach listener if opp info is uploaded to firebase
                if(dataSnapshot.exists()){
                    // update local db match info here then detach listener(s)
                    DataSource.shared.rootRef.child("Matches").child(id).child(oppEmail).removeEventListener(this);
                    PrevMatchesAdapter adapter = (PrevMatchesAdapter) prevMatchesListView.getAdapter();
                    ArrayList<MatchRecord> arrayList = (ArrayList<MatchRecord>) DataSource.shared.matches;
                    arrayList
                }
                // else do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // do nothing on failure
            }
        });
    }


    @Override
    public String selector() {
        return "id="+id;
    }


    @Override
    public ContentValues composeUpdateValues() {
        ContentValues values2Update = new ContentValues();
        values2Update.put("id", id);
        values2Update.put("cardIds", Utils.concatenate(cardIds));
        values2Update.put("oppEmail", oppEmail);
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
