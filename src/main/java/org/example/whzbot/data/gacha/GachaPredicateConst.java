package org.example.whzbot.data.gacha;

public class GachaPredicateConst implements GachaPredicate {
    double prob;

    public GachaPredicateConst(double prob_in) {
        this.prob = prob_in;
    }

    @Override
    public double getProb(double a, double b) {
        return prob;
    }
}
