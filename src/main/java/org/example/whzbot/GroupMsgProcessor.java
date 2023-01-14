package org.example.whzbot;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

import org.example.whzbot.command.Command;
import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.command.Permission;
import org.example.whzbot.data.Pool;
import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.helper.TranslateHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GroupMsgProcessor extends MsgProcessorBase {
    boolean is_at;

    public GroupMsgProcessor(GroupMessageEvent event) {
        super(event);
        this.event_type = 2;
        this.user = Pool.getGroup(this.event.getSubject().getId()).getMember(this.event.getSender().getId());
        this.permission = Permission.mergePermit(
                ((net.mamoe.mirai.contact.Member) this.event.getSender())
                        .getPermission().getLevel() + 1,
                this.user.getId() == JavaMain.master_qq ? 2 : 0
        );
    }

    public void sendFile(String file_name, byte[] file_content) {
        try (InputStream stream = new ByteArrayInputStream(file_content)) { // 安全地使用 InputStream
            net.mamoe.mirai.contact.Group contact = (Group) this.event.getSubject();
            ExternalResource resource = ExternalResource.create(stream);
            contact.getFiles().uploadNewFile(file_name, resource);
            //contact.getFiles().uploadNewFile(file_name, resource); // 或者用来上传文件
        } catch (IOException ignored) {}
    }

    protected int processAt() {
        this.event.getBot().getLogger().debug(String.valueOf(((At) msg).getTarget()));
        if (((At) msg).getTarget() != this.event.getBot().getId())
            return 2;
        else
            is_at = true;
        return 0;
    }

    protected int processText() {
        String text = ((PlainText) msg).getContent();
        if (is_at) {
            text = text.replaceFirst(String.format("@%s", this.bot.getNick()), "");
            text = text.substring(StringHelper.skipSpace(text, 0));
        }

        if (!CommandHolder.isCommand(text))
            return 1;
        if ((text.isBlank()) && is_at) {
            replyTranslated("bot.empty_call");
            return 2;
        }

        CommandHolder holder = new CommandHolder(text, 1);

        boolean inhibited = false;
        if (this.isBotOff() && holder.getCmd() != Command.set) {
            inhibited = true;
        }
        if (!inhibited && this.isCmdOff(holder.getCmd().type)) {
            inhibited = true;
            replyTranslated("bot.inhibited");
        }

        if (!inhibited) {
            int rtn = super.execute_command(holder);
            if (rtn == 0) {
                reply(new TranslateHelper(
                        "unknown_command", 1
                ).translate(user.getLang()));
            }
        }
        return 1;
    }
}
