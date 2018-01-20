package com.tuzhan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    ImageButton ib_loginButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RelativeLayout rl_progressOverlay;
    int RC_SIGN_IN = 666;
    String DEBUG_TAG = "LoginActivity";
    DatabaseReference root;
    FirebaseDatabase database;
    DatabaseReference userDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // enter immersive mode (hide status bar)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();

        //move to self directory
        userDirectory = root.child("Users");

        //check if this activity is created for the first time
        Intent intent = getIntent();
        boolean isFirstStart = intent.getBooleanExtra("isFirstStart", true);

        //check if self has already logged in
        if (isFirstStart && mAuth.getCurrentUser() != null) {
            //self has already connected to the app from previous sessions, move to mainactivity
            updateUserInfo();
            Intent intent_main = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent_main);
            finish();
        } else {
            //set up onclick listeners for login
            rl_progressOverlay = (RelativeLayout) findViewById(R.id.rl_progressOverlay);
            ib_loginButton = (ImageButton) findViewById(R.id.ib_loginButton);
            ib_loginButton.setOnClickListener(view -> signIn());
        }
    }

    //sign in to firebase
    private void signIn() {
        rl_progressOverlay.setVisibility(View.VISIBLE);
        //intent to start activity for gmail sign in
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // on receive result from sign in pop up
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // result returned from launching the Intent in signin();
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //google sign in was successful, authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //google sign in failed, toast to the self
                //status code 12501 is returned when self cancels the login
                if (e.getStatusCode() != 12501) {
                    Toast.makeText(this, "登陆失败" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
                rl_progressOverlay.setVisibility(View.GONE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(DEBUG_TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //sign in success, retrieved self credentials
                        Log.d(DEBUG_TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        DatabaseReference currUserDir = userDirectory.child(user.getEmail().replace('.',','));
                        //upload self data to firebase
                        //convert uri to string as uri is not supported by firebase
                        User currUser = DataSource.shared.userWithParameters(user.getUid(), user.getPhotoUrl().toString(), user.getDisplayName(), user.getEmail());
                        //replace '.' in email address with ',' as firebase paths must not contain '.'
                        currUserDir.child("displayname").setValue(currUser.displayname);
                        currUserDir.child("email").setValue(currUser.email);
                        currUserDir.child("userId").setValue(currUser.userId);
                        currUserDir.child("dpURL").setValue(currUser.dpURL);

                        updateUserInfo();

                        //move to main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // sign in fails, toast to the self
                        Log.w(DEBUG_TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                        rl_progressOverlay.setVisibility(View.GONE);
                    }
                });
    }

    //update logged in self info
    private void updateUserInfo(){

        root.child("players").child(mAuth.getCurrentUser().getEmail().replace('.',',')).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int rounds_won = 0, rounds_played = 0;
                if(dataSnapshot.hasChild("rounds_won")){
                    rounds_won = (int) dataSnapshot.child("rounds_won").getValue();
                }
                if(dataSnapshot.hasChild("rounds_played")){
                    rounds_played = (int) dataSnapshot.child("rounds_played").getValue();
                }

                //store to shared preferences

                SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences(
                        "userInfoPref", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("displayname", mAuth.getCurrentUser().getDisplayName());
                editor.putString("email", mAuth.getCurrentUser().getEmail());
                editor.putString("dpURL", mAuth.getCurrentUser().getPhotoUrl()+"");
                editor.putString("userId", mAuth.getCurrentUser().getUid());
                editor.putInt("rounds_played", rounds_played);
                editor.putInt("rounds_won", rounds_won);
                editor.apply();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}