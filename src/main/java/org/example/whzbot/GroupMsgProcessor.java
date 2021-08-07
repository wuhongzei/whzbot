package org.example.whzbot;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;

import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.data.Group;
import org.example.whzbot.data.Pool;
import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.helper.TranslateHelper;

public class GroupMsgProcessor extends MsgProcessorBase{
    protected Group group;

    public GroupMsgProcessor(GroupMessageEvent event) {
        super(event);
        this.event_type = 2;
        this.group = Pool.getGroup(this.event.getSubject().getId());
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
            }
            else
                is_at = true;
        }

        if (msg instanceof PlainText) {
            String text = ((PlainText) msg).getContent();
            int temp;
            if (is_at){
                temp = text.indexOf(String.format("@%s", this.bot.getNick()));
                if (temp == 0)
                    debug("find @ at 0");
                else
                    debug("find @ not 0");
                text = text.replaceFirst(String.format("@%s", this.bot.getNick()), "");
                text = text.substring(StringHelper.skipSpace(text, 0));
                this.debugString(text);
            }
            this.debug(text);

            if (!CommandHolder.isCommand(text))
                return;
            if ((text.equals("") || text.equals(" ")) && is_at) {
                reply("why call me?");
                return;
            }

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
