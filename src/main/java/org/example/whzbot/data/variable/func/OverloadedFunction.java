package org.example.whzbot.data.variable.func;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.Memory;

import java.util.ArrayList;
import java.util.List;

public class OverloadedFunction extends AbstractFunction
        implements IBinaryFunction, IUnaryFunction {
    ArrayList<AbstractFunction> overloads;

    public OverloadedFunction(String name, Memory mem) {
        super(name, mem);
        this.overloads = new ArrayList<>();
    }

    public OverloadedFunction(AbstractFunction func) {
        this(func.getName(), func.getMemory());
        this.overloads.add(func);
    }

    @Override
    public boolean compatible(List<IVariable> parameter) {
        for (AbstractFunction f : this.overloads)
            if (f.compatible(parameter))
                return true;
        return false;
    }

    @Override
    public Result run(List<IVariable> parameter) {
        for (AbstractFunction f : this.overloads)
            if (f.compatible(parameter))
                return f.run(parameter);
        return wrapIncompatible(this.func_name, parameter.toString());
    }

    @Override
    public Result run(IVariable var1, IVariable var2) {
        for (AbstractFunction f : this.overloads)
            if (f instanceof IBinaryFunction)
                return ((IBinaryFunction)f).run(var1, var2);
        return wrapIncompatible(this.func_name, "binary function");
    }

    @Override
    public Result run(IVariable var) {
        for (AbstractFunction f : this.overloads)
            if (f instanceof IUnaryFunction)
                return ((IUnaryFunction)f).run(var);
        return wrapIncompatible(this.func_name, "unary function");
    }

    public String read() {
        for (AbstractFunction f : this.overloads)
            if (f instanceof BotFunction)
                return ((BotFunction)f).read();
        return "";
    }

    public void join(AbstractFunction f) {
        this.overloads.add(f);
    }
}
