package com.acwchineseapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Al Cheong on 13/11/2017.
 * This class is is to manage the fragments for the ViewPager in the Gameplay UI.
 */

public class GamePagerAdapter extends FragmentStatePagerAdapter {

    int numberOfPages;

    public GamePagerAdapter(FragmentManager fm, int numberOfPages) {
        super(fm);
        this.numberOfPages = numberOfPages;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }
}
