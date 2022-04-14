package org.example.whzbot.command;

public enum Command {
    state(CommandType.GENERAL, Permission.ANYONE, "state"),
    bot(CommandType.GENERAL, Permission.ANYONE, "bot"),
    help(CommandType.GENERAL, Permission.ANYONE, "help"),
    exec(CommandType.GENERAL, Permission.ANY_ADMIN, "exec"),
    send(CommandType.GENERAL, Permission.ANY_ADMIN, "send"),
    lang(CommandType.GENERAL, Permission.ANYONE, "lang"),

    stat(CommandType.ADMIN, Permission.BOT_ADMIN, "stat"),
    admin(CommandType.ADMIN, Permission.BOT_ADMIN, "admin"),
    master(CommandType.ADMIN, Permission.BOT_OWNER, "master"),
    on(CommandType.ADMIN, Permission.BOT_OWNER, "on"),
    off(CommandType.ADMIN, Permission.BOT_OWNER, "off"),
    exit(CommandType.ADMIN, Permission.BOT_OWNER, "exit"),
    save(CommandType.ADMIN, Permission.BOT_OWNER, "save"),
    ban(CommandType.ADMIN, Permission.BOT_ADMIN, "ban"),
    un_ban(CommandType.ADMIN, Permission.BOT_ADMIN, "unban"),
    ban_group(CommandType.ADMIN, Permission.BOT_ADMIN, "bangroup"),
    un_ban_group(CommandType.ADMIN, Permission.BOT_ADMIN, "unbangroup"),
    white_list(CommandType.ADMIN, Permission.BOT_ADMIN, "white"),
    reload(CommandType.ADMIN, Permission.BOT_ADMIN, "reload"),
    version(CommandType.ADMIN, Permission.ANYONE, "version"),

    me_on(CommandType.GROUP, Permission.ANY_ADMIN, "meon"),
    me_off(CommandType.GROUP, Permission.ANY_ADMIN, "meoff"),
    jrrp_on(CommandType.GROUP, Permission.ANY_ADMIN, "jrrpon"),
    jrrp_off(CommandType.GROUP, Permission.ANY_ADMIN, "jrrpoff"),
    clock_on(CommandType.GROUP, Permission.ANY_ADMIN, "clockon"),
    clock_off(CommandType.GROUP, Permission.ANY_ADMIN, "clockoff"),
    dismiss(CommandType.GROUP, Permission.ANY_ADMIN, "dismiss"),
    help_doc(CommandType.GROUP, Permission.GROUP_ADMIN, "helpdoc"),
    welcome(CommandType.GROUP, Permission.GROUP_ADMIN, "welcome"),
    set_coc(CommandType.GROUP, Permission.GROUP_ADMIN, "setcoc"),
    group(CommandType.GROUP, Permission.ANY_ADMIN, "group"),
    silent(CommandType.GROUP, Permission.GROUP_ADMIN, "silent"),
    link(CommandType.GROUP, Permission.GROUP_ADMIN, "link"),
    name(CommandType.GROUP, Permission.GROUP_MEMBER, "name"),

