package com.tuzhan;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FragmentFlashCards extends Fragment {


    public FragmentFlashCards() {
        // Required empty public constructor
    }

    ProgressBar pbFlashCardProgress;
    ImageButton bCross, bTick, bRestart;
    NonSwipeableViewPager vpFlashCards;
    RelativeLayout rl_ans_reveal, rl_finished_page;
    TextView tv_user_ans, tv_correct_ans;
    int guessedCards = 0, original_size = 0;
    List<QuestionCard> questionCards_ = new ArrayList<>(), questionCards = new ArrayList<>();
    List<String> userEntries_ = new ArrayList<>(), userEntries = new ArrayList<>();
    MatchRecord matchRecord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_fragment_flash_cards, container, false);

        pbFlashCardProgress = (ProgressBar) rootview.findViewById(R.id.pb_flash_card_progress);
        bCross = (ImageButton) rootview.findViewById(R.id.bCross);
        bTick = (ImageButton) rootview.findViewById(R.id.bTick);
        vpFlashCards = (NonSwipeableViewPager) rootview.findViewById(R.id.vp_flash_cards);
        rl_ans_reveal = (RelativeLayout) rootview.findViewById(R.id.rl_ans_reveal);
        rl_finished_page = (RelativeLayout) rootview.findViewById(R.id.rl_finished_page);
        tv_correct_ans = (TextView) rootview.findViewById(R.id.tv_correct_ans);
        tv_user_ans = (TextView) rootview.findViewById(R.id.tv_user_ans);
        bRestart = (ImageButton) rootview.findViewById(R.id.bRestart);

        matchRecord = GameFinishedActivity.matchRecord;
        userEntries_.addAll(matchRecord.entriesSelf);


        pbFlashCardProgress.setProgress(0);
        List<Integer> cardIds = Utils.splitToInts(GameFinishedActivity.cardIds);
        String theme = GameFinishedActivity.theme;

        DataSource.shared.fetchCards(theme, cardIds, fetched -> {
            if(fetched != null) {

                questionCards_.addAll(fetched);
                original_size = questionCards_.size();

                userEntries.addAll(userEntries_);
                questionCards.addAll(questionCards_);

                GamePagerAdapter gamePagerAdapter = new GamePagerAdapter(getFragmentManager(), questionCards.size(), questionCards);
                vpFlashCards.setAdapter(gamePagerAdapter);


                bCross.setOnClickListener(v -> {

                    //remove entries and add to the back of the list
                    QuestionCard questionCard = questionCards.get(0);
                    String entry = userEntries.get(0);
                    questionCards.remove(0);
                    userEntries.remove(0);
                    questionCards.add(questionCard);
                    userEntries.add(entry);

                    //update vp
                    GamePagerAdapter gamePagerAdapter1 = new GamePagerAdapter(getFragmentManager(), questionCards.size(), questionCards);
                    vpFlashCards.setAdapter(gamePagerAdapter1);

                });

                bTick.setOnClickListener(v ->{

                    if(questionCards.size() > 0) {

                        //update vp and reveal answers
                        revealAns(questionCards.get(0), userEntries.get(0));

                        //remove guessed entries
                        questionCards.remove(0);
                        userEntries.remove(0);

                        //variable used for updating progress bar
                        guessedCards+=1;

                        //update progress bar
                        pbFlashCardProgress.setProgress((int) (((double) guessedCards / (double) original_size) * 100));
                        if(questionCards.size() != 0){
                            vpFlashCards.setCurrentItem(0);
                        }

                    }

                });

            }
        });

        bRestart.setOnClickListener(v -> restart());

        return rootview;
    }

    private void restart(){

        rl_finished_page.setVisibility(View.GONE);

        userEntries.addAll(userEntries_);
        questionCards.addAll(questionCards_);

        GamePagerAdapter gamePagerAdapter = new GamePagerAdapter(getFragmentManager(), questionCards.size(), questionCards);

        vpFlashCards.setAdapter(gamePagerAdapter);
        pbFlashCardProgress.setProgress(0);
        guessedCards = 0;


    }

    private void revealAns(QuestionCard questionCard, String entry){
        rl_ans_reveal.setVisibility(View.VISIBLE);

        tv_correct_ans.setText(questionCard.answers.get(0));
        tv_user_ans.setText(entry);

        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //do nothing
            }

            @Override
            public void onFinish() {
                //update vp
                rl_ans_reveal.setVisibility(View.GONE);
                GamePagerAdapter gamePagerAdapter1 = new GamePagerAdapter(getFragmentManager(), questionCards.size(), questionCards);
                vpFlashCards.setAdapter(gamePagerAdapter1);

                if (questionCards.size() == 0){
                    rl_finished_page.setVisibility(View.VISIBLE);
                }

            }
        };

        countDownTimer.start();
    }

}
