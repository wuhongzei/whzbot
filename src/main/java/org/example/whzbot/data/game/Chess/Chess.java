package org.example.whzbot.data.game.Chess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

import org.example.whzbot.data.game.IGame;
import org.example.whzbot.helper.AreaHelper;
import org.example.whzbot.storage.json.JsonObjectNode;

/**
 * Formal chess game.
 * \u2654\u2655\u2656\u2657\u2658\u2659 white
 * \u265a\u265b\u265c\u265d\u265e\u265f black
 * K Q R B N P
 * 75, 81, 82, 66, 78, 80       white, upper case
 * 107, 113, 114, 98, 110, 112  black, lower case
 */
public class Chess implements IGame {
    //public final static char[] PIECES_STR = {'\u2654', '\u2655', '\u2656', '\u2657', '\u2658', '\u2659', '\u265a', '\u265b', '\u265c', '\u265d', '\u265e', '\u265f'};
    public final static char[] PIECES_STR = {'K', 'Q', 'R', 'B', 'N', 'P', 'k', 'q', 'r', 'b', 'n', 'p'};

    ChessRule rule;
    byte[] pieces;
    int turn = 0; // 0=p1, 1=p2
    int mate = 0;

    int peace_count = 0;
    int repeat_count = 0; //todo: add repeat draw.
    int can_white_castle = 3; // 0 = king moved, 1 = Ra1 moved, 2 = Rh1 moved.
    int can_black_castle = 3; // 0 = king moved, 1 = Ra8 moved, 2 = Rh8 moved.
    int pass_pawn = 9;

    // Cache variables. Used to reduce redundant calculation.
    char[] board = null;
    byte[] bind_map = null;
    AreaHelper unsafe = null;

    public Chess(ChessRule rule_in) {
        this.rule = rule_in;
        this.pieces = ChessRule.DEFAULT_STATE[this.rule.init_state];
    }

    @Override
    public int getTurn() {
        return this.turn;
    }

    private void checkEnd() {
        if (this.moves().length < 1) {
            int loc = this.findPiece(getColoredPiece('K'));
            if (loc < 0)
                this.mate = -1;
            else {
                loc = this.pieces[loc + 1];
                AreaHelper unsafe = loadUnsafe(this.turn == 0);

                if (unsafe.has(loc))
                    this.mate = 1;
                else
                    this.mate = -1;
            }
        } else if (this.repeat_count >= 3 || this.peace_count > 50)
            this.mate = -1;
    }

    public char[] loadBoard(boolean reload) {
        if (this.board != null && !reload)
            return this.board;
        this.board = new char[64];
        char p = (char) pieces[0];
        for (byte i = 1; i < pieces.length; i++) {
            if (pieces[i] < 64)
                this.board[pieces[i]] = p;
            else
                p = (char) pieces[i];
        }
        return this.board;
    }

