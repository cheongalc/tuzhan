package com.tuzhan;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameplayActivity extends AppCompatActivity implements GameFragmentInterface {

    private static final String LOG_TAG = "GAMEPLAYACTIVITY";

    //ALL PRIVATE VARIABLES PERTAINING TO LAYOUT
    private NonSwipeableViewPager viewPager;
    private RelativeLayout rootLayout;

    //ALL PRIVATE VARIABLES PERTAINING TO QUESTION CARDS
    private ArrayList<QuestionCard> questionCardArrayList = new ArrayList<>(); // represents the list of Question Card, passed over from CountdownActivity
    private QuestionCard currQuestionCard; // represents current Question Card
    private static final int NUM_IMAGES = 4; // 4 for experimental purposes
    private static final int DELAY = 12000;
    private int currImageIndex = 0;
    private int maxScore = 0, currScore = 0;
    private List<String> formattedAnswers = new ArrayList<>(); // global list to store the answers
    private List<String> formattedHarderAnswers = new ArrayList<>(); // global list to store harder answers
    private List< Pair<String, Integer> > possibleMatches = new ArrayList<>();
    private Runnable mainRunnable, scoreThreadRunnable;
    private CountDownTimer cdt;
    private String currentEntry = "";

    //ALL PRIVATE VARIABLES PERTAINING TO MATCH INFORMATION
    private String matchID, theme;
    private List<String> cardIDs; // stores a list of the IDs of the cards involved in the current match
    private User opponent;
    private long startTime, endTime, elapsedTime;
    private double timeSelf;
    private int scoreSelf = 0;
    private ArrayList<Integer> scoresSelf = new ArrayList<>(); // stores a list of individual scores of each card
    private ArrayList<String> entriesSelf = new ArrayList<>(); // stores a list of individual entries of each card
    //TAKE NOTE FOR ENTRIES, IF THERE IS A "P" IN FRONT IT MEANS THAT IT IS PARTIALLY DONE
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay_ui);

        // Save instance of the root layout to use for modification during submitting words
        rootLayout = (RelativeLayout) findViewById(R.id.rl_gameplayUI);


        // setup the intent extras
        Intent pastIntent = getIntent();
        questionCardArrayList =  Constants.M.questionCardArrayList;
        cardIDs = Utils.split(pastIntent.getStringExtra(Constants.C_CARD_IDS_STRING));
        matchID = pastIntent.getStringExtra(Constants.C_MATCH_ID);
        theme = pastIntent.getStringExtra(Constants.C_THEME);
        opponent = (User) pastIntent.getSerializableExtra(Constants.C_USER_OPPONENT);


        // Setup the viewpager and its fragment manager
        viewPager = (NonSwipeableViewPager) findViewById(R.id.vp_imagePager);
        final GamePagerAdapter adapter = new GamePagerAdapter(getSupportFragmentManager(), NUM_IMAGES, questionCardArrayList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);


        startGame();
        startTime = System.nanoTime();
    }

    @Override
    public void onImageFragmentCreated(RelativeLayout relativeLayout) {
        // Might need to place a currentRelativeLayouts list, removed for now because its not of use.
    }

    public void startTimer() {
        final TextView tv_playerTime = (TextView) findViewById(R.id.tv_playerTime);
        cdt = new CountDownTimer(DELAY-1000, 1000) {
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
        // use a focus dummy view so that the android system doesnt auto focus the <EditText>
        final View v_focusDummy = findViewById(R.id.v_focusDummy);
        v_focusDummy.requestFocus();

        // Set up IME Options Handler for the Edit Text so that we don't waste time
        final EditText et_wordEntry = (EditText) findViewById(R.id.et_wordEntry);
        et_wordEntry.setOnEditorActionListener((view, actionID, keyEvent) -> {
            boolean handled = false;
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                String word = String.valueOf(view.getText());
                if (word.length() > 0) submitWord(word);
                et_wordEntry.setText(""); // set text to nothing
                handled = true;

                if (currScore == maxScore) {
                    Log.e(LOG_TAG, "currentScore = " + currScore + ", maxScore = " + maxScore);
                    // jump straight to the next card.
                    currScore = 0;
                    handler.removeCallbacks(mainRunnable);
                    handler.removeCallbacks(scoreThreadRunnable);
                    awardScore(maxScore/5);
                    cdt.cancel();
                    if (currImageIndex < NUM_IMAGES) handler.postDelayed(mainRunnable, 500);
                    else moveToGameFinished();
                }

                Log.w(LOG_TAG, "Logging from Submit: " + Arrays.toString(new List[]{entriesSelf}));
            }
            return handled;
        });

        // Setup the player score text view
        TextView tv_playerScore = (TextView) findViewById(R.id.tv_playerScore);
        tv_playerScore.setText(String.valueOf(scoreSelf));


        // attach the Runnable that does the gameplay functionality, recurs once every 15 seconds
        mainRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e(LOG_TAG, "mainRunnable reposted");

                currQuestionCard = questionCardArrayList.get(currImageIndex);
                possibleMatches.clear(); // clear possible combinations of card answers


                LinearLayout ll_characterBoxWrapper = (LinearLayout) findViewById(R.id.ll_characterBoxWrapper); // LinearLayout that wraps around the blue boxes
                ll_characterBoxWrapper.removeAllViews(); // remove all the light blue boxes for re-initialization

                startTimer(); // start countdown timer

                if (currImageIndex < NUM_IMAGES) viewPager.setCurrentItem(currImageIndex, true);

                formattedAnswers = currQuestionCard.answers;
                formattedHarderAnswers = currQuestionCard.harderAnswers;

                maxScore = formattedAnswers.get(0).length()*5;

                fillBlueBoxes(formattedAnswers.get(0).length()); // okay to take the first element, because all answers will always have the same number of chars
                currImageIndex++;



                // attach Runnable that executes once every 15 seconds
                scoreThreadRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.w(LOG_TAG, "Logging from STR (entries): " + Arrays.toString(new List[]{entriesSelf}));
                        Log.e(LOG_TAG, "scoreThreadRunnable reposted");
                        int numFilledBoxes = countFilledBoxes();
                        awardScore(numFilledBoxes);
                        showAnswers(numFilledBoxes);

                        if (currImageIndex < NUM_IMAGES) {
                            handler.postDelayed(this, DELAY);
                        } else {
                            moveToGameFinished();
                        }
                    }
                };
                handler.postDelayed(scoreThreadRunnable, DELAY-1000); // a bit shorter so you can check the answers


                if (currImageIndex < NUM_IMAGES) {
                    handler.postDelayed(this, DELAY); // 15 seconds delay is to make sure that self can see the full countdown
                } else if (currImageIndex > NUM_IMAGES) {
                    Log.e(LOG_TAG, "removing excess score thread runnables");
                    handler.removeCallbacks(scoreThreadRunnable);
                }
            }
        };
        handler.post(mainRunnable); // post the Runnable for the first time
    }

    private void moveToGameFinished() {
        endTime = System.nanoTime();
        elapsedTime = (endTime - startTime);
        timeSelf = (double) elapsedTime / 1000000000.0;
        timeSelf = roundOff(timeSelf, 4);

        Log.e(LOG_TAG, "timeSelf: " + String.valueOf(timeSelf));
        Log.e(LOG_TAG, "scoresSelf: " + String.valueOf(scoresSelf.size()));
        Intent i = new Intent(GameplayActivity.this, GameFinishedActivity.class);

        i.putExtra(Constants.C_MATCH_ID, matchID);
        i.putExtra(Constants.C_THEME, theme);
        i.putExtra(Constants.C_CARD_IDS_LIST, (Serializable) Utils.splitToInts(Utils.concatenate(cardIDs)));
        i.putExtra(Constants.C_OPPONENT_EMAIL, opponent.email);
        i.putExtra(Constants.C_SCORE_SELF, scoreSelf);
        i.putExtra(Constants.C_SCORE_SELF_LIST, (Serializable) scoresSelf);
        i.putExtra(Constants.C_TIME_SELF, timeSelf);

        i.putExtra(Constants.C_OPPONENT_DPURL, opponent.dpURL);
        i.putStringArrayListExtra(Constants.C_PLAYER_ENTRIES_LIST, entriesSelf);

        i.putExtra(Constants.C_GAMEFINISHED_KEY, Constants.M.START_FROM_GAMEPLAY);
        startActivity(i);
        handler.removeCallbacks(scoreThreadRunnable);
        handler.removeCallbacksAndMessages(null);
    }

    private double roundOff(double value, int numOfPlaces) {
        if (numOfPlaces < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(numOfPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void showAnswers(int numFilledBoxes) {
        final int answerLength = formattedAnswers.get(0).length();
        if (numFilledBoxes < answerLength) {
            //Show the first answer because it is the most important.
            for (int i = 0; i < answerLength; i++) {
                TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(i);
                tv_characterBox.setText(String.valueOf(formattedAnswers.get(0).charAt(i)));
                tv_characterBox.setBackgroundResource(R.drawable.background_accent_red);
            }
        }
    }

    private void awardScore(int numFilledBoxes) {
        scoreSelf += numFilledBoxes * 5;
        scoresSelf.add(numFilledBoxes * 5);
        entriesSelf.add(currentEntry);
        currentEntry = "";
        TextView tv_playerScore = (TextView) findViewById(R.id.tv_playerScore);
        tv_playerScore.setText(String.valueOf(scoreSelf));
    }

    private int countFilledBoxes() {
        String entryToAdd = "";
        int result = 0;
        StringBuilder stack = new StringBuilder();
        for (int i = 0; i < formattedAnswers.get(0).length(); i++) {
            TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(i);
            String content = String.valueOf(tv_characterBox.getText());
            if (!content.equals("")) {
                // it is filled
                result++;
                stack.append(content);
            }
        }
        if (stack.length() < formattedAnswers.get(0).length()) {
            // this means it is partial
            entryToAdd += "p";
        }
        return result;
    }

    private void submitWord(String word) {
        currScore = 0;
        // start reading the word from left to right
        char firstCharacter = word.charAt(0);
        // check whether formatted answer dict contains the first character or not
        Log.d(LOG_TAG, Arrays.toString(new List[]{formattedAnswers}));
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
        currentEntry = word;
        word = word.substring(1, word.length()); // take out first character
        for (int i = 0; i < word.length(); i++) {
            char currentCharacter = word.charAt(i);
            for (int j = 0; j < possibleMatches.size(); j++) {
                if (possibleMatches.get(j).getLeft().equals(String.valueOf(currentCharacter))) {
                    fillBlueBox(possibleMatches.get(j).getRight(), currentCharacter, 0);
                }
            }
        }
        currScore = countFilledBoxes() * 5;
        Log.e(LOG_TAG, String.valueOf(currScore));
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

    private void fillBlueBox(int index, char characterToFill, int mode) {
        Log.d(LOG_TAG, "Index: " + index);
        TextView tv_characterBox = (TextView) rootLayout.findViewWithTag(index);
        tv_characterBox.setText(String.valueOf(characterToFill));
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
            assert inflater != null;
            View child = inflater.inflate(R.layout.item_character_box, ll_characterBoxWrapper, false);
            child.setTag(i); // so that the position of each character is tracked
            ll_characterBoxWrapper.removeView(child);
            ll_characterBoxWrapper.addView(child);
        }
    }


    // Show the English translation of the current Theme.
    public void showTooltip(View view) {

    }


}
