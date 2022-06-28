package org.example.whzbot.data.game.Nylium;

import java.util.Random;

import org.example.whzbot.data.game.IGame;
import org.example.whzbot.helper.AreaHelper;
import org.example.whzbot.storage.json.JsonObjectNode;

import static java.lang.System.arraycopy;

/**
 * Basic 3x3 game
 * |O|X|O|
 * |X|X|O|
 * |X|O|O|
 * <p>
 * in game id can be 1 / 2
 */
public class NyliumChess implements IGame {
    NyliumRule rule = null;
    int[] board;
    int turn = 0; // 0=p1, 1=p2
    int count1, count2;// occupancy for p1 and p2.

    public NyliumChess() {
        this.board = new int[64];
        this.count1 = 0;
        this.count2 = 0;
    }

    public NyliumChess(NyliumRule rule_in) {
        this();
        this.rule = rule_in;
        if (this.rule.init_state == 1) {
            this.board[7] = 1;
            this.board[56] = 2;
            this.count1 = 1;
            this.count2 = 1;
        }
    }

    public NyliumChess(byte[] identity, NyliumRule rule_in) {
        this(rule_in);
        int bias = 2;
        int index = 0;
        int state = identity[0] % 4;
        if (state == 3) {
            for (int i = 0; i < 64; i++) {
                this.board[i] = (identity[index] >> bias) & 1;
                index += bias / 7;
                bias = (bias + 1) & 7;
            }
        } else {
            int[] h_code = {state, state == 0 ? 1 : 0, state, state == 2 ? 2 : 1};
            int[] h_len = {1, 2, 1, 2};

            int temp;
            for (int i = 0; i < 64; i++) {
                temp = (identity[index] >> bias) & 3;
                bias += h_len[temp];
                index += bias >> 3;
                bias &= 7;

                this.board[i] = h_code[temp];
                this.count1 += h_code[temp] & 1;
                this.count2 += h_code[temp] >> 1;
            }
        }
    }

    public int getTurn() {
        return this.turn + 1;
    }

    @Override
    public NyliumChess doMove(String move) {
        // read move and get location
        int x = move.charAt(0);
        if (x > 96)
            x -= 97;
        else if (x > 64)
            x -= 65;
        else if (x > 48)
            x -= 49;

        int y = move.charAt(1);
        if (y > 96)
            y -= 97;
        else if (y > 64)
            y -= 65;
        else if (y > 48)
            y -= 49;
        int loc = y * 8 + x;

        // make new state
        int temp = this.turn + 1;
        NyliumChess rtn = this.clone();

        AreaHelper area = new AreaHelper(8, 8);
        int c1 = 0;
        int c2 = 0;
        area = area.addSqrEdge(loc, 1);
        for (int i : area.addSqrEdge(loc, 1)) {
            if (rtn.board[i] == 1)
                c1++;
            else if (rtn.board[i] == 2)
                c2++;
        }
        if (c1 > 0 && c2 > 0) {
            rtn.board[loc] = new Random().nextDouble() > 0.5 ? temp : 3 - temp;
            rtn.count1 += rtn.board[loc] & 1;
            rtn.count2 += rtn.board[loc] >> 1;
        } else if (c1 > 0) {
            rtn.board[loc] = 1;
            rtn.count1++;
        } else if (c2 > 0) {
            rtn.board[loc] = 2;
            rtn.count2++;
        }
        rtn.checkEnclosed(loc);
        rtn.turn = 1 - rtn.turn;

        return rtn;
    }