    public byte[] loadBinding(boolean reload) {
        if (this.bind_map == null || reload) {
            this.bind_map = new byte[64];
            int loc = this.findPiece(getColoredPiece('K'));
            if (loc < 0)
                return this.bind_map;
            loc = this.pieces[loc + 1];

            Predicate<Byte> same_color = (Byte b) -> board[b] <= 96 == this.turn < 1;
            byte[] binding_map = new byte[64];

            Predicate<Byte> can_move_cross = (Byte b) -> "RQrq".indexOf(board[b]) > 0;
            Predicate<Byte> can_move_dgn = (Byte b) -> "BQbq".indexOf(board[b]) > 0;
            byte cap = (byte) (loc & 0b111000);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b >= cap,
                    same_color, -1, can_move_cross);
            byte cap1 = (byte) (loc | 0b000111);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b <= cap1,
                    same_color, +1, can_move_cross);
            byte cap2 = (byte) (loc % 8 + 56);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b <= cap2,
                    same_color, +8, can_move_cross);
            byte cap3 = (byte) (loc % 8);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b >= cap3,
                    same_color, -8, can_move_cross);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b % 8 < 7 && b >= 0,
                    same_color, -9, can_move_dgn);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b % 8 > 0 && b <= 64,
                    same_color, +9, can_move_dgn);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b % 8 > 0 && b >= 0,
                    same_color, -7, can_move_dgn);
            addBinding(binding_map, (byte) loc, board, (Byte b) -> b % 8 < 7 && b <= 64,
                    same_color, +7, can_move_dgn);

        }
        return this.bind_map;
    }

    @Override
    public IGame doMove(String move) {
        Chess rtn = this.clone();
        char[] board = this.loadBoard(true);

        System.out.println(Arrays.toString(this.pieces));

        boolean is_peace = true;
        if (move.equals("0-0-0") || move.toLowerCase().equals("o-o-o")) {
            rtn.movePiece(getColoredPiece('R'), getMirrorLoc(0), getMirrorLoc(3));
            rtn.movePiece(getColoredPiece('K'), getMirrorLoc(4), getMirrorLoc(2));
            rtn.pass_pawn = 9;
        } else if (move.equals("0-0") || move.toLowerCase().equals("o-o")) {
            rtn.movePiece(getColoredPiece('R'), getMirrorLoc(7), getMirrorLoc(5));
            rtn.movePiece(getColoredPiece('K'), getMirrorLoc(4), getMirrorLoc(6));
            rtn.pass_pawn = 9;
        } else {
            boolean is_pass_pawn = false;
            byte y1 = 0, x1 = 0, x2, y2 = 0;
            byte piece;

            int index = move.length() - 1; // search from back for 1-8
            for (; index >= 0; index--) {
                y2 = (byte) move.charAt(index);
                if ('0' < y2 && y2 < '9')
                    break;
            }

            if (index < 0) { //short form 'cd'
                piece = 'P';
                x1 = (byte) move.charAt(0);
                index = 1;
                if (x1 == 'P' || x1 == 'p') {
                    x1 = (byte) move.charAt(index);
                    index++;
                }
                x2 = (byte) move.charAt(index);
                x1 -= 97;
                x2 -= 97;
                y2 = this.findPiece((byte) (80 + 32 * this.turn));
                for (int i = y2 + 1; this.pieces[i] < 64; i++) {
                    int temp = x2 + (this.pieces[i] & 0b111000) + 8 - 16 * this.turn;
                    if (this.pieces[i] % 8 == x1 && board[temp] != 0 &&
                            (board[temp] <= 97 != this.turn < 1)) {
                        y1 = (byte) (this.pieces[i] / 8);
                        y2 = (byte) (y1 + 1 - 2 * this.turn);
                        break;
                    } else if (this.pieces[i] / 8 == 4 - this.turn &&
                            pass_pawn == x2) {
                        y1 = (byte) (4 - this.turn);
                        y2 = y1;
                        is_pass_pawn = true;
                        break;
                    }
                }
            } else {
                index--;
                x2 = (byte) move.charAt(index);
                y2 -= 49;
                x2 -= 97;
                int specify = 0;

                if (index == 0)
                    piece = 'P';
                else {
                    index--;
                    piece = (byte) move.charAt(index);
                    if (piece == 'x') {
                        index--;
                        piece = (byte) move.charAt(index);
                    }
                    if (piece >= '1' && piece <= '8') {
                        y1 = (byte) (piece - 49);
                        piece = index == 0 ? (byte) 'P' : (byte) move.charAt(index - 1);
                        specify = 2;
                    } else if (piece >= 'a' && piece <= 'h') {
                        x1 = (byte) (piece - 97);
                        piece = index == 0 ? (byte) 'P' : (byte) move.charAt(index - 1);
                        specify = 1;
                    }
                    if (piece == 'P') {
                        index += 1;
                        if (move.charAt(index) < '1' && move.charAt(index) > '8')
                            index++;
                        if (move.charAt(index) >= 'a' && move.charAt(index) <= 'h') {
                            x1 = x2;
                            y1 = y2;
                            x2 = (byte) (move.charAt(index) - 97);
                            y2++;
                            specify = 3;
                        }
                    }
                }
                piece = getColoredPiece((char) piece);
                int piece_loc = findPiece(piece);
                if (specify == 0) {
                    AreaHelper area = new AreaHelper(8, 8);
                    for (int i = piece_loc + 1; i < pieces.length && this.pieces[i] < 64; i++) {
                        addReach(area, (char) piece, this.pieces[i], board);
                        if (area.has(x2 + y2 * 8)) {
                            x1 = this.pieces[i];
                            break;
                        }
                        area.clear();
                    }
                } else if (specify == 1) {
                    AreaHelper area = new AreaHelper(8, 8);
                    for (byte i = 0; i < 8; i++) {
                        if (board[x1 + i * 8] == piece) {
                            addReach(area, (char) piece, (byte) (x1 + i * 8), board);
                            if (area.has(x2 + y2 * 8)) {
                                y1 = i;
                                break;
                            }
                            area.clear();
                        }
                    }
                } else if (specify == 2) {
                    AreaHelper area = new AreaHelper(8, 8);
                    for (byte i = 0; i < 8; i++) {
                        if (board[i + y1 * 8] == piece) {
                            addReach(area, (char) piece, (byte) (i + y1 * 8), board);
                            if (area.has(x2 + y2 * 8)) {
                                x1 = i;
                                break;
                            }
                            area.clear();
                        }
                    }
                } else {
                    if (board[y2 * 8 + x2] == 0) { // eat pass pawn
                        is_pass_pawn = true;
                    }
                }
            }
            x1 = (byte) (y1 * 8 + x1);
            x2 = (byte) (y2 * 8 + x2);

            if (board[x2] != 0) {
                rtn.removePiece(x2);
                is_peace = false;
            } else if (is_pass_pawn) {
                rtn.removePiece(x2);
                x2 += 1 - this.turn * 2;
                is_peace = false;
            }
            rtn.movePiece(piece, x1, x2);
            if (piece == 'P' && (y2 == 0 || y2 == 7)) {
                index = move.length() - 1;
                if (move.charAt(index) == '+')
                    index--;
                rtn.removePiece(x2);
                rtn.addPiece(getColoredPiece(move.charAt(index)), x2);
            }

            piece -= 32 * this.turn;
            is_peace &= piece != 'P';
            if (this.turn == 0) {
                if (can_white_castle > 0 && piece == 'K')
                    rtn.can_white_castle = 0;
                else if (can_white_castle > 1 && piece == 'R' && x1 == 0)
                    rtn.can_white_castle -= 2;
                else if (can_white_castle % 2 == 1 && piece == 'R' && x1 == 7)
                    rtn.can_white_castle -= 1;
            } else {
                if (can_black_castle > 0 && piece == 'K')
                    rtn.can_black_castle = 0;
                else if (can_black_castle > 1 && piece == 'R' && x1 == 56)
                    rtn.can_black_castle -= 2;
                else if (can_black_castle % 2 == 1 && piece == 'R' && x1 == 63)
                    rtn.can_black_castle -= 1;
            }
            if (piece == 'P' && (x1 - x2 > 10 || x2 - x1 > 10))
                rtn.pass_pawn = x1 % 8;
            else
                rtn.pass_pawn = 9;
        }
        System.out.println(Arrays.toString(rtn.pieces));

        if (is_peace)
            rtn.peace_count++;
        else
            rtn.peace_count = 0;
        rtn.turn = 1 - this.turn;
        rtn.checkEnd();
        return rtn;
    }

    private byte getColoredPiece(char p) {
        return (byte) (p + this.turn * 32);
    }

    private void movePiece(byte p, byte from_loc, byte to_loc) {
        byte i = findPiece(p, from_loc);
        if (i >= 0)
            this.pieces[i] = to_loc;
    }

    private void addPiece(byte p, byte loc) {
        byte pi = findPiece(p);
        byte[] new_pieces;
        if (pi < 0) {
            new_pieces = new byte[this.pieces.length + 2];
            System.arraycopy(this.pieces, 0,
                    new_pieces, 0, this.pieces.length);
            new_pieces[this.pieces.length] = getColoredPiece((char) p);
            new_pieces[this.pieces.length + 1] = loc;
        } else {
            new_pieces = new byte[this.pieces.length + 1];
            System.arraycopy(this.pieces, 0,
                    new_pieces, 0, pi);
            new_pieces[pi] = loc;
            System.arraycopy(this.pieces, pi,
                    new_pieces, pi + 1,
                    this.pieces.length - pi);
        }
        this.pieces = new_pieces;
    }

    private void removePiece(byte loc) {
        int pi = 0;
        byte[] new_pieces;
        for (int i = 0; i < pieces.length; i++) {
            System.out.println(pi + " " + i);
            if (pieces[i] >= 64) {
                pi = i;
            } else if (pieces[i] == loc) {
                if (pi == i - 1 && (i == pieces.length - 1 || pieces[i + 1] >= 64)) {
                    new_pieces = new byte[pieces.length - 2];
                    System.arraycopy(this.pieces, 0,
                            new_pieces, 0, pi);
                    System.arraycopy(this.pieces, i + 1,
                            new_pieces, pi,
                            pieces.length - i - 1);
                } else {
                    new_pieces = new byte[pieces.length - 1];
                    System.arraycopy(this.pieces, 0,
                            new_pieces, 0, i);
                    System.arraycopy(this.pieces, i + 1,
                            new_pieces, i,
                            pieces.length - i - 1);
                }
                this.pieces = new_pieces;
                return;
            }
        }
    }

    private byte getMirrorLoc(int loc) {
        return (byte) (loc % 8 +
                this.turn * (7 - loc / 8) +
                (1 - this.turn) * (loc / 8));
    }

    @Override
    public IGame undoMove(String move) {
        return null;
    }

    @Override
    public boolean canMove(String move) {
        boolean is_white = this.turn == 0;
        char[] board = this.loadBoard(false);

        if (move.equals("0-0-0") || move.toLowerCase().equals("o-o-o")) {
            if ((is_white ? can_white_castle : can_black_castle) <= 1) {
                return false; //king/rook moved.
            }
            if (board[getMirrorLoc(1)] != 0 || board[getMirrorLoc(2)] != 0 || board[getMirrorLoc(3)] != 0)
                return false;
            AreaHelper unsafe_for_king = this.loadUnsafe(is_white);
            return is_white ?
                    !(unsafe_for_king.has(2) || unsafe_for_king.has(3) ||
                            unsafe_for_king.has(4)) :
                    !(unsafe_for_king.has(58) || unsafe_for_king.has(59) ||
                            unsafe_for_king.has(60));
        } else if (move.equals("0-0") || move.toLowerCase().equals("o-o")) {
            if ((is_white ? can_white_castle : can_black_castle) % 2 == 0) {
                return false; //king/rook moved.
            }
            if (board[getMirrorLoc(5)] != 0 || board[getMirrorLoc(6)] != 0)
                return false;
            AreaHelper unsafe_for_king = this.loadUnsafe(is_white);
            return is_white ?
                    !(unsafe_for_king.has(5) || unsafe_for_king.has(6) ||
                            unsafe_for_king.has(4)) :
                    !(unsafe_for_king.has(61) || unsafe_for_king.has(62) ||
                            unsafe_for_king.has(60));
        }
        if (move.length() < 2)
            return false;
        byte y1 = 0, x1 = 0, x2, y2 = 0;
        byte piece = 0;

        int index = move.length() - 1; // search from back for 1-8
        for (; index >= 0; index--) {
            y2 = (byte) move.charAt(index);
            if ('0' < y2 && y2 < '9')
                break;
        }

        if (index < 0) { //short form 'cd'
            x1 = (byte) move.charAt(0);
            index = 1;
            if (x1 == 'P') {
                x1 = (byte) move.charAt(index);
                index++;
            }
            if (x1 < 'a' || x1 > 'h' || move.length() <= index) {
                return false;
            }
            x2 = (byte) move.charAt(index);
            if (x2 < 'a' || x2 > 'h') {
                return false;
            }
            int dif = x1 - x2;
            if (dif != 1 && dif != -1)
                return false;
            x1 -= 97;
            x2 -= 97;
            y2 = this.findPiece(getColoredPiece('P'));
            for (int i = y2 + 1; i < this.pieces.length && this.pieces[i] < 64; i++) {
                int temp = x2 + (this.pieces[i] & 0b111000) + 8 - 16 * this.turn;
                if (this.pieces[i] % 8 == x1 && board[temp] != 0 &&
                        (board[temp] < 97 != is_white)) {
                    if (piece != 0) //ambiguous.
                        return false;
                    piece = 'P';
                    y1 = (byte) (this.pieces[i] >> 3);
                    y2 = (byte) (temp / 8);
                } else if (this.pieces[i] / 8 == 4 - this.turn &&
                        pass_pawn == x2) {
                    if (piece != 0) //ambiguous.
                        return false;
                    piece = 'P';
                    y1 = (byte) (4 - this.turn);
                    y2 = y1;
                }
            }
            if (piece == 0) {
                return false;
            } else {
                byte[] binding = this.loadBinding(false);
                int from_loc = x1 + y1 * 8;
                int to_loc = x2 + y2 * 8;
                return (binding[from_loc] == 0 || binding[to_loc] == binding[from_loc]) &&
                        isKingSave(from_loc, board[to_loc] == 0 ? to_loc + 1 - 2 * this.turn : to_loc,
                                getColoredPiece('P'), board[to_loc] == 0 ? to_loc : -1);
            }
        } else {
            index--;
            if (index < 0)
                return false;
            x2 = (byte) move.charAt(index);
            if (x2 < 'a' || x2 > 'h')
                return false;
            y2 -= 49;
            x2 -= 97;
            int specify = 0;

            if (index == 0)
                piece = getColoredPiece('P');
            else {
                index--;
                piece = (byte) move.charAt(index);
                if (piece == 'x')
                    index--;
                if (piece >= '1' && piece <= '8') {
                    y1 = (byte) (piece - 49);
                    piece = index == 0 ? (byte) 'P' : (byte) move.charAt(index - 1);
                    specify = 2;
                } else if (piece >= 'a' && piece <= 'h') {
                    x1 = (byte) (piece - 97);
                    piece = index == 0 ? (byte) 'P' : (byte) move.charAt(index - 1);
                    specify = 1;
                }
                if (piece == 'p' || piece == 'P') {
                    index += 1;
                    if (move.charAt(index) < '1' && move.charAt(index) > '8')
                        index++;
                    if (move.charAt(index) >= 'a' && move.charAt(index) <= 'h') {
                        x1 = x2;
                        y1 = y2;
                        x2 = (byte) (move.charAt(index) - 97);
                        y2++;
                        specify = 3;
                    }
                }
                piece = getColoredPiece((char) piece);
            }
            if (board[x2 + y2 * 8] != 0 && board[x2 + y2 * 8] < 96 == is_white)
                return false; // cannot eat self.
            int piece_loc = findPiece(piece);
            if (piece_loc == -1)
                return false; // piece not found.
            if (specify == 0) {
                AreaHelper area = new AreaHelper(8, 8);
                for (int i = piece_loc + 1; i < pieces.length && this.pieces[i] < 64; i++) {
                    addReach(area, (char) piece, this.pieces[i], board);
                    if (area.has(x2 + y2 * 8)) {
                        if (specify == 0) {
                            specify = 4;
                            x1 = this.pieces[i];
                        } else
                            return false; // ambiguous move.
                    }
                    area.clear();
                }
            } else if (specify == 1) {
                AreaHelper area = new AreaHelper(8, 8);
                for (byte i = 0; i < 8; i++) {
                    if (board[x1 + i * 8] == piece) {
                        addReach(area, (char) piece, (byte) (x1 + i * 8), board);
                        if (area.has(x2 + y2 * 8)) {
                            if (specify == 1) {
                                specify = 4;
                                y1 = i;
                            } else
                                return false; // ambiguous move.
                        }
                    }
                }
            } else if (specify == 2) {
                AreaHelper area = new AreaHelper(8, 8);
                for (byte i = 0; i < 8; i++) {
                    if (board[i + y1 * 8] == piece) {
                        addReach(area, (char) piece, (byte) (i + y1 * 8), board);
                        if (area.has(x2 + y2 * 8)) {
                            if (specify == 2) {
                                specify = 4;
                                x1 = i;
                            } else
                                return false; // ambiguous move.
                        }
                    }
                }
            } else {
                if (board[y2 * 8 + x2] == 0) { // eat pass pawn
                    if (y2 != 6 - 3 * this.turn || x2 != pass_pawn)
                        return false;
                }
            }
            if (specify < 3)
                return false;

            x1 = (byte) (x1 + y1 * 8);
            x2 = (byte) (x2 + y2 * 8);

            if (!isKingSave(
                    x1, x2, piece, -1)
            ) return false;
        }
        if ((piece == 'P') && (y2 == 0 || y2 == 7)) {
            index = move.length() - 1;
            if (move.charAt(index) == '+')
                index--;
            char c = move.charAt(index);
            return "QRBN".indexOf(c) < 0; // promotion required.
        }

        return true;
    }

    /**
     * Check whether or not king is save after some move.
     *
     * @param from_loc  original location of piece
     * @param to_loc    destination of piece
     * @param piece     name of piece
     * @param extra_loc if movement causes extra change, use this; otherwise -1.
     * @return true if king is save.
     * @implNote this method cheats with unsafe and board cache.
     */
    private boolean isKingSave(int from_loc, int to_loc, byte piece, int extra_loc) {
        boolean is_white = this.turn < 1;
        int index = findPiece(getColoredPiece('K'));
        byte[] binding = this.loadBinding(false);
        if (index != -1 && piece != 'K' && piece != 'k') {
            if (binding[from_loc] != 0 && binding[to_loc] != binding[from_loc])
                return false;
            loadBoard(false);
            char old_board = board[from_loc];
            char old_extra = 0;
            if (extra_loc >= 0) {
                old_extra = board[extra_loc];
                board[extra_loc] = 0;
            }
            board[from_loc] = 0;
            board[to_loc] = (char) piece;
            AreaHelper old_unsafe = this.unsafe;
            this.unsafe = null;
            loadUnsafe(!is_white);
            this.board[from_loc] = old_board;
            if (extra_loc >= 0) {
                board[extra_loc] = old_extra;
            }
            if (this.unsafe.has(this.pieces[index + 1])) {
                this.unsafe = old_unsafe;
                return false;
            } else {
                this.unsafe = old_unsafe;
            }
        } else if (piece == getColoredPiece('K')) {
            return !loadUnsafe(!is_white).has(to_loc);
        }
        return true;
    }

    private byte findPiece(byte piece) {
        byte rtn = 0;
        while (rtn < pieces.length && pieces[rtn] != piece)
            rtn++;
        if (rtn == pieces.length)
            rtn = -1;
        return rtn;
    }

    private byte findPiece(byte piece, byte loc) {
        byte rtn = findPiece(piece);
        if (rtn < 0)
            return rtn;
        while (rtn < pieces.length && pieces[rtn] != loc)
            rtn++;
        if (rtn == pieces.length)
            return -1;
        return rtn;
    }

    @Override
    public String[] moves() { //todo: castle not included; Possible for dup.; Check for pass pawn
        char[] board = this.loadBoard(false);
        HashSet<Integer> potential = new HashSet<>();

        byte piece = 0;
        boolean is_white = this.turn == 0;
        Predicate<Byte> same_color = (Byte b) -> board[b] <= 96 == is_white;
        AreaHelper moves = new AreaHelper(8, 8);
        byte[] binding_map = this.loadBinding(false);
        AreaHelper unsafe = loadUnsafe(!is_white);
        for (byte b : this.pieces) {
            if (b < 64) {
                if (piece < 97 == is_white) {
                    addReach(moves, (char) piece, b, board);
                    if (piece == 'K' || piece == 'k') {
                        for (int l : moves) {
                            if ((board[l] == 0 || !same_color.test((byte) l)) && !unsafe.has(l))
                                potential.add((int) b * 64 + l);
                        }
                    } else if (binding_map[b] != 0) {
                        for (int l : moves) {
                            if ((board[l] == 0 || !same_color.test((byte) l)) &&
                                    binding_map[l] == binding_map[b] &&
                                    isKingSave(b, l, piece, -1))
                                potential.add((int) b * 64 + l);
                        }
                    } else {
                        for (int l : moves) {
                            if ((board[l] == 0 || !same_color.test((byte) l)) &&
                                    isKingSave(b, l, piece, -1))
                                potential.add((int) b * 64 + l);
                        }
                    }
                    moves.clear();
                }
            } else {
                piece = b;
            }
        }
        HashMap<String, Integer> rtn = new HashMap<>();
        for (int f : potential) {
            String m = "" + board[f / 64];
            m = m.toUpperCase() + (char) (f % 8 + 97) + (char) (f % 64 / 8 + 49);
            f /= 64;
            if (rtn.get(m) != null) {
                if (rtn.get(m) % 8 != f % 8) {
                    rtn.put(m.substring(0, 1) + (char) (f % 8 + 97) + m.substring(1), f);
                    rtn.put(m.substring(0, 1) + (char) (rtn.get(m) % 8 + 97) + m.substring(1), f);
                } else {
                    rtn.put(m.substring(0, 2) + (char) (f / 8 + 49) + m.substring(2), f);
                    rtn.put(m.substring(0, 2) + (char) (rtn.get(m) / 8 + 49) + m.substring(2), f);
                }
                rtn.remove(m);
            } else {
                rtn.put(m, f);
            }
        }
        return rtn.keySet().toArray(new String[0]);
    }

    /**
     * Scan and add binding info to map if necessary.
     *
     * @param binding_map binding map given.
     * @param king_loc    location of king (whether king is actually here is trivial.
     * @param board       an array of current board.
     * @param is_not_end  condition in for loop continuation.
     * @param same_color  condition to test whether piece has same color as king.
     * @param inc         increment for each step.
     * @param has_dir     test for whether a piece can bind in this direction.
     */
    private void addBinding(byte[] binding_map, byte king_loc, char[] board,
                            Predicate<Byte> is_not_end, Predicate<Byte> same_color,
                            int inc, Predicate<Byte> has_dir) {
        int binding_count = 0;
        byte termination = -1;
        AreaHelper temp = new AreaHelper(8, 8);
        for (byte i = (byte) (king_loc + inc); is_not_end.test(i); i = king_loc += inc) {
            temp.add(i);
            if (board[i] != 0 && (same_color.test((i)))) {
                binding_count++;
                if (binding_count > 1) {
                    break;
                }
            } else if (board[i] != 0 && !same_color.test(i)) {
                if (has_dir.test(i))
                    termination = i;
                break;
            }
        }
        if (termination >= 0) {
            for (int i : temp)
                binding_map[i] = termination;
        }
    }

    @Override
    public boolean isEnd() {
        return this.mate != 0;
    }

    @Override
    public boolean isWin(int player) {
        return player == this.getWinner();
    }

    @Override
    public float getScore(int player) {
        return this.mate == -1 ? (float) 0.5 : (isWin(player) ? 1 : 0);
    }

    @Override
    public int getWinner() {
        return 2 - this.turn;
    }

    @Override
    public int evaluate(int player) {
        return 0;
    }

    public Chess clone() {
        Chess cln;
        try {
            cln = (Chess) super.clone();
        } catch (CloneNotSupportedException e) {
            cln = new Chess(this.rule);
            cln.turn = this.turn;
            cln.pass_pawn = this.pass_pawn;
            cln.can_white_castle = this.can_white_castle;
            cln.can_black_castle = this.can_black_castle;
            cln.mate = this.mate;
            cln.peace_count = this.peace_count;
            cln.repeat_count = this.repeat_count;
        }
        cln.pieces = new byte[this.pieces.length];
        System.arraycopy(this.pieces, 0, cln.pieces, 0, pieces.length);

        return cln;
    }

    @Override
    public JsonObjectNode toJson() {
        return null;
    }

    @Override
    public void fromJson(JsonObjectNode node) {

    }

    public String toString() {
        char[] board = this.loadBoard(true);
        StringBuilder rtn;
        if (this.turn == 0) {
            rtn = new StringBuilder(" a b c d e f g h\n");
            for (int i = 7; i >= 0; i--) {
                rtn.append((char) (i + 49));
                for (int j = 0; j < 8; j++) {
                    if (board[i * 8 + j] == 0)
                        //rtn.append("   ");
                        rtn.append(" ");
                    else {
                        rtn.append(getChessChar(board[i * 8 + j]));
                    }
                }
                rtn.append('\n');
            }
        } else {
            rtn = new StringBuilder(" h g f e d c b a\n");
            for (int i = 0; i < 8; i++) {
                rtn.append((char) (i + 49));
                for (int j = 7; j >= 0; j--) {
                    if (board[i * 8 + j] == 0)
                        //rtn.append("   ");
                        rtn.append(" ");
                    else {
                        rtn.append(getChessChar(board[i * 8 + j]));
                    }
                }
                rtn.append('\n');
            }
        }
        return rtn.toString();
    }

    /**
     * load all unsafe points to area, value will be cached.
     *
     * @param is_white the color of pieces being queried.
     * @return AreaHelper object with all unsafe location.
     */
    private AreaHelper loadUnsafe(boolean is_white) {
        if (this.unsafe != null)
            return this.unsafe;
        this.unsafe = new AreaHelper(8, 8);

        char[] board = this.loadBoard(false);
        int king_loc = findPiece(getColoredPiece('K'));
        char temp = (char) 64;
        if (king_loc >= 0) {
            king_loc = this.pieces[king_loc + 1];
            temp = board[king_loc];
            board[king_loc] = 0;
        }
        for (int i = 0; i < 64; i++) {
            if (board[i] != 0 && board[i] < 97 == is_white)
                addAiming(unsafe, board[i], (byte) i, board);
        }
        if (temp < 64)
            board[king_loc] = temp;
        return unsafe;
    }

    private static char getChessChar(char var) {
        switch (var) {
            case 75:
                return PIECES_STR[0];
            case 81:
                return PIECES_STR[1];
            case 82:
                return PIECES_STR[2];
            case 66:
                return PIECES_STR[3];
            case 78:
                return PIECES_STR[4];
            case 80:
                return PIECES_STR[5];
            case 107:
                return PIECES_STR[6];
            case 113:
                return PIECES_STR[7];
            case 114:
                return PIECES_STR[8];
            case 98:
                return PIECES_STR[9];
            case 110:
                return PIECES_STR[10];
            case 112:
                return PIECES_STR[11];
            default:
                return ' ';
        }
    }

    /**
     * Add all possible eating move to selection. does not skip piece, but can include same color.
     * Pawn is not aiming its front.
     *
     * @param area  an AreaHelp object (8*8) to store selection.
     * @param piece any piece permitted.
     * @param loc   location of piece.
     * @param board current board situation.
     */
    private static void addAiming(AreaHelper area, char piece, byte loc, char[] board) {
        switch (piece) {
            case 75:
            case 107:
                area.addSqrEdge(loc, 1);
                break;
            case 81:
            case 113:
                area.addDiagonal(loc, (Integer i) -> board[i] == 0, 8, true);
            case 82:
            case 114:
                area.addCross(loc, (Integer i) -> board[i] == 0, 8, true);
                break;
            case 66:
            case 98:
                area.addDiagonal(loc, (Integer i) -> board[i] == 0, 8, true);
                break;
            case 78:
            case 110:
                area.addHorseJump(loc, (Integer i) -> false);
                break;
            case 80:
                if (loc % 8 > 0)
                    area.add(loc + 7);
                if (loc % 8 < 7)
                    area.add(loc + 9);
                break;
            case 112:
                if (loc % 8 > 0)
                    area.add(loc - 9);
                if (loc % 8 < 7)
                    area.add(loc - 7);
                break;
        }
    }

    /**
     * Add all possible moves to selection. does not skip piece, but can include same color.
     *
     * @param area  an AreaHelp object (8*8) to store selection.
     * @param piece any piece permitted.
     * @param loc   location of piece.
     * @param board current board situation.
     */
    private static void addReach(AreaHelper area, char piece, byte loc, char[] board) {
        if (piece != 80 && piece != 112)
            addAiming(area, piece, loc, board);
        if (piece == 80) {
            if (board[loc + 8] == 0) {
                area.add(loc + 8);
                if (loc / 8 == 1 && board[loc + 16] == 0)
                    area.add(loc + 16);
            }
            if (loc % 8 > 0 && board[loc + 7] > 0)
                area.add(loc + 7);
            if (loc % 8 < 7 && board[loc + 9] > 0)
                area.add(loc + 9);
        }
        if (piece == 112) {
            if (board[loc - 8] == 0) {
                area.add(loc - 8);
                if (loc / 8 == 6 && board[loc - 16] == 0)
                    area.add(loc - 16);
            }
            if (loc % 8 > 0 && board[loc - 9] > 0)
                area.add(loc - 9);
            if (loc % 8 < 7 && board[loc - 7] > 0)
                area.add(loc - 7);
        }
    }
}
