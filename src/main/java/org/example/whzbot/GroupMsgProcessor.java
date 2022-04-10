package org.example.whzbot;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.Permission;
import org.example.whzbot.data.Group;
import org.example.whzbot.data.Pool;
import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonStringNode;

public class GroupMsgProcessor extends MsgProcessorBase {
    protected Group group;

    public GroupMsgProcessor(GroupMessageEvent event) {
        super(event);
        this.event_type = 2;
        this.group = Pool.getGroup(this.event.getSubject().getId());
        this.user = this.group.getMember(this.event.getSender().getId());
        this.permission = Permission.mergePermit(
                ((net.mamoe.mirai.contact.Member) this.event.getSender())
                        .getPermission().getLevel() + 1,
                this.user.getId() == JavaMain.master_qq ? 2 : 0
        );
    }

    public void process() {
        MessageQueue msgq = new MessageQueue(this.event.getMessage());
        this.event.getBot().getLogger().debug(msgq.toString());
        if (msgq.isEmpty())
            return;

        SingleMessage msg = msgq.poll();
        boolean is_at = false;
        if (msg instanceof At) {
            this.event.getBot().getLogger().debug(String.valueOf(((At) msg).getTarget()));
            this.event.getBot().getLogger().debug(String.valueOf(this.event.getBot().getId()));
            if (((At) msg).getTarget() != this.event.getBot().getId())
                return;

            msg = msgq.poll();
            if (msg == null) {
                reply("why call me?");
                return;
            } else
                is_at = true;
        } else if (msg instanceof LightApp) {
            if (this.user.getSetting("web.on", 0) != 0 &&
                    this.user.getSetting("web.anti_app", 0) != 0
            ) {
                JsonNode node = Json.fromString(((LightApp) msg).getContent());
                if (node != null) {
                    JsonNode str_node = node.get("meta.detail_1.host.qqdocurl");
                    if (!(str_node instanceof JsonStringNode)) {
                        str_node = node.get("meta.detail_1.qqdocurl");
                        if (!(str_node instanceof JsonStringNode)) {
                            //reply("Cannot recognize");
                            this.debug(((LightApp) msg).getContent());
                        }
                        else
                            reply(str_node.getContent().replaceAll("\\\\/", "/"));
                    } else
                        reply(str_node.getContent().replaceAll("\\\\/", "/"));
                }
            }
            return;
        } else if (msg instanceof Image) {
            if (this.user.getSetting("web.on", 0) != 0) {
                String url = Image.queryUrl((Image) msg);
                if (this.user.getSetting("web.image_url", 0) != 0)
                    reply(url);
                else
                    user.setStorage("last_reply", url);
            }
            return;
        } else if (msg instanceof FlashImage) {
            if (this.user.getSetting("web.on", 0) != 0) {
                String url = Image.queryUrl(((FlashImage) msg).getImage());
                if (this.user.getSetting("web.image_url", 0) != 0)
                    reply(url);
                else
                    user.setStorage("last_reply", url);
            }
            return;
        }

        if (msg instanceof PlainText) {
            String text = ((PlainText) msg).getContent();
            if (is_at) {
                text = text.replaceFirst(String.format("@%s", this.bot.getNick()), "");
                text = text.substring(StringHelper.skipSpace(text, 0));
            }
            //this.debug(text);

            if (!CommandHolder.isCommand(text))
                return;
            if ((text.isBlank()) && is_at) {
                replyTranslated("bot.empty_call");
                return;
            }

            CommandHolder holder = new CommandHolder(text, 1);
            //this.debug(holder.getName());

            boolean inhibited = false;
            if (user.getSetting("bot.on", 1) == 0 &&
                    holder.getCmd() != Command.set
            ) {
                inhibited = true;
            }
            switch (holder.getCmd().type) {
                case DICE:
                    if (user.getSetting("dice.on", 1) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case TAROT:
                    if (user.getSetting("tarot.on", 1) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case SIMCHAT:
                    if (user.getSetting("simple_chat.on", 0) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case MCSERVER:
                    if (user.getSetting("mc_server.on", 0) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case MATH:
                    if (user.getSetting("math.on", 1) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case GROUP:
                    if (user.getSetting("group.on", 0) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case WEB:
                    if (user.getSetting("web.on", 0) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
                case GENERAL:
                    if (user.getSetting("general.on", 1) == 0) {
                        replyTranslated("bot.inhibited");
                        inhibited = true;
                    }
                    break;
            }

            if (!inhibited) {
                int rtn = super.execute_command(holder);
                if (rtn == 0) {
                    reply(new TranslateHelper(
                            "unknown_command", 1
                    ).translate(user.getLang()));
                }
            }
        }
    }
}
