package org.example.whzbot.data.variable;

import org.example.whzbot.data.result.FailedResult;
import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.func.AssignmentFunction;
import org.example.whzbot.data.variable.func.IBinaryFunction;
import org.example.whzbot.data.variable.func.IUnaryFunction;
import org.example.whzbot.data.variable.func.InitiationFunction;
import org.example.whzbot.data.variable.func.OverloadedFunction;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * memory stores variable and is responsible for get, set, modify variables
 */
public class Memory implements Iterable<IVariable> {
    public static Memory global;

    Memory upper = null;
    int size = 0;
    Map<String, IVariable> heap;
    Map<String, Memory> sub_scope;
    String scope;

    public Memory() {
        this.heap = new HashMap<>();
        this.sub_scope = null;
    }

    public Memory(String name) {
        this();
        this.scope = name;
    }

    /**
     * Put a variable into memory. Fail if same name already exist
     * todo: be capable to put in a OverloadedFunction. *DO NOT DO IT NOW.
     *
     * @param var a new named variable.
     * @return whether adding new variable success.
     */
    public boolean put(IVariable var) {
        if (var instanceof AbstractFunction) {
            IVariable f = this.heap.get(var.getName());
            if (f == null) {
                this.heap.put(var.getName(),
                        new OverloadedFunction((AbstractFunction) var));
                return true;
            } else if (f instanceof OverloadedFunction) {
                ((OverloadedFunction)f).join((AbstractFunction) var);
                return true;
            } else
                return false;
        }
        if (this.heap.containsKey(var.getName()))
            return false;
        this.heap.put(var.getName(), var);
        return true;
    }

    /**
     * Force to put in a variable. If already exist one, return the old one.
     * Do not push function variables!
     *
     * @param var a named variable.
     * @return null if no collide, otherwise return collision var.
     */
    public IVariable push(IVariable var) {
        return this.heap.put(var.getName(), var);
    }

    public IVariable remove(String name) {
        return this.heap.remove(name);
    }

    public IVariable get(String name) {
        return this.heap.get(name);
    }

    public IVariable get(String name, AbstractType typ) {
        IVariable rtn = this.heap.get(name);
        if (rtn == null)
            return null;
        if (typ.isInstance(rtn))
            return rtn;
        if (typ.canCast(rtn))
            return null;
        return null;
    }

    public Result func(String func_name, IVariable var) {
        OverloadedFunction func = (OverloadedFunction) this.get(func_name, StaticType.func_type);
        if (func == null)
            return new FailedResult("memory.not_found");
        return ((IUnaryFunction) func).run(var);
    }

    public Result func(String func_name, IVariable var1, IVariable var2) {
        OverloadedFunction func = (OverloadedFunction) this.get(func_name, StaticType.func_type);
        if (func == null) {
            if (this.upper != null)
                return this.upper.func(func_name, var1, var2);
            return new FailedResult("memory.not_found");
        }
        return ((IBinaryFunction) func).run(var1, var2);
    }

    public Result assign(IVariable acceptor, IVariable donor) {
        return this.func("assign", acceptor, donor);
    }

    public Memory getScope(String scope_path) {
        if (scope_path.isEmpty())
            return this;
        if (this.sub_scope == null)
            return null;
        int dot = scope.indexOf('.');
        if (dot == -1) {
            return this.sub_scope.get(scope_path);
        } else {
            return this.sub_scope.get(scope_path.substring(0, dot))
                    .getScope(scope_path.substring(dot + 1));
        }
    }

    public void addScope(Memory sub) {
        if (this.sub_scope == null)
            this.sub_scope = new HashMap<>();
        this.sub_scope.put(sub.scope, sub);
    }

    public void setUpper(Memory up) {
        this.upper = up;
    }

    public void clear() {
        this.heap.clear();
    }

    public boolean isEmpty() {
        return this.heap.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<IVariable> iterator() {
        return this.heap.values().iterator();
    }

    public JsonObjectNode toJson() {
        return new JsonObjectNode("mem");
    }

    public static void initGlobal() {
        global = new Memory();
        global.put(StaticType.type_type);
        global.put(StaticType.func_type);
        global.put(StaticType.integer_type);
        global.put(new InitiationFunction(global));
        global.put(new AssignmentFunction(global));
    }

    public static Memory fromJson(JsonNode node) {
        if (!(node instanceof JsonObjectNode))
            return new Memory();
        Memory rtn = new Memory();
        JsonObjectNode root = (JsonObjectNode) node;
        rtn.scope = Json.readString(root, "scope", "");
        JsonNode temp = root.get("variables");
        if (!(temp instanceof JsonObjectNode))
            return rtn;
        JsonObjectNode vars = (JsonObjectNode) temp;
        temp = root.get("values");
        if (!(temp instanceof JsonListNode))
            return rtn;
        JsonListNode val = (JsonListNode) temp;


        return rtn;
    }
}
