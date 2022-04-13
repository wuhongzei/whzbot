package org.example.whzbot;

import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class MsgProcessorShort extends MsgProcessorBase{
    MessageChainBuilder chain_builder;

    protected MsgProcessorShort(AbstractMessageEvent event) {
        super(event);
    }

    public void reply(String str) {
        this.chain_builder.add(str);
        this.debug(str);
        this.user.setStorage("last_reply", str);
    }

    @Override
    public int process() {
        MessageQueue queue = new MessageQueue(this.event.getMessage());
        this.msg = queue.poll();
        return 0;
    }

    public String getReplyString() {
        return this.chain_builder.build().toString();
    }

    public MessageChain getReply() {
        return this.chain_builder.build();
    }
}
