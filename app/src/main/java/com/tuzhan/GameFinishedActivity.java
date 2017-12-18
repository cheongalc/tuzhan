package com.tuzhan;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GameFinishedActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GAMEFINISHED";
    private List<String> formattedEntries = new ArrayList<>();

    ListView lv_oppEntries, lv_userEntries;
    TextView tv_userScore, tv_oppScore;
    CircleImageView civ_user, civ_opp;
    MatchRecord matchRecord;
    FirebaseUser curUser;
    FirebaseDatabase database;
    DatabaseReference rootRef;
    Boolean isOppInfoPresent = false;

    int userScore = -1, oppScore = -1;
    double userTime = 0.0, oppTime = 0.0;
    List<Integer> userScores = new ArrayList<>(), oppScores = new ArrayList<>();
    List<String> userEntries = new ArrayList<>(), oppEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finished);

        //get user info
        curUser = FirebaseAuth.getInstance().getCurrentUser();

        //set up fdb
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();

        tv_userScore = (TextView) findViewById(R.id.tv_userScore);
        tv_oppScore = (TextView) findViewById(R.id.tv_oppScore);

        civ_opp = (CircleImageView) findViewById(R.id.civ_opp_dp);
        civ_user = (CircleImageView) findViewById(R.id.civ_user_dp);

        lv_userEntries = (ListView) findViewById(R.id.lv_user_entries);
        lv_oppEntries = (ListView) findViewById(R.id.lv_opp_entries);


        Intent intent = getIntent();
        String matchId = intent.getStringExtra("matchId");
        //boolean value to determine whether the user is accessing this page immediately after match or from mainactivity list
        Boolean isMatchFinished = intent.getBooleanExtra("isMatchFinished", false);

        //user is accessing match from MainActivity, retrieve cached match record
        if(matchId != null && isMatchFinished){
            //TODO get matchrecords

            userScore = matchRecord.scoreSelf;
            oppScore = matchRecord.scoreOpp;

            userScores = matchRecord.scoresSelf;
            userEntries = matchRecord.entriesSelf;

            //check if opponent has finished the match
            String oppEmail = matchRecord.oppEmail;
            if(oppEmail != null && oppEmail.length() > 0){
                //opponent info exists
                isOppInfoPresent = true;

                oppEntries = matchRecord.entriesOpp;
                oppScores = matchRecord.scoresOpp;

                //set opponent info
                setOppStuff();
            }
            getOppStuff(oppEmail);

        }else{
            String topic = intent.getStringExtra("topic");
            String oppEmail = intent.getStringExtra("oppEmail");
            userScore = intent.getIntExtra("scoreSelf", -1);
            userTime = intent.getDoubleExtra("timeSelf", 0.0);
            userEntries = intent.getStringArrayListExtra("entriesSelf");
            userScores = intent.getIntegerArrayListExtra("scoresSelf");
            List<Integer> cardIds = intent.getIntegerArrayListExtra("cardIds");

            //format stuff for uploading to fdb
            String formattedUserEntries = Utils.concatenate(userEntries);
            String formattedUserScores = Utils.concatenate(userScores);

            //set up references
            DatabaseReference matchRef = rootRef.child("Matches").child(matchId);
            DatabaseReference userInfoRef = matchRef.child("players").child(curUser.getEmail().replace('.',','));

            //upload user match data
            userInfoRef.child("entries").setValue(formattedUserEntries);
            userInfoRef.child("scores").setValue(formattedUserScores);
            userInfoRef.child("score").setValue(userScore);
            userInfoRef.child("time").setValue(userTime);
            userInfoRef.child("state").setValue("fin");

            //check up on opponent data
            matchRef.child("players").child(oppEmail.replace('.',',')).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("state").getValue().toString().equals("fin")){
                        //opponent has finished the match, retrieve *HIS* data
                        isOppInfoPresent = true;
                        oppScore = (int) dataSnapshot.child("score").getValue();
                        oppEntries = Utils.split(dataSnapshot.child("entries").getValue()+"");
                        oppScores = Utils.splitToInts(dataSnapshot.child("scores").getValue()+"");
                        oppTime = (double) dataSnapshot.child("time").getValue();

                        //create complete MatchRecord object
                        MatchRecord matchRecord = new MatchRecord(
                                matchId, topic, cardIds,
                                oppEmail, userScore, oppScore,
                                userTime, oppTime, userEntries,
                                oppEntries, userScores, oppScores);
                        //update local db
                        matchRecord.updateDB(DataSource.shared.database);

                        //update opponent list
                        setOppStuff();
                    }else{
                        //opponent did not finish the match
                        isOppInfoPresent = false;

                        //create partial MacthRecord object
                        MatchRecord matchRecord = new MatchRecord(
                                matchId, topic,
                                cardIds, oppEmail,
                                userScore, userTime,
                                userEntries, userScores);
                        //update local db
                        matchRecord.updateDB(DataSource.shared.database);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            getOppStuff(oppEmail);
        }

        // display score
        if(userScore > 0){
            tv_userScore.setText("DNF");
        }else tv_userScore.setText(userScore + "");

        PlayerEntriesAdapter userEntriesAdapter = new PlayerEntriesAdapter(this, userEntries, userScores);
        lv_userEntries.setAdapter(userEntriesAdapter);

        Picasso.with(this).load(curUser.getPhotoUrl()).into(civ_user);

    }

    private void setOppStuff(){
        if(oppScore > 0){
            tv_oppScore.setText("DNF");
        }else tv_oppScore.setText(oppScore + "");

        PlayerEntriesAdapter oppEntriesAdapter = new PlayerEntriesAdapter(this, oppEntries, oppScores);
        lv_userEntries.setAdapter(oppEntriesAdapter);
    }

    private void getOppStuff(String email){
        DatabaseReference userRef = rootRef.child("User").child(email.replace('.',','));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dpPhotoUrl = dataSnapshot.child("dpURL").getValue()+"";
                Picasso.with(GameFinishedActivity.this).load(dpPhotoUrl).into(civ_opp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private List<String> formatEntries(String unformattedEntries) {
        List<String> output = new ArrayList<>();
        String currentStack = "";
        for (int i = 0; i < unformattedEntries.length(); i++) {
            char currentChar = unformattedEntries.charAt(i);
            if (currentChar == '-') {
                output.add(currentStack);
                currentStack = "";
            } else {
                currentStack += currentChar;
            }
        }
        return output;
    }
}
