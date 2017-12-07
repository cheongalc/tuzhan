package com.tuzhan;

import android.graphics.drawable.ColorDrawable;
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
    CircleImageView civDpPhoto;
    TextView tvUserName;
    FirebaseAuth mAuth;
    TextView tvUserEmail;
    TextView tvUserkd;
    TextView tvUserRounds;
    FirebaseDatabase database;
    DatabaseReference root;
    FirebaseUser curUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setPopUp(0.9d, 0.55d);

        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();

        civDpPhoto = (CircleImageView) findViewById(R.id.user_dp);
        tvUserName = (TextView) findViewById(R.id.user_dn);
        tvUserEmail = (TextView) findViewById(R.id.user_email);
        tvUserkd = (TextView) findViewById(R.id.user_kd);
        tvUserRounds = (TextView) findViewById(R.id.user_rounds_played);

        Picasso.with(this).load(curUser.getPhotoUrl()).into(civDpPhoto);
        tvUserName.setText(curUser.getDisplayName());
        tvUserEmail.setText(curUser.getEmail());

        //set user win rate and rounds played
        setWR();

    }

    public void setWR() {

        DatabaseReference user_info =  root.child("Users").child(curUser.getEmail().replace('.',','));

        user_info.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //make sure that there rounds_won is present under user info
                if(dataSnapshot.hasChild("rounds_won")) {
                    int rounds_won = (int) dataSnapshot.child("rounds_won").getValue();
                    int rounds_played = (int) dataSnapshot.child("rounds_played").getValue();
                    double win_rate = (double) rounds_won / (double) rounds_played;
                    win_rate = win_rate * 100;
                    tvUserkd.setText(win_rate + "%");
                    tvUserRounds.setText(rounds_played);
                }else{
                    tvUserkd.setText("无");
                    tvUserRounds.setText("0");
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
