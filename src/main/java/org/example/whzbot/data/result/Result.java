package org.example.whzbot.data.result;

import org.example.whzbot.data.variable.IVariable;

public abstract class Result {
    boolean suc;
    String code;

    public Result(boolean s, String c) {
        this.suc = s;
        this.code = c;
    }

    public String get() {
        return this.code;
    }

    public IVariable get(int i) {
        return null;
    }

    public String[] gets() {
        return new String[]{};
    }

    public boolean isSuccess() {
        return this.suc;
    }

    public abstract String toString();
}
