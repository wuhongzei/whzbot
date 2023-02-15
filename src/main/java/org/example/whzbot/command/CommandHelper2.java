package org.example.whzbot.command;

import org.example.whzbot.data.IUser;
import org.example.whzbot.data.result.FailedResult;
import org.example.whzbot.data.result.ListResult;
import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.result.StringResult;
import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.Memory;
import org.example.whzbot.data.variable.StaticType;
import org.example.whzbot.data.variable.func.BotFunction;
import org.example.whzbot.data.variable.func.OverloadedFunction;
import org.example.whzbot.data.variable.instance.IntVar;
import org.example.whzbot.data.variable.instance.StringVar;

import java.util.function.BiFunction;

public class CommandHelper2 {
    public static BiFunction<IUser, String, String> msg_wrapper;

    public static Result commandVariable(IUser user, CommandHolder holder) {
        if (!holder.hasNext()) {
            return new FailedResult("no_arg");
        }
        String typ = holder.getNextArg();
        String name;
        Memory memory = user.getCharacter().getMemory();
        IVariable var;
        if (!holder.hasNext()) {
            name = typ;
            var = memory.get(name);
            typ = readVar(typ, var);
            return new StringResult("variable.read", new String[]{name, typ});
        }
        name = holder.getNextArg();
        switch (typ) {
            case "int":
                IntVar temp_int = new IntVar(name);
                if (holder.hasNext() && holder.isNextSignedInt()) {
                    temp_int.assign(Long.parseLong(holder.getNextSignedInt()));
                }
                var = temp_int;
                break;
            case "str":
                StringVar temp_str = new StringVar(name);
                if (holder.hasNext()) {
                    temp_str.assign(holder.getRest());
                }
                var = temp_str;
                break;
            case "func":
                if (holder.hasNext()) {
                    var = new BotFunction(name, memory, holder.getRest());
                } else {
                    var = null;
                }
                break;
            default:
                var = null;
        }
        if (var == null) {
            return new FailedResult("variable.type_err");
        }
        IVariable var2 = memory.get(name, var.getType());
        if (var2 != null) {
            memory.assign(var2, var); //todo: parse result here.
            return new StringResult("variable.assign").add(var.getName());
        }
        if (memory.put(var)) {
            return new StringResult("variable.put")
                    .add(var.getType().getName()).add(var.getName());
        } else {
            return new FailedResult("variable.err_collide");
        }
    }

    private static String readVar(String typ, IVariable var) {
        if (var == null) {
            typ = "null";
        } else if (var instanceof IntVar) {
            typ = String.valueOf(((IntVar) var).read());
        } else if (var instanceof StringVar) {
            typ = String.format("\"%s\"", ((StringVar) var).read());
        } else if (var instanceof OverloadedFunction) {
            typ = ((OverloadedFunction) var).read();
        }
        return typ;
    }

    public static Result commandFunction(IUser user, CommandHolder holder) {
        if (!holder.hasNext()) {
            return new FailedResult("no_arg");
        }
        IVariable var = user.getCharacter().getMemory()
                .get(holder.getNextArg(), StaticType.func_type);
        if (var instanceof OverloadedFunction) {
            return new StringResult(msg_wrapper.apply(
                    user, ((OverloadedFunction)var).read()
            ));
        } else {
            return new FailedResult("function.null");
        }
    }

    public static Result commandMemory(IUser user, CommandHolder holder) {
        if (!holder.isNextWord()) {
            return new FailedResult("no_arg");
        }
        Memory mem = user.getCharacter().getMemory();
        String code = holder.getNextWord();
        switch (code) {
            case "list":
                if (mem.isEmpty())
                    return new StringResult("memory.empty");
                ListResult rtn = new ListResult("memory.list");
                for (IVariable var : mem) {
                    rtn.add(new StringVar(var.getName(), readVar("", var)));
                }
                return rtn;
            case "clear":
                mem.clear();
                return new StringResult("memory.clear");
            default:
                return new FailedResult("memory.unknown");
        }
    }
}
