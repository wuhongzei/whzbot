package org.example.whzbot.data.variable.instance;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.StaticType;

public class StringVar extends Instance {
    protected String value;

    public StringVar() {
        this("");
    }

    public StringVar(String name) {
        this(name, "");
    }

    public StringVar(String name, String val) {
        super(StaticType.string_type, name);
        this.value = val;
    }

    public Result assign(IVariable var) {
        if (var instanceof StringVar) {
            this.value = ((StringVar) var).value;
        } else if (var instanceof IntVar) {
            this.value = String.valueOf(((IntVar)var).read());
        } else{
            return AbstractFunction.wrapIncompatible("assign", var.getType().toString());
        }
        return AbstractFunction.wrapReturn(this);
    }

    public void assign(String val) {
        this.value = val;
    }

    public String read() {
        return this.value;
    }

    public String toString() {
        return String.format("%s = %s", this.name, this.value);
    }
}
