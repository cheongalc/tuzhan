package com.tuzhan;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dhaulagiri on 7/12/2017.
 */

public class MatchDetailsAdapter extends ArrayAdapter<String> {

    private List<String> match_ids;
    private List<MatchDetails> matchDetailsList;
    private Boolean isNewMatch = false;

    MatchDetailsAdapter(@NonNull Context context, List<String> match_ids, List<MatchDetails> matchDetailsList, Boolean isNewMatch) {
        super(context, 0, match_ids);

        this.match_ids = match_ids;
        this.matchDetailsList = matchDetailsList;
        this.isNewMatch = isNewMatch;
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

        switch (outcome) {
            case "1":
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentGreen));
                civOutcome.setImageResource(R.mipmap.tuzhan_win);
                break;
            case "0":
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentRed));
                civOutcome.setImageResource(R.mipmap.tuzhan_lost);
                break;
            case "2":
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorPrimary));
                civOutcome.setImageResource(R.mipmap.tuzhan_draw);
                break;
            case "dnf":
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorDNFGrey));
                civOutcome.setImageResource(R.mipmap.tuzhan_dnf);
                break;
            default:
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentYellow));
                civOutcome.setImageResource(R.mipmap.tuzhan_vs);
                break;
        }

        // set an onClick listener for the list item.

        convertView.setOnClickListener(v -> {
            if (!isNewMatch) {
                // if the current list item is listing out a PREVIOUS MATCH
                Intent i = new Intent(getContext(), GameFinishedActivity.class);
                i.putExtra(Constants.C_MATCH_ID, matchDetails.match_id);
                i.putExtra(Constants.C_GAMEFINISHED_KEY, Constants.M.START_FROM_MAIN);
                i.putExtra(Constants.C_OPPONENT_DPURL, opponent.dpURL);
                getContext().startActivity(i);
            } else {
                // if the current list item is listing out a CHALLENGE

                //retrieve self info
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                User self = DataSource.shared.userWithParameters(currentUser.getUid(), currentUser.getPhotoUrl().toString(), currentUser.getDisplayName(), currentUser.getEmail());

                Intent i = new Intent(getContext(), CountdownActivity.class);
                i.putExtra(Constants.C_USER_OPPONENT, opponent);
                i.putExtra(Constants.C_THEME, topic);
                i.putExtra(Constants.C_USER_SELF, self);
                i.putExtra(Constants.C_MATCH_ID, matchDetails.match_id);

                //retrieve card ids of the current match under /Matches/matchID/cardIds
                FirebaseDatabase.getInstance().getReference().child(Constants.F_MATCHES).child(matchDetails.match_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String cardIds = dataSnapshot.child(Constants.F_MATCHES_CARDIDS).getValue()+"";
                        i.putExtra(Constants.C_CARD_IDS_STRING, cardIds);
                        getContext().startActivity(i);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            }
        });

        Picasso.with(getContext()).load(opponent.dpURL).into(civOpponentDp);

        tvOpponentName.setText(opponent.displayname);
        tvTopic.setText(topic);


        return convertView;
    }
}
