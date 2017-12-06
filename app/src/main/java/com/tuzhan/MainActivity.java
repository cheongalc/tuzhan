package com.tuzhan;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FirebaseUser curUser;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView civDpPhoto;
    TextView tvUserName;
    RelativeLayout userInfoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //retrieve user info
        mAuth = FirebaseAuth.getInstance();
        curUser = mAuth.getCurrentUser();

        //required for signing out the user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        civDpPhoto = (CircleImageView) findViewById(R.id.user_dp);
        tvUserName = (TextView) findViewById(R.id.user_dn);
        userInfoButton = (RelativeLayout) findViewById(R.id.u_info_button);

        Picasso.with(this).load(curUser.getPhotoUrl()).into(civDpPhoto);
        tvUserName.setText(curUser.getDisplayName());

        userInfoButton.setOnClickListener(userInfoClick);
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
