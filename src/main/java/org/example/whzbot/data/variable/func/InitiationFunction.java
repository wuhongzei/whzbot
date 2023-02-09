package org.example.whzbot.data.variable.func;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractFunction;
import org.example.whzbot.data.variable.AbstractType;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.Memory;
import org.example.whzbot.data.variable.StaticType;
import org.example.whzbot.data.variable.instance.Instance;
import org.example.whzbot.data.variable.instance.IntVar;

import java.util.List;

public class InitiationFunction extends AbstractFunction {
    public InitiationFunction(Memory mem) {
        super("init", mem);
    }

    @Override
    public boolean compatible(List<IVariable> parameter) {
        if (parameter.size() == 1) {
            if (parameter.get(0) == StaticType.integer_type)
                return true;
        }
        return false;
    }

    @Override
    public Result run(List<IVariable> parameter) {
        if (parameter.size() == 1) {
            if (parameter.get(0) == StaticType.integer_type)
                return wrapReturn(new IntVar());
        }
        return wrapIncompatible(
                this.func_name, parameter.toString()
        );
    }

    public Result init(AbstractType typ, String name) {
        if (typ.equals(StaticType.integer_type)) {
            Instance rtn = new IntVar(name);
            return wrapReturn(rtn);
        }
        return wrapIncompatible(
                this.func_name, typ.getName()
        );
    }
}
