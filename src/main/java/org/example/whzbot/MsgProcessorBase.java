package org.example.whzbot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.contact.Contact;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHelper;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.Permission;
import org.example.whzbot.data.IUser;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.Character;
import org.example.whzbot.data.game.GameManager;
import org.example.whzbot.helper.DiceHelper;
import org.example.whzbot.helper.HttpHelper;
import org.example.whzbot.helper.ProbabilityHelper;
import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.helper.CardDeckHelper;
import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.Language;

import java.util.UUID;

import static org.example.whzbot.JavaMain.storing_dir;

/**
 * Message Processor Base
 * This class should handle received messages.
 * User permission, habits and prohibitions should be processed.
 * */
public abstract class MsgProcessorBase {
    protected AbstractMessageEvent event;
    public int event_type;
    public int msg_type;
    protected Bot bot;
    protected IUser user;
    protected Permission permission = Permission.ANYONE;

    protected MsgProcessorBase(AbstractMessageEvent event) {
        this.event = event;
        this.bot = event.getBot();
        this.user = Pool.getUser(this.event.getSender().getId());
        this.user.setName(this.event.getSenderName());
    }

    public AbstractMessageEvent getEvent() {
        return this.event;
    }

    public void reply(String str) {
        this.event.getSubject().sendMessage(str);
        this.debug(str);
        this.user.setStorage("last_reply", str);
    }

    public void replyTranslated(String str) {
        reply(new TranslateHelper(str, 1).translate(user.getLang()));
    }

    public void replyTranslated(String str, String val) {
        reply(new TranslateHelper(
                str,
                new String[]{user.getNickName(), val},
                1
        ).translate(user.getLang()));
    }

    public void send(String str, Contact someone) {
        someone.sendMessage(str);
    }

    public void replyQuote(String str) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void replyImage(String image_url) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void debug(String str) {
        this.bot.getLogger().debug(str);
    }

