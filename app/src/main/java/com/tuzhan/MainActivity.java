package com.tuzhan;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView civDpPhoto;
    TextView tvUserName;
    TextView tvUserKD;
    TextView tvUserRoundsPlayed;
    ListView lvPrevMatches;
    RelativeLayout userInfoButton;
    FirebaseDatabase database;
    DatabaseReference root;

    List<String> prev_match_ids = new ArrayList<>();
    List<MatchDetails> prev_match_details = new ArrayList<>();

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

        //required for signing out the user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        civDpPhoto = (CircleImageView) findViewById(R.id.user_dp);
        tvUserName = (TextView) findViewById(R.id.user_dn);
        tvUserKD = (TextView) findViewById(R.id.tv_userKD);
        tvUserRoundsPlayed = (TextView) findViewById(R.id.tv_userRoundsPlayed);
        userInfoButton = (RelativeLayout) findViewById(R.id.u_info_button);
        lvPrevMatches = (ListView) findViewById(R.id.lv_prev_matches);

        Picasso.with(this).load(currUser.getPhotoUrl()).into(civDpPhoto);
        tvUserName.setText(currUser.getDisplayName());

        userInfoButton.setOnClickListener(userInfoClick);

        getPrevMatches();

        //set user status to online
        root.child("Users").child(currUser.getEmail().replace('.',',')).child("isOnline").setValue(true);

        root.child("Users").child(currUser.getEmail().replace('.',',')).child("isOnline").setValue(true);

        Intent intent = new Intent(this, ClosingService.class);
        intent.putExtra("email", currUser.getEmail());
        startService(intent);
    }

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
                prev_match_ids.remove(dataSnapshot.getKey());
                List<MatchDetails> matches_to_remove = new ArrayList<>();
                for(MatchDetails matchDetails : prev_match_details){
                    if(matchDetails.match_id.equals(dataSnapshot.getKey())){
                        matches_to_remove.add(matchDetails);
                    }
                }
                prev_match_details.removeAll(matches_to_remove);

                //refresh list
                PrevMatchesAdapter prevMatchesAdapter = new PrevMatchesAdapter(MainActivity.this, prev_match_ids, prev_match_details);
                lvPrevMatches.setAdapter(prevMatchesAdapter);
                setListViewHeightBasedOnChildren(lvPrevMatches);
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
        DatabaseReference match_details_ref = root.child("Matches").child(match_id);

        match_details_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String topic = dataSnapshot.child("topic").getValue() + "";
                String opponent_email = "";
                int opponent_score = 0;
                int user_score = 0;

                //get scores for respective players and retrieve opponent email
                for(DataSnapshot child : dataSnapshot.child("players").getChildren())
                {
                    if(!child.getKey().equals(currUser.getEmail().replace('.', ','))){
                        opponent_email = child.getKey();
                        opponent_score = Integer.parseInt(child.child("score").getValue()+"");
                    }else user_score = Integer.parseInt(child.child("score").getValue()+"");
                }

                //set match outcome, "0" means user lost, "1" means user won and "2" means draw
                String outcome = "0";
                if(user_score > opponent_score) outcome = "1";
                else if(user_score < opponent_score)outcome = "0";
                else outcome = "2";

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

                        //add match details into global list
                        prev_match_details.add(matchDetails);

                        //adds match id to global list
                        prev_match_ids.add(match_id);

                        //all info retrieved, set listview
                        PrevMatchesAdapter prevMatchesAdapter = new PrevMatchesAdapter(MainActivity.this, prev_match_ids, prev_match_details);
                        lvPrevMatches.setAdapter(prevMatchesAdapter);
                        setListViewHeightBasedOnChildren(lvPrevMatches);
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
                        signout();
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

    private void signout()
    {
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
