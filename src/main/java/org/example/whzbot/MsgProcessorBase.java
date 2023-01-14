package org.example.whzbot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.RawForwardMessage;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.ExternalResource;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHelper;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.CommandType;
import org.example.whzbot.command.Permission;
import org.example.whzbot.data.Character;
import org.example.whzbot.data.IUser;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.game.GameManager;
import org.example.whzbot.helper.CardDeckHelper;
import org.example.whzbot.helper.DiceHelper;
import org.example.whzbot.helper.HttpHelper;
import org.example.whzbot.helper.ProbabilityHelper;
import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.Language;
import org.example.whzbot.storage.ProfileSaveAndLoad;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonLoader;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.example.whzbot.JavaMain.storing_dir;

/**
 * Message Processor Base
 * This class should handle received messages.
 * User permission, habits and prohibitions should be processed.
 */
public abstract class MsgProcessorBase {
    protected AbstractMessageEvent event;
    public int event_type;
    //public int msg_type;
    protected Bot bot;
    protected IUser user;
    protected Permission permission = Permission.ANYONE;
    protected SingleMessage msg;

    protected MsgProcessorBase() {
        this.event = null;
        this.bot = null;
        this.user = null;
        this.event_type = -1;
    }

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

    /**
     * Zip reply into forward message.
     * e.g ab, cd, ef -> ab\rcd\ref
     *
     * @param tstr     raw untranslated message
     * @param split    target loc to split on
     * @param max_line try to split such that each partition is shorter than max_line.
     */
    public void replyZipped(TranslateHelper tstr, String split, int max_line) {
        String rpy = tstr.translate(user.getLang());

        if (rpy.length() < max_line) {
            reply(rpy);
            return;
        }

        net.mamoe.mirai.contact.Group group;
        if (event instanceof GroupMessageEvent)
            group = ((GroupMessageEvent) event).getGroup();
        else
            group = bot.getGroupOrFail(235212404);
        ForwardMessageBuilder builder = new ForwardMessageBuilder(group);
        builder.setDisplayStrategy(new ForwardMessage.DisplayStrategy() {
            @NotNull
            @Override
            public String generateTitle(@NotNull RawForwardMessage forward) {
                return "BotReply";
            }
        });
        int i = 0, j;
        String partition;

        while (i + max_line < rpy.length()) {
            j = rpy.lastIndexOf(split, i + max_line);
            if (j < i) {
                j = rpy.indexOf(split, i + max_line);
                if (j < 0)
                    j = rpy.length();
            }
            partition = rpy.substring(i, j);
            i = j + split.length();
            builder.add(bot, new PlainText(partition));
        }
        if (i < rpy.length() - 1)
            builder.add(bot, new PlainText(rpy.substring(i)));

        debug(builder.toString());
        this.event.getSubject().sendMessage(builder.build());
    }

    public void send(String str, Contact someone) {
        someone.sendMessage(str);
    }

    public void replyQuote(String str) {
        throw new UnsupportedOperationException();
    }

    public void replyImage(String image_id) {
        this.event.getSubject().sendMessage(Image.fromId(image_id));
        //contact.uploadImage(resource); // 用来上传图片

    }

    public void replyImage(byte[] image_content) {
        try (InputStream stream = new ByteArrayInputStream(image_content)) { // 安全地使用 InputStream
            net.mamoe.mirai.contact.Contact contact = this.event.getSubject();
            ExternalResource resource = ExternalResource.create(stream);
            contact.uploadImage(resource); //用来上传文件
        } catch (IOException ignored) {}
    }

    public void sendFile(String file_name, byte[] file_content) {
        reply(String.format(
                "Failed to send \"%s\" here, file length %d",
                file_name, file_content.length)
        );
    }

    public void debug(String str) {
        this.bot.getLogger().debug(str);
    }

