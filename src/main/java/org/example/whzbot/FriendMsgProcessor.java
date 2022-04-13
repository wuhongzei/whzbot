package org.example.whzbot;

import net.mamoe.mirai.event.events.FriendMessageEvent;

import org.example.whzbot.command.Permission;

public class FriendMsgProcessor extends MsgProcessorBase {
    public FriendMsgProcessor(FriendMessageEvent event) {
        super(event);
        this.event_type = 1;
        if (this.user.getId() == JavaMain.master_qq)
            this.permission = Permission.BOT_OWNER;
    }
}
