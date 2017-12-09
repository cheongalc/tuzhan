package com.tuzhan;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DataSource extends Application {

    // private database objects
    private FirebaseDatabase fireDatabase = FirebaseDatabase.getInstance();
    public SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    private void open() throws SQLException{database = dbHelper.getWritableDatabase();}

    // singleton database object to be used
    static DataSource shared;

    public DatabaseReference rootRef = fireDatabase.getReference();
    public Map<String, ArrayList<QuestionCard>> themeToCards = new HashMap<>();
    public List<MatchRecord> matches;

    // init function to be called at the start of MainActivity
    static void init(Context context){
        shared = new DataSource(context);
        ArrayList<QuestionCard> allCards = shared.fetchAllCards();
        for (QuestionCard card:allCards) {
            shared.addCard(card);
        }
        shared.matches = shared.fetchAllMatches();
    }

    public DataSource(Context context){
        dbHelper = new DatabaseHelper(context);
        this.open();
    }

    private void addCard(QuestionCard card){
        card.updateDB(database);
        if(!themeToCards.containsKey(card.theme)){
            themeToCards.put(card.theme, new ArrayList<QuestionCard>());
        }
        themeToCards.get(card.theme).add(card);
    }

    private ArrayList<QuestionCard> fetchAllCards(){
        Cursor cursor = database.rawQuery("SELECT * FROM CARDS", null);
        ArrayList<QuestionCard> cards = new ArrayList<>();

        if(cursor.moveToFirst()){
            do{
                cards.add(new QuestionCard(cursor));
            } while(cursor.moveToNext());
        }

        return cards;
    }

    private QuestionCard fetchSavedCard(String theme, int id){
        ArrayList<QuestionCard> cards = themeToCards.get(theme);
        if(cards == null) return null;
        QuestionCard found = null;
        for(QuestionCard card:cards){
            if(card.id == id){
                found = card;
                break;
            }
        }
        return found;
    }

    private void fetchCardFromFirebase(final String theme, final int id, final DataFetchedCallback<QuestionCard> callback){
        rootRef.child("Cards").child(theme).child(String.valueOf(id)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                QuestionCard card = new QuestionCard(dataSnapshot, theme, id);
                card.updateDB(database);
                addCard(card);
                callback.fetched(card);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.fetched(null);
            }
        });
    }

    // use this method to fetch a single card
    public void fetchCard(String theme, int id, final DataFetchedCallback<QuestionCard> callback){
        QuestionCard memCached = fetchSavedCard(theme, id);
        if(memCached != null) callback.fetched(memCached);

        else fetchCardFromFirebase(theme, id, callback);
    }


    // use this method to fetch a group of cards
    // note for current implementation: elements of returned cards array CAN BE NULL if fetch fails for that card!
    public void fetchCards(String theme, final int[] ids, final DataFetchedCallback<ArrayList<QuestionCard>> callback){
        final ArrayList<QuestionCard> fetched = new ArrayList<>();
        for(int id:ids){
            fetchCard(theme, id, data -> {
                fetched.add(data);
                if(fetched.size() == ids.length){
                    Collections.sort(fetched, (o1, o2) -> o1.id.compareTo(o2.id));
                    callback.fetched(fetched);
                }
            });
        }
    }

    private List<MatchRecord> fetchAllMatches(){
        Cursor cursor = database.rawQuery("SELECT * FROM MATCHES", null);
        ArrayList<MatchRecord> matches = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do{
                matches.add(new MatchRecord(cursor));
            } while(cursor.moveToNext());
        }

        return matches;
    }

    // use this method to add a newly created MatchRecord to memory & database
    public void addMatch(MatchRecord match){
        match.updateDB(database);
        matches.add(match);
    }

}
