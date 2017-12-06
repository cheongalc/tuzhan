package com.tuzhan;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setPopUp(0.9d, 0.55d);

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser curUser = mAuth.getCurrentUser();

        civDpPhoto = (CircleImageView) findViewById(R.id.user_dp);
        tvUserName = (TextView) findViewById(R.id.user_dn);

        tvUserEmail = (TextView) findViewById(R.id.user_email);
        tvUserkd = (TextView) findViewById(R.id.user_kd);
        tvUserRounds = (TextView) findViewById(R.id.user_rounds_played);

        Picasso.with(this).load(curUser.getPhotoUrl()).into(civDpPhoto);
        tvUserName.setText(curUser.getDisplayName());

        tvUserEmail.setText(curUser.getEmail());

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
