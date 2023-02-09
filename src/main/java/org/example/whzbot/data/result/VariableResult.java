package org.example.whzbot.data.result;

import org.example.whzbot.data.variable.IVariable;

public class VariableResult extends Success{
    IVariable var;

    public VariableResult(IVariable v) {
        super("function.success");
        this.var = v;
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
