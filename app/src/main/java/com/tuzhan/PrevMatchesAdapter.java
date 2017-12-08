package com.tuzhan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dhaulagiri on 7/12/2017.
 */

public class PrevMatchesAdapter extends ArrayAdapter<String> {

    List<String> match_ids;
    List<MatchDetails> matchDetailsList;

    public PrevMatchesAdapter(@NonNull Context context, List<String> match_ids, List<MatchDetails> matchDetailsList) {
        super(context, 0, match_ids);

        this.match_ids = match_ids;
        this.matchDetailsList = matchDetailsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_prev_match, parent, false);
        }

        MatchDetails matchDetails = matchDetailsList.get(position);
        String outcome = matchDetails.outcome;
        User opponent = matchDetails.opponent;
        String topic = matchDetails.topic;

        TextView tvOpponentName = (TextView) convertView.findViewById(R.id.tv_opponent_name);
        TextView tvTopic = (TextView) convertView.findViewById(R.id.tv_topic);

        CircleImageView civOpponentDp = (CircleImageView) convertView.findViewById(R.id.civ_opponent);
        CircleImageView civOutcome = (CircleImageView) convertView.findViewById(R.id.civ_outcome);

        if(outcome.equals("1")){
            civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentGreen));
            civOutcome.setImageResource(R.mipmap.tuzhan_win);
        }else if(outcome.equals("0")){
            civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentRed));
            civOutcome.setImageResource(R.mipmap.tuzhan_lost);
        }else{
            civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorPrimary));
            civOutcome.setImageResource(R.mipmap.tuzhan_draw);
        }

        Picasso.with(getContext()).load(opponent.dpURL).into(civOpponentDp);

        tvOpponentName.setText(opponent.displayname);
        tvTopic.setText(topic);


        return convertView;
    }
}
