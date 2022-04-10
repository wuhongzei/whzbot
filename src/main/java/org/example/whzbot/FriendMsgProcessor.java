package org.example.whzbot;

import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.SingleMessage;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.CommandType;
import org.example.whzbot.command.Permission;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonStringNode;

public class FriendMsgProcessor extends MsgProcessorBase {
    public FriendMsgProcessor(FriendMessageEvent event) {
        super(event);
        this.event_type = 1;
        if (this.user.getId() == JavaMain.master_qq)
            this.permission = Permission.BOT_OWNER;
    }

    public void process() {
        MessageQueue msgq = new MessageQueue(this.event.getMessage());
        this.event.getBot().getLogger().debug(msgq.toString());
        if (msgq.isEmpty())
            return;

        SingleMessage msg = msgq.poll();
        this.debug(msg.getClass().toString());

        if (msg instanceof LightApp) {
            if (this.user.getSetting("web.on", 1) != 0 &&
                    this.user.getSetting("web.anti_app", 1) != 0
            ) {
                JsonNode node = Json.fromString(((LightApp) msg).getContent());
                if (node != null) {
                    JsonNode str_node = node.get("meta.detail_1.host.qqdocurl");
                    if (!(str_node instanceof JsonStringNode)) {
                        str_node = node.get("meta.detail_1.qqdocurl");
                        if (!(str_node instanceof JsonStringNode))
                            reply("Cannot recognize");
                        else
                            reply(str_node.getContent().replaceAll("\\\\/", "/"));
                    } else
                        reply(str_node.getContent().replaceAll("\\\\/", "/"));
                }
                this.debug(((LightApp) msg).getContent());
            }
            return ;
        } else if (msg instanceof Image) {
            if (this.user.getSetting("web.on", 1) != 0) {
                String url = Image.queryUrl((Image) msg);
                if (this.user.getSetting("web.image_url", 1) != 0)
                    reply(url);
                else
                    user.setStorage("last_reply", url);
            }
            return;
        } else if (msg instanceof FlashImage) {
            if (this.user.getSetting("web.on", 1) != 0) {
                String url = Image.queryUrl(((FlashImage) msg).getImage());
                if (this.user.getSetting("web.image_url", 1) != 0)
                    reply(url);
                else
                    user.setStorage("last_reply", url);
            }
            return;
        }

        if (msg instanceof PlainText) {
            //this.debug(msg.toString());
            //this.debugString(msg.toString());
            String text = ((PlainText) msg).getContent();
            //this.debugString(msg.toString());

            if (!CommandHolder.isCommand(text))
                return;
            CommandHolder holder = new CommandHolder(text, 1);
            //this.debug(holder.getName());

            boolean inhibited = false;
            if (user.getSetting("bot.on", 1) == 0 &&
                    (holder.getCmd().type != CommandType.ADMIN ||
                            holder.getCmd() != Command.set)
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
                    if (user.getSetting("simple_chat.on", 1) == 0) {
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
                    if (user.getSetting("web.on", 1) == 0) {
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
