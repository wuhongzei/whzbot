package org.example.whzbot.data.result;

public abstract class Success extends Result {
    public Success(String c) {
        super(true, c);
    }
}
