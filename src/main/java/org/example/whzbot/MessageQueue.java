package org.example.whzbot;

import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.ArrayDeque;

public class MessageQueue extends ArrayDeque<SingleMessage> {
    SingleMessage header;
    MessageChain msgChain = null;

    public MessageQueue(MessageChain chain) {
        this.msgChain = chain;
        boolean flag = true;
        for (SingleMessage msg : chain) {
            if (flag) {
                flag = false;
                header = msg;
            }
            else {
                this.add(msg);
            }
        }
    }

    public String toString() {
        StringBuilder rtn = new StringBuilder();
        rtn.append(header.getClass().toString());
        for (SingleMessage iter : this) {
            rtn.append(" -> ");
            rtn.append(iter.toString());
        }

        return rtn.toString();
    }
}