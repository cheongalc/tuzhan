package com.tuzhan;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    static MainActivity shared;

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView civ_displayPhoto;
    TextView tv_displayName;
    TextView tv_userKD;
    TextView tv_userRoundsPlayed;
    ListView lv_prevMatches;
    RelativeLayout rl_userInfoBtnContainer;
    FirebaseDatabase database;
    DatabaseReference root;

    private List<String> prevMatchIds = new ArrayList<>();
    private List<MatchDetails> prevMatchDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.shared = this;

        DataSource.init(this);

        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();

        //required for signing out the user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        civ_displayPhoto = (CircleImageView) findViewById(R.id.user_dp);
        tv_displayName = (TextView) findViewById(R.id.user_dn);
        tv_userKD = (TextView) findViewById(R.id.tv_userKD);
        tv_userRoundsPlayed = (TextView) findViewById(R.id.tv_userRoundsPlayed);
        rl_userInfoBtnContainer = (RelativeLayout) findViewById(R.id.rl_userInfoBtnContainer);
        lv_prevMatches = (ListView) findViewById(R.id.lv_prev_matches);

        Picasso.with(this).load(currUser.getPhotoUrl()).into(civ_displayPhoto);
        tv_displayName.setText(currUser.getDisplayName());

        rl_userInfoBtnContainer.setOnClickListener(userInfoClick);

        getPrevMatches();

        //set user status to online
        root.child("OnlineUsers").child(currUser.getEmail().replace('.',',')).setValue(true);


        Intent intent = new Intent(this, ClosingService.class);
        intent.putExtra("email", currUser.getEmail());
        startService(intent);

        //update lv_prev_matches
        getPrevMatches();
    }

    //listener to check individual match id under match object of user
    private void getPrevMatches() {
        DatabaseReference user_matches_ref = root.child("Users").child(currUser.getEmail().replace('.',',')).child("matches");

        user_matches_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //retrieves individual match details
                getMatchesDetails(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //remove match from global list
                prevMatchIds.remove(dataSnapshot.getKey());
                List<MatchDetails> matches_to_remove = new ArrayList<>();
                for(MatchDetails matchDetails : prevMatchDetails){
                    if(matchDetails.match_id.equals(dataSnapshot.getKey())){
                        matches_to_remove.add(matchDetails);
                    }
                }
                prevMatchDetails.removeAll(matches_to_remove);

                //refresh list
                PrevMatchesAdapter prevMatchesAdapter = new PrevMatchesAdapter(MainActivity.this, prevMatchIds, prevMatchDetails);
                lv_prevMatches.setAdapter(prevMatchesAdapter);
                setListViewHeightBasedOnChildren(lv_prevMatches);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMatchesDetails(final String match_id) {

        //move to actual match object under Matches
        DatabaseReference match_details_ref = root.child("Matches").child(match_id);

        match_details_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String topic = dataSnapshot.child("topic").getValue() + "";

                //get list of cards used in match
                List<Integer> cardIds = Utils.splitToInts(dataSnapshot.child("cardIds").getValue() + "");

                String opponent_email = "";

                int opp_score = 0;
                int user_score = 0;

                double user_time = 0.0;
                double opp_time = 0.0;

                List<String> user_entries = new ArrayList<>();
                List<String> opp_entries = new ArrayList<>();

                List<Integer> user_scores = new ArrayList<>();
                List<Integer> opp_scores = new ArrayList<>();


                //get scores, time and entries for respective players and retrieve opponent email
                for(DataSnapshot child : dataSnapshot.child("players").getChildren())
                {
                    if(!child.getKey().equals(currUser.getEmail().replace('.', ','))){
                        //set opponent info
                        opponent_email = child.getKey();
                        if(child.hasChild("score") && child.getChildrenCount() > 3) {
                            opp_score = Integer.parseInt(child.child("score").getValue() + "");
                            opp_time = (double) child.child("time").getValue();
                            opp_entries = Utils.split(child.child("entries").getValue() + "");
                            opp_scores = Utils.splitToInts(child.child("scores").getValue()+"");
                        }else opp_score = -1;
                        //opp_score of -1 means opponent info has not been uploaded

                    }else{
                        //set user info
                        user_score = Integer.parseInt(child.child("score").getValue()+"");
                        user_time = (double) child.child("time").getValue();
                        user_entries = Utils.split(child.child("entries").getValue() + "");
                        user_scores = Utils.splitToInts(child.child("scores").getValue()+"");
                    }
                }

                //default value , match did not finish
                String outcome = "dnf";
                if(opp_score >= 0) {
                    //set match outcome, "0" means user lost, "1" means user won and "2" means draw
                    if (user_score > opp_score) outcome = "1";
                    else if (user_score < opp_score) outcome = "0";
                    else outcome = "2";

                    //create new complete MatchRecord object to update local database
                    MatchRecord matchRecord = new MatchRecord(match_id, topic, cardIds, opponent_email, user_score, opp_score, user_time, opp_time, user_entries, opp_entries, user_scores, opp_scores);
                    matchRecord.updateDB(DataSource.shared.database);

                    //match is complete, stop listening for updates
                    match_details_ref.removeEventListener(this);

                }else{
                    //create new unfinished MatchRecord object to update local database
                    MatchRecord matchRecord = new MatchRecord(match_id, topic, cardIds, opponent_email, user_score, user_time, user_entries, user_scores);
                    matchRecord.updateDB(DataSource.shared.database);
                }

                //final versions of oppemail and match outcome to pass to second listener
                final String fin_opponent_email = opponent_email;
                final String fin_outcome = outcome;

                //retrieve opponent display name and profile url
                root.child("Users").child(fin_opponent_email.replace('.', ',')).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String display_name = dataSnapshot.child("displayname").getValue()+"";
                        String dpURL = dataSnapshot.child("dpURL").getValue()+"";
                        String userId = dataSnapshot.child("userId").getValue()+"";

                        User opponent = new User(display_name, fin_opponent_email, userId, dpURL);
                        MatchDetails matchDetails = new MatchDetails(match_id, opponent, fin_outcome, topic);

                        //check if match id exists
                        if(prevMatchIds.indexOf(match_id) != -1)
                        {
                            //match id exists, update current match detail
                            for(MatchDetails mMatch : prevMatchDetails){
                                if(mMatch.match_id.equals(matchDetails.match_id)){
                                    prevMatchDetails.remove(mMatch);
                                    break;
                                }
                            }
                            prevMatchIds.remove(match_id);
                        }

                        //add match id to global list
                        prevMatchIds.add(match_id);
                        //add match details into global list
                        prevMatchDetails.add(matchDetails);


                        //all info retrieved, set listview
                        if(lv_prevMatches.getAdapter() == null) {
                            PrevMatchesAdapter prevMatchesAdapter = new PrevMatchesAdapter(MainActivity.this, prevMatchIds, prevMatchDetails);
                            lv_prevMatches.setAdapter(prevMatchesAdapter);
                        }else{
                            ((PrevMatchesAdapter) lv_prevMatches.getAdapter()).notifyDataSetChanged();
                        }

                        setListViewHeightBasedOnChildren(lv_prevMatches);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("cannot retrieve match", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("cannot retrieve match", databaseError.getMessage());
            }
        });
    }

    View.OnClickListener userInfoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, UserInfo.class);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        //set up dialog builder for log out dialog
        AlertDialog.Builder logout_diaog_builder = new AlertDialog.Builder(MainActivity.this);
        logout_diaog_builder.setCancelable(true)
                .setMessage("你确定这样的行为是正确的吗? " + ("\u26a0") + ("\u26a0") + ("\u26a0") + ("\u26a0"))
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }
                })
                .setCancelable(true);

        final AlertDialog logout_dialog = logout_diaog_builder.create();
        logout_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //set button color, default color seems to be white
                logout_dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                logout_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        logout_dialog.show();
    }

    private void signOut() {
        //change user status
        root.child("Users").child(currUser.getEmail().replace('.',',')).child("isOnline").setValue(false);
        root.child("Users").child(currUser.getEmail().replace('.',',')).child("isOnline").setValue(false);
        //sign out of firebase
        mAuth.signOut();
        //sign out of google
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(MainActivity.this, LoginPage.class);
                intent.putExtra("isFirstStart", false);
                startActivity(intent);
                finish();
            }
        });
    }

    public void findMatch(View view) {
        Intent i = new Intent(MainActivity.this, FindingMatch.class);
        startActivity(i);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            int totalHeight = 10;
            View view = null;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                view = listAdapter.getView(i, view, listView);
                if (i == 0) {
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, -2));
                }
                view.measure(desiredWidth, 0);
                totalHeight += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + totalHeight;
            listView.setLayoutParams(params);
        }
    }

}
