package com.tuzhan;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class FindingMatchActivity extends AppCompatActivity {

    FirebaseUser currUser;
    FirebaseAuth mAuth;

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
        root.child("Matches").child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String cardsIds = dataSnapshot.child("cardIds").getValue()+"";
                String theme = dataSnapshot.child("theme").getValue()+"";

                //get opponent data
                String opp_email = "";

                for(DataSnapshot player : dataSnapshot.child("players").getChildren()){
                    if(!player.getKey().equals(currUser.getEmail().replace('.', ','))){
                        opp_email = player.getKey()+"";
                    }
                }

                final String opp_email_fin = opp_email;
                final String cardIds_fin = cardsIds;
                final String theme_fin = theme;

                root.child("Users").child(opp_email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String displayername = dataSnapshot.child("displayname").getValue()+"";
                        String dpURL = dataSnapshot.child("dpURL").getValue()+"";
                        String userId = dataSnapshot.child("userId").getValue()+"";

                        //make opp user object
                        User opp = new User(displayername, opp_email_fin.replace(',','.'), userId, dpURL);
                        //make curruser user object
                        User user = new User(currUser.getDisplayName(), currUser.getEmail(), currUser.getUid(), currUser.getPhotoUrl().toString());

                        Intent i = new Intent(FindingMatchActivity.this, CountdownActivity.class);
                        i.putExtra("cardIds", cardIds_fin);
                        i.putExtra("theme", theme_fin);
                        i.putExtra("user", user);
                        i.putExtra("opp", opp);
                        i.putExtra("matchID", matchId);
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
