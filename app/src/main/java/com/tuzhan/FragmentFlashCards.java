package com.tuzhan;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;


public class FragmentFlashCards extends Fragment {


    public FragmentFlashCards() {
        // Required empty public constructor
    }

    ProgressBar pbFlashCardProgress;
    ImageButton bCross, bTick;
    NonSwipeableViewPager vpFlashCards;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_fragment_flash_cards, container, false);

        pbFlashCardProgress = (ProgressBar) rootview.findViewById(R.id.pb_flash_card_progress);
        bCross = (ImageButton) rootview.findViewById(R.id.bCross);
        bTick = (ImageButton) rootview.findViewById(R.id.bTick);
        vpFlashCards = (NonSwipeableViewPager) rootview.findViewById(R.id.vp_flash_cards);

        pbFlashCardProgress.setProgress(0);

        //TODO configure flash cards view pager

        return rootview;
    }

}
