package com.tuzhan;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.List;

/**
 * Created by Al Cheong on 13/11/2017.
 * This class is is to manage the fragments for the ViewPager in the Gameplay UI.
 */

public class GamePagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfPages;

    private static final String LOG_TAG = "GAMEPAGERADAPTER";

    List<QuestionCard> questionCardList;

    public GamePagerAdapter(FragmentManager fm, int numberOfPages, List<QuestionCard> questionCardList) {
        super(fm);
        this.numberOfPages = numberOfPages;
        this.questionCardList = questionCardList;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(position, questionCardList.get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }
}
