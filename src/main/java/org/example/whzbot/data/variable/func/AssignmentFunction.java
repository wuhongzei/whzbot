package org.example.whzbot.data.variable.func;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.Memory;
import org.example.whzbot.data.variable.instance.Instance;

import java.util.List;

public class AssignmentFunction extends AbstractFunction implements IBinaryFunction {
    public AssignmentFunction(Memory mem) {
        super("assign", mem);
    }

    @Override
    public boolean compatible(List<IVariable> parameter) {
        return parameter.size() == 2 && parameter.get(0) instanceof Instance;
    }

    @Override
    public Result run(List<IVariable> parameter) {
        if (parameter.size() != 2)
            wrapIncompatible("assign", parameter.toString());
        return this.run(parameter.get(0), parameter.get(1));
    }

    @Override
    public Result run(IVariable var1, IVariable var2) {
        if (var1 instanceof Instance) {
            return ((Instance)var1).assign(var2);
        } else if (var1 instanceof OverloadedFunction) {
            ((OverloadedFunction) var1).assign(var2);
        }
        return wrapIncompatible("assign", var1.toString());
    }
}
