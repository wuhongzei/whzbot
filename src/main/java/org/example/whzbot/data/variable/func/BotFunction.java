package org.example.whzbot.data.variable.func;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.Memory;

import java.util.List;

public class BotFunction extends AbstractFunction {
    String cmd;

    public BotFunction(String name, Memory mem, String command) {
        super(name, mem);
        this.cmd = command;
    }

    @Override
    public boolean compatible(List<IVariable> parameter) {
        return false;
    }

    @Override
    public Result run(List<IVariable> parameter) {
        return wrapIncompatible("null", "null");
    }

    public String read() {
        return this.cmd;
    }

    public Result assign(String str) {
        this.cmd = str;
        return wrapReturn(this);
    }
}
