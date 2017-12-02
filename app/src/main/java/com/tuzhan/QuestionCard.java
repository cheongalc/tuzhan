package com.tuzhan;
import android.support.annotation.Nullable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dhaulagiri on 2/12/2017.
 */

public class QuestionCard {

    URL image_url;
    String theme;
    String credit;
    List<String> answers;
    List<String> harderAnswers;

    public QuestionCard(){}

    public QuestionCard(String theme, URL image_url, @Nullable String credit, String answers, @Nullable String harderAnswers){
        this.theme = theme;
        this.image_url = image_url;
        this.credit = credit;

        this.answers = Arrays.asList(answers.split("-"));
        if(harderAnswers != null) {
            this.harderAnswers = Arrays.asList(harderAnswers.split("-"));
        }else this.harderAnswers = new ArrayList<>();
    }
}