    /**
     * Use to process a message.
     * returns int to trace stats.
     * if return < 0, input must be incorrect.
     * if return = 0, this method processed nothing.
     * if return = 1, success.
     * return = 2, bot is @ but not success.
     */
    protected int process() {
        MessageQueue msgq = new MessageQueue(this.event.getMessage());
        this.event.getBot().getLogger().debug(msgq.toString());
        if (msgq.isEmpty())
            return -1;

        debug(msgq.toString());

        this.msg = msgq.poll();

        if (this.isBotOff())
            return -2;
        if (msg instanceof At) {
            int at_rtn = this.processAt();
            if (at_rtn != 0)
                return at_rtn;
            else if (msgq.isEmpty()) {
                replyTranslated("bot.empty_at");
                return 2;
            } else
                msg = msgq.poll();
        }

        if (msg instanceof LightApp) {
            this.processApp();
            return 1;
        } else if (msg instanceof Image) {
            this.processImage();
            return 1;
        } else if (msg instanceof FlashImage) {
            msg = ((FlashImage) msg).getImage();
            this.processImage();
            return 1;
        } else if (msg instanceof PlainText) {
            return this.processText();
        }
        return 0;
    }

    protected int processAt() {
        return 0;
    }

    protected void processApp() {
        if (this.user.getSetting("web.on", 0) != 0 &&
                this.user.getSetting("web.anti_app", 0) != 0
        ) {
            JsonNode node = new JsonLoader(((LightApp) msg).getContent(), "").load();
            if (node != null) {
                JsonNode str_node = node.get("meta.detail_1.host.qqdocurl");
                if (!(str_node instanceof JsonStringNode)) {
                    str_node = node.get("meta.detail_1.qqdocurl");
                    if (!(str_node instanceof JsonStringNode))
                        this.debug(((LightApp) msg).getContent());
                    else
                        reply(str_node.getContent().replaceAll("\\\\/", "/"));
                } else
                    reply(str_node.getContent().replaceAll("\\\\/", "/"));
            }
            this.debug(((LightApp) msg).getContent());
        }
    }

    protected void processImage() {
        if (this.user.getSetting("web.on", 0) != 0) {
            String url = Image.queryUrl((Image) msg);
            if (this.user.getSetting("web.image_url", 1) != 0)
                reply(url);
            else
                user.setStorage("last_reply", url);
        }
    }

