package com.tuzhan;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by Al Cheong on 13/11/2017.
 * This class is is to manage the fragments for the ViewPager in the Gameplay UI.
 */

public class GamePagerAdapter extends FragmentStatePagerAdapter {

    int numberOfPages;

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
    public int getCount() {
        return numberOfPages;
    }
}
