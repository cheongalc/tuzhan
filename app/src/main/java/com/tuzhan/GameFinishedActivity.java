package com.tuzhan;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;


public class GameFinishedActivity extends AppCompatActivity {

    FragmentManager fragmentManager;
    ImageButton btn_back, btn_flip;

    String TAG_FINISHED = "game_finished_frag";
    String TAG_FLASH = "flash_cards_frag";
    String currentFrag = "game_finished";

    static String cardIds;
    static String theme;
    static MatchRecord matchRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finished);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //set page to game finished fragment
        fragmentTransaction.replace(R.id.fragment_container, new FragmentGameFinished(), TAG_FINISHED);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        btn_back = (ImageButton) findViewById(R.id.btn_returnHome);
        btn_flip = (ImageButton) findViewById(R.id.btn_flip_card);

        btn_back.setOnClickListener(v -> finish());
        btn_flip.setOnClickListener(v -> {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if(currentFrag.equals("game_finished")) {
                currentFrag = "flash_cards";
                //current page is game finished frag, set page to flash card fragment
                FragmentFlashCards fragmentFlashCards = new FragmentFlashCards();
                transaction.replace(R.id.fragment_container, fragmentFlashCards);
                transaction.addToBackStack(null);
                transaction.commit();
            }else{
                currentFrag = "game_finished";
                //current page is flash cards, change back to game finished frag
                FragmentGameFinished fragmentGameFinished = (FragmentGameFinished) fragmentManager.findFragmentByTag(TAG_FINISHED);
                if(fragmentGameFinished != null) transaction.replace(R.id.fragment_container, fragmentGameFinished);
                else  transaction.replace(R.id.fragment_container, new FragmentGameFinished());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

}
