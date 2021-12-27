package org.example.whzbot.data.gacha;

public class GachaPredicateKinked implements GachaPredicate {
    double base;
    int kink;
    double step;

    public GachaPredicateKinked(double base_in, int kink_in, double step_in) {
        this.base = base_in;
        this.kink = kink_in;
        this.step = step_in;
    }

    @Override
    public double getProb(double a, double b) {
        return a < this.kink ? base : base + (a - kink + 1) * step;
    }
}
