package com.tuzhan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FindingMatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_match);
    }

    public void matchFound(View view) {
        Intent i = new Intent(FindingMatchActivity.this, CountdownActivity.class);
        startActivity(i);
    }
}
