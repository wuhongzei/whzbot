package org.example.whzbot.data.game;

/**
 * Interface for rules. Rules are used to create variants for a game.
 * Notice that a game tree should be build up of same game with identical rules.
 */
public interface IRule {
    /**
     * Determine whether this and other are identical.
     * Even if this exists, it is better to use one rule object all the time.
     *
     * @param other another rule object.
     * @return true if other is identical to this.
     */
    boolean equal(IRule other);

    /**
     * Determine whether two rules are compatible.
     * Sometime rules maybe different, but they work in the same way.
     * e.g. The same game rules with different initial state.
     *
     * @param other another rule object.
     * @return true if other is compatible with this.
     */
    boolean compatible(IRule other);
}
