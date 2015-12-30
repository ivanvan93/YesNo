package com.example.ivanivan0002.yesno;

/**
 * Created by ivanivan0002 on 17/11/15.
 */
public class Question {

    private String question;
    private int noResult;
    private int yesResult;

    public Question(int noResult, String question , int yesResult){
        this.question = question;
        this.noResult = noResult;
        this.yesResult = yesResult;
    }

    public String getQuestion(){
        return question;
    }

    public int getNoResult(){
        return noResult;
    }

    public int getYesResult(){
        return yesResult;
    }
}
