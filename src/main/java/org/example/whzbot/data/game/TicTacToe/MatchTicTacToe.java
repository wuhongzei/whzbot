package org.example.whzbot.data.game.TicTacToe;

import java.util.Random;

import org.example.whzbot.data.game.GeneralChessMatch;
import org.example.whzbot.data.game.IRule;
import org.example.whzbot.storage.json.JsonObjectNode;

public class MatchTicTacToe extends GeneralChessMatch<TicTacToe, Rule> {
    public MatchTicTacToe() {
        super(new Rule());
    }

    public boolean begin() {
        if (super.begin()) {
            if (this.rule.first_move == 0)
                this.flip_order = new Random().nextBoolean();
            else
                this.flip_order = this.rule.first_move % 2 == 0;
            this.board = new TicTacToe(this.rule.init_state, this.rule);
            this.game_state = 1;
            return true;
        }
        return false;
    }

    @Override
    public void fromJson(JsonObjectNode root) {

    }

    public static MatchTicTacToe make(IRule rule) {
        return new MatchTicTacToe();
    }
}
