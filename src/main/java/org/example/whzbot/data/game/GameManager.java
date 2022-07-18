package org.example.whzbot.data.game;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.data.IUser;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.User;
import org.example.whzbot.data.game.Chess.ChessMatch;
import org.example.whzbot.data.game.Nylium.NyliumMatch;
import org.example.whzbot.data.game.TicTacToe.MatchTicTacToe;
import org.example.whzbot.helper.TranslateHelper;

/**
 * GameManager stores a map of implemented games.
 * Match pool would be kept by Pool.
 * <p>
 * sub-commands:
 * -init [game]   creates a new match, owned by the user.
 * -join [uuid]   try to join a match by uuid.
 * -watch [uuid]  observe a match by uuid.
 * -quit          leave the game.
 * -kick [id]     kick a player from the game.
 * -search [game] get a list of live games by game type.
 * -invite [id]   invite user for the current game by id.
 * -intro (game)  get introduction to the game.
 * -summon (AI)   summon an AI and add to the game.
 * -rule [rule]   set the rules for this match
 * -set [rule]    change default setting of a game.
 * -ready         set self to ready until match begin.
 * -move [move]   make a move.
 * -query (uuid)  get record of a match.
 */
public class GameManager {

    public static final HashMap<UUID, IMatch<? extends IGame>> matches = new HashMap<>();
    public static final HashMap<String, Function<IRule, IMatch<? extends IGame>>> game_factories = new HashMap<>();

    public static BiConsumer<Long, String> send = (Long id, String msg) -> System.out.println(id.toString() + "<-" + msg);
    public static BiFunction<TranslateHelper, String, String> translator = TranslateHelper::translate;

    public static void init(Bot bot) {
        game_factories.put("tictactoe", MatchTicTacToe::make);
        game_factories.put("TicTacToe", MatchTicTacToe::make);
        game_factories.put("JunYan", NyliumMatch::make);
        game_factories.put("junyan", NyliumMatch::make);
        game_factories.put("chess", ChessMatch::make);
        game_factories.put("Chess", ChessMatch::make);
        send = (Long id, String msg) -> {
            Friend f = bot.getFriend(id);
            if (f != null)
                f.sendMessage(msg);
        };
    }

    public static String executeCmd(IUser user, CommandHolder holder) {
        if (!holder.hasNext())
            return getReply(user, "game.err_empty");
        switch (holder.getNextWord()) {
            case "init": {
                if (!holder.hasNext())
                    return getReply(user, "game.err_no_game");
                String game_name = holder.getNextWord();
                Function<IRule, IMatch<? extends IGame>> maker = game_factories.get(game_name);
                if (maker == null)
                    return getReply(user, "game.err_no_game");

                // rules read game setting.
                String game_setting = holder.hasNext() ? holder.getRest() : "";

                // check no current game.
                String cur = user.getStorage("game.live");
                if (!cur.isEmpty())
                    return getReply(user, "game.err_in_game");
                IMatch<? extends IGame> match = maker.apply(null);
                match.join(user.getId());
                UUID uuid = UUID.randomUUID();
                match.assignUUID(uuid);
                matches.put(uuid, match);
                user.setStorage("game.live", uuid.toString());
                return getReply(user, "game.init", uuid.toString());
            }
            case "join": {
                if (!holder.hasNext()) {
                    return getReply(user, "game.search_empty");
                } else {
                    String uuid_str = holder.getNextArg();
                    IMatch<? extends IGame> match;
                    try {
                        UUID uuid = UUID.fromString(uuid_str);
                        match = matches.get(uuid);
                    } catch (IllegalArgumentException e) {
                        return getReply(user, "game.invalid_uuid");
                    }
                    if (match == null)
                        return getReply(user, "game.err_no_match");
                    match.join(user.getId());
                    send.accept(match.getPlayers()[0], getReply(
                            match.getPlayers()[0], "game.join", ""));

                    user.setStorage("game.live", match.getUUID().toString());
                    return getReply(user, "game.join");
                }
            }
            case "watch":
            case "ob":
            case "observe":
                return getReply(user, "game.watch");
            case "quit":
                return getReply(user, "game.quit");
            case "begin": {
                IMatch<? extends IGame> match = getMatch(user);
                if (match == null)
                    return getReply(user, "game.err_not_in_match");
                if (holder.hasNext() && holder.isNextWord() &&
                        holder.getNextWord().equals("self")) {
                    match.join(user.getId());
                }
                if (match.begin()) {
                    for (long id : match.getPlayers()) {
                        send.accept(id, getReply(id, "game.begin", user.getNickName()));
                    }
                } else {
                    return getReply(user, "game.not_begin");
                }
                long next_id = match.getNextPlayer();
                send.accept(
                        next_id,
                        getReply(next_id, "game.your_turn", match.getBoard().toString())
                );
                return getReply(user, "game.begin", match.getPlayers()[1]);
            }
            case "move": {
                IMatch<? extends IGame> match = getMatch(user);
                if (match == null)
                    return getReply(user, "game.err_not_in_match");
                if (match.getPhase() != 1)
                    return getReply(user, "game.err_not_playing");
                if (match.getNextPlayer() != user.getId())
                    return getReply(user, "game.err_not_your_turn");
                if (!holder.hasNext())
                    return Arrays.toString(match.hint());
                String move = holder.getNextArg();
                if (match.move(move)) {
                    if (match.getPhase() == 1) {
                        send.accept(
                                match.getNextPlayer(),
                                getReply(match.getNextPlayer(), "game.last_move", move)
                        );
                        send.accept(
                                match.getNextPlayer(),
                                getReply(match.getNextPlayer(), "game.your_turn", match.getBoard().toString())
                        );
                    } else {
                        for (long id : match.getPlayers()) {
                            send.accept(id,
                                    getReply(id, "game.end", String.valueOf(match.getResult(id)))
                            );
                            user.setStorage("game.live", "");
                        }
                    }
                    return getReply(user, "game.updated", match.getBoard().toString());
                } else {
                    return getReply(user, "game.err_invalid_move");
                }
            }
            default:
                return getReply(user, "game.unknown");
        }
    }

    private static String getReply(IUser user, String s) {
        return translator.apply(
                new TranslateHelper(s, new String[]{user.getNickName()}, 1),
                user.getLang());
    }

    private static String getReply(IUser user, String s, String s1) {
        return translator.apply(
                new TranslateHelper(s, new String[]{user.getNickName(), s1}, 1),
                user.getLang());
    }

    private static String getReply(long id, String s, String s1) {
        User user = Pool.getUser(id);
        return translator.apply(
                new TranslateHelper(s, new String[]{user.getNickName(), s1}, 1),
                user.getLang());
    }

    private static String getReply(IUser user, String s, long usr_id1) {
        User p2 = Pool.getUser(usr_id1);
        String s1 = p2.getNickName();
        if (s1.isEmpty())
            s1 = String.valueOf(usr_id1);
        return translator.apply(
                new TranslateHelper(s, new String[]{user.getNickName(), s1}, 1),
                user.getLang());
    }


    private static IMatch<? extends IGame> getMatch(IUser user) {
        String uuid_str = user.getStorage("game.live");
        if (uuid_str.isEmpty())
            return null;
        UUID uuid = UUID.fromString(uuid_str);
        return matches.get(uuid);
    }
}