    protected void checkEnclosed(int loc) {
        AreaHelper area = new AreaHelper(8, 8);
        int c = 0;
        int centre_color = this.board[loc];
        for (int i : area.addAdjacent(loc)) {
            if (this.board[i] == centre_color)
                c++;
        }
        if (c >= 2 || (area.size() == 3 && c > 0)) {
            area.clear();
            AreaHelper area2 = new AreaHelper(8, 8);
            for (int j : area2.addSqrEdge(loc, 1)) {
                if (this.board[j] == 0) {
                    area.clear();
                    if (area.addRadiant(
                            j, (Integer index) -> this.board[index] != centre_color,
                            (Integer index) -> this.board[index] != 3 - centre_color
                    ) != null) {
                        c = area.size();
                        for (int i : area) {
                            this.board[i] = centre_color;
                        }
                        this.count1 += (centre_color & 1) * c;
                        this.count2 += (centre_color >> 1) * c;
                    }
                }
            }
        }
    }

    @Override
    public IGame undoMove(String move) {
        return null;
    }

    @Override
    public boolean canMove(String move) {
        if (move.length() != 2)
            return false;
        int x = move.charAt(0);
        if (x > 96)
            x -= 97;
        else if (x > 64)
            x -= 65;
        else if (x > 48)
            x -= 49;

        int y = move.charAt(1);
        if (y > 96)
            y -= 97;
        else if (y > 64)
            y -= 65;
        else if (y > 48)
            y -= 49;
        int loc = y * 8 + x;
        return loc < 64 && board[loc] == 0;
    }

    @Override
    public String[] moves() {
        int count = 0;
        for (int i = 0; i < 64; i++)
            count += (board[i] + 1) / 2;
        String[] rtn = new String[count];
        for (int i = 8; count > 0; i--) {
            if (board[i] == 0)
                rtn[--count] = String.valueOf((char) (i + 97));
        }
        return rtn;
    }

    @Override
    public boolean isEnd() {
        return this.count1 + this.count2 == 64;
    }

    @Override
    public boolean isWin(int player) {
        return this.count1 != this.count2 && (player == 1) == (this.count1 > this.count2);
    }

    /**
     * @implNote Should not be used when this.isEnd() not true
     * @implNote p1 - 1 = 0, 2 - end(1) = 1. 0 ^ 1 = 1
     * @implNote p1 - 1 = 0, 2 - end(2) = 0. 0 ^ 1 = 0
     * @implNote p2 - 1 = 1, 2 - end(1) = 1. 1 ^ 1 = 0
     * @implNote p2 - 1 = 1, 2 - end(2) = 0. 1 ^ 0 = 1 //in case I forgot.
     */
    @Override
    public float getScore(int player) {
        return this.count1 == this.count2 ? 0.5f : (2 - this.getWinner()) ^ (player - 1);
    }

    @Override
    public int getWinner() {
        return this.count1 > this.count2 ? 1 : 2 +
                this.count1 == this.count2 ? 1 : 0;
    }

    @Override
    public int evaluate(int player) {
        return player == 1 ? count1 - count2 : count2 - count1;
    }

    @Override
    public JsonObjectNode toJson() {
        return new JsonObjectNode();
    }

    @Override
    public void fromJson(JsonObjectNode node) {

    }

    public NyliumChess clone() {
        NyliumChess cln;
        try {
            cln = (NyliumChess) super.clone();
            cln.board = new int[64];
        } catch (CloneNotSupportedException e) {
            cln = new NyliumChess();
        }
        //noinspection SuspiciousSystemArraycopy
        arraycopy(this.board, 0, cln.board, 0, 64);
        cln.rule = this.rule;
        cln.turn = this.turn;
        cln.count1 = this.count1;
        cln.count2 = this.count2;
        return cln;
    }

    public String toString() {
        char[] temp = new char[64];
        for (int i = 0; i < 64; i++) {
            if (this.board[i] == 0)
                temp[i] = ' ';
            else if (this.board[i] == 1)
                temp[i] = 'o';
            else
                temp[i] = 'x';
        }
        StringBuilder rtn = new StringBuilder("  a b c d e f g h\n");
        for (int i = 0; i < 8; i++) {
            rtn.append((char) (i + 49));
            for (int j = 0; j < 8; j++) {
                rtn.append(' ');
                rtn.append(temp[i * 8 + j]);
            }
            rtn.append('\n');
        }
        return rtn.toString();
    }
}
