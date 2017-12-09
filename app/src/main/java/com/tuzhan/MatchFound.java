package com.tuzhan;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchFound extends AppCompatActivity {

    CircleImageView civ_user_dp, civ_opp_dp;
    CircleImageView civ_center_count_down;

    TextView tv_user_name, tv_opp_name;

    ArrayList<QuestionCard> questionCardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_found);

        Intent pastIntent = getIntent();

        //retrieve player info from finding match
        User user = (User) pastIntent.getSerializableExtra("user");
        User opp = (User) pastIntent.getSerializableExtra("opp");

        tv_user_name.setText(user.displayname);
        tv_opp_name.setText(opp.displayname);

        Picasso.with(this).load(user.dpURL).into(civ_user_dp);
        Picasso.with(this).load(opp.dpURL).into(civ_opp_dp);
    }

    //TODO retrieve relevant stuff (eg. card ids) from local db or firebase
    private void retrieveMaterials() {
        //populate questionCardList...
        beginCountDown();
    }

    private void beginCountDown(){

        int[] count_down_numbers = {R.mipmap.tuzhan_1, R.mipmap.tuzhan_2, R.mipmap.tuzhan_3, R.mipmap.tuzhan_4, R.mipmap.tuzhan_5};

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
               int seconds_remaining = (int) millisUntilFinished/1000;
               civ_center_count_down.setImageResource(count_down_numbers[seconds_remaining]);
            }

            public void onFinish() {
                Intent intent = new Intent(MatchFound.this, GameplayUI.class);
                intent.putParcelableArrayListExtra("question_cards", questionCardList);
                startActivity(intent);
            }
        }.start();

    }

}
