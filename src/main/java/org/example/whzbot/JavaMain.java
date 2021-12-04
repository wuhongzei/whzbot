package org.example.whzbot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.BotLeaveEvent;
import net.mamoe.mirai.event.events.BotMuteEvent;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;

import org.example.whzbot.data.Pool;
import org.example.whzbot.storage.ProfileSaveAndLoad;
import org.example.whzbot.storage.json.JsonLoader;
import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import org.example.whzbot.storage.GlobalVariable;

public class JavaMain {
    public static long master_qq = 1195693771L;
    public static long bot_qq = 2247902937L;
    public static String password = "20000506wwwhz2";
    public static final String version = "2.5.1.62";
    public static String working_dir = "F:\\work\\java\\wherai_bot\\working_dictionary";
    public static String setting_path = "settings.whz";
    public static boolean running;
    public static int offline_facter = 1;

    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println(args[0]);
            System.out.println(args[1]);
            setting_path = args[1];
        }
        loadSetting(setting_path);

        Bot bot = BotFactory.INSTANCE.newBot(
                bot_qq,
                password,
                new BotConfiguration() {{
                    fileBasedDeviceInfo();
                    setWorkingDir(new File(working_dir));
                    setProtocol(MiraiProtocol.ANDROID_PAD);
                }}
        );
        bot.login();
        JavaMain.afterLogin(bot);
        running = true;
        bot.join();
        while (running) {
            System.out.println("looped");
            try {
                String relogin_msg = reLogin(bot, offline_facter);
                JavaMain.afterLogin(bot);
                notifyMaster(bot, relogin_msg);
                break;
            } catch (Exception e) {
                bot.getLogger().error(e);
            }
            bot.join();
        }
    }

    public static void afterLogin(@NotNull Bot bot) {
        ProfileSaveAndLoad.setLogger(bot.getLogger());
        loadAlias();
        loadLanguage();
        loadCardDeck();

        bot.getEventChannel().subscribeAlways(
                FriendMessageEvent.class,
                (event) -> new FriendMsgProcessor(event).process()
        );
        bot.getEventChannel().subscribeAlways(
                GroupMessageEvent.class,
                (event) -> new GroupMsgProcessor(event).process()
        );
        bot.getEventChannel().subscribeAlways(
                BotOfflineEvent.Dropped.class,
                (event) -> offline_facter = 1000
        );
        bot.getEventChannel().subscribeAlways(
                BotOfflineEvent.Force.class,
                (event) -> offline_facter = 60000
        );
        bot.getEventChannel().subscribeAlways(
                BotLeaveEvent.Kick.class,
                (event) -> notifyMaster(
                        event.getBot(),
                        String.format(
                                "bot kicked by %d from %d",
                                event.getOperator().getId(),
                                event.getGroupId()
                        )
                )
        );
        bot.getEventChannel().subscribeAlways(
                BotMuteEvent.class,
                (event) -> notifyMaster(
                        event.getBot(), String.format(
                                "bot muted by %d in %d",
                                event.getOperator().getId(),
                                event.getGroupId()
                        ))
        );
    }

    public static String reLogin(Bot bot, int time) {
        int count = 1;
        while (!bot.isOnline())
            try {
                bot.login();
                break;
            } catch (Exception e) {
                bot.getLogger().error(e);
                try {
                    Thread.sleep(time * count);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                time++;
            }
        return String.format(
                "Bot disconnected %d * %d ^2/2",
                count, time);
    }

    private static void notifyMaster(Bot bot, String msg) {
        Friend master = bot.getFriend(master_qq);
        if (master != null)
            master.sendMessage(msg);
    }

    public static void loadSetting(String file_path) {
        JsonObjectNode setting = null;
        try {
            JsonLoader loader = new JsonLoader(file_path);
            setting = (JsonObjectNode) loader.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (setting != null) {
            JsonNode node = setting.get("bot_qq");
            if (node instanceof JsonLongNode)
                bot_qq = Long.parseLong(node.getContent());
            node = setting.get("bot_pass");
            if (node instanceof JsonStringNode)
                password = node.getContent();
            node = setting.get("master_qq");
            if (node instanceof JsonLongNode)
                master_qq = Long.parseLong(node.getContent());
            node = setting.get("working_dir");
            if (node instanceof JsonStringNode)
                working_dir = node.getContent();
        }
    }

    public static void loadAlias() {
        GlobalVariable.loadCmdAlias(working_dir + "/resources/CmdAlias.whz");
        GlobalVariable.loadDrawAlias(working_dir + "/resources/DrawAlias.whz");
        GlobalVariable.loadPresetAlias(working_dir + "/resources/PresetAlias.whz");
    }

    public static void loadLanguage() {
        GlobalVariable.loadLanguageList(working_dir + "/resources/Language.whz");
        GlobalVariable.loadLanguages(working_dir + "/resources");
    }

    public static void loadCardDeck() {
        GlobalVariable.loadCardDeck(working_dir + "/resources/CardDeck.whz");
    }

    public static void saveProfile() {
        Pool.unloadCharacters();
        Pool.unloadGroups();
        Pool.unloadUsers();
    }
}


/*if (event.getSender().getId() == tester_qq) {
    event.getSubject().sendMessage(new MessageChainBuilder()
            .append(new QuoteReply(event.getMessage()))
            .append("Hi, you just said: '")
            .append(event.getMessage())
            .append("'")
            .build()
    );
}*/