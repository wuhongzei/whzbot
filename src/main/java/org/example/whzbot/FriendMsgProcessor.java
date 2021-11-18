package org.example.whzbot;

import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;

import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.helper.TranslateHelper;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonStringNode;

public class FriendMsgProcessor extends MsgProcessorBase {
    public FriendMsgProcessor(FriendMessageEvent event) {
        super(event);
        this.event_type = 1;
    }

    public void process() {
        MessageQueue msgq = new MessageQueue(this.event.getMessage());
        this.event.getBot().getLogger().debug(msgq.toString());
        if (msgq.isEmpty())
            return;

        SingleMessage msg = msgq.poll();
        this.debug(msg.getClass().toString());

        if (msg instanceof LightApp) {
            JsonNode node = Json.fromString(
                    StringHelper.parseRichText(msg.toString(), "app"));
            if (node != null) {
                JsonNode str_node = node.get("node.get(\"meta.detail_1.qqdocurl\")");
                if (!(str_node instanceof JsonStringNode))
                    this.debug(node.toString());
                else
                    reply(str_node.getContent());
            }
        }

        if (msg instanceof PlainText) {
            this.debug(msg.toString());
            this.debugString(msg.toString());
            String text = ((PlainText) msg).getContent();
            this.debugString(msg.toString());

            if (!CommandHolder.isCommand(text))
                return;
            CommandHolder holder = new CommandHolder(text, 1);
            this.debug(holder.getName());

            int rtn = super.execute_command(holder);
            if (rtn == 0) {
                reply(new TranslateHelper(
                        "unknownCommand", 1
                ).translate(user.getLang()));
            }
        }

    }
}
