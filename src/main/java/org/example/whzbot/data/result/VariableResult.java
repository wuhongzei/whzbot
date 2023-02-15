package org.example.whzbot.data.result;

import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.instance.IntVar;

public class VariableResult extends Success{
    IVariable var;

    public VariableResult(String key, long val) {
        super(key);
        this.var = new IntVar("", val);
    }

    public VariableResult(IVariable v) {
        super(v.getName());
        this.var = v;
        //this.code = key;
    }

    @Override
    public IVariable get(int i) {
        return this.var;
    }

    @Override
    public String toString() {
        return null;
    }
}
