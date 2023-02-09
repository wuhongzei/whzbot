package org.example.whzbot.data.result;

import java.util.ArrayList;

public class FailedResult extends Result {
    ArrayList<String> items;
    public FailedResult(String c) {
        super(false, c);
        this.items = new ArrayList<>();
        this.items.add("");
    }

    public FailedResult add(String s) {
        this.items.add(s);
        return this;
    }

    public String[] gets() {
        return this.items.toArray(new String[0]);
    }

    /**
     * Get FULL description about the problem.
     * @return a string describing where, why, what goes wrong.
     */
    public String report() {
        return "";
    }

    public String toString() {
        return this.report();
    }
}
