package com.tuzhan;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPage extends AppCompatActivity {

    ImageButton bLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RelativeLayout progress_overlay;
    int RC_SIGN_IN = 666;
    String TAG = "LoginActivity";
    DatabaseReference root;
    FirebaseDatabase database;
    DatabaseReference user_directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //move to user directory
        user_directory = root.child("Users");

        //check if this activity is created for the first time
        Intent intent = getIntent();
        boolean isFirstStart = intent.getBooleanExtra("isFirstStart", true);

        //check if user has already logged in
        if (isFirstStart && mAuth.getCurrentUser() != null) {
            //user has already connected to the app from previous sessions, move to mainactivity
            Intent intent_main = new Intent(LoginPage.this, MainActivity.class);
            startActivity(intent_main);
            finish();
        } else {
            //set up onclick listeners for login
            progress_overlay = (RelativeLayout) findViewById(R.id.progress_overlay);
            bLogin = (ImageButton) findViewById(R.id.login_button);
            bLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
        }
    }

    //sign in to firebase
    private void signIn() {
        progress_overlay.setVisibility(View.VISIBLE);
        //intent to start activity for gmail sign in
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //receive result from sign in pop up
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
                //google sign in failed, toast to the user
                //status code 12501 is returned when user cancels the login
                if (e.getStatusCode() != 12501) {
                    Toast.makeText(this, "登陆失败" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
                progress_overlay.setVisibility(View.GONE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //sign in success, retrieved user credentials
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            DatabaseReference curUserDir = user_directory.child(user.getEmail().replace('.',','));
                            //upload user data to firebase
                            //convert uri to string as uri is not supported by firebase
                            User curuser = new User(user.getDisplayName(), user.getEmail(), user.getUid(), user.getPhotoUrl().toString());
                            //replace '.' in email address with ',' as firebase paths must not contain '.'
                            curUserDir.child("displayname").setValue(curuser.displayname);
                            curUserDir.child("email").setValue(curuser.email);
                            curUserDir.child("userId").setValue(curuser.userId);
                            curUserDir.child("dpURL").setValue(curuser.dpURL);

                            //move to main activity
                            Intent intent = new Intent(LoginPage.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // sign in fails, toast to the user
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginPage.this, "登陆失败", Toast.LENGTH_SHORT).show();
                            progress_overlay.setVisibility(View.GONE);
                        }
                    }
                });
    }

}