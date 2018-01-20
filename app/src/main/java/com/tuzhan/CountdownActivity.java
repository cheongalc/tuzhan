package com.tuzhan;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CountdownActivity extends AppCompatActivity {

    CircleImageView civ_selfDP, civ_opponentDP;
    CircleImageView civ_centerCountDown;

    TextView tv_selfName, tv_opponentName;
    String cardIDsString, theme, matchID;

    ArrayList<QuestionCard> questionCardList = new ArrayList<>();

    User self, opponent;

    CountDownTimer countDownTimer;

    int[] countDownNumbers = {R.mipmap.tuzhan_1, R.mipmap.tuzhan_2, R.mipmap.tuzhan_3, R.mipmap.tuzhan_4, R.mipmap.tuzhan_5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        Intent pastIntent = getIntent();

        //retrieve player info from finding match activity
        self = (User) pastIntent.getSerializableExtra(Constants.C_USER_SELF);
        opponent = (User) pastIntent.getSerializableExtra(Constants.C_USER_OPPONENT);
        cardIDsString = pastIntent.getStringExtra(Constants.C_CARD_IDS_STRING);
        theme = pastIntent.getStringExtra(Constants.C_THEME);
        matchID = pastIntent.getStringExtra(Constants.C_MATCH_ID);

        // init the text views
        tv_selfName = (TextView) findViewById(R.id.tv_user_name);
        tv_selfName.setText(self.displayname);
        tv_opponentName = (TextView) findViewById(R.id.tv_opp_name);
        tv_opponentName.setText(opponent.displayname);

        // init the circle image views
        civ_opponentDP = (CircleImageView) findViewById(R.id.civ_opp_dp);
        civ_selfDP = (CircleImageView) findViewById(R.id.civ_user_dp);
        civ_centerCountDown = (CircleImageView) findViewById(R.id.civ_center_count_down);

        // load the display photos into the circle image views
        Picasso.with(this).load(self.dpURL).into(civ_selfDP);
        Picasso.with(this).load(opponent.dpURL).into(civ_opponentDP);

        Intent i = new Intent(CountdownActivity.this, GameplayActivity.class);
        i.putExtra(Constants.C_QUESTION_CARD_LIST, questionCardList);
        i.putExtra(Constants.C_CARD_IDS_STRING, cardIDsString);
        i.putExtra(Constants.C_MATCH_ID, matchID);
        i.putExtra(Constants.C_OPPONENT_DPURL, opponent.dpURL);
        i.putExtra("meme", "Test");

        // init the countdown timer
        countDownTimer = new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) millisUntilFinished/1000;
                civ_centerCountDown.setImageResource(countDownNumbers[secondsRemaining-1]);
            }

            public void onFinish() {
                startActivity(i);
            }
        };

        retrieveMaterials();
    }

    private void retrieveMaterials() {
        //populate questionCardList...
        List<Integer> cardIDsList = Utils.splitToInts(cardIDsString);

        DataSource.shared.fetchCards(theme, cardIDsList, qCardList -> {
            if(qCardList != null) {
                questionCardList.addAll(qCardList);
            }
        });
        beginCountDown();
    }

    private void beginCountDown(){
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }
}
