package com.tuzhan;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chenchangheng on 10/5/17.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    // Database Meta Info
    public static int DATABASE_VERSION = 7;
    public static String DATABASE_NAME = "Records.db";

    // Database init
    public static final String CREATE_MATCHES_TABLE = "create table MATCHES(" +
            "id text primary key,"  +
            "topic text,"           +
            "cardIds text,"         +
            "emailOpp text,"        +
            "winnerEmail text,"     +
            "scoreSelf integer,"    +
            "scoreOpp integer,"     +
            "timeSelf double,"      +
            "timeOpp double,"       +
            "entriesSelf text,"     +
            "entriesOpp text,"      +
            "scoresSelf text,"      +
            "scoresOpp text"        +
            ");";
    public static final String CREATE_CARDS_TABLE = "create table CARDS(" +
            "id integer,"                    +
            "theme text,"                    +
            "imageURL text primary key,"     +
            "answersRaw text,"               +
            "credit text"                    +
            ");";

    public static final String DATABASE_DESTROY = "drop table if exists MATCHES;";
    public static final String DATABASE_DESTROY2 = "drop table if exists CARDS;";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_MATCHES_TABLE);
        database.execSQL(CREATE_CARDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DATABASE_DESTROY);
        db.execSQL(DATABASE_DESTROY2);
        onCreate(db);
    }

}
