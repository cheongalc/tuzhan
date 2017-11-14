package com.acwchineseapp;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.sephiroth.android.library.tooltip.Tooltip;

public class GameplayUI extends AppCompatActivity implements GameFragmentInterface {

    private static final String LOG_TAG = "GAMEPLAYUI";
    private NonSwipeableViewPager viewPager;
    private RelativeLayout rootLayout;
    private List<RelativeLayout> currImageRelativeLayouts = new ArrayList<>(); // Because ViewPager generates 1 page more than the current one, keep array to prevent overriding


    private static final int NUM_IMAGES = 4;
    private static final int DELAY = 15000;
    private static final int TOOLTIP_SHOW_ANS_ID = 123;
    private int playerScore = 0, currentImage = 0; // currImage represents the current position in ViewPager
    private List<String> formattedAnswers = new ArrayList<>(); // global list to store the answers
    private List< Pair<String, Integer> > possibleMatches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_ui);

        // Set toolbar title to nothing so that the theme prevails
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_toolbarGameplay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // Save instance of the root layout to use for modification during submitting words
        rootLayout = (RelativeLayout) findViewById(R.id.rl_gameplayUI);

        // Setup the viewpager and its fragment manager
        viewPager = (NonSwipeableViewPager) findViewById(R.id.vp_imagePager);
        final GamePagerAdapter adapter = new GamePagerAdapter(getSupportFragmentManager(), NUM_IMAGES); // change to 20 later on
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        startGame();
    }

    @Override
    public void onImageFragmentCreated(RelativeLayout relativeLayout) {
        currImageRelativeLayouts.add(relativeLayout);
    }

    public void startTimer() {
        final TextView tv_playerTime = (TextView) findViewById(R.id.tv_playerTime);
        new CountDownTimer(DELAY-5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_playerTime.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tv_playerTime.setText("0");
            }
        }.start();
    }

    public void startGame() {
        final View v_focusDummy = findViewById(R.id.v_focusDummy);
        v_focusDummy.requestFocus();

        // Set up IME Options Handler for the Edit Text so that we don't waste time
        final EditText et_wordEntry = (EditText) findViewById(R.id.et_wordEntry);
        et_wordEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionID, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionID == EditorInfo.IME_ACTION_DONE) {
                    String word = String.valueOf(view.getText());
                    if (word.length() > 0) submitWord(word);
                    et_wordEntry.setText(""); // set text to nothing
                    handled = true;
                }
                return handled;
            }
        });

        // Setup the score text view

        TextView tv_playerScore = (TextView) findViewById(R.id.tv_playerScore);
        tv_playerScore.setText(String.valueOf(playerScore));

        final Handler handler = new Handler();
        final Runnable mainRunnable = new Runnable() {
            @Override
            public void run() {
                possibleMatches.clear();
                LinearLayout ll_characterBoxWrapper = (LinearLayout) findViewById(R.id.ll_characterBoxWrapper);
                ll_characterBoxWrapper.removeAllViews(); // remove all the light blue boxes for re-initialization
                startTimer(); // start countdown timer
                if (currentImage < NUM_IMAGES) viewPager.setCurrentItem(currentImage, true);

                ImageView iv_gameplayImage = (ImageView) currImageRelativeLayouts.get(currentImage).findViewById(R.id.iv_gameplayImage);
                String unformattedAnswers = (String) iv_gameplayImage.getTag(); // remember the unformatted answers were passed on through the image tag?
                formattedAnswers = formatAnswers(unformattedAnswers);
                fillBlueBoxes(formattedAnswers.get(0).length()); // okay to take the first element, all answers will always have the same number of chars
                currentImage++;

                if (currentImage < NUM_IMAGES) {
                    handler.postDelayed(this, DELAY); // 15 seconds delay is to make sure that user can see the full countdown
                }
            }
        };
        handler.post(mainRunnable); // post the Runnable for the first time
        final Runnable scoreThreadRunnable = new Runnable() {
            @Override
            public void run() {
                awardScore();
                showAnswers();

                if (currentImage < NUM_IMAGES) {
                    handler.postDelayed(this, DELAY-(DELAY-12000));
                }
            }
        };
        handler.postDelayed(scoreThreadRunnable, DELAY); // 12 seconds delay then check the answers
        if (currentImage == NUM_IMAGES) {
            // go to finish game activity
            Toast.makeText(this, "Game Finished", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnswers() {
        final int answerLength = formattedAnswers.get(0).length();
        int numFilledBoxes = countFilledBoxes();
        if (numFilledBoxes < answerLength) {
            //Show the first answer because it is the most important.
            for (int i = 0; i < answerLength; i++) {
                TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(i);
                String content = String.valueOf(tv_characterBox.getText());
                if (content.equals("")) {
                    tv_characterBox.setText(String.valueOf(formattedAnswers.get(0).charAt(i)));
                    tv_characterBox.setBackgroundResource(R.drawable.background_accent_red);
                }
            }
        }
    }

    private void awardScore() {
        // Firstly, count the number of boxes which are filled
        int numFilledBoxes = countFilledBoxes();
        playerScore += numFilledBoxes * 5;
        TextView tv_playerScore = (TextView) findViewById(R.id.tv_playerScore);
        tv_playerScore.setText(String.valueOf(playerScore));
    }

    private int countFilledBoxes() {
        int result = 0;
        for (int i = 0; i < formattedAnswers.get(0).length(); i++) {
            TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(i);
            String content = String.valueOf(tv_characterBox.getText());
            if (!content.equals("")) {
                // it is filled
                result++;
            }
        }
        return result;
    }

    private void submitWord(String word) {
        // start reading the word from left to right
        char firstCharacter = word.charAt(0);
        // check whether formatted answer dict contains the first character or not
        if (isAnswer(firstCharacter)) {
            if (possibleMatches.size() > 0) {
                Log.d(LOG_TAG, "This isn't first time entering something.");
                // this means that a first character has been entered before
                boolean isContinuation = false;
                for (int i = 0; i < possibleMatches.size(); i++) {
                    if (possibleMatches.get(i).getLeft().equals(String.valueOf(firstCharacter))) {
                        isContinuation = true;
                        break;
                    }
                }
                Log.d(LOG_TAG, String.valueOf(isContinuation));
                if (!isContinuation) {
                    // if there is a discontinuation
                    possibleMatches.clear();
                    clearBlueBoxes(firstCharacter);
                    possibleMatches = findPossibleMatches(firstCharacter); // regenerate the array
                }

            } else possibleMatches = findPossibleMatches(firstCharacter); // find all the other possible characters that match
        }
        Log.d(LOG_TAG, possibleMatches.toString());
        word = word.substring(1, word.length()); // take out first character
        for (int i = 0; i < word.length(); i++) {
            char currentCharacter = word.charAt(i);
            for (int j = 0; j < possibleMatches.size(); j++) {
                if (possibleMatches.get(j).getLeft().equals(String.valueOf(currentCharacter))) {
                    fillBlueBox(possibleMatches.get(j).getRight(), currentCharacter, 0);
                }
            }
        }
    }

    private void clearBlueBoxes(char excludedChar) {
        for (int i = 0; i < formattedAnswers.get(0).length(); i++) {
            TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(i);
            if (!String.valueOf(tv_characterBox.getText()).equals(String.valueOf(excludedChar))) {
                tv_characterBox.setText("");
                tv_characterBox.setBackgroundResource(R.drawable.background_accent_blue);
            }
        }
    }

    private boolean isAnswer(char firstCharacter) {
        boolean output = false;
        for (int i = 0; i < formattedAnswers.size(); i++) {
            String s = formattedAnswers.get(i);
            int index = s.indexOf(firstCharacter);
            if (index != -1) {
                fillBlueBox(index, firstCharacter, 0);
                output = true;
            }
        }
        return output;
    }

    private void fillBlueBox(int index, char firstCharacter, int mode) {
        TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(index);
        tv_characterBox.setText(String.valueOf(firstCharacter));
        if (mode == 0) {
            tv_characterBox.setBackgroundResource(R.drawable.background_accent_green);
        } else if (mode == 1) {
            tv_characterBox.setBackgroundResource(R.drawable.background_accent_red);
        }
    }

    private List< Pair <String, Integer> > findPossibleMatches(char character) {
        List< Pair <String, Integer> > output = new ArrayList<>(); // put size as 100 for buffer
        for (int i = 0; i < formattedAnswers.size(); i++) {
            String currentCorrectWord = formattedAnswers.get(i);
            Log.d(LOG_TAG, "Currently looking at word " + formattedAnswers.get(i));
            Log.d(LOG_TAG, String.valueOf(currentCorrectWord.indexOf(character)));
            if (currentCorrectWord.indexOf(character) == -1) continue; // if not found, continue
            for (int j = 0; j < currentCorrectWord.length(); j++) {
                // loop through each word
                if (currentCorrectWord.charAt(j) != character) {
                    // find all the characters that can be matched with this character
                    output.add(new Pair<>(String.valueOf(currentCorrectWord.charAt(j)), j));
                }
            }
        }
        return output;
    }


    private void fillBlueBoxes(int numberOfCharacters) {
        // fills the necessary number of blue boxes into the horizontal scroll view.
        LinearLayout ll_characterBoxWrapper = (LinearLayout) findViewById(R.id.ll_characterBoxWrapper);
        for (int i = 0; i < numberOfCharacters; i++) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View child = inflater.inflate(R.layout.item_character_box, ll_characterBoxWrapper, false);
            child.setTag(i); // so that the position of each character is tracked
            ll_characterBoxWrapper.removeView(child);
            ll_characterBoxWrapper.addView(child);
        }
    }

    private List<String> formatAnswers(String unformattedAnswers) {
        List<String> output = new ArrayList<>();
        String currentStack = "";
        for (int i = 0; i < unformattedAnswers.length(); i++) {
            char currentChar = unformattedAnswers.charAt(i);
            if (currentChar == '-') {
                output.add(currentStack);
                currentStack = "";
            } else {
                currentStack += currentChar;
            }
        }
        return output;
    }

    // Show the English translation of the current Theme.
    public void showTooltip(View view) {

    }


}
