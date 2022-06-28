package org.example.whzbot.data.game.Nylium;

import java.util.Random;

import org.example.whzbot.data.game.GeneralChessMatch;
import org.example.whzbot.data.game.IRule;
import org.example.whzbot.storage.json.JsonObjectNode;

public class NyliumMatch extends GeneralChessMatch<NyliumChess, NyliumRule> {
    public NyliumMatch() {
        super(new NyliumRule());
    }

    public NyliumMatch(NyliumRule rule_in) {
        super(rule_in);
    }

    @Override
    public boolean begin() {
        if (super.begin()) {
            if (this.rule.first_move == 0)
                this.flip_order = new Random().nextBoolean();
            else
                this.flip_order = this.rule.first_move % 2 == 0;
            this.board = new NyliumChess(this.rule);
            this.game_state = 1;
            return true;
        }
        return false;
    }

    @Override
    public void fromJson(JsonObjectNode root) {

    }

    public static NyliumMatch make(IRule rule) {
        if (rule instanceof NyliumRule) {
            return new NyliumMatch((NyliumRule)rule);
        }
        return new NyliumMatch();
    }

}
