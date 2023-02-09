package org.example.whzbot.data.result;

import java.util.ArrayList;
import java.util.Arrays;

import org.example.whzbot.data.variable.IVariable;
import org.example.whzbot.data.variable.instance.StringVar;

public class StringResult extends Success {
    ArrayList<String> items;

    public StringResult(String c) {
        super(c);
        this.items = new ArrayList<>();
        this.items.add("");
    }

    public StringResult(String c, String[] vars) {
        super(c);
        this.items = new ArrayList<>();
        this.items.add("");
        this.items.addAll(Arrays.asList(vars));
    }

    public StringResult add(String s) {
        this.items.add(s);
        return this;
    }

    @Override
    public IVariable get(int i) {
        return new StringVar("temp", this.items.get(i));
    }

    public String[] gets() {
        return this.items.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "";
    }
}
