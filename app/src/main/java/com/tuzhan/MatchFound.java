package com.tuzhan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MatchFound extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_found);
    }

    public void startGame(View view) {
        Intent i = new Intent(MatchFound.this, GameplayUI.class);
        startActivity(i);
    }
}
