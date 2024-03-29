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

    private void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    // singleton database object to be used
    static DataSource shared;

    public DatabaseReference rootRef = fireDatabase.getReference();
    public Map<String, ArrayList<QuestionCard>> themeToCards = new HashMap<>();
    public List<MatchRecord> matches;
    public List<User> encounteredUsers;

    public static final String LOG_TAG = "DATASOURCE";

    // init function to be called at the start of MainActivity
    static void init(Context context){
        if (shared != null) return ;
        shared = new DataSource(context);
        ArrayList<QuestionCard> allCards = shared.fetchAllCards();
        for (QuestionCard card:allCards) {
            shared.addCard(card);
        }
        shared.matches = shared.fetchAllMatches();
        shared.encounteredUsers = shared.fetchAllEncounteredUsers();
        Log.d(LOG_TAG, String.valueOf(shared.matches.size()));
    }

    public DataSource(Context context){
        dbHelper = new DatabaseHelper(context);
        this.open();
    }

    private void addCard(QuestionCard card){
        card.updateDB(database);
        if(!themeToCards.containsKey(card.theme)){
            themeToCards.put(card.theme, new ArrayList<>());
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
        cursor.close();
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
    public void fetchCards(String theme, final List<Integer> ids, final DataFetchedCallback<ArrayList<QuestionCard>> callback){
        final ArrayList<QuestionCard> fetched = new ArrayList<>();
        for(Integer id:ids){
            fetchCard(theme, id, data -> {
                fetched.add(data);
                if(fetched.size() == ids.size()){
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
        cursor.close();
        return matches;
    }

    // use this method to add a newly created MatchRecord to memory & database
    public void addMatch(MatchRecord match){
        match.updateDB(database);
        matches.add(match);
    }

    // fetch all encountered users from database
    private List<User> fetchAllEncounteredUsers(){
        Cursor cursor = database.rawQuery("SELECT * FROM USERS", null);
        ArrayList<User> users = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do{
                users.add(new User(cursor));
            } while(cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    // retrieves self by email from local array, returns null if self with the specified email is not found
    public User userForEmail(String userEmail){
        for(User user: encounteredUsers){
            if(user.email.equals(userEmail)) return user;
        }
        return null;
    }


    // call this method instead of the 'arbitrary self' constructor of the User class
    // checks if self object with same email already exists
    // if not, create new User object with given parameters and store it in local array & database
    // if yes, returns the found self object
    public User userWithParameters(String userId, String dpURL, String dpName, String email){

        User user = userForEmail(email);
        if(user != null) return user;

        // create self & add it to local array and database
        user = new User(dpName, email, userId, dpURL);
        encounteredUsers.add(user);
        user.updateDB(database);

        Log.d("encountered", encounteredUsers.size()+"");

        return user;
    }


}
