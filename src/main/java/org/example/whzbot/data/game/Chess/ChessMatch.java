package org.example.whzbot.data.game.Chess;

import java.util.Random;

import org.example.whzbot.data.game.GeneralChessMatch;
import org.example.whzbot.data.game.IRule;
import org.example.whzbot.storage.json.JsonObjectNode;

public class ChessMatch extends GeneralChessMatch<Chess, ChessRule> {
    public ChessMatch() {
        super(new ChessRule());
    }

    public ChessMatch(ChessRule rule) {
        super(rule);
    }

    @Override
    public boolean begin() {
        if (super.begin()) {
            if (this.rule.first_move == 0)
                this.flip_order = new Random().nextBoolean();
            else
                this.flip_order = this.rule.first_move % 2 == 0;
            this.board = new Chess(this.rule);
            this.game_state = 1;
            return true;
        }
        return false;
    }

    @Override
    public void fromJson(JsonObjectNode root) {

    }

    public static ChessMatch make(IRule rule) {
        if (rule instanceof ChessRule) {
            return new ChessMatch((ChessRule)rule);
        }
        return new ChessMatch();
    }
}
