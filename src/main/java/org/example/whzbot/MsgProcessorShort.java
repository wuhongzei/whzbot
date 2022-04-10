package org.example.whzbot;

import net.mamoe.mirai.event.events.AbstractMessageEvent;

public class MsgProcessorShort extends MsgProcessorBase{
    protected MessageQueue queue;

    public MsgProcessorShort(AbstractMessageEvent event, MessageQueue queue) {
        super(event);
        this.queue = queue;
    }

    protected MsgProcessorShort(AbstractMessageEvent event) {
        super(event);
    }

    @Override
    void process() {

    }
}
