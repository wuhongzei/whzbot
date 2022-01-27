package org.example.whzbot.data.game.TicTacToe;

import org.example.whzbot.data.game.IGame;
import org.example.whzbot.storage.json.JsonObjectNode;

/**
 * Basic 3x3 game
 * |O|X|O|
 * |X|X|O|
 * |X|O|O|
 * <p>
 * in game id can be 1 / 2
 */
public class TicTacToe implements IGame {
    Rule rule = null;
    int[] board;
    int turn = 0; // 0=p1, 1=p2
    int end = 0; // 0=not finish, 1=p1 won, 2=p2 won, 3=draw.

    public TicTacToe() {
        this.board = new int[9];
    }

    public TicTacToe(Rule rule_in) {
        this();
        this.rule = rule_in;
    }

    public TicTacToe(int identity, Rule rule_in) {
        this(rule_in);
        for (int i = 0; i < 9; i++) {
            this.board[i] = identity % 3;
            identity /= 3;
        }
    }

    public int getTurn() {
        return this.turn + 1;
    }

    @Override
    public TicTacToe doMove(String move) {
        // read move and get location
        int loc = move.charAt(0);
        if (loc > 96)
            loc -= 97;
        else if (loc > 64)
            loc -= 65;
        else if (loc > 48)
            loc -= 49;

        // make new state
        int temp = this.turn + 1;
        TicTacToe rtn = this.clone();
        rtn.board[loc] = temp;
        rtn.turn = 1 - this.turn;

        // determine if game finished.
        if (
                (loc == 0 && (
                        (rtn.board[1] == temp && rtn.board[2] == temp) ||
                                (rtn.board[4] == temp && rtn.board[8] == temp) ||
                                (rtn.board[3] == temp && rtn.board[6] == temp)
                )) ||
                        (loc == 1 && (
                                (rtn.board[0] == temp && rtn.board[2] == temp) ||
                                        (rtn.board[4] == temp && rtn.board[7] == temp)
                        )) ||
                        (loc == 2 && (
                                (rtn.board[0] == temp && rtn.board[1] == temp) ||
                                        (rtn.board[4] == temp && rtn.board[6] == temp) ||
                                        (rtn.board[5] == temp && rtn.board[8] == temp)
                        )) ||
                        (loc == 3 && (
                                (rtn.board[4] == temp && rtn.board[5] == temp) ||
                                        (rtn.board[0] == temp && rtn.board[6] == temp)
                        )) ||
                        (loc == 4 && (
                                (rtn.board[3] == temp && rtn.board[5] == temp) ||
                                        (rtn.board[0] == temp && rtn.board[8] == temp) ||
                                        (rtn.board[2] == temp && rtn.board[6] == temp) ||
                                        (rtn.board[1] == temp && rtn.board[7] == temp)
                        )) ||
                        (loc == 5 && (
                                (rtn.board[3] == temp && rtn.board[4] == temp) ||
                                        (rtn.board[2] == temp && rtn.board[8] == temp)
                        )) ||
                        (loc == 6 && (
                                (rtn.board[7] == temp && rtn.board[8] == temp) ||
                                        (rtn.board[2] == temp && rtn.board[4] == temp) ||
                                        (rtn.board[0] == temp && rtn.board[3] == temp)
                        )) ||
                        (loc == 7 && (
                                (rtn.board[6] == temp && rtn.board[8] == temp) ||
                                        (rtn.board[1] == temp && rtn.board[4] == temp)
                        )) ||
                        (loc == 8 && (
                                (rtn.board[6] == temp && rtn.board[7] == temp) ||
                                        (rtn.board[0] == temp && rtn.board[4] == temp) ||
                                        (rtn.board[2] == temp && rtn.board[5] == temp)
                        ))
        )
            rtn.end = temp;
        if (rtn.moves().length == 0)
            rtn.end = 3;
        return rtn;
    }

    @Override
    public IGame undoMove(String move) {
        return null;
    }

    @Override
    public boolean canMove(String move) {
        if (move.length() != 1)
            return false;
        int loc = move.charAt(0);
        if (loc > 96)
            loc -= 97;
        else if (loc > 64)
            loc -= 65;
        else if (loc > 48)
            loc -= 49;
        else return false;
        return loc < 9 && board[loc] == 0;
    }

    @Override
    public String[] moves() {
        int count = 0;
        for (int i = 0; i < 9; i++)
            count += board[i] == 0 ? 1 : 0;
        String[] rtn = new String[count];
        for (int i = 8; count > 0; i--) {
            if (board[i] == 0)
                rtn[--count] = String.valueOf((char) (i + 97));
        }
        return rtn;
    }

    @Override
    public boolean isEnd() {
        return this.end != 0;
    }

    @Override
    public boolean isWin(int player) {
        return this.end == player;
    }

    /**
     * @implNote Should not be used when this.end==0
     * @implNote p1 - 1 = 0, 2 - end(1) = 1. 0 ^ 1 = 1
     * @implNote p1 - 1 = 0, 2 - end(2) = 0. 0 ^ 1 = 0
     * @implNote p2 - 1 = 1, 2 - end(1) = 1. 1 ^ 1 = 0
     * @implNote p2 - 1 = 1, 2 - end(2) = 0. 1 ^ 0 = 1 //in case I forgot.
     */
    @Override
    public float getScore(int player) {
        return this.end == 3 ? 0.5f : (2 - this.end) ^ (player - 1);
    }

    @Override
    public int getWinner() {
        return this.end;
    }

    @Override
    public int evaluate(int player) {
        return 0;
    }

    @Override
    public JsonObjectNode toJson() {
        return new JsonObjectNode();
    }

    @Override
    public void fromJson(JsonObjectNode node) {

    }

    public TicTacToe clone() {
        TicTacToe cln = new TicTacToe(this.rule);
        System.arraycopy(this.board, 0, cln.board, 0, 9);
        cln.rule = this.rule;
        cln.turn = this.turn;
        return cln;
    }

    public String toString() {
        char[] temp = new char[9];
        for (int i = 0; i < 9; i++) {
            if (this.board[i] == 0)
                temp[i] = ' ';
            else if (this.board[i] == 1)
                temp[i] = 'O';
            else
                temp[i] = 'X';
        }
        return String.format("%c%c%c\n%c%c%c\n%c%c%c",
                temp[0], temp[1], temp[2],
                temp[3], temp[4], temp[5],
                temp[6], temp[7], temp[8]
        );
    }
}
