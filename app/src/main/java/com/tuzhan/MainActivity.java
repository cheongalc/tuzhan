package com.tuzhan;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView civ_displayPhoto;
    TextView tv_displayName;
    TextView tv_userKD;
    TextView tv_userRoundsPlayed;
    ListView lv_prevMatches, lv_newMatches;
    RelativeLayout rl_userInfoBtnContainer;
    FirebaseDatabase database;
    DatabaseReference root;
    LinearLayout prev_matches, new_matches;
    RelativeLayout prev_matches_title, new_challenges_title;

    private List<String> prevMatchIds = new ArrayList<>();
    private List<MatchDetails> prevMatchDetails = new ArrayList<>();

    private List<String> newMatchIds = new ArrayList<>();
    private List<MatchDetails> newMatchDetails = new ArrayList<>();

    AVLoadingIndicatorView list_view_load;

    ImageView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataSource.init(this);

        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();

        DatabaseReference user_ref = root.child("UsersStates").child(currUser.getEmail().replace('.', ','));

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
        lv_newMatches = (ListView) findViewById(R.id.lv_new_matches);
        prev_matches = (LinearLayout) findViewById(R.id.linear_new_matches);
        new_matches = (LinearLayout) findViewById(R.id.linear_prev_matches);
        prev_matches_title = (RelativeLayout) findViewById(R.id.tuzhan_matches_title);
        new_challenges_title = (RelativeLayout) findViewById(R.id.tuzhan_challenge_title);
        list_view_load = (AVLoadingIndicatorView) findViewById(R.id.list_view_load_indicator);
        background = (ImageView) findViewById(R.id.pattern_back);
        ImageButton bStartGame = (ImageButton) findViewById(R.id.bStartGame);

        Picasso.with(this).load(currUser.getPhotoUrl()).into(civ_displayPhoto);
        tv_displayName.setText(currUser.getDisplayName());
        Picasso.with(this).load(R.mipmap.tuzhan_pattern).into(background);

        rl_userInfoBtnContainer.setOnClickListener(userInfoClick);

        ScrollView scrollView = (ScrollView) findViewById(R.id.main_scroll_view);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);


        //check for user network status and update accordingly
        DatabaseReference ConnectionRef = root.child(".info/connected");
        ConnectionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    user_ref.setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        user_ref.onDisconnect().setValue(false);


        Intent intent = new Intent(this, ClosingService.class);
        intent.putExtra("email", currUser.getEmail());
        startService(intent);

        prev_matches_title.setOnClickListener(v -> {
            if (lv_prevMatches.getVisibility() == View.VISIBLE) {
                collapse(lv_prevMatches);
            } else expand(lv_prevMatches);
        });

        new_challenges_title.setOnClickListener(v -> {
            if (lv_newMatches.getVisibility() == View.VISIBLE) {
                collapse(lv_newMatches);
            } else expand(lv_newMatches);
        });

        //get match data from local db
        List<MatchRecord> matchRecords = DataSource.shared.matches;
        List<MatchDetails> prevmatchDetailsList = new ArrayList<>();
        List<String> prevmatchId = new ArrayList<>();

        for (MatchRecord matchRecord : matchRecords) {
            User opponent = DataSource.shared.userForEmail(matchRecord.oppEmail.replace('.',','));
            if (opponent != null) {
                prevmatchId.add(matchRecord.id);

                String outcome = "dnf";

                if (matchRecord.scoreOpp != null) {
                    if (matchRecord.scoreOpp < matchRecord.scoreSelf) outcome = "1";
                    else if (matchRecord.scoreOpp > matchRecord.scoreSelf) outcome = "0";
                    else if (Objects.equals(matchRecord.scoreOpp, matchRecord.scoreSelf))
                        outcome = "2";
                }
                prevmatchDetailsList.add(new MatchDetails(matchRecord.id, opponent, outcome, matchRecord.topic));
            }
        }
        updateMatchesList(lv_prevMatches, prevmatchId, prevmatchDetailsList, prev_matches, false);

        //update matches list
        getPrevMatches();
    }

    //listener to check individual match id under match object of user
    private void getPrevMatches() {
        DatabaseReference user_matches_ref = root.child("Users").child(currUser.getEmail().replace('.', ',')).child("matches");

        user_matches_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //retrieves individual match details
                getMatchesDetails(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //remove match from global list
                //new matches cannot be removed
                prevMatchIds.remove(dataSnapshot.getKey());
                List<MatchDetails> matches_to_remove = new ArrayList<>();
                for (MatchDetails matchDetails : prevMatchDetails) {
                    if (matchDetails.match_id.equals(dataSnapshot.getKey())) {
                        matches_to_remove.add(matchDetails);
                    }
                }
                prevMatchDetails.removeAll(matches_to_remove);

                //refresh list
                updateMatchesList(lv_prevMatches, prevMatchIds, prevMatchDetails, prev_matches, false);
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

                final String topic = dataSnapshot.child("theme").getValue() + "";

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

                //default outcome value , match did not finish
                String outcome = "dnf";

                //get scores, time and entries for respective players and retrieve opponent email
                for (DataSnapshot child : dataSnapshot.child("players").getChildren()) {
                    if (!child.getKey().equals(currUser.getEmail().replace('.', ','))) {
                        //set opponent info
                        opponent_email = child.getKey();
                        if (child.child("state").getValue().toString().equals("fin")) {
                            opp_score = Integer.parseInt(child.child("score").getValue() + "");
                            opp_time = (double) child.child("time").getValue();
                            opp_entries = Utils.split(child.child("entries").getValue() + "");
                            opp_scores = Utils.splitToInts(child.child("scores").getValue() + "");
                        } else opp_score = -1;
                        //opp_score of -1 means opponent info has not been uploaded

                    } else {
                        if (!child.child("state").getValue().toString().equals("dns")) {
                            //set user info
                            user_score = Integer.parseInt(child.child("score").getValue() + "");
                            user_time = (double) child.child("time").getValue();
                            user_entries = Utils.split(child.child("entries").getValue() + "");
                            user_scores = Utils.splitToInts(child.child("scores").getValue() + "");
                        } else outcome = "dns";
                    }
                }


                //figure out match outcome
                if (opp_score >= 0 && !outcome.equals("dns")) {
                    //match finished
                    //set match outcome, "0" means user lost, "1" means user won and "2" means draw
                    if (user_score > opp_score) outcome = "1";
                    else if (user_score < opp_score) outcome = "0";
                    else outcome = "2";

                    //create new complete MatchRecord object to update local database
                    MatchRecord matchRecord = new MatchRecord(match_id, topic, cardIds, opponent_email, user_score, opp_score, user_time, opp_time, user_entries, opp_entries, user_scores, opp_scores);
                    matchRecord.updateDB(DataSource.shared.database);

                    //match is complete, stop listening for updates
                    match_details_ref.removeEventListener(this);

                } else if (!outcome.equals("dns")) {
                    //match did not finish, create new unfinished MatchRecord object to update local database
                    MatchRecord matchRecord = new MatchRecord(match_id, topic, cardIds, opponent_email, user_score, user_time, user_entries, user_scores);
                    matchRecord.updateDB(DataSource.shared.database);
                }//else match did not start

                //final versions of oppemail and match outcome to pass to second listener
                final String fin_opponent_email = opponent_email;
                final String fin_outcome = outcome;

                //retrieve opponent display name and profile url
                root.child("Users").child(fin_opponent_email.replace('.', ',')).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String display_name = dataSnapshot.child("displayname").getValue() + "";
                        String dpURL = dataSnapshot.child("dpURL").getValue() + "";
                        String userId = dataSnapshot.child("userId").getValue() + "";

                        User opponent = DataSource.shared.userWithParameters(userId, dpURL, display_name, fin_opponent_email);
                        MatchDetails matchDetails = new MatchDetails(match_id, opponent, fin_outcome, topic);

                        if (!fin_outcome.equals("dns")) {
                            //match is not new challenge, check if match id exists
                            if (prevMatchIds.indexOf(match_id) != -1) {
                                //match id exists, update current match detail
                                for (MatchDetails mMatch : prevMatchDetails) {
                                    if (mMatch.match_id.equals(matchDetails.match_id)) {
                                        prevMatchDetails.remove(mMatch);
                                        break;
                                    }
                                }
                                prevMatchIds.remove(match_id);
                            }

                            prevMatchIds.add(match_id);
                            prevMatchDetails.add(matchDetails);

                            updateMatchesList(lv_prevMatches, prevMatchIds, prevMatchDetails, prev_matches, false);

                        } else {
                            //match is new challenge
                            newMatchDetails.add(matchDetails);
                            newMatchIds.add(match_id);

                            updateMatchesList(lv_newMatches, newMatchIds, newMatchDetails, new_matches, true);
                        }
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

    public void updateMatchesList(ListView lv, List<String> matchIds, List<MatchDetails> matchDetailsList, LinearLayout container, boolean isNewMatch) {

        if (matchDetailsList.size() == 0) container.setVisibility(View.GONE);
        else container.setVisibility(View.VISIBLE);

        //all info retrieved, set listview
        if (lv.getAdapter() == null) {
            PrevMatchesAdapter prevMatchesAdapter = new PrevMatchesAdapter(MainActivity.this, matchIds, matchDetailsList, isNewMatch);
            lv.setAdapter(prevMatchesAdapter);
        } else {
            ((PrevMatchesAdapter) lv.getAdapter()).notifyDataSetChanged();
        }
        setListViewHeightBasedOnChildren(lv);
        list_view_load.setVisibility(View.GONE);
    }

    View.OnClickListener userInfoClick = v -> {
        Intent intent = new Intent(MainActivity.this, UserInfo.class);
        startActivity(intent);
    };

    @Override
    public void onBackPressed() {
        //set up dialog builder for log out dialog
        AlertDialog.Builder logout_diaog_builder = new AlertDialog.Builder(MainActivity.this);
        logout_diaog_builder.setCancelable(true)
                .setMessage("你确定这样的行为是正确的吗? " + ("\u26a0") + ("\u26a0") + ("\u26a0") + ("\u26a0"))
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> signOut())
                .setCancelable(true);

        final AlertDialog logout_dialog = logout_diaog_builder.create();
        logout_dialog.setOnShowListener(dialog -> {
            //set button color, default color seems to be white
            logout_dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            logout_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        });
        logout_dialog.show();
    }

    private void signOut() {
        //change user status
        root.child("Users").child(currUser.getEmail().replace('.', ',')).child("isOnline").setValue(false);
        //sign out of firebase
        mAuth.signOut();
        //sign out of google
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("isFirstStart", false);
            startActivity(intent);
            finish();
        });
    }

    public void findMatch(View view) {
        if (isConnectedInternet(this)) {
            Intent i = new Intent(MainActivity.this, FindingMatchActivity.class);
            startActivity(i);
        } else Toast.makeText(this, "无网络", Toast.LENGTH_SHORT).show();
    }

    static public boolean isConnectedInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isconnected = false;
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) isconnected = true;
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo.isConnected()) isconnected = true;
        return isconnected;
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

    public static int getListViewHeight(ListView listView) {
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
            return (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + totalHeight;
        } else return 0;
    }

    private void expand(ListView lv) {
        //set Visible
        lv.setVisibility(View.VISIBLE);
        int finalHeight = getListViewHeight(lv);

        ValueAnimator mAnimator = slideAnimator(0, finalHeight, lv);
        mAnimator.start();
    }

    private void collapse(ListView lv) {
        int finalHeight = getListViewHeight(lv);

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, lv);
        mAnimator.start();

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                lv.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end, ListView lv) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(valueAnimator -> {
            //Update Height
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = lv.getLayoutParams();
            layoutParams.height = value;
            lv.setLayoutParams(layoutParams);

        });
        return animator;
    }


}
