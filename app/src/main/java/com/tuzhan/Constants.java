package com.tuzhan;

import java.util.ArrayList;

/**
 * Created by Al Cheong on 20/01/2018.
 */

public class Constants {

    // Constants for intent keys

    public static final String C_QUESTION_CARD_LIST = "C_QUESTION_CARD_LIST";
    public static final String C_CARD_IDS_LIST = "C_CARD_IDS_LIST";
    public static final String C_CARD_IDS_STRING = "C_CARD_IDS_STRING";
    public static final String C_MATCH_ID = "C_MATCH_ID";
    public static final String C_THEME = "C_THEME";
    public static final String C_OPPONENT_DPURL = "C_OPPONENT_DPURL";
    public static final String C_USER_SELF = "C_USER_SELF";
    public static final String C_USER_OPPONENT = "C_USER_OPPONENT";
    public static final String C_PLAYER_ENTRIES_LIST = "C_PLAYER_ENTRIES_LIST";
    public static final String C_SCORE_SELF_LIST = "C_SCORE_SELF_LIST";
    public static final String C_GAMEFINISHED_KEY = "C_GAMEFINISHED_KEY";
    public static final String C_OPPONENT_EMAIL = "C_OPPONENT_EMAIL";
    public static final String C_SCORE_SELF = "C_SCORE_SELF";
    public static final String C_TIME_SELF = "C_TIME_SELF";

    // Constants for firebase dictionary names

    public static final String F_CARDS = "Cards";
    public static final String F_MATCHES = "Matches";
        public static final String F_MATCHES_CARDIDS = "cardIds";
        public static final String F_MATCHES_PLAYERS = "players";
        public static final String F_MATCHES_PLAYERS_STATE = "state";
        public static final String F_MATCHES_THEME = "theme";
    public static final String F_USERS = "Users";
        public static final String F_USERS_DISPLAYNAME = "displayname";
        public static final String F_USERS_DPURL = "dpURL";
        public static final String F_USERS_EMAIL = "email";
        public static final String F_USERS_MATCHES = "matches";
        public static final String F_USERS_USERID = "userId";
    public static final String F_USERSSTATES = "UsersStates";


    // Constants for SQLiteDatabase fields

    public static final String SQL_MATCHES = "MATCHES";
        public static final String SQL_MATCHES_ID = "id";
        public static final String SQL_MATCHES_TOPIC = "topic";
        public static final String SQL_MATCHES_CARDIDS = "cardIds";
        public static final String SQL_MATCHES_EMAILOPP = "emailOpp";
        public static final String SQL_MATCHES_WINNEREMAIL = "winnerEmail";
        public static final String SQL_MATCHES_SCORESELF = "scoreSelf";
        public static final String SQL_MATCHES_SCOREOPP = "scoreOpp";
        public static final String SQL_MATCHES_TIMESELF = "timeSelf";
        public static final String SQL_MATCHES_TIMEOPP = "timeOpp";
        public static final String SQL_MATCHES_ENTRIESSELF = "entriesSelf";
        public static final String SQL_MATCHES_ENTRIESOPP = "entriesOpp";
        public static final String SQL_MATCHES_SCORESSELF = "scoresSelf";
        public static final String SQL_MATCHES_SCORESOPP = "scoresOpp";
    public static final String SQL_CARDS = "CARDS";
        public static final String SQL_CARDS_ID = "id";
        public static final String SQL_CARDS_THEME = "theme";
        public static final String SQL_CARDS_IMAGEURL = "imageURL";
        public static final String SQL_CARDS_ANSWERSRAW = "answersRaw";
        public static final String SQL_CARDS_CREDIT = "credit";
    public static final String SQL_USERS = "USERS";
        public static final String SQL_USERS_ID = "id";
        public static final String SQL_USERS_DPURL = "dpURL";
        public static final String SQL_USERS_DPNAME = "dpName";
        public static final String SQL_USERS_EMAIL = "email";

    // Miscellaneous
    public static class M {
        public static ArrayList<QuestionCard> questionCardArrayList;
        public static final boolean START_FROM_MAIN = true;
        public static final boolean START_FROM_GAMEPLAY = false;
        public static final String AUDIO_POSITION = "AUDIO_POSITION";
    }
}
