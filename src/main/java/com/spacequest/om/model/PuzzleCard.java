package com.spacequest.om.model;
import java.util.List;

public class PuzzleCard {
    public String question;
    public String correctAnswer;
    public List<String> answerOptions;
    public String explanation;
    public int timeSeconds;

    public PuzzleCard(String q, String correct, List<String> options, String expl, int t) {
        this.question = q;
        this.correctAnswer = correct;
        this.answerOptions = options;
        this.explanation = expl;
        this.timeSeconds = t;
    }
}