    set(CommandType.DICE, Permission.GROUP_ADMIN, "set"),
    str(CommandType.DICE, Permission.GROUP_ADMIN, "str"),
    character(CommandType.DICE, Permission.ANYONE, "character"),
    coc7d(CommandType.DICE, Permission.ANYONE, "cocsepd"),
    coc6d(CommandType.DICE, Permission.ANYONE, "cochexd"),
    rules(CommandType.DICE, Permission.ANYONE, "rules"),
    coc6(CommandType.DICE, Permission.ANYONE, "cochex"),
    coc(CommandType.DICE, Permission.ANYONE, "coc"),
    draw(CommandType.DICE, Permission.ANYONE, "draw"),
    deck(CommandType.DICE, Permission.ANYONE, "deck"),
    init(CommandType.DICE, Permission.ANYONE, "init"),
    dnd(CommandType.DICE, Permission.ANYONE, "dnd"),
    nnn(CommandType.DICE, Permission.ANYONE, "nnn"),
    en(CommandType.DICE, Permission.ANYONE, "en"),
    li(CommandType.DICE, Permission.ANYONE, "li"), //ti
    me(CommandType.DICE, Permission.ANYONE, "me"),
    nn(CommandType.DICE, Permission.ANYONE, "nn"),
    observe(CommandType.DICE, Permission.ANYONE, "ob"),
    roll_det(CommandType.DICE, Permission.ANYONE, "ra"), //rc
    ri(CommandType.DICE, Permission.ANYONE, "ri"),
    san_check(CommandType.DICE, Permission.ANYONE, "sc"),
    set_attr(CommandType.DICE, Permission.ANYONE, "st"),
    ww(CommandType.DICE, Permission.ANYONE, "ww"),
    roll(CommandType.DICE, Permission.ANYONE, "r"),
    show(CommandType.DICE, Permission.ANYONE, "show"),

    jrrp(CommandType.TAROT, Permission.ANYONE, "jrrp"),
    omkj(CommandType.TAROT, Permission.ANYONE, "omkj"),
    tarot(CommandType.TAROT, Permission.ANYONE, "tarot"),
    st_triangle(CommandType.TAROT, Permission.ANYONE, "st_tri"),
    st_tetra(CommandType.TAROT, Permission.ANYONE, "st_tet"),
    st_cross(CommandType.TAROT, Permission.ANYONE, "st_crs"),
    st_hex(CommandType.TAROT, Permission.ANYONE, "st_hex"),
    st_dec(CommandType.TAROT, Permission.ANYONE, "st_dec"),
    gacha(CommandType.TAROT, Permission.ANYONE, "gacha"),

    echo(CommandType.SIMCHAT, Permission.ANYONE, "echo"),
    chat(CommandType.SIMCHAT, Permission.ANYONE, "chat"),

    survival(CommandType.MCSERVER, Permission.ANY_ADMIN, "sur"),
    creative(CommandType.MCSERVER, Permission.ANY_ADMIN, "cre"),
    event(CommandType.MCSERVER, Permission.ANY_ADMIN, "eve"),

    math(CommandType.MATH, Permission.ANYONE, "math"),
    add(CommandType.MATH, Permission.ANYONE, "add"),
    mul(CommandType.MATH, Permission.ANYONE, "mul"),

    porb(CommandType.MATH, Permission.ANYONE, "prob"),
    bnmd(CommandType.MATH, Permission.ANYONE, "bnmd"), //binomial distribution
    nord(CommandType.MATH, Permission.ANYONE, "nord"), //normal distribution

    image(CommandType.WEB, Permission.ANYONE, "image"),
    app_share(CommandType.WEB, Permission.ANYONE, "share"),
    image_save(CommandType.WEB, Permission.BOT_ADMIN, "imgs"),
    http(CommandType.WEB, Permission.ANYONE, "http"),
    game(CommandType.WEB, Permission.ANYONE, "game"),

    unknown(CommandType.GENERAL, "unknown");

    public final CommandType type;
    public final Permission permission;
    private final String cmd_name;
    Command(CommandType cmd_type, Permission level, String cmd_name) {
        this.type = cmd_type;
        this.permission = level;
        this.cmd_name = cmd_name;
    }
    Command(CommandType cmd_type, String cmd_name) {
        this(cmd_type, Permission.ANYONE, cmd_name);
    }
    public static Command fromString(String cmd) {
        for (Command c : Command.values()) {
            if (c.cmd_name.equalsIgnoreCase(cmd)) {
                return c;
            }
        }
        return Command.unknown;
    }
    public static boolean hasPermit(Command cmd, Permission permit) {
        return Permission.hasPermit(cmd.permission, permit);
    }
}
