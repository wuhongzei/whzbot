package org.example.whzbot.data.game;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.data.IUser;
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
    public static BiFunction<String, String, String> translator = (String msg, String lang) -> {
        return new TranslateHelper(msg, 1).translate(lang);
    };

    public static void init(Bot bot) {
        game_factories.put("tictactoe", MatchTicTacToe::make);
        game_factories.put("TicTacToe", MatchTicTacToe::make);
        game_factories.put("JunYan", NyliumMatch::make);
        game_factories.put("junyan", NyliumMatch::make);
        send = (Long id, String msg) -> {
            Friend f = bot.getFriend(id);
            if (f != null)
                f.sendMessage(msg);
        };
    }

    public static String executeCmd(IUser user, CommandHolder holder) {
        if (!holder.hasNext())
            return "game.err_empty";
        switch (holder.getNextWord()) {
            case "init": {
                if (!holder.hasNext())
                    return "game.err_no_game";
                String game_name = holder.getNextWord();
                Function<IRule, IMatch<? extends IGame>> maker = game_factories.get(game_name);
                if (maker == null)
                    return "game.err_no_game";

                // rules read game setting.
                String game_setting = holder.hasNext() ? holder.getRest(): "";

                // check no current game.
                String cur = user.getStorage("game.live");
                if (!cur.isEmpty())
                    return "game.err_in_game";
                IMatch<? extends IGame> match = maker.apply(null);
                match.join(user.getId());
                UUID uuid = UUID.randomUUID();
                match.assignUUID(uuid);
                matches.put(uuid, match);
                user.setStorage("game.live", uuid.toString());
                return "game.init " + uuid.toString();
            }
            case "join": {
                if (!holder.hasNext()) {
                    return "game.search_empty";
                } else {
                    String uuid_str = holder.getNextArg();
                    IMatch<? extends IGame> match;
                    try {
                        UUID uuid = UUID.fromString(uuid_str);
                        match = matches.get(uuid);
                    } catch (IllegalArgumentException e) {
                        return "game.invalid_uuid";
                    }
                    if (match == null)
                        return "game.err_no_match";
                    match.join(user.getId());
                    send.accept(match.getPlayers()[0], "game.joined");

                    user.setStorage("game.live", match.getUUID().toString());
                    return "game.join";
                }
            }
            case "watch":
            case "ob":
            case "observe":
                return "game.watch";
            case "quit":
                return "game.quit";
            case "begin": {
                IMatch<? extends IGame> match = getMatch(user);
                if (match == null)
                    return "game.err_not_in_match";
                if (holder.hasNext() && holder.isNextWord() &&
                        holder.getNextWord().equals("self")) {
                    match.join(user.getId());
                }
                if (match.begin()) {
                    for (long id : match.getPlayers()) {
                        send.accept(id, "game.begin");
                    }
                } else {
                    return "game.not_begin";
                }
                long next_id = match.getNextPlayer();
                send.accept(next_id, "game.your_turn " + match.getBoard());
                return "game.begin";
            }
            case "move": {
                IMatch<? extends IGame> match = getMatch(user);
                if (match == null)
                    return "game.err_not_in_match";
                if (match.getPhase() != 1)
                    return "game.err_not_playing";
                if (match.getNextPlayer() != user.getId())
                    return "game.err_not_your_turn";
                String move = holder.getNextArg();
                if (match.move(move)) {
                    if (match.getPhase() == 1) {
                        send.accept(match.getNextPlayer(), "game.last_move" + move);
                        send.accept(user.getId(), "game.updated " + match.getBoard());
                    } else {
                        for (long id : match.getPlayers()) {
                            send.accept(id, "game.end" + match.getResult(id));
                            user.setStorage("game.live", "");
                        }
                    }
                    return "game.updated " + match.getBoard();
                } else {
                    return "game.err_invalid_move";
                }
            }
            default:
                return "game.unknown";
        }
    }

    private static IMatch<? extends IGame> getMatch(IUser user) {
        String uuid_str = user.getStorage("game.live");
        if (uuid_str.isEmpty())
            return null;
        UUID uuid = UUID.fromString(uuid_str);
        return matches.get(uuid);
    }
}
