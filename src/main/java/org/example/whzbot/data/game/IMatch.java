package org.example.whzbot.data.game;

import java.util.UUID;

import org.example.whzbot.storage.json.JsonObjectNode;

/**
 * A match of a game is where players sit around and play.
 * There are phases for each match.
 * First, the match is initiated and players are joining and leaving.
 * Second, the game is initiated and match starts.
 * Third, the game is finished and the match ends.
 * Last, the game record can be read later, again.
 * To make it more friendly, AIs are usually allowed to join, as part of the system.
 *
 * @param <T> the game type of this match.
 * @implNote IGame uses in-game id, which is different from user id.
 * Any id return in long here should be user id,
 * and this interface is designed to decouple game from the users.
 * @implNote UUID is used to store and access the match.
 * The list of players and observers might be removed once game is finished.
 */
public interface IMatch<T extends IGame> {

    // controls of game phases.

    /**
     * Add one player to player list.
     *
     * @param id a user id.
     * @return true if the user is allowed to join.
     * e.g. if game is already full, fail to join and return false.
     */
    boolean join(long id);

    /**
     * Remove a player from the player list.
     * Should be used before game begins.
     *
     * @param id a user id.
     * @return true if the user can be removed.
     * if the user was not in the game for game has begun, return false.
     */
    boolean quit(long id);

    /**
     *
     * @return get the current state of this match. 0: waiting, 1: playing, 2: ended.
     */
    int getPhase();

    /**
     * Set game to begin, apply rules and initiate board.
     * Players and rules are locked after this method is done.
     * Observers are free to join and leave.
     *
     * @return true if the game is ready to begin.
     * Missing or conflicting rules, unfulfilled player may cause return false.
     */
    boolean begin();

    float getResult(long id);

    // passive methods.

    /**
     * Get a list of seats, and each seat is for a player.
     * id 0 means it is empty, and id less than 0 is for AIs.
     *
     * @return A list of player id.
     */
    long[] getPlayers();

    /**
     * Get who to play
     *
     * @return id representing a player.
     */
    long getNextPlayer();

    /**
     * Play next move in the move.
     *
     * @param input a move to the game.
     * @return whether input is valid
     */
    boolean move(String input);

    /**
     * Get the current game state.
     *
     * @return A game state.
     */
    T getBoard();

    // storage
    void assignUUID(UUID uuid);

    /**
     * Get the uuid of this match.
     *
     * @return UUID of this match.
     */
    UUID getUUID();

    /**
     * Get the string representation of the game board.
     * Maybe customized for different user.
     *
     * @param display_mode display mode depend on games.
     * @return A string enough to represent the game state.
     */
    String getBoard(int display_mode);

    /**
     * Get the game history of the game.
     * Maybe customized for different user.
     *
     * @param record_mode record mode depend on games.
     * @return A list of strings. Each element represents a move.
     * First element of return should reserve for init (maybe rules).
     */
    String[] getHistory(int record_mode);

    /**
     * Get a JsonObjectNode storing all the information of this match.
     *
     * @return the root node should contain:
     * list[int] "players" a list of players' user ids.
     * list[int] "observers" a list of observers' ids.
     * obj "state" the object node from game state.
     * rules may be removed since it can be in the outer node.
     * [any] "order" the order of next player.
     * (any) "rules" the rules of this match.
     * str "beg_time" the begin time of this match.
     * str "end_time" the end time of this match.
     * list[str] "record" the game history of this match.
     * str "uuid" the uuid of this match.
     */
    JsonObjectNode toJson();

    /**
     * Load a match from a json obj node.
     *
     * @param root the root node should be from toJson and contain:
     *             list[int] "players" a list of players' user ids.
     *             list[int] "observers" a list of observers' ids.
     *             obj "state" the object node from game state.
     *             rules may be removed since it can be in the outer node.
     *             [any] "order" the order of next player.
     *             (any) "rules" the rules of this match.
     *             str "beg_time" the begin time of this match.
     *             str "end_time" the end time of this match.
     *             list[str] "record" the game history of this match.
     *             str "uuid" the uuid of this match.
     */
    void fromJson(JsonObjectNode root);
}
