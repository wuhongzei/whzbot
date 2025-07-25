package org.example.whzbot;

import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.message.data.PlainText;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHelper;
import org.example.whzbot.command.CommandHelper2;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.data.Character;
import org.example.whzbot.data.IUser;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.game.GameManager;
import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.instance.StringVar;
import org.example.whzbot.helper.CardDeckHelper;
import org.example.whzbot.helper.DiceHelper;
import org.example.whzbot.helper.HttpHelper;
import org.example.whzbot.helper.ProbabilityHelper;
import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.Language;
import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonLoader;

import java.util.UUID;

public class MsgProcessorShort extends MsgProcessorBase {
    String reply;
    int stack_level = 0; //use to mark up level of recursion, prevent stack overflow.
    int exec_loc = -1;
    boolean suc;

    @SuppressWarnings("unused")
    public MsgProcessorShort(AbstractMessageEvent event) {
        super(event);
    }

    public MsgProcessorShort(IUser user_in) {
        super();
        this.user = user_in;
        this.suc = true;
    }

    public void reply(String str) {
        this.reply = str;
        this.user.setStorage("last_reply", str);
        this.suc = true;
    }

    public void err(String info) {
        this.reply = info;
        this.suc = false;
    }

    public void setMsg(String text) {
        this.msg = new PlainText(text);
    }

    public String getReply() {
        return this.reply;
    }

    public boolean isSuc() {
        return this.suc;
    }

    public void addLevel() {
        this.stack_level++;
    }

    public void decLevel() {
        this.stack_level--;
    }

    public boolean stackCheck() {
        return this.stack_level < 3;
    }


    protected int process() {
        if (this.isBotOff())
            return -2;
        if (this.msg instanceof PlainText)
            return this.processText();
        return -1;
    }


    public int processText() {
        String text = ((PlainText) msg).getContent();

        if (!CommandHolder.isCommand(text))
            return 0;
        CommandHolder holder;
        if (!CommandHolder.isCommand(text))
            holder = new CommandHolder(text, 0);
        else
            holder = new CommandHolder(text, 1);

        boolean inhibited = false;
        if (this.isBotOff() && holder.getCmd() != Command.set) {
            inhibited = true;
        }
        if (!inhibited && this.isCmdOff(holder.getCmd().type)) {
            return 1;
        }

        if (!inhibited) {
            int rtn = this.execute_command(holder);
            if (rtn < 1) {
                return 3;
            } else {
                this.exec_loc = holder.getCursor();
                return 1;
            }
        }
        return 3;
    }

