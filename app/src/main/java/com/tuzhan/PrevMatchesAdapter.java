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

public class PrevMatchesAdapter extends ArrayAdapter<String> {

    private List<String> match_ids;
    private List<MatchDetails> matchDetailsList;
    private Boolean isNewMacth = false;

    PrevMatchesAdapter(@NonNull Context context, List<String> match_ids, List<MatchDetails> matchDetailsList, Boolean isNewMatch) {
        super(context, 0, match_ids);

        this.match_ids = match_ids;
        this.matchDetailsList = matchDetailsList;
        this.isNewMacth = isNewMatch;
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
//        CardView cvMatch = (CardView) convertView.findViewById(R.id.cvMatch);

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
//                cvMatch.setBackground(getContext().getResources().getDrawable(R.drawable.challenge_list_background));
                civOutcome.setBorderColor(getContext().getResources().getColor(R.color.colorAccentYellow));
                civOutcome.setImageResource(R.mipmap.tuzhan_vs);
                break;
        }

        convertView.setOnClickListener(v -> {
            if(!isNewMacth) {
                Intent intent = new Intent(getContext(), GameFinishedActivity.class);
                intent.putExtra("matchId", matchDetails.match_id);
                intent.putExtra("isMatchFinished", true);
                intent.putExtra("opp_dpURL", opponent.dpURL);
                getContext().startActivity(intent);
            }else{
                //retrieve user info
                FirebaseUser curuser = FirebaseAuth.getInstance().getCurrentUser();
                User user = new User(curuser.getDisplayName(), curuser.getEmail(), curuser.getUid(), curuser.getPhotoUrl().toString());

                Intent intent = new Intent(getContext(), CountdownActivity.class);
                intent.putExtra("opp", opponent);
                intent.putExtra("theme", topic);
                intent.putExtra("user", user);

                //retrieve cardIds
                FirebaseDatabase.getInstance().getReference().child("Matches").child(matchDetails.match_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String cardIds = dataSnapshot.child("cardIds").getValue()+"";
                        intent.putExtra("cardIds", cardIds);
                        getContext().startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        Picasso.with(getContext()).load(opponent.dpURL).into(civOpponentDp);

        tvOpponentName.setText(opponent.displayname);
        tvTopic.setText(topic);


        return convertView;
    }
}
