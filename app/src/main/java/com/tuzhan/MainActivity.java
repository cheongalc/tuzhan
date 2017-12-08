package com.tuzhan;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FirebaseUser currUser;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView civ_dpPhoto;
    TextView tv_userDisplayName;
    TextView tv_userKD;
    TextView tv_userRoundsPlayed;
    RelativeLayout userInfoButton;
    FirebaseDatabase database;
    DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        civ_dpPhoto = (CircleImageView) findViewById(R.id.civ_userDisplayPhoto);
        tv_userDisplayName = (TextView) findViewById(R.id.tv_userDisplayName);
        tv_userKD = (TextView) findViewById(R.id.tv_userKD);
        tv_userRoundsPlayed = (TextView) findViewById(R.id.tv_userRoundsPlayed);
        userInfoButton = (RelativeLayout) findViewById(R.id.u_info_button);

        Picasso.with(this).load(currUser.getPhotoUrl()).into(civ_dpPhoto);
        tv_userDisplayName.setText(currUser.getDisplayName());

        userInfoButton.setOnClickListener(userInfoClick);

        root.child("Users").child(currUser.getEmail().replace('.',',')).child("isOnline").setValue(true);
        Intent intent = new Intent(this, ClosingService.class);
        intent.putExtra("email", currUser.getEmail());
        startService(intent);
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

}
