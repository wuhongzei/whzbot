package org.example.whzbot.data.variable;

import java.util.Map;

public abstract class AbstractType implements IVariable {
    protected AbstractType parent;
    protected Map<String, AbstractFunction> func_list;
    String name;
    public AbstractType(String name){
        this.name = name;
    }

    abstract public boolean canCast(IVariable var);

    public AbstractFunction getFunc(String func) {
        return this.func_list.get(func);
    }

    public void addFunc(AbstractFunction func) {
        String fn = func.getName();
        AbstractFunction overload_func = this.func_list.put(fn, func);
        if (overload_func != null)
            throw new UnsupportedOperationException();
    }

    public AbstractType getParent() {
        return this.parent;
    }

    public boolean isChild(AbstractType typ) {
        return this.parent == null;
    }

    public boolean isInstance(IVariable var) {
        return var.getType().equals(this) ||
                (this.parent != null && this.parent.isInstance(var));
    }

    @Override
    public AbstractType getType() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String n) {
        throw new RuntimeException("cannot change type name");
    }

    public int getMemorySize() {
        return 4 * (this.func_list.size() + 1);
    }

    public String toString() {
        return this.name;
    }

    public IVariable init() {
        return null;
    }
}
