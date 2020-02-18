package de.lmu.cis.ocrd.calamari;

import java.util.ArrayList;
import java.util.List;

public class Prediction {
    private List<Position> positions = new ArrayList<>();
    private String id = "";
    private String sentence = "";
    private String linePath = "";
    private double avgCharProbability = 0;
    private boolean isVotedResult = false;

    public List<Position> getPositions() {
        return positions;
    }

    public String getId() {
        return id;
    }

    public String getSentence() {
        return sentence;
    }

    public String getLinePath() {
        return linePath;
    }

    public double getAvgCharProbability() {
        return avgCharProbability;
    }

    public boolean isVotedResult() {
        return isVotedResult;
    }
}
