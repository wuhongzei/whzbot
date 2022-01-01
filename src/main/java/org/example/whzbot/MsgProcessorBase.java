package org.example.whzbot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.MessageChain;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHelper;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.Permission;
import org.example.whzbot.data.IUser;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.Character;
import org.example.whzbot.helper.DiceHelper;
import org.example.whzbot.helper.HttpHelper;
import org.example.whzbot.helper.ProbabilityHelper;
import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.helper.CardDeckHelper;
import org.example.whzbot.storage.GlobalVariable;

import java.util.UUID;

import static org.example.whzbot.JavaMain.storing_dir;

/*
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

    public void reply(MessageChain reply_chain) {
        this.event.getSubject().sendMessage(reply_chain);
    }

    public void reply(String str, Contact someone) {
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
        if (Command.hasPermit(holder.getCmd(), this.permission)) {
            reply(new TranslateHelper(
                    "no_permit",
                    1
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
                        "jrrp",
                        new String[]{this.event.getSenderName(), String.valueOf(rp)},
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
                        "omkj",
                        new TranslateHelper[]{CardDeckHelper.draw(omkj_type)},
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
                        reply(new TranslateHelper("successRateErr", 1).translate(lang_name));
                    } else {
                        int d = RandomHelper.hundred();
                        String result_type = DiceHelper.rollResultName(d, cutoff, 0);

                        TranslateHelper tr;
                        if (reason == null) {
                            tr = new TranslateHelper(
                                    "rollSkill",
                                    new TranslateHelper[]{
                                            new TranslateHelper(this.event.getSenderName()),
                                            new TranslateHelper(skill_name),
                                            new TranslateHelper(result_type, 1)
                                    },
                                    1
                            );
                        } else {
                            tr = new TranslateHelper(
                                    "rollSkillReason",
                                    new TranslateHelper[]{
                                            new TranslateHelper(this.event.getSenderName()),
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
                    reply(new TranslateHelper("unknownPropErr", 1).translate(lang_name));
                }
                break;
            case roll:
                result = CommandHelper.roll_dice(user, holder).split(" ", 2);
                if (result[0].equals("err")) {
                    reply(result[1]);
                } else if (holder.hasNext()) {
                    reply(new TranslateHelper(
                            "rollDiceReason",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    new TranslateHelper(result[1]),
                                    new TranslateHelper(holder.getRest())
                            },
                            1
                    ).translate(lang_name));
                } else {
                    reply(new TranslateHelper(
                            "rollDice",
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
                        reply(result[1]);
                        break;
                    case "clr":
                        reply(new TranslateHelper("propCleared", 1).
                                translate(lang_name));
                        break;
                    case "del":
                        reply(new TranslateHelper(
                                "propDeleted", 1
                        ).translate(lang_name));
                        break;
                    case "mod":
                        reply(new TranslateHelper(
                                "stModify",
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
                                    "stModify",
                                    new TranslateHelper[]{
                                            new TranslateHelper(user.getNickName()),
                                            new TranslateHelper(result[3]),
                                            new TranslateHelper(result[1]),
                                            new TranslateHelper(result[2])
                                    }, 1
                            ).translate(lang_name));
                        } else {
                            reply(new TranslateHelper(
                                    "stDetail",
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
                                "stShow",
                                new TranslateHelper[]{
                                        new TranslateHelper(user.getNickName()),
                                        new TranslateHelper(result[2]),
                                        new TranslateHelper(result[1])
                                }, 1
                        ).translate(lang_name));
                        break;
                    default:
                        reply("unknown err");
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
                    default:
                        if ("san_unset".equals(result[1])) {
                            reply(new TranslateHelper("sanEmpty", 1)
                                    .translate(lang_name));
                        } else {
                            reply(new TranslateHelper("scInvalid", 1)
                                    .translate(lang_name));
                        }
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
                            "illegalArgument", 1
                    ).translate(lang_name));
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 20) {
                    reply(new TranslateHelper(
                            "drawLimited", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count < 0) {
                    reply(new TranslateHelper(
                            "drawPositive", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count == 1) {
                    reply(new TranslateHelper(
                            "drawCard",
                            new TranslateHelper[]{
                                    new TranslateHelper(this.event.getSenderName()),
                                    CardDeckHelper.draw(deck_name)
                            },
                            1).translate(lang_name)
                    );
                } else {
                    reply(new TranslateHelper(
                            "drawCard",
                            new TranslateHelper[]{
                                    new TranslateHelper(this.event.getSenderName()),
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
                        reply(new TranslateHelper("deckNotFound", 1).translate(lang_name));
                    } else {
                        reply(new TranslateHelper(
                                "showCardDeck",
                                new TranslateHelper[]{
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
                            "listCardDeck",
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
                            "illegalArgument", 1
                    ).translate(lang_name));
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 100) {
                    reply(new TranslateHelper(
                            "drawLimited", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count < 0) {
                    reply(new TranslateHelper(
                            "drawPositive", 1
                    ).translate(lang_name));
                    break;
                } else if (draw_count == 1) {
                    reply(new TranslateHelper(
                            "drawCard",
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
                            "drawCard",
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
                    reply("err no_arg");
                    break;
                }
                switch (holder.getNextWord()) {
                    case "find":
                    case "search":
                        String cool_down_key = "image.search";
                        if (!CoolDown.isCool(cool_down_key) &&
                                !Permission.hasPermit(Permission.BOT_OWNER, this.permission)) {
                            reply("image.cool_down" + (CoolDown.checkCoolDown(cool_down_key) / 1000));
                            break;
                        }
                        if (!holder.hasNext()) {
                            String url = user.getStorage("last_reply");
                            if (!url.isEmpty() && HttpHelper.isUri(url)) {
                                reply(HttpHelper.ascii2d(url));
                                CoolDown.setCoolDown(cool_down_key, 120000);
                            } else {
                                reply("image.no_image");
                            }
                        }
                        else {
                            String url = holder.getRest();
                            reply(HttpHelper.ascii2d(url));
                            CoolDown.setCoolDown(cool_down_key, 120000);
                        }
                        break;
                    case "save":
                        reply("err whz.not_implemented");
                        break;
                    default:
                        reply("err image.unknown_arg");
                        break;
                }
                break;
            }
            case set: {
                if (!holder.hasNext()) {
                    reply("no_val");
                    break;
                }
                String path = holder.getNextArg();

                // replace some short path
                if (path.equals("dd"))
                    path = "dice.default_dice";

                if (!holder.hasNext()) {
                    user.removeSetting(path);
                    reply("set.removed");
                } else {
                    user.changeSetting(path, holder.getNextArg());
                    reply("set.changed");
                }
                break;
            }
            case reload: {
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                if (!holder.hasNext()) {
                    reply(new TranslateHelper(
                            "illegalArgument", 1
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
