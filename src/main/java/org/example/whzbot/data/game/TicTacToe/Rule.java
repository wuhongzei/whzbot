package org.example.whzbot.data.game.TicTacToe;

import org.example.whzbot.data.game.IRule;

public class Rule implements IRule {
    public int extra_rule = 0;
    public int init_state = 0;
    public int undo_times = 0;
    public int first_move = 0;

    @Override
    public boolean equal(IRule other) {
        return other == this;
    }

    @Override
    public boolean compatible(IRule other) {
        return true;
    }
}
