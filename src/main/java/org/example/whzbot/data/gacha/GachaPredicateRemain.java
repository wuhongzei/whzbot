package org.example.whzbot.data.gacha;

public class GachaPredicateRemain implements GachaPredicate{
    GachaPredicateRemain() {}

    @Override
    public double getProb(double a, double b) {
        return 1 - b;
    }
}
