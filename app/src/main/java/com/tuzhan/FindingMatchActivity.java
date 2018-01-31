package com.tuzhan;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class FindingMatchActivity extends AppCompatActivity {

    public static final String LOG_TAG = "FINDINGMATCHACTIVITY";

    FirebaseUser currUser;

    FirebaseDatabase database;
    DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_match);

        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        findMatch(currUser.getEmail(), Volley.newRequestQueue(this));
    }

    // finding match by sending a POST request to zairui's herokuapp node server.
    public void findMatch(String email, RequestQueue find_match_queue){

        String url = "https://shielded-anchorage-95513.herokuapp.com/?email=";
        url += email;

        StringRequest matchRequest = new StringRequest(Request.Method.POST, url, this::getCardIds, error -> {
            Toast.makeText(FindingMatchActivity.this, "Failed to find match!", Toast.LENGTH_SHORT).show();
            finish();
        });

        find_match_queue.add(matchRequest);
    }


    public void getCardIds(String matchId){
        root.child(Constants.F_MATCHES).child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String cardsIds = dataSnapshot.child(Constants.F_MATCHES_CARDIDS).getValue()+"";
                String theme = dataSnapshot.child(Constants.F_MATCHES_THEME).getValue()+"";

                //get opponent data
                String opp_email = "";

                for(DataSnapshot player : dataSnapshot.child(Constants.F_MATCHES_PLAYERS).getChildren()){
                    if(!player.getKey().equals(currUser.getEmail().replace('.', ','))){
                        opp_email = player.getKey()+"";
                    }
                }

                final String cardIds_fin = cardsIds;
                final String theme_fin = theme;


                root.child(Constants.F_USERS).child(opp_email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        Log.e(LOG_TAG, dataSnapshot.getKey());

                        //make opponent self object
                        User opp = DataSource.shared.userWithParameters(
                                dataSnapshot.child(Constants.F_USERS_USERID).getValue().toString(),
                                dataSnapshot.child(Constants.F_USERS_DPURL).getValue().toString(),
                                dataSnapshot.child(Constants.F_USERS_DISPLAYNAME).getValue().toString(),
                                dataSnapshot.child(Constants.F_USERS_EMAIL).getValue().toString());
                        //make curruser self object
                        User user = DataSource.shared.userWithParameters(currUser.getUid(), currUser.getPhotoUrl().toString(), currUser.getDisplayName(), currUser.getEmail());

                        Log.e(LOG_TAG, opp.email);

                        Intent i = new Intent(FindingMatchActivity.this, CountdownActivity.class);
                        i.putExtra(Constants.C_CARD_IDS_STRING, cardIds_fin);
                        i.putExtra(Constants.C_THEME, theme_fin);
                        i.putExtra(Constants.C_USER_SELF, user);
                        i.putExtra(Constants.C_USER_OPPONENT, opp);
                        i.putExtra(Constants.C_MATCH_ID, matchId);
                        startActivity(i);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //failed to retrieve opponent info
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //failed to find match
            }
        });
    }
}
