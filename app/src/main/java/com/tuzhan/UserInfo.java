package com.tuzhan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfo extends AppCompatActivity {

    int windowHeight = 0;
    int windowWidth = 0;
    CircleImageView civ_userDisplayPhoto;
    TextView tv_userDisplayName;
    FirebaseAuth mAuth;
    TextView tv_userEmail;
    TextView tv_userKD;
    TextView tv_userRounds;
    FirebaseDatabase database;
    DatabaseReference root;
    FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setPopUp(1.0d, 0.55d);

        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();

        civ_userDisplayPhoto = (CircleImageView) findViewById(R.id.civ_userDisplayPhoto);
        tv_userDisplayName = (TextView) findViewById(R.id.tv_userDisplayName);
        tv_userEmail = (TextView) findViewById(R.id.user_email);
        tv_userKD = (TextView) findViewById(R.id.tv_userKD);
        tv_userRounds = (TextView) findViewById(R.id.tv_userRoundsPlayed);

        Picasso.with(this).load(currUser.getPhotoUrl()).into(civ_userDisplayPhoto);
        tv_userDisplayName.setText(currUser.getDisplayName());
        tv_userEmail.setText(currUser.getEmail());

        //set user win rate and rounds played
        if(MainActivity.isConnectedInternet(this)) {
            setWR();
        }else{
            SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            int rounds_won = sharedPreferences.getInt("rounds_won", 0);
            int rounds_played = sharedPreferences.getInt("rounds_played", 0);
            if(rounds_won > 0 && rounds_played > 0) {
                double win_rate = (double) rounds_won / (double) rounds_played;
                win_rate = win_rate * 100;
                tv_userKD.setText(win_rate + "%");
                tv_userRounds.setText(rounds_played);
            }else{
                tv_userKD.setText("无");
                tv_userRounds.setText("0");
            }
        }

    }

    public void setWR() {

        DatabaseReference user_info =  root.child("Users").child(currUser.getEmail().replace('.',','));

        user_info.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //make sure that there rounds_won is present under user info
                if(dataSnapshot.hasChild("rounds_won")) {
                    int rounds_won = (int) dataSnapshot.child("rounds_won").getValue();
                    int rounds_played = (int) dataSnapshot.child("rounds_played").getValue();
                    double win_rate = (double) rounds_won / (double) rounds_played;
                    win_rate = win_rate * 100;
                    tv_userKD.setText(win_rate + "%");
                    tv_userRounds.setText(rounds_played);
                }else{
                    tv_userKD.setText("无");
                    tv_userRounds.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Failed to retrieve wr", databaseError.getMessage());
            }
        });

    }

    public void setPopUp(double widthD, double heightD) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        windowWidth = width;
        windowHeight = height;
        getWindow().setLayout((int) (((double) width) * widthD), (int) (((double) height) * heightD));
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }

}