    protected int processText() {
        String text = ((PlainText) msg).getContent();

        if (!CommandHolder.isCommand(text))
            return 1;
        CommandHolder holder = new CommandHolder(text, 1);

        boolean inhibited = false;
        if (this.isBotOff() && holder.getCmd() != Command.set) {
            inhibited = true;
        }
        if (!inhibited && this.isCmdOff(holder.getCmd().type)) {
            //inhibited = true;
            replyTranslated("bot.inhibited");
            return 1;
        }

        if (!inhibited) {
            int rtn = this.execute_command(holder);
            if (rtn == 0) {
                reply(new TranslateHelper(
                        "unknown_command", 1
                ).translate(user.getLang()));
            }
        }
        return 0;
    }

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
            case none:
                break;
            case echo:
                if (holder.hasNext())
                    reply(holder.getRest());
                else
                    reply("Aaaaaaaaaa...");
                break;
            case repeat:
                if (holder.hasNext())
                    reply(holder.getRest());
                else
                    reply(this.user.getStorage("last_reply"));
                break;
            case exec:
                if (holder.hasNext()) {
                    reply(MsgProcessorShort.wrapper(
                            new MsgProcessorShort(this.user),
                            holder.getRest()
                    ));
                }
                break;
            case jrrp:
                int rp = (int) (100 * RandomHelper.jrrpRandom(this.user.getId()));
                reply(new TranslateHelper(
                        "jrrp.reply",
                        new String[]{user.getNickName(), String.valueOf(rp)},
                        1).translate(lang_name)
                );
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
                                "san_check.err." + result[1], 1
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
                } else {
                    TranslateHelper card_val;
                    if (draw_count == 1) {
                        card_val = CardDeckHelper.draw(deck_name);
                    } else {
                        card_val = new TranslateHelper(
                                "|",
                                CardDeckHelper.draw(deck_name, draw_count),
                                4
                        );
                    }
                    String cards_result = new TranslateHelper(
                            "draw.card",
                            new TranslateHelper[]{
                                    new TranslateHelper(user.getNickName()),
                                    card_val},
                            1
                    ).translate(user.getLang());
                    reply(MsgProcessorShort.wrapper(
                            new MsgProcessorShort(this.user),
                            cards_result
                    ));
                }
                break;
            }
            case help:
                reply(MsgProcessorShort.wrapper(
                        new MsgProcessorShort(this.user),
                        new TranslateHelper(
                                holder.hasNext() ? holder.getRest() : "help",
                                2
                        ).translate(lang_name)
                ));
                break;
            case deck: {
                if (holder.hasNext()) {
                    String deck_name = holder.getNextArg();
                    if (!GlobalVariable.CARD_DECK.containsKey(deck_name)) {
                        reply(new TranslateHelper(
                                "deck.err.deck_not_found", 1
                        ).translate(lang_name));
                    } else {
                        replyZipped(new TranslateHelper(
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
                        ), ",", 200);
                    }
                } else {
                    replyZipped(new TranslateHelper(
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
                    ), ",", 200);
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
            case web_save: {
                // http://owlcraft.cn/files/store/temp.json
                // characters.json; costumes.json; loc.json; namecards.json
                // https://enka.network/u/100084078/__data.json

                if (!holder.hasNext()) {
                    reply("err no_arg");
                    break;
                }
                String file_type = holder.getNextArg();
                if (!holder.hasNext()) {
                    reply("err no_arg");
                    break;
                }
                String url = holder.getNextArg();
                byte[] content = HttpHelper.httpToFile(url);
                reply(new String(content));
                switch (file_type) {
                    case "json":
                        String json_str = new String(content,
                                ProfileSaveAndLoad.detectCharset(content)
                        );
                        JsonNode node = new JsonLoader(json_str, "").load();
                        this.sendFile(
                                "temp.json",
                                node.toString(0, 60).getBytes(StandardCharsets.UTF_8)
                        );
                        break;
                    case "txt":
                    case "text":
                        this.sendFile(
                                "temp_text.txt",
                                content
                        );
                        break;
                    case "img":
                    case "image":
                        replyImage(content);
                        break;
                }
                break;
            }
            case genshin_stats: {
                int uid = user.getCharacter().getSkill("ys");
                if (uid < 100000000) {
                    reply("ys.uid_err");
                    break;
                }
                String url = String.format("https://enka.network/u/%d/__data.json", uid);
                reply("ys.getting_and_wait");
                byte[] content = HttpHelper.httpToFile(url);
                String json_str = new String(content,
                        ProfileSaveAndLoad.detectCharset(content)
                );
                JsonNode node = new JsonLoader(json_str, "").load();
                this.sendFile(
                        "temp.json",
                        node.toString(0, 60).getBytes(StandardCharsets.UTF_8)
                );
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
            case nnn: {
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
                        holder.revert(arg.length());
                        name = holder.getRest();
                        if (name.length() > 20) {
                            replyTranslated("name.too_long");
                        } else {
                            user.getCharacter().setName(name);
                            replyTranslated("name.changed", name);
                        }
                    }
                } else {
                    String name = holder.getRest();
                    if (name.length() > 20) {
                        replyTranslated("name.too_long");
                    } else {
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
                    replyTranslated("no_arg");
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
                        JavaMain.loadDefaultSetting();
                        reply("Reloaded Everything");
                        break;
                    default:
                        reply("Reloading! Nope, it's a plank.");
                        break;
                }
                break;
            }
            case update: {
                if (!holder.hasNext()) {
                    replyTranslated("no_arg");
                    break;
                }
                String update_type = holder.getNextWord();

                String path = holder.getNextArg();
                String value = holder.getNextArg();
                switch (update_type) {
                    case "alias": // "alias" "cmd (var)"
                        GlobalVariable.updateAlias(path, value);
                        reply("updated Alias");
                        break;
                    case "lang":
                    case "language": // "lang.[doc/var/ctl]" "path" "val"
                        int i = path.indexOf('.');
                        GlobalVariable.updateLanguage(
                                path.substring(0, i), path.substring(i + 1),
                                value, holder.getNextArg()
                        );
                        reply("updated Language");
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
                            reply("{" + '"' + path + "\":" + value + "}");
                            JsonNode temp = new JsonLoader(value, path).load();
                            if (temp == null) {
                                reply("invalid json");
                                break;
                            }
                            if (temp instanceof JsonListNode)
                                GlobalVariable.updateCardDeck(path, (JsonListNode) temp);
                            else {
                                reply("expect json list");
                                break;
                            }
                        }
                        reply("updated Card Deck");
                        break;
                    default:
                        reply("?");
                        break;
                }
                break;
            }
            case output: {
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                if (!holder.hasNext()) {
                    replyTranslated("no_arg");
                    break;
                }
                String inner_path = holder.getNextArg();
                if (!holder.hasNext()) {
                    replyTranslated("no_arg");
                    break;
                }
                String out_name = holder.getNextArg();
                String file_content = null;
                switch (inner_path) {
                    case "carddeck":
                        JsonObjectNode root = new JsonObjectNode("");
                        Json.reconstruct(
                                GlobalVariable.CARD_DECK, root,
                                (String[] ss) -> {
                                    JsonListNode node = new JsonListNode();
                                    for (String s : ss) {
                                        node.add(new JsonStringNode(s));
                                    }
                                    return node;
                                }
                        );
                        System.out.println(root);
                        file_content = root.toString(0, 60);
                        break;
                    case "variable":
                        String lang;
                        if (!holder.hasNext()) {
                            lang = "dummy";
                        } else
                            lang = holder.getNextWord();
                        if (GlobalVariable.LANGUAGE_LIST.containsKey(lang)) {
                            root = new JsonObjectNode("");
                            Json.reconstruct(
                                    GlobalVariable.LANGUAGES.get(lang).global_variables,
                                    root, JsonStringNode::new
                            );
                            file_content = root.toString(0, 60);
                        }
                        break;
                    case "helpdoc":
                        if (!holder.hasNext()) {
                            lang = "dummy";
                        } else
                            lang = holder.getNextWord();
                        if (GlobalVariable.LANGUAGE_LIST.containsKey(lang)) {
                            root = new JsonObjectNode("");
                            Json.reconstruct(
                                    GlobalVariable.LANGUAGES.get(lang).help_doc,
                                    root, JsonStringNode::new
                            );
                            file_content = root.toString(0, 60);
                        }
                        break;
                }
                if (file_content == null)
                    break;
                this.sendFile(
                        out_name, file_content.getBytes()
                );
                break;
            }
            case save:
                if (event.getSender().getId() != JavaMain.master_qq) {
                    reply("Who are you?");
                    break;
                }
                JavaMain.saveProfile();
                reply("saved.");
                break;
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

    protected boolean isBotOff() {
        return user.getSetting("bot.on", 1) == 0;
    }

    protected boolean isCmdOff(CommandType t) {
        switch (t) {
            case DICE:
                if (user.getSetting("dice.on", 1) == 0) {
                    return true;
                }
                break;
            case TAROT:
                if (user.getSetting("tarot.on", 0) == 0) {
                    return true;
                }
                break;
            case SIMCHAT:
                if (user.getSetting("simple_chat.on", 0) == 0) {
                    return true;
                }
                break;
            case MCSERVER:
                if (user.getSetting("mc_server.on", 0) == 0) {
                    return true;
                }
                break;
            case MATH:
                if (user.getSetting("math.on", 0) == 0) {
                    return true;
                }
                break;
            case GROUP:
                if (user.getSetting("group.on", 0) == 0) {
                    return true;
                }
                break;
            case WEB:
                if (user.getSetting("web.on", 0) == 0) {
                    return true;
                }
                break;
            case GENERAL:
                if (user.getSetting("general.on", 0) == 0) {
                    return true;
                }
                break;
        }
        return false;
    }
}
