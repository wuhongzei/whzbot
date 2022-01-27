package org.example.whzbot.data.game;

import org.example.whzbot.storage.json.JsonObjectNode;

/**
 * Interface for all multiplayer games.
 * Assumes there are N players id from 0 to n-1.
 * It is designed for board games that players play one-by-one.
 *
 * Although it might be redundant to create so many states,
 * this interface is designed both for human and AI, game tree search specifically.
 */
public interface IGame {

    /**
     * Get the next player.
     *
     * @return the id of next player to play.
     */
    int getTurn();

    /**
     * Make a move on the board.
     * @param move a command to which a move can be made.
     *             it is required to check canMove first.
     * @return A new game state, when move is made.
     */
    IGame doMove(String move);

    /**
     * Undo a move, if applicable.
     * The game state itself does not remember the last state,
     * so it would be generated, and might differ from original state.
     * @param move The last move made.
     * @return A new game state, before such move is made.
     */
    IGame undoMove(String move);

    /**
     * Check whether a move is legal.
     * This also apply custom rules.
     * @param move A potential move.
     * @return true if this move is applicable.
     */
    boolean canMove(String move);

    /**
     * List all the possible moves.
     * canMove check does not necessarily depend on this, this is left for AI.
     * @return A list of possible moves in String.
     */
    String[] moves();


    /**
     * Check for the end of the game.
     * There is no infinite game, if it is well-designed.
     * Usually end of end is queried after each move and stored.
     * @return true if the game ended.
     */
    boolean isEnd();

    /**
     * Check if the player wins.
     * Should be used after end of game.
     * Designed for games allowing multiple winners, otherwise use getWinner.
     * @param player the in-game id of a player.
     * @return true if the player won this game.
     */
    boolean isWin(int player);

    /**
     * Determine how much a player won.
     * Should be used after end of game.
      @param player the in-game id of a player.
     * @return it would be more positive for a greater advantage.
     */
    float getScore(int player);

    /**
     * Get the winner of this game.
     * Should be used after end of game.
     * @return in-game id for the won player.
     */
    int getWinner();

    // for ai uses.
    /**
     * Evaluate the current state for a player.
     * Meaningless for human, designed for AI, maybe.
     * @param player in-game id
     * @return some number the could represent the situation.
     */
    int evaluate(int player);

    // to storage.

    /**
     * Convert the game state to json.
     * @return a JsonObjectNode, containing:
     *      int "turn" who to play next
     *      (boolean "is_end" is this game ended)
     *      [any] "board" any type, enough to reconstruct the board.
     *      obj "rules" some rules.
     */
    JsonObjectNode toJson();

    /**
     * Read a JsonObjectNode structured by toJson, and get back a game state.
     * @param node a JsonObjectNode, containing:
     *      int "turn" who to play next
     *      (boolean "is_end" is this game ended)
     *      [any] "board" any type, enough to reconstruct the board.
     *      obj "rules" some rules.
     */
    void fromJson(JsonObjectNode node);

    /**
     * Text-based output method.
     * use ascii/unicode to represent the game.
     * this output goes to ui.
     * @return a string representing the board.
     */
    String toString();
}
