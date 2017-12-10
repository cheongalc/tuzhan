package com.tuzhan;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameFinishedActivity extends AppCompatActivity {

    private static final String LOG_TAG = "GAMEFINISHED";
    private List<String> formattedEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finished);

        Bundle bundle = getIntent().getExtras();
        String unformattedEntries = bundle.getString("playerEntries");
        int playerScore = bundle.getInt("playerScore");
        Log.d(LOG_TAG, unformattedEntries);
        formattedEntries = formatEntries(unformattedEntries);
        Log.d(LOG_TAG, formattedEntries.toString());

        // display score
        TextView tv_playerScore = (TextView) findViewById(R.id.tv_playerFinalScore);
        tv_playerScore.setText(String.valueOf(playerScore));

        // inflate layout into player entries
        LinearLayout ll_playerEntries = (LinearLayout) findViewById(R.id.ll_playerEntries);
        for (int i = 0; i < formattedEntries.size(); i++) {
            String currentEntry = formattedEntries.get(i);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout child = (RelativeLayout) inflater.inflate(R.layout.item_word_entry, ll_playerEntries, false);
            TextView content = (TextView) child.findViewById(R.id.tv_playerEntry);
            if (currentEntry.charAt(0) == 'p') {
                content.setTextColor(ContextCompat.getColor(GameFinishedActivity.this, R.color.colorAccentOrange));
                currentEntry = currentEntry.substring(1, currentEntry.length());
            }
            content.setText(currentEntry);
            ll_playerEntries.removeView(child);
            ll_playerEntries.addView(child);
        }
    }

    private List<String> formatEntries(String unformattedEntries) {
        List<String> output = new ArrayList<>();
        String currentStack = "";
        for (int i = 0; i < unformattedEntries.length(); i++) {
            char currentChar = unformattedEntries.charAt(i);
            if (currentChar == '-') {
                output.add(currentStack);
                currentStack = "";
            } else {
                currentStack += currentChar;
            }
        }
        return output;
    }
}
