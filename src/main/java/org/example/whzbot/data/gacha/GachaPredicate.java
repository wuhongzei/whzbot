package org.example.whzbot.data.gacha;

public interface GachaPredicate {
    /**
     * Get probability of this item.
     * @param a possible parameter given to predicate
     * @param b possible parameter given to predicate
     * @return total probability given.
     */
    double getProb(double a, double b);
}
