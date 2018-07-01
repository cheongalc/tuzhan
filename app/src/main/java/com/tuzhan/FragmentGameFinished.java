package com.tuzhan;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dhaulagiri on 19/12/2017.
 */

public class FragmentGameFinished extends android.support.v4.app.Fragment {

    private static final String LOG_TAG = "FRAGMENTGAMEFINISHED";

    ListView lv_oppEntries, lv_userEntries;
    TextView tv_userScore, tv_oppScore, tv_userTime, tv_oppTime;
    CircleImageView civ_user, civ_opp;
    FirebaseUser curUser;
    FirebaseDatabase database;
    DatabaseReference rootRef;
    Boolean isOppInfoPresent = false;

    String opponentDPURL;

    int userScore = -1, oppScore = -1;
    double userTime = 0.0, oppTime = 0.0;
    List<Integer> userScores = new ArrayList<>(), oppScores = new ArrayList<>();
    List<String> userEntries = new ArrayList<>(), oppEntries = new ArrayList<>();

    MatchRecord matchRecord;

    public FragmentGameFinished() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_game_finished, container, false);

        //get self info
        curUser = FirebaseAuth.getInstance().getCurrentUser();

        //set up fdb
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();

        tv_userScore = (TextView) rootview.findViewById(R.id.tv_userScore);
        tv_oppScore = (TextView) rootview.findViewById(R.id.tv_oppScore);

        tv_oppTime = (TextView) rootview.findViewById(R.id.t_opp_time);
        tv_userTime = (TextView) rootview.findViewById(R.id.t_user_time);

        civ_opp = (CircleImageView) rootview.findViewById(R.id.civ_opp_dp);
        civ_user = (CircleImageView) rootview.findViewById(R.id.civ_user_dp);

        lv_userEntries = (ListView) rootview.findViewById(R.id.lv_user_entries);
        lv_oppEntries = (ListView) rootview.findViewById(R.id.lv_opp_entries);

        Intent intent = getActivity().getIntent();

        String matchId = intent.getStringExtra(Constants.C_MATCH_ID);
        opponentDPURL = intent.getStringExtra(Constants.C_OPPONENT_DPURL);
        //boolean value to determine whether the self is accessing this page immediately after match or from mainactivity list
        Boolean isMatchFinished = intent.getBooleanExtra(Constants.C_GAMEFINISHED_KEY, Constants.M.START_FROM_GAMEPLAY);

        //self is accessing match from MainActivity, retrieve cached match record
        if(matchId != null && isMatchFinished){

            for(MatchRecord match : DataSource.shared.matches){
                if(match.id.equals(matchId)){
                    matchRecord = match;
                    break;
                }
            }
            GameFinishedActivity.matchRecord = matchRecord;
            assert matchRecord != null;

            GameFinishedActivity.cardIds = Utils.concatenate(matchRecord.cardIds);
            GameFinishedActivity.theme = matchRecord.topic;

            userScore = matchRecord.scoreSelf;
            userScores = matchRecord.scoresSelf;
            userEntries = matchRecord.entriesSelf;
            userTime = matchRecord.timeSelf;


            //check if opponent has finished the match
            String oppEmail = matchRecord.oppEmail;
            Log.w(LOG_TAG, oppEmail);
            if(oppEmail != null && oppEmail.length() > 0 && matchRecord.scoreOpp != null){
                //opponent info exists
                isOppInfoPresent = true;

                oppEntries = matchRecord.entriesOpp;
                oppScores = matchRecord.scoresOpp;
                oppScore = matchRecord.scoreOpp;
                oppTime = matchRecord.timeOpp;

                //set opponent info
                setOppStuff();
            }

        }else{
            String topic = intent.getStringExtra(Constants.C_THEME);
            String oppEmail = intent.getStringExtra(Constants.C_OPPONENT_EMAIL);
            userScore = intent.getIntExtra(Constants.C_SCORE_SELF, -1);
            userTime = intent.getDoubleExtra(Constants.C_TIME_SELF, 0.0);
            userEntries = intent.getStringArrayListExtra(Constants.C_PLAYER_ENTRIES_LIST);
            Log.w(LOG_TAG, Arrays.toString(new List[]{userEntries}));
            userScores = intent.getIntegerArrayListExtra(Constants.C_SCORE_SELF_LIST);
            List<Integer> cardIds = intent.getIntegerArrayListExtra(Constants.C_CARD_IDS_LIST);

            //read out score
            String scoreRead = "你得到了"+userScore+"分";
            MainActivity.readText(scoreRead);

            GameFinishedActivity.cardIds = Utils.concatenate(cardIds);
            GameFinishedActivity.theme = topic;

            // TODO: 26/05/2018 check tutorial here

            //format stuff for uploading to fdb
            String formattedUserEntries = Utils.concatenate(userEntries);
            String formattedUserScores = Utils.concatenate(userScores);

            //set up references
            DatabaseReference matchRef = rootRef.child("Matches").child(matchId);
            DatabaseReference userInfoRef = matchRef.child("players").child(curUser.getEmail().replace('.',','));

            //upload self match data
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
                        oppScore = (int)(long) dataSnapshot.child("score").getValue();
                        oppEntries = Utils.split(dataSnapshot.child("entries").getValue()+"");
                        oppScores = Utils.splitToInts(dataSnapshot.child("scores").getValue()+"");
                        oppTime = (double) dataSnapshot.child("time").getValue();

                        //create complete MatchRecord object
                        matchRecord = new MatchRecord(
                                matchId, topic, cardIds,
                                oppEmail, userScore, oppScore,
                                userTime, oppTime, userEntries,
                                oppEntries, userScores, oppScores);
                        //update local db
                        matchRecord.updateDB(DataSource.shared.database);
                        GameFinishedActivity.matchRecord = matchRecord;

                        //update opponent list
                        setOppStuff();

                        //determine winner
                        String winner = "";
                        if(oppScore > userScore){
                            winner = oppEmail;
                        }else if(userScore > oppScore){
                            winner = curUser.getEmail();
                        }else if(userScore == oppScore){
                            if(userTime < oppTime){
                                winner = curUser.getEmail();
                            }else{
                                winner = oppEmail;
                            }
                        }

                        //game is finished, update both players' info
                        updateInfo(dataSnapshot, oppEmail, winner);

                        final String winner_ = winner;

                        rootRef.child("players").child(curUser.getEmail().replace('.',',')).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                updateInfo(dataSnapshot, curUser.getEmail(), winner_);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    }else{
                        //opponent did not finish the match
                        isOppInfoPresent = false;

                        //create partial MacthRecord object
                        matchRecord = new MatchRecord(
                                matchId, topic,
                                cardIds, oppEmail,
                                userScore, userTime,
                                userEntries, userScores);
                        //update local db
                        matchRecord.updateDB(DataSource.shared.database);
                        GameFinishedActivity.matchRecord = matchRecord;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        // display scores
        if(userScore < 0){
            tv_userScore.setText("DNF");
        }else tv_userScore.setText(userScore + "");
        tv_userTime.setText(String.format("%.1f", userTime) + "秒");


        PlayerEntriesAdapter userEntriesAdapter = new PlayerEntriesAdapter(getActivity(), userEntries, userScores);
        lv_userEntries.setAdapter(userEntriesAdapter);

        Picasso.with(getActivity()).load(curUser.getPhotoUrl()).into(civ_user);
        Picasso.with(getActivity()).load(opponentDPURL).into(civ_opp);


        return rootview;
    }

    private void updateInfo(DataSnapshot dataSnapshot, String email, String winner){
        if(dataSnapshot.hasChild("rounds_played")){
            int opp_rounds_played = (int)(long) dataSnapshot.child("rounds_played").getValue();
            rootRef.child("players").child(email.replace('.',',')).child("rounds_played").setValue(opp_rounds_played + 1);

            if(winner.equals(email) || winner.equals("")){
                int opp_rounds_won = (int)(long) dataSnapshot.child("rounds_won").getValue();
                rootRef.child("players").child(email.replace('.',',')).child("rounds_won").setValue(opp_rounds_won+ 1);
            }

        }else{
            rootRef.child("players").child(email.replace('.',',')).child("rounds_played").setValue(1);
            if(winner.equals(email) || winner.equals("")){
                rootRef.child("players").child(email.replace('.',',')).child("rounds_won").setValue(1);
            }
        }
    }

    private void setOppStuff(){
        if(oppScore <= 0){
            tv_oppScore.setText("DNF");
        }else {
            tv_oppScore.setText(oppScore + "");
            tv_oppTime.setText(String.format("%.1f", oppTime) + "秒");

            PlayerEntriesAdapter oppEntriesAdapter = new PlayerEntriesAdapter(getActivity(), oppEntries, oppScores);
            lv_oppEntries.setAdapter(oppEntriesAdapter);
        }
    }

}
