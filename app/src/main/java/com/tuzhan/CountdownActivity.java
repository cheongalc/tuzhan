package com.tuzhan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.constraint.solver.Cache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CountdownActivity extends AppCompatActivity {

    private static final String LOG_TAG = "COUNTDOWNACTIVITY";

    CircleImageView civ_selfDP, civ_opponentDP;
    CircleImageView civ_centerCountDown;

    TextView tv_selfName, tv_opponentName;
    String cardIDsString, theme, matchID;

    int cardSize = 0;

    ArrayList<QuestionCard> questionCardArrayList = new ArrayList<>();
    List<Integer> cardIDsList;

    public static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    public static int maxCacheSize = maxMemory / 4;
    public static LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(maxCacheSize){

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }

    };


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

        Log.d("cards_for_match", cardIDsString);

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

        MainActivity.readText("看图片，输入汉语拼音!");
        // init the countdown timer
        countDownTimer = new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) millisUntilFinished/1000;
                civ_centerCountDown.setImageResource(countDownNumbers[secondsRemaining]);
            }

            public void onFinish() {
                i.putExtra(Constants.C_CARD_IDS_STRING, cardIDsString);
                i.putExtra(Constants.C_MATCH_ID, matchID);
                i.putExtra(Constants.C_THEME, theme);
                i.putExtra(Constants.C_OPPONENT_DPURL, opponent.dpURL);
                i.putExtra(Constants.C_USER_OPPONENT, opponent);
                Constants.M.questionCardArrayList = questionCardArrayList;
                startActivity(i);
            }
        };

        retrieveMaterials();
    }

    private void retrieveMaterials() {
        //populate questionCardArrayList...
        cardIDsList = Utils.splitToInts(cardIDsString);

        DataSource.shared.fetchCards(theme, cardIDsList, qCardList -> {
            if(qCardList != null) {
                questionCardArrayList.addAll(qCardList);

                for(QuestionCard card : questionCardArrayList){
                    new DownloadImage().execute(card.imageURL);
                }

            }
        });


    }

    private void beginCountDown(){
        countDownTimer.start();
        Log.e(LOG_TAG, "countdown started");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    private class DownloadImage extends AsyncTask<URL,Integer, Bitmap>{


        @Override
        protected Bitmap doInBackground(URL... params) {
            URL image = params[0];

            if(image != null) {

                if (GetBitMapFromCache(image.toString()) == null) {
                    try {
                        Bitmap feteched = BitmapFactory.decodeStream(image.openStream());
                        if(feteched!= null) {
                            Cache(image.toString(), feteched);
                            return BitmapFactory.decodeStream(image.openStream());
                        }else return  null;

                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    return GetBitMapFromCache(image.toString());
                }
            }else return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            cardSize++;
            if(cardSize == cardIDsList.size()){
                beginCountDown();
            }
            super.onPostExecute(bitmap);
        }

    }

    private void Cache(String key, Bitmap bitmap){
        if ( GetBitMapFromCache(key) == null) cache.put(key, bitmap);
    }

    public static Bitmap GetBitMapFromCache(String key){
        return cache.get(key);
    }

//    private class MyParams{
//
//        URL image;
//        DataFetchedCallback callback;
//
//        public MyParams(URL image, DataFetchedCallback callback) {
//            this.image = image;
//            this.callback = callback;
//        }
//    }

}
