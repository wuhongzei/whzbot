package org.example.whzbot.data.variable;

import org.example.whzbot.data.result.FailedResult;
import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.result.VariableResult;

import java.util.List;

public abstract class AbstractFunction implements IVariable {
    protected String func_name;
    protected boolean loaded;
    protected Memory memory;

    public AbstractFunction(String name, Memory mem) {
        this.func_name = name;
        this.memory = mem;
    }

    public String getName() {
        return this.func_name;
    }

    public void setName(String n) {
        this.func_name = n;
    }

    public Memory getMemory() {
        return this.memory;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public AbstractType getType() {
        return StaticType.func_type;
    }

    public int getMemorySize() {
        return 4;
    }

    public String toString() {
        return this.func_name;
    }

    public abstract boolean compatible(List<IVariable> parameter);

    public abstract Result run(List<IVariable> parameter);

    public static Result wrapReturn(IVariable rtn) {
        return new VariableResult(rtn);
    }

    public static Result wrapIncompatible(String fn, String given) {
        return new FailedResult("function.incompatible").add(fn).add(given);
    }
}