    protected int execute_command(CommandHolder holder) {
        String lang_name = user.getLang();
        if (!Command.hasPermit(holder.getCmd(), this.permission)) {
            reply("err");
            return -1;
        }

        String[] result;
        switch (holder.getCmd()) {
            case echo:
                if (holder.hasNext())
                    reply(holder.getRest());
                break;
            case repeat:
                reply(this.user.getStorage("last_reply"));
                break;
            case exec:
                if (holder.hasNext() && this.stack_level < 16) {
                    this.stack_level++;
                    reply(MsgProcessorShort.wrapper(
                            this,
                            holder.getRest()
                    ));
                    this.stack_level--;
                }
                break;
            case jrrp:
                int rp = (int) (100 * RandomHelper.jrrpRandom(this.user.getId()));
                reply(String.valueOf(rp));
                break;
            case omkj:
                int omkj_rp = (int) (100 * RandomHelper.jrrpRandom(this.user.getId()));
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

                reply(CardDeckHelper.draw(omkj_type).translate(lang_name));
                break;

            case roll_det:
                int cutoff;
                boolean has_cutoff;
                String skill_name;
                if (holder.isNextInt()) {
                    cutoff = Integer.parseInt(holder.getNextInt());
                    has_cutoff = true;
                } else {
                    skill_name = holder.getNextWord();

                    if (holder.isNextInt()) {
                        cutoff = Integer.parseInt(holder.getNextInt());
                        has_cutoff = true;
                    } else {
                        cutoff = (int) user.getCharacter().getSkill(skill_name);
                        has_cutoff = cutoff != -1;
                    }
                }

                if (has_cutoff) {
                    if (cutoff < 0 || cutoff > 100) {
                        reply("err");
                    } else {
                        int d = RandomHelper.hundred();
                        String result_type = DiceHelper.rollResultName(
                                d, cutoff, user.getSetting("dice.rule", 0)
                        );
                        reply(String.format(
                                "%d/%d%s",
                                d, cutoff,
                                new TranslateHelper(result_type, 1).translate(lang_name)
                        ));
                    }
                } else {
                    reply("err");
                }
                break;
            case roll:
                result = CommandHelper.roll_dice(user, holder).split(" ");
                if (result[0].equals("err")) {
                    reply("err");
                } else
                    reply(result[1]);
                break;
            case set_attr:
                result = CommandHelper.set_skill(user, holder).split(" ");
                switch (result[0]) {
                    case "clr":
                        reply("clr");
                        break;
                    case "del":
                        reply("del");
                        break;
                    case "mod":
                        reply("mod" + result[1]);
                        break;
                    case "set":
                        reply("set" + result[1]);
                        break;
                    case "show":
                        reply(result[1]);
                        break;
                    default:
                        reply("err");
                }
                break;
            case san_check:
                result = CommandHelper.san_check(user, holder).split(" ");
                switch (result[0]) {
                    case "fal":
                        reply(new TranslateHelper("rollFailure", 1).translate(lang_name) +
                                result[3]);
                        break;
                    case "suc":
                        reply(new TranslateHelper("rollRegularSuccess", 1).translate(lang_name) +
                                result[3]);
                        break;
                    case "err":
                        reply(new TranslateHelper(
                                "san_check.err" + result[1], 1
                        ).translate(lang_name));
                        break;
                    default:
                        reply("err");
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
                        reply("null");
                    break;
                } else if (holder.isNextWord()) {
                    switch (holder.getNextWord()) {
                        case "drop":
                            user.setCharacter(null).setUsed(false);
                            reply(uuid.toString());
                            break;
                        case "use":
                            if (!holder.hasNext()) {
                                reply("err");
                                break;
                            }
                            try {
                                UUID new_id = UUID.fromString(holder.getNextArg());
                                Character ch = Pool.getCharacter(new_id);
                                if (ch.isUsed()) {
                                    reply("err");
                                    break;
                                } else
                                    ch.setUsed(true);
                                ch = user.setCharacter(ch);
                                if (ch != null) {
                                    ch.setUsed(false);
                                    reply("err");
                                } else {
                                    reply("suc");
                                }
                                break;
                            } catch (IllegalArgumentException e) {
                                reply("err");
                                break;
                            }
                        default:
                            break;
                    }
                    break;
                } else {
                    reply("err");
                }
                break;
            }
            case draw: {
                String deck_name;
                int draw_count;

                if (holder.hasNext()) {
                    deck_name = holder.getNextWord();
                } else {
                    reply("err");
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 20) {
                    reply("err");
                    break;
                } else if (draw_count < 0) {
                    reply("err");
                    break;
                } else if (draw_count == 1) {
                    reply(CardDeckHelper.draw(deck_name).translate(lang_name));
                } else {
                    reply(new TranslateHelper(
                            "|",
                            CardDeckHelper.draw(deck_name, draw_count),
                            4
                    ).translate(lang_name));
                }
                break;
            }
            case help:
                reply(new TranslateHelper(
                        holder.hasNext() ? holder.getNextArg() : "help",
                        2
                ).translate(lang_name));
                break;

            case deck: {
                if (holder.hasNext()) {
                    String deck_name = holder.getNextWord();
                    if (!GlobalVariable.CARD_DECK.containsKey(deck_name)) {
                        reply("err");
                    } else {
                        reply(new TranslateHelper(
                                ", ",
                                GlobalVariable.CARD_DECK.get(deck_name),
                                4
                        ).translate(lang_name));
                    }
                } else {
                    reply(new TranslateHelper(
                            ", ",
                            GlobalVariable.CARD_DECK.keySet().toArray(new String[0]),
                            4
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
                    reply("err");
                    break;
                }
                if (holder.isNextInt()) {
                    draw_count = Integer.parseInt(holder.getNextInt());
                } else
                    draw_count = 1;
                if (draw_count > 100) {
                    reply("err");
                    break;
                } else if (draw_count < 0) {
                    reply("err");
                    break;
                } else if (draw_count == 1) {
                    reply(CardDeckHelper.gacha(gacha_name, user).translate(lang_name)
                    );
                } else {
                    TranslateHelper[] temp = new TranslateHelper[draw_count];
                    for (int i = 0; i < draw_count; i++)
                        temp[i] = CardDeckHelper.gacha(gacha_name, user);
                    reply(new TranslateHelper(
                            "|",
                            temp,
                            4
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
                    reply("err");
                    break;
                }
                if (holder.isNextInt()) {
                    n = Integer.parseInt(holder.getNextInt());
                } else {
                    reply("err");
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
                    reply("err");
                    break;
                }
                double prob = ProbabilityHelper.normal_distribution(x);
                reply(Double.toString(prob));
                break;
            }
            case variable: {
                Result r = CommandHelper2.commandVariable(user, holder);
                if (r.isSuccess()) {
                    reply(((StringVar)r.get(2)).read());
                }
                else {
                    this.suc = false;
                    reply(r.get());
                }
                break;
            }
            case function: {
                Result r = CommandHelper2.commandFunction(user, holder);
                this.suc = r.isSuccess();
                reply(r.get());
                break;
            }
            case memory: {
                Result r = CommandHelper2.commandMemory(user, holder);
                this.suc = r.isSuccess();
                reply(r.get());
                break;
            }
            case http: {
                if (!holder.hasNext()) {
                    reply("err");
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
                        JavaMain.storing_dir + "/download/" + file_name
                );
                reply(Integer.toString(code));
                reply(url);
                break;
            }
            case image: {
                if (!holder.hasNext()) {
                    reply("err");
                    break;
                }
                switch (holder.getNextWord()) {
                    case "find":
                    case "search":
                        replyTranslated("whz.disabled");
                        break;
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
                    reply("err");
                    break;
                }
                reply(GameManager.executeCmd(user, holder));
                break;
            }
            case set: {
                if (!holder.hasNext()) {
                    reply("err");
                    break;
                }
                String path = holder.getNextArg();

                if (path.equals("show") || path.equals("query")) {
                    if (!holder.hasNext()) {
                        reply("err");
                        break;
                    }
                    path = holder.getNextArg();
                    if (!GlobalVariable.DEFAULT_USER_SETTING.containsKey(path)
                            && !GlobalVariable.DEFAULT_GROUP_SETTING.containsKey(path))
                        reply("err");
                    else
                        reply(user.getSetting(path, "null"));
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
                    reply("err");
                else {
                    if (!holder.hasNext()) {
                        user.removeSetting(path);
                        reply("err");
                    } else {
                        String val = holder.getNextArg();
                        if (val.equals("on"))
                            val = "1";
                        else if (val.equals("off"))
                            val = "0";
                        user.changeSetting(path, val);
                        replyTranslated("set");
                    }
                }
                break;
            }
            case lang: {
                if (!holder.hasNext()) {
                    reply(lang_name);
                } else {
                    String new_lang_name = holder.getNextArg();
                    if (GlobalVariable.LANGUAGE_LIST.containsKey(new_lang_name)) {
                        user.setLang(new_lang_name);
                        reply("changed");
                    } else {
                        reply("err");
                    }
                }
                break;
            }
            case nn:
            case nnn: {
                if (!holder.hasNext()) {
                    user.getCharacter().setName("");
                    reply("del");
                } else if (holder.isNextWord()) {
                    String arg = holder.getNextArg();
                    String name = Language.getLanguage(user.getLang()).getRandomName(arg);
                    if (!name.equals("lang_not_support")) {
                        user.getCharacter().setName(name);
                        reply(name);
                    } else {
                        holder.revert(arg.length());
                        name = holder.getRest();
                        if (name.length() > 20) {
                            reply("err");
                        } else {
                            user.getCharacter().setName(name);
                            reply(name);
                        }
                    }
                } else {
                    String name = holder.getRest();
                    if (name.length() > 20) {
                        reply("err");
                    } else {
                        user.getCharacter().setName(name);
                        reply(name);
                    }
                }
                break;
            }
            case version:
                reply(JavaMain.version);
                break;
            case update: {
                if (!holder.hasNext()) {
                    replyTranslated("illegal_arg");
                    break;
                }
                String update_type = holder.getNextWord();

                String path = holder.getNextArg();
                String value = holder.getNextArg();
                switch (update_type) {
                    case "alias": // "alias" "cmd (var)"
                        GlobalVariable.updateAlias(path, value);
                        reply("updated");
                        break;
                    case "lang":
                    case "language": // "lang.[doc/var/ctl]" "path" "val"
                        int i = path.indexOf('.');
                        GlobalVariable.updateLanguage(
                                path.substring(0, i), path.substring(i + 1),
                                value, holder.getNextArg()
                        );
                        reply("updated");
                        break;
                    case "deck": // ["deck.card" int]/["deck" json_list["cards"]"
                    case "carddeck":
                        holder.revert(1);
                        if (holder.isNextInt()) {
                            i = path.lastIndexOf('.');
                            GlobalVariable.updateCardDeck(
                                    path.substring(0, i), path.substring(i + 1),
                                    Integer.parseInt(value)
                            );
                        } else {
                            JsonListNode temp = (JsonListNode) new JsonLoader(value, path).load();
                            if (temp == null) {
                                reply("err_json");
                                break;
                            }
                            GlobalVariable.updateCardDeck(path, temp);
                        }
                        reply("updated");
                        break;
                    default:
                        reply("null");
                        break;
                }
                break;
            }
            case unknown:
            default:
                return 0;
        }
        return holder.getCmd().type.getId();
    }

    /**
     * execute every "{}" block which is command.
     * recursion is allow using .exec; without .exec, inner block will be reserved
     *
     * @param proc a valid processor.
     * @param str  input string.
     * @return input string with all "{}" blocks replaced with command result.
     */
    public static String wrapper(MsgProcessorShort proc, String str) {
        int i = str.indexOf('{');
        int j;
        int last_i = 0;
        String sub_str;
        StringBuilder builder = new StringBuilder();
        while (i != -1) {
            j = StringHelper.encloseBracket(str, i);
            if (i > 0 && str.charAt(i - 1) == '.') {
                str = str.substring(0, i - 1) + str.substring(i);
                j--;
                i = str.indexOf('{', j);
            }
            else if (j > 0 && j < str.length()) {
                sub_str = str.substring(i + 1, j);
                if (proc.stackCheck()) {
                    proc.addLevel();
                    String temp = wrapper(proc, sub_str);
                    if (!proc.isSuc()) { // todo: break recursion correctly.
                        proc.decLevel();
                        return temp;
                    }
                    proc.setMsg(temp);
                    proc.decLevel();
                } else
                    proc.setMsg(sub_str);

                if (proc.process() == 1) {
                    builder.append(str, last_i, i);
                    if (proc.isSuc())
                        builder.append(proc.getReply());
                    else {
                        return builder.toString();
                    }
                    last_i = j + 1;
                }
                i = str.indexOf('{', j);
            } else
                i = -1;
        }
        builder.append(str, last_i, str.length());
        return builder.toString();
    }

    public static String wrapperEntry(IUser user, String cmd) {
        return wrapper(new MsgProcessorShort(user), cmd);
    }
}
