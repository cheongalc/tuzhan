package com.tuzhan;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dhaulagiri on 2/12/2017.
 */

public class QuestionCard extends DB_DataModel {

    Integer id;
    String theme;
    URL image_url;
    List<String> answers = new ArrayList<>();
    List<String> harderAnswers = new ArrayList<>();
    String credit;

    public QuestionCard(Cursor dbCursor){
        init(dbCursor.getString(1), dbCursor.getInt(0), dbCursor.getString(2), dbCursor.getString(3), dbCursor.getString(4));
    }

    public QuestionCard(DataSnapshot snapshot, String theme, Integer id){
        init(theme, id, snapshot.child("imageURL").getValue().toString(), snapshot.child("answersRaw").getValue().toString(), snapshot.child("credit").getValue().toString());
    }

    private void init(String theme, Integer id, String image_url , String answersRaw, String credit){
        this.tableName = "CARDS";
        String[] anss = answersRaw.split(";");
        this.id = id;
        this.theme = theme;
        try {
            this.image_url = new URL(image_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.credit = credit;
        this.answers = Utils.split(anss[0]);
        if(anss.length>1) {
            this.harderAnswers = Utils.split(anss[1]);
        }
    }

    @Override
    public String selector() {
        return "theme='" + theme + "' AND id=" + id;
    }

    @Override
    public ContentValues composeUpdateValues() {
        ContentValues values2Update = new ContentValues();
        values2Update.put("id", id);
        values2Update.put("theme", theme);
        values2Update.put("imageURL", image_url.toString());
        values2Update.put("answersRaw", Utils.concatenate(answers)+";"+Utils.concatenate(harderAnswers));
        values2Update.put("credit", credit);
        return values2Update;
    }


    // (debug only) will be deprecated
    // if using this constructor, remember to add card to themeToCards in DataSource.shared
    public QuestionCard(Integer id, String theme, URL image_url , String answers, String harderAnswers, String credit){
        this.id = id;
        this.theme = theme;
        this.image_url = image_url;
        this.credit = credit;
        this.answers = Utils.split(answers);
        this.harderAnswers = Utils.split(harderAnswers);
    }

}
