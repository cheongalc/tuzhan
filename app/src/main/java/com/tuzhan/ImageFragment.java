package com.tuzhan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Al Cheong on 13/11/2017.
 * This class holds the layout for each image.
 */

public class ImageFragment extends Fragment {

    public RelativeLayout rootLayout;

    private int imageIndex;

    private static final String LOG_TAG = "IMAGEFRAGMENT";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageIndex = getArguments().getInt("imageIndex");
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

        // Pull data from JSON
        String jsonOutput = pullJSONData(getContext());
        // Parse the data from JSON String
        try {
            JSONObject jsonObject = new JSONObject(jsonOutput);

            String imageUri = jsonObject.getString("image_uri"); // uri to pull the image from @drawable
            String imageCredit = jsonObject.getString("credit"); // text to fill in the image credits
            String unformattedAnswers = jsonObject.getString("answers"); // the image will be tagged with this string.

            ImageView iv_gameplayImage = (ImageView) rootLayout.findViewById(R.id.iv_gameplayImage);
            TextView tv_imageCredits = (TextView) rootLayout.findViewById(R.id.tv_imageCredits);

            Drawable finalImage = pullImage(imageUri, getContext());
            if (finalImage != null) {
                iv_gameplayImage.setImageDrawable(finalImage);
                iv_gameplayImage.setTag(unformattedAnswers);
                tv_imageCredits.setText(imageCredit);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }

        try {
            GameFragmentInterface gfi = (GameFragmentInterface) getActivity();
            gfi.onImageFragmentCreated(rootLayout);
        } catch (ClassCastException e) {
            Log.e("ERROR", String.valueOf(getActivity()) + " must implement GameFragmentInterface");
        }
    }

    private Drawable pullImage(String imageUri, Context context) {
        Uri uri = Uri.parse(imageUri);
        Drawable output = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            output = Drawable.createFromStream(inputStream, uri.toString());
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return output;
    }

    private String pullJSONData(Context context) {
        String jsonFileName = "card" + String.valueOf(imageIndex) + ".json";
        String jsonOutput = null;
        try {
            InputStream inputStream = context.getAssets().open("data/" + jsonFileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonOutput = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, jsonOutput);
        return jsonOutput;
    }

    public static ImageFragment newInstance(int imageIndex) {

        Bundle args = new Bundle();
        args.putInt("imageIndex", imageIndex + 1);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }
}

