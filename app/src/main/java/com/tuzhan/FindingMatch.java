package com.tuzhan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FindingMatch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_match);
    }

    public void matchFound(View view) {
        Intent i = new Intent(FindingMatch.this, MatchFound.class);
        startActivity(i);
    }
}
