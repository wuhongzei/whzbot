package org.example.whzbot.data.result;

import org.example.whzbot.data.variable.IVariable;

import java.util.ArrayList;

public class ListResult extends Success{
    ArrayList<IVariable> vars;
    public ListResult(String str) {
        super(str);
        this.vars = new ArrayList<>();
    }

    public ListResult add(IVariable var) {
        this.vars.add(var);
        return this;
    }

    public IVariable get(int i) {
        return this.vars.get(i);
    }

    public String[] gets() {
        String[] rtn = new String[this.vars.size()];
        for (int i = 0; i < this.vars.size(); i++) {
            rtn[i] = this.vars.get(i).toString();
        }
        return rtn;
    }

    @Override
    public String toString() {
        return null;
    }
}
