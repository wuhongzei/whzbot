package org.example.whzbot.data.game.Chess;

import org.example.whzbot.data.game.IRule;

public class ChessRule implements IRule {
    public static final byte[][] DEFAULT_STATE = {
            new byte[] {
                    'P', 8, 9, 10, 11, 12, 13,14,15, 'R', 0, 7,
                    'N', 1, 6, 'B', 2, 5, 'Q', 3, 'K', 4,
                    'p', 48, 49, 50, 51, 52, 53, 54, 55, 'r', 56, 63,
                    'n', 57, 62, 'b', 58, 61, 'q', 59, 'k', 60
            },
            new byte[] {}
    } ;
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