    public void debugString(String str) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            b.append((byte) str.charAt(i));
        }
        this.debug(b.toString());
    }

    /*
     * Use to process a message.
     * returns a int to trace stats.
     */
    abstract void process();

    protected int execute_command(CommandHolder holder) {
        String lang_name = user.getLang();
        if (!Command.hasPermit(holder.getCmd(), this.permission)) {
            reply(new TranslateHelper(
                    "no_permit",
                    new String[]{
                            user.getNickName(),
                            holder.getCmd().permission.toString(),
                            this.permission.toString()
                    }, 1
            ).translate(lang_name));
            return -1;
        }

        String[] result;
        switch (holder.getCmd()) {
            case echo:
                if (holder.hasNext())
                    reply(holder.getRest());
                else
                    reply("Aaaaaaaaaa...");
                break;
            case jrrp:
                int rp = (int) (100 * RandomHelper.jrrpRandom(this.event.getSender().getId()));
                reply(new TranslateHelper(
                        "jrrp.reply",
                        new String[]{user.getNickName(), String.valueOf(rp)},
                        1).translate(lang_name)
                );
                break;
            case omkj:
                int omkj_rp = (int) (100 * RandomHelper.jrrpRandom(this.event.getSender().getId()));
                String omkj_type;
                if (omkj_rp > 85)
                    omkj_type = "omkj_type0";
                else if (omkj_rp > 65)
                    omkj_type = "omkj_type1";
                else if (omkj_rp > 40)
                    omkj_type = "omkj_type2";
                else if (omkj_rp > 15)
                    omkj_type = "omkj_type3";
                else
                    omkj_type = "omkj_type4";

                reply(new TranslateHelper(
                        "omkj.reply",
                        new TranslateHelper[]{
                                new TranslateHelper(user.getNickName()),
                                CardDeckHelper.draw(omkj_type)
                        },
                        1).translate(lang_name)
                );
                break;

            case roll_det:
                int cutoff;
                boolean has_cutoff;
                String skill_name = "";
                String reason = null;
                if (holder.isNextInt()) {
                    cutoff = Integer.parseInt(holder.getNextInt());
                    has_cutoff = true;
                } else {
                    skill_name = holder.getNextWord();

                    if (holder.isNextInt()) {
                        cutoff = Integer.parseInt(holder.getNextInt());
                        has_cutoff = true;
                    } else {
                        cutoff = user.getCharacter().getSkill(skill_name);
                        has_cutoff = cutoff != -1;
                    }
                }
                if (holder.hasNext()) {
                    reason = holder.getRest();
                }

                if (has_cutoff) {
                    if (cutoff < 0 || cutoff > 100) {
                        reply(new TranslateHelper("roll_det.err_suc_rate", 1).translate(lang_name));
                    } else {
                        int d = RandomHelper.hundred();
                        String result_type = DiceHelper.rollResultName(
                                d, cutoff, user.getSetting("dice.rule", 0)
                        );

                        TranslateHelper tr;
                        if (reason == null) {
                            tr = new TranslateHelper(
                                    "roll_det.reply",
                                    new TranslateHelper[]{
                                            new TranslateHelper(user.getNickName()),
                                            new TranslateHelper(skill_name),
                                            new TranslateHelper(result_type, 1)
                                    },
                                    1
                            );
                        } else {
                            tr = new TranslateHelper(
                                    "roll_det.reply_reason",
                                    new TranslateHelper[]{
                                            new TranslateHelper(user.getNickName()),
                                            new TranslateHelper(skill_name),
                                            new TranslateHelper(result_type, 1),
                                            new TranslateHelper(reason)
                                    },
                                    1
                            );
                        }
                        reply(String.format(tr.translate(lang_name), d, cutoff));
                    }
                } else {
                    reply(new TranslateHelper("roll_det.err_unknown_prop", 1).translate(lang_name));
                }
                break;
            case roll:
                result = CommandHelper.roll_dice(user, holder).split(" ", 2);
                if (result[0].equals("err")) {
                    reply("roll.err." + result[1]);
                } else if (holder.hasNext()) {
                    reply(new TranslateHelper(
                            "roll.reply_reason",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    new TranslateHelper(result[1]),
                                    new TranslateHelper(holder.getRest())
                            },
                            1
                    ).translate(lang_name));
                } else {
                    reply(new TranslateHelper(
                            "roll.reply",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    new TranslateHelper(result[1]),
                            },
                            1
                    ).translate(lang_name));
                }
                break;
            case set_attr:
                result = CommandHelper.set_skill(user, holder).split(" ");
                switch (result[0]) {
                    case "err":
                        reply(new TranslateHelper(
                                "set_attr.err." + result[1], 1
                        ).translate(lang_name));
                        break;
                    case "clr":
                        reply(new TranslateHelper(
                                "set_attr.clr",
                                new String[]{user.getNickName()},
                                1
                        ).translate(lang_name));
                        break;
                    case "del":
                        reply(new TranslateHelper(
                                "set_attr.del",
                                new String[]{user.getNickName(), result[1]},
                                1
                        ).translate(lang_name));
                        break;
                    case "mod":
                        reply(new TranslateHelper(
                                "set_attr.mod",
                                new TranslateHelper[]{
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[3]),
                                        new TranslateHelper(result[1]),
                                        new TranslateHelper(result[2])
                                },
                                1
                        ).translate(lang_name));
                        break;
                    case "set":
                        if (result.length > 3) {
                            reply(new TranslateHelper(
                                    "set_attr.mod",
                                    new TranslateHelper[]{
                                            new TranslateHelper(user.getNickName()),
                                            new TranslateHelper(result[3]),
                                            new TranslateHelper(result[1]),
                                            new TranslateHelper(result[2])
                                    }, 1
                            ).translate(lang_name));
                        } else {
                            reply(new TranslateHelper(
                                    "set_attr.set",
                                    new TranslateHelper[]{
                                            new TranslateHelper(user.getNickName()),
                                            new TranslateHelper(result[2]),
                                            new TranslateHelper(result[1])
                                    }, 1
                            ).translate(lang_name));
                        }
                        break;
                    case "show":
                        reply(new TranslateHelper(
                                "set_attr.show",
                                new TranslateHelper[]{
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[2]),
                                        new TranslateHelper(result[1])
                                }, 1
                        ).translate(lang_name));
                        break;
                    default:
                        reply(new TranslateHelper(
                                "set_attr.err.unknown", 1
                        ).translate(lang_name));
                }
                break;
            case san_check:
                result = CommandHelper.san_check(user, holder).split(" ");
                switch (result[0]) {
                    case "fal":
                        reply(new TranslateHelper(
                                "rollSc",
                                new TranslateHelper[]{
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[1]),
                                        new TranslateHelper(result[2]),
                                        new TranslateHelper("rollFailure", 1),
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[3]),
                                        new TranslateHelper(result[4]),
                                },
                                1
                        ).translate(lang_name));
                        break;
                    case "suc":
                        reply(new TranslateHelper(
                                "rollSc",
                                new TranslateHelper[]{
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[1]),
                                        new TranslateHelper(result[2]),
                                        new TranslateHelper("rollRegularSuccess", 1),
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[3]),
                                        new TranslateHelper(result[4]),
                                },
                                1
                        ).translate(lang_name));
                        break;
                    case "err":
                        reply(new TranslateHelper(
                                "san_check.err" + result[1], 1
                        ).translate(lang_name));
                        break;
                    default:
                        reply(new TranslateHelper(
                                "san_check.err.unknown", 1
                        ).translate(lang_name));
                        break;
                }
                break;
            case en:
                reply(CommandHelper.enhance(user, holder));
                break;
            case character: {
                UUID uuid = user.getCharacterUUID();
                if (!holder.hasNext()) {
                    if (uuid != null)
                        reply(uuid.toString());
                    else
                        reply("You have not initialize your character.");
                    break;
                } else if (holder.isNextWord()) {
                    switch (holder.getNextWord()) {
                        case "drop":
                            user.setCharacter(null).setUsed(false);
                            reply("You drop you character " + uuid.toString());
                            break;
                        case "use":
                            if (!holder.hasNext()) {
                                reply("need uuid");
                                break;
                            }
                            try {
                                UUID new_id = UUID.fromString(holder.getRest());
                                Character ch = Pool.getCharacter(new_id);
                                if (ch.isUsed()) {
                                    reply("someone is using this character.");
                                    break;
                                } else
                                    ch.setUsed(true);
                                ch = user.setCharacter(ch);
                                if (ch != null) {
                                    ch.setUsed(false);
                                    reply("you have changed character, you old char is " +
                                            uuid.toString());
                                } else {
                                    reply("you are now using a new character.");
                                }
                                break;
                            } catch (IllegalArgumentException e) {
                                reply("not uuid");
                                break;
                            }
                        default:
                            break;
                    }
                    break;
                } else {
                    reply("unknown arg");
                }
                break;
            }
            case draw: {
                String deck_name;
                int draw_count;

                if (holder.hasNext()) {
                    deck_name = holder.getNextWord();
                } else {
                    reply(new TranslateHelper(
                            "draw.illegal_arg", 1
                    ).translate(lang_name));
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 20) {
                    reply(new TranslateHelper(
                            "draw.limited", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count < 0) {
                    reply(new TranslateHelper(
                            "draw.positive", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count == 1) {
                    reply(new TranslateHelper(
                            "draw.card",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    CardDeckHelper.draw(deck_name)
                            },
                            1).translate(lang_name)
                    );
                } else {
                    reply(new TranslateHelper(
                            "draw.card",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    new TranslateHelper(
                                            "|",
                                            CardDeckHelper.draw(deck_name, draw_count),
                                            4
                                    )
                            },
                            1
                    ).translate(lang_name));
                }
                break;
            }
            case help:
                reply(new TranslateHelper(
                        holder.hasNext() ? holder.getNextWord() : "help",
                        2
                ).translate(lang_name));
                break;

            case deck: {
                if (holder.hasNext()) {
                    String deck_name = holder.getNextWord();
                    if (!GlobalVariable.CARD_DECK.containsKey(deck_name)) {
                        reply(new TranslateHelper(
                                "deck.err.deck_not_found", 1
                        ).translate(lang_name));
                    } else {
                        reply(new TranslateHelper(
                                "deck.show",
                                new TranslateHelper[]{
                                        new TranslateHelper(),
                                        new TranslateHelper(deck_name),
                                        new TranslateHelper(
                                                ", ",
                                                GlobalVariable.CARD_DECK.get(deck_name),
                                                4
                                        )
                                },
                                1
                        ).translate(lang_name));
                    }
                } else {
                    reply(new TranslateHelper(
                            "deck.list",
                            new TranslateHelper[]{
                                    new TranslateHelper(),
                                    new TranslateHelper(
                                            ", ",
                                            GlobalVariable.CARD_DECK.keySet().toArray(new String[0]),
                                            4
                                    )
                            },
                            1
                    ).translate(lang_name));
                }
                break;
            }
            case gacha: {
                String gacha_name;
                int draw_count;

                if (holder.hasNext()) {
                    gacha_name = holder.getNextWord();
                } else {
                    reply(new TranslateHelper(
                            "draw.illegal_arg", 1
                    ).translate(lang_name));
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 100) {
                    reply(new TranslateHelper(
                            "draw.limited", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count < 0) {
                    reply(new TranslateHelper(
                            "draw.positive", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count == 1) {
                    reply(new TranslateHelper(
                            "draw.card",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    CardDeckHelper.gacha(gacha_name, user)
                            },
                            1).translate(lang_name)
                    );
                } else {
                    TranslateHelper[] temp = new TranslateHelper[draw_count];
                    for (int i = 0; i < draw_count; i++)
                        temp[i] = CardDeckHelper.gacha(gacha_name, user);
                    reply(new TranslateHelper(
                            "draw.card",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    new TranslateHelper(
                                            "|",
                                            temp,
                                            4
                                    )
                            },
                            1
                    ).translate(lang_name));
                }
                break;
            }
            case bnmd: {
                int x, n;
                double p;
                if (holder.isNextInt()) {
                    x = Integer.parseInt(holder.getNextInt());
                } else {
                    reply("err no_arg");
                    break;
                }
                if (holder.isNextInt()) {
                    n = Integer.parseInt(holder.getNextInt());
                } else {
                    reply("err no_arg");
                    break;
                }
                if (holder.isNextSignedInt()) {
                    p = Double.parseDouble(holder.getNextFloat());
                } else
                    p = 0.5;
                double prob = ProbabilityHelper.binomial_distribution(x, n, p);
                reply(Double.toString(prob));
                break;
            }
            case nord: {
                double x;
                if (holder.isNextSignedInt()) {
                    x = Double.parseDouble(holder.getNextFloat());
                } else {
                    reply("err no_arg");
                    break;
                }
                double prob = ProbabilityHelper.normal_distribution(x);
                reply(prob + " " + x);
                break;
            }
            case http: {
                if (!holder.hasNext()) {
                    reply("err no_arg");
                    break;
                }
                String url = holder.getNextArg();
                if (!holder.hasNext()) {
                    HttpHelper.testFunc(url);
                    reply("suc");
                    break;
                }
                String file_name = holder.getNextArg();
                int code = HttpHelper.httpToFile(
                        url,
                        storing_dir + "/download/" + file_name
                );
                reply(Integer.toString(code));
                reply(url);
                break;
            }
            case image: {
                if (!holder.hasNext()) {
                    reply("no_arg");
                    break;
                }
                switch (holder.getNextWord()) {
                    case "find":
                    case "search":
                        replyTranslated("whz.disabled");
                        break;
                        /*String cool_down_key = "image.search";
                        if (!CoolDown.isCool(cool_down_key) &&
                                !Permission.hasPermit(Permission.BOT_OWNER, this.permission)) {
                            replyTranslated("image.cool_down",
                                    String.valueOf((CoolDown.checkCoolDown(cool_down_key) / 1000))
                            );
                            break;
                        }
                        if (!holder.hasNext()) {
                            String url = user.getStorage("last_reply");
                            if (!url.isEmpty() && HttpHelper.isUri(url)) {
                                reply(HttpHelper.ascii2d(url));
                                CoolDown.setCoolDown(cool_down_key, 120000);
                            } else {
                                replyTranslated("image.no_image");
                            }
                        } else {
                            String url = holder.getRest();
                            reply(HttpHelper.ascii2d(url));
                            CoolDown.setCoolDown(cool_down_key, 120000);
                        }
                        break;*/
                    case "save":
                        replyTranslated("whz.not_implemented");
                        break;
                    default:
                        replyTranslated("image.unknown_arg");
                        break;
                }
                break;
            }
            case game: {
                if (!holder.hasNext()) {
                    reply("game.no_arg");
                    break;
                }
                reply(GameManager.executeCmd(user, holder));
                break;
            }
            case set: {
                if (!holder.hasNext()) {
                    replyTranslated("set.no_val");
                    break;
                }
                String path = holder.getNextArg();

                if (path.equals("show") || path.equals("query")) {
                    if (!holder.hasNext()) {
                        replyTranslated("set.no_arg");
                        break;
                    }
                    path = holder.getNextArg();
                    if (!GlobalVariable.DEFAULT_USER_SETTING.containsKey(path)
                            && !GlobalVariable.DEFAULT_GROUP_SETTING.containsKey(path))
                        replyTranslated("set.invalid_key");
                    else
                        replyTranslated("set.reply",
                                user.getSetting(path, "null")
                        );
                    break;
                }

                // replace some short path
                if (path.equals("dd"))
                    path = "dice.default_dice";
                if (path.equals("rule"))
                    path = "dice.rule";
                if (path.equals("bot"))
                    path = "bot.on";

                if (!GlobalVariable.DEFAULT_USER_SETTING.containsKey(path)
                    && !GlobalVariable.DEFAULT_GROUP_SETTING.containsKey(path))
                    replyTranslated("set.invalid_key");
                else {
                    if (!holder.hasNext()) {
                        user.removeSetting(path);
                        replyTranslated("set.removed");
                    } else {
                        String val = holder.getNextArg();
                        if (val.equals("on"))
                            val = "1";
                        else if (val.equals("off"))
                            val = "0";
                        user.changeSetting(path, val);
                        replyTranslated("set.changed");
                    }
                }
                break;
            }
            case lang: {
                if (!holder.hasNext()) {
                    replyTranslated("lang.current", lang_name);
                } else {
                    String new_lang_name = holder.getNextArg();
                    if (GlobalVariable.LANGUAGE_LIST.containsKey(new_lang_name)) {
                        user.setLang(new_lang_name);
                        replyTranslated("lang.changed");
                    } else {
                        replyTranslated("lang.unknown_lang");
                    }
                }
                break;
            }
            case nn:
            case nnn:{
                if (!holder.hasNext()) {
                    user.getCharacter().setName("");
                    replyTranslated("name.removed", "");
                } else if (holder.isNextWord()) {
                    String arg = holder.getNextArg();
                    String name = Language.getLanguage(user.getLang()).getRandomName(arg);
                    if (!name.equals("lang_not_support")) {
                        user.getCharacter().setName(name);
                        replyTranslated("name.changed", name);
                    } else {
                        name = arg + " " + holder.getRest();
                        if (name.length() > 20) {
                            replyTranslated("name.too_long");
                        }
                        else {
                            user.getCharacter().setName(name);
                            replyTranslated("name.changed", name);
                        }
                    }
                } else {
                    String name = holder.getRest();
                    if (name.length() > 20) {
                        replyTranslated("name.too_long");
                    }
                    else {
                        user.getCharacter().setName(name);
                        replyTranslated("name.changed", name);
                    }
                }
                break;
            }
            case reload: {
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                if (!holder.hasNext()) {
                    replyTranslated(new TranslateHelper(
                            "illegal_arg", 1
                    ).translate(lang_name));
                    break;
                }
                switch (holder.getNextWord()) {
                    case "alias":
                        JavaMain.loadAlias();
                        reply("Reloaded Alias");
                        break;
                    case "lang":
                    case "language":
                        JavaMain.loadLanguage();
                        reply("Reloaded Language");
                        break;
                    case "card deck":
                    case "carddeck":
                    case "cardDeck":
                        JavaMain.loadCardDeck();
                        reply("Reloaded Card Deck");
                        break;
                    case "all":
                        JavaMain.loadCardDeck();
                        JavaMain.loadLanguage();
                        JavaMain.loadAlias();
                        reply("Reloading Everything");
                        break;
                    default:
                        reply("Reloading! Nope, it's a plank.");
                        break;
                }
                break;
            }
            case save:
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                JavaMain.saveProfile();
                reply("saved.");
            case exit:
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                JavaMain.saveProfile();
                reply("ready to exit.");
                bot.close();
                JavaMain.running = false;
                break;
            case version:
                reply(JavaMain.version);
                break;
            case unknown:
            default:
                return 0;
        }
        return holder.getCmd().type.getId();
    }
}
