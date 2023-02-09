package org.example.whzbot.data.variable.instance;

import org.example.whzbot.data.result.Result;
import org.example.whzbot.data.variable.AbstractType;
import org.example.whzbot.data.variable.IVariable;

public abstract class Instance implements IVariable {
    protected AbstractType type;
    protected String name;
    protected boolean loaded;

    public Instance(AbstractType typ) {
        this.type = typ;
        this.loaded = false;
    }
    public Instance(AbstractType typ, String n) {
        this.type = typ;
        this.name = n;
        this.loaded = false;
    }

    @Override
    public AbstractType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String n) {
        this.name = n;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public int getMemorySize() {
        return 4;
    }

    public abstract Result assign(IVariable var);
}
