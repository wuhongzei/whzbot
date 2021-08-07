package org.example.whzbot.data;

public class Member {
    User user;
    long group_id;

    public Member(long group_id, User user) {
        this.user = user;
        this.group_id = group_id;
    }
    public Member(long group_id, long id) {
        this.user = new User(id);
        this.group_id = group_id;
    }
}
