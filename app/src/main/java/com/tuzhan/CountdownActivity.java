package com.tuzhan;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CountdownActivity extends AppCompatActivity {

    CircleImageView civ_user_dp, civ_opp_dp;
    CircleImageView civ_center_count_down;

    TextView tv_user_name, tv_opp_name;
    String cardIdsString, theme, matchID;

    ArrayList<QuestionCard> questionCardList = new ArrayList<>();

    User user, opp;

    CountDownTimer countDownTimer;

    int[] count_down_numbers = {R.mipmap.tuzhan_1, R.mipmap.tuzhan_2, R.mipmap.tuzhan_3, R.mipmap.tuzhan_4, R.mipmap.tuzhan_5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_found);

        Intent pastIntent = getIntent();

        //retrieve player info from finding match
        user = (User) pastIntent.getSerializableExtra("user");
        opp = (User) pastIntent.getSerializableExtra("opp");
        cardIdsString = pastIntent.getStringExtra("cardIds");
        theme = pastIntent.getStringExtra("theme");
        matchID = pastIntent.getStringExtra("matchID");

        tv_user_name = (TextView) findViewById(R.id.tv_user_name);
        tv_opp_name = (TextView) findViewById(R.id.tv_opp_name);

        civ_opp_dp = (CircleImageView) findViewById(R.id.civ_opp_dp);
        civ_user_dp = (CircleImageView) findViewById(R.id.civ_user_dp);
        civ_center_count_down = (CircleImageView) findViewById(R.id.civ_center_count_down);

        tv_user_name.setText(user.displayname);
        tv_opp_name.setText(opp.displayname);

        Picasso.with(this).load(user.dpURL).into(civ_user_dp);
        Picasso.with(this).load(opp.dpURL).into(civ_opp_dp);

        countDownTimer = new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds_remaining = (int) millisUntilFinished/1000;
                civ_center_count_down.setImageResource(count_down_numbers[seconds_remaining-1]);
            }

            public void onFinish() {
                Intent intent = new Intent(CountdownActivity.this, GameplayActivity.class);
                intent.putParcelableArrayListExtra("question_cards", questionCardList);
                intent.putExtra("card_IDs_string", cardIdsString);
                intent.putExtra("matchID", matchID);
                intent.putExtra("opp_dpURL", opp.dpURL);
                startActivity(intent);
            }
        };

        retrieveMaterials();
    }

    private void retrieveMaterials() {
        //populate questionCardList...
        List<Integer> cardIdsList = Utils.splitToInts(cardIdsString);

        DataSource.shared.fetchCards(theme, cardIdsList, qCardList ->{
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
