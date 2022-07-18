package org.example.whzbot.data.game;

import java.util.UUID;

import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

public abstract class GeneralChessMatch<
        GeneralChess extends IGame, Rule extends IRule
        > implements IMatch<IGame> {
    protected GeneralChess board;
    protected Rule rule;
    protected long[] players;
    protected boolean flip_order = false;
    protected int game_state = 0; // 0: waiting, 1:running, 2:ended.
    protected UUID uuid;

    public GeneralChessMatch() {
        this.players = new long[2];
    }

    public GeneralChessMatch(Rule rule_in) {
        this();
        this.rule = rule_in;
    }

    public boolean join(long id) {
        if (this.players[0] == 0) {
            this.players[0] = id;
            return true;
        }
        if (this.players[1] == 0) {
            this.players[1] = id;
            return true;
        } else
            return false;
    }

    /**
     * @param id the user to quit.
     * @return whether or not any one is waiting in game.
     */
    public boolean quit(long id) {
        if (this.players[0] == id) {
            this.players[0] = this.players[1];
            this.players[1] = 0;
            return this.players[0] == 0;
        } else if (this.players[1] == id) {
            this.players[1] = 0;
            return false;
        } else {
            return false;
        }
    }

    @Override
    public int getPhase() {
        return this.game_state;
    }

    public boolean begin() {
        if (this.game_state != 0)
            return false;
        return this.players[0] != 0 && this.players[1] != 0;
    }

    @Override
    public long[] getPlayers() {
        return this.players;
    }

    @Override
    public long getNextPlayer() {
        return this.players[
                (this.board.getTurn() - 1 == 0) == this.flip_order ? 1 : 0
                ];
    }

    @Override
    public boolean move(String input) {
        if (this.game_state != 1 || !this.board.canMove(input))
            return false;
        //noinspection unchecked
        this.board = (GeneralChess) this.board.doMove(input);
        if (this.board.isEnd())
            this.game_state = 2;
        return true;
    }

    @Override
    public GeneralChess getBoard() {
        return this.board;
    }

    @Override
    public void assignUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getBoard(int display_mode) {
        return this.board.toString();
    }

    public float getResult(long id) {
        if (this.game_state != 2)
            return -1;
        if (id == this.players[0])
            return this.board.getScore(this.flip_order ? 2 : 1);
        else if (id == this.players[1])
            return this.board.getScore(this.flip_order ? 1 : 2);
        return -1;
    }

    @Override
    public String[] getHistory(int record_mode) {
        return new String[]{"game.history_unavailable"};
    }

    @Override
    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();
        rtn.add(new JsonStringNode("history", ""));
        JsonNode temp = this.board.toJson();
        temp.setName("state");
        rtn.add(temp);
        temp = new JsonListNode("players");
        rtn.add(temp);
        return rtn;
    }

    @Override
    public String[] hint() {
        return this.board.moves();
    }
}
