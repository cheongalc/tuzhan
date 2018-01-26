package com.tuzhan;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dhaulagiri on 2/12/2017.
 */

public class QuestionCard extends DatabaseModel implements Parcelable {

    private static final String LOG_TAG = "QUESTIONCARD";

    Integer id;
    String theme;
    URL imageURL;
    List<String> answers = new ArrayList<>();
    List<String> harderAnswers = new ArrayList<>();
    String credit;

    public QuestionCard(Cursor dbCursor){
        init(dbCursor.getString(1), dbCursor.getInt(0), dbCursor.getString(2), dbCursor.getString(3), dbCursor.getString(4));
    }

    public QuestionCard(DataSnapshot snapshot, String theme, Integer id){
        init(theme, id, snapshot.child("imageURL").getValue().toString(), snapshot.child("answersRaw").getValue().toString(), snapshot.child("credit").getValue().toString());
    }


    protected QuestionCard(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        theme = in.readString();
        answers = in.createStringArrayList();
        harderAnswers = in.createStringArrayList();
        credit = in.readString();
    }

    public static final Creator<QuestionCard> CREATOR = new Creator<QuestionCard>() {
        @Override
        public QuestionCard createFromParcel(Parcel in) {
            return new QuestionCard(in);
        }

        @Override
        public QuestionCard[] newArray(int size) {
            return new QuestionCard[size];
        }
    };

    private void init(String theme, Integer id, String imageURL, String answersRaw, String credit){
        this.tableName = "CARDS";
        String[] anss = answersRaw.split(";");
        this.id = id;
        this.theme = theme;
        try {
            this.imageURL = new URL(imageURL);
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
        values2Update.put("imageURL", imageURL.toString());
        values2Update.put("answersRaw", Utils.concatenate(answers)+";"+Utils.concatenate(harderAnswers));
        values2Update.put("credit", credit);
        return values2Update;
    }


    // (debug only) will be deprecated
    // if using this constructor, remember to add card to themeToCards in DataSource.shared
    @Deprecated
    public QuestionCard(Integer id, String theme, URL imageURL, String answers, String harderAnswers, String credit){
        this.id = id;
        this.theme = theme;
        this.imageURL = imageURL;
        this.credit = credit;
        this.answers = Utils.split(answers);
        this.harderAnswers = Utils.split(harderAnswers);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        String finalURL = imageURL + "";
        dest.writeString(theme);
        dest.writeStringList(answers);
        dest.writeStringList(harderAnswers);
        dest.writeString(credit);
        dest.writeString(finalURL);
    }
}
