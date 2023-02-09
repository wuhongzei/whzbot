package org.example.whzbot.data.variable.instance;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.StaticType;

public class IntVar extends Instance{
    protected long value;
    public IntVar() {
        super(StaticType.integer_type);
    }
    public IntVar(String name) {
        super(StaticType.integer_type, name);
    }
    public IntVar(String name, long val) {
        this(name);
        this.value = val;
    }
    public Result assign(IVariable var) {
        if (var instanceof IntVar) {
            this.value = ((IntVar) var).value;
            return AbstractFunction.wrapReturn(this);
        } else {
            return AbstractFunction.wrapIncompatible("assign", var.getType().toString());
        }
    }

    public void assign(long val) {
        this.value = val;
    }

    public long read() {
        return this.value;
    }
}
