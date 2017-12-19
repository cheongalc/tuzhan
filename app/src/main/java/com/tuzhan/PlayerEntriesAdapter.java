package com.tuzhan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Dhaulagiri on 18/12/2017.
 */

public class PlayerEntriesAdapter extends ArrayAdapter<String> {

    private List<String> entires;
    private List<Integer> scores;

    public PlayerEntriesAdapter(@NonNull Context context, List<String> entries, List<Integer> scores) {
        super(context, 0, entries);
        this.entires = entries;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_entries_item, parent, false);
        }

        String entry = entires.get(position);
        int score = scores.get(position);

        TextView tvEntry = (TextView) convertView.findViewById(R.id.tv_entry);
        TextView tvScore = (TextView) convertView.findViewById(R.id.tv_score);
        CardView cvScore = (CardView) convertView.findViewById(R.id.cv_score);

        tvEntry.setText(entry);

        tvScore.setText("+" + score);


        if(score == 0) cvScore.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorAccentRed));
        else if(score == 1) cvScore.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorAccentGreen));
        else if(score == 2) cvScore.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
        else cvScore.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorAccentYellow));

        return convertView;
    }
}
