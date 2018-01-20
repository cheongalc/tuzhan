package com.tuzhan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Al Cheong on 13/11/2017.
 * This class holds the layout for each image.
 */

public class ImageFragment extends Fragment {

    public RelativeLayout rootLayout;

    private int imageIndex;
    private QuestionCard questionCard;

    private static final String LOG_TAG = "IMAGEFRAGMENT";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageIndex = getArguments().getInt("imageIndex");
        questionCard = getArguments().getParcelable("questionCard");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_image, container, false);
        rootLayout = (RelativeLayout) viewGroup.findViewById(R.id.rl_imageContainer);
        return viewGroup;
    }

    // We try to post the relative layout we got from onCreateView to our interface
    // This is such that we can access the views inside the fragment later on.

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Parse the current Question Card
        URL imageURL = questionCard.imageURL;
        String credit = questionCard.credit;

        ImageView iv_gameplayImage = (ImageView) rootLayout.findViewById(R.id.iv_gameplayImage);
        TextView tv_imageCredits = (TextView) rootLayout.findViewById(R.id.tv_imageCredits);


        Picasso.with(getContext()).load(imageURL+"").into(iv_gameplayImage);
        tv_imageCredits.setText(credit);

        try {
            GameFragmentInterface gfi = (GameFragmentInterface) getActivity();
            gfi.onImageFragmentCreated(rootLayout);
        } catch (ClassCastException e) {
            Log.e("ERROR", String.valueOf(getActivity()) + " must implement GameFragmentInterface");
        }
    }

    public static ImageFragment newInstance(int imageIndex, QuestionCard questionCard) {
        Bundle args = new Bundle();
        args.putInt("imageIndex", imageIndex + 1);
        args.putParcelable("questionCard", questionCard);


        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }
}

