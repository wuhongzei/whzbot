package org.example.whzbot;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.BotLeaveEvent;
import net.mamoe.mirai.event.events.BotMuteEvent;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;

import org.example.whzbot.command.CommandHelper2;
import org.example.whzbot.data.Pool;
import org.example.whzbot.data.game.GameManager;
import org.example.whzbot.data.variable.Memory;
import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.ProfileSaveAndLoad;
import org.example.whzbot.storage.json.JsonLoader;
import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;

public class JavaMain {
    public static long master_qq = -1L;
    public static long bot_qq = 0L;
    public static String password = null;
    public static final String version = "2.10.3.105";
    public static String working_dir = "";
    public static String resource_dir = "";
    public static String storing_dir = "";
    public static String setting_path = "settings.whz";
    public static boolean running;
    public static int offline_facter = 1;

    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println(args[0]);
            System.out.println(args[1]);
            setting_path = args[1];
        }
        MiraiProtocol protocol = MiraiProtocol.ANDROID_PAD;
        if (args.length > 2) {
            switch (args[2]) {
                case "phone":
                    protocol = MiraiProtocol.ANDROID_PHONE;
                    break;
                case "pad":
                    protocol = MiraiProtocol.ANDROID_PAD;
                    break;
                case "ipad":
                    protocol = MiraiProtocol.IPAD;
                    break;
                //IPAD, ANDROID_PAD, ANDROID_PHONE
            }
        }
        if (!loadSetting(setting_path)) {
            System.err.println("Setting Incomplete");
            return;
        }

        MiraiProtocol finalProtocol = protocol;
        BotConfiguration botConfig = new BotConfiguration() {{
            fileBasedDeviceInfo();
            setWorkingDir(new File(working_dir));
            setProtocol(finalProtocol); //IPAD, ANDROID_PAD, ANDROID_PHONE
            noBotLog();
            autoReconnectOnForceOffline();
            setReconnectionRetryTimes(5);
        }};
        //Bot bot = BotFactory.INSTANCE.newBot(
        //        bot_qq, password, botConfig
        //);
        Bot bot = BotFactory.INSTANCE.newBot(
                bot_qq, BotAuthorization.byQRCode(), botConfig
        );
        bot.login();
        JavaMain.afterLogin(bot);
        running = false;
        bot.join();
        /*while (running) {
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
        }*/
    }

    public static void afterLogin(@NotNull Bot bot) {
        ProfileSaveAndLoad.setLogger(bot.getLogger());
        loadAlias();
        loadLanguage();
        loadCardDeck();
        loadDefaultSetting();
        GameManager.init(bot);
        Memory.initGlobal();
        CommandHelper2.msg_wrapper = MsgProcessorShort::wrapperEntry;

        bot.getEventChannel().subscribeAlways(
                FriendMessageEvent.class,
                (event) -> new FriendMsgProcessor(event).process()
        );
        bot.getEventChannel().subscribeAlways(
                GroupMessageEvent.class,
                (event) -> new GroupMsgProcessor(event).process()
        );
        bot.getEventChannel().subscribeAlways(
                BotLeaveEvent.Kick.class,
                (event) -> notifyMaster(
                        ((BotLeaveEvent)event).getBot(),
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
        bot.getEventChannel().subscribeAlways(
                NewFriendRequestEvent.class,
                (event) -> {
                    event.accept();
                    notifyMaster(
                        event.getBot(), String.format(
                                "new friend %d @%s",
                                event.getFromId(),
                                event.getFromNick()
                        ));
                }
        );
        bot.getEventChannel().subscribeAlways(
                BotInvitedJoinGroupRequestEvent.class,
                (event) -> {
                    event.accept();
                    notifyMaster(
                        event.getBot(), String.format(
                                "new group %d ->%s",
                                event.getInvitorId(),
                                event.getGroupName()
                        ));
                }
        );
        bot.getEventChannel().subscribeAlways(
                BotOfflineEvent.Dropped.class,
                (event) -> JavaMain.saveProfile()
        );
        bot.getEventChannel().subscribeAlways(
                BotOfflineEvent.Force.class,
                (event) -> JavaMain.saveProfile()
        );
        bot.getEventChannel().subscribeAlways(
                BotOfflineEvent.MsfOffline.class,
                (event) -> {
                    JavaMain.saveProfile();
                    bot.close();
                }
        );
    }

    public static String reLogin(Bot bot, int time) {
        int count = 1;
        saveProfile();
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

    public static boolean loadSetting(String file_path) {
        JsonObjectNode setting;
        try {
            JsonLoader loader = new JsonLoader(file_path);
            setting = (JsonObjectNode) loader.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (setting != null) {
            JsonNode node = setting.get("bot_qq");
            if (node instanceof JsonLongNode)
                bot_qq = Long.parseLong(node.getContent());
            else
                return false;
            node = setting.get("bot_pass");
            if (node instanceof JsonStringNode)
                password = node.getContent();
            else
                return false;
            node = setting.get("master_qq");
            if (node instanceof JsonLongNode)
                master_qq = Long.parseLong(node.getContent());
            node = setting.get("working_dir");
            if (node instanceof JsonStringNode)
                working_dir = node.getContent();
            else
                return false;
            node = setting.get("resource_dir");
            if (node instanceof JsonStringNode)
                resource_dir = node.getContent();
            else
                resource_dir = working_dir + "\\resources";
            node = setting.get("storing_dir");
            if (node instanceof JsonStringNode)
                storing_dir = node.getContent();
            else
                storing_dir = working_dir + "\\data";
            return true;
        } else
            return false;
    }

    public static void loadAlias() {
        GlobalVariable.loadCmdAlias(resource_dir + "/CmdAlias.whz");
        GlobalVariable.loadDrawAlias(resource_dir + "/DrawAlias.whz");
        GlobalVariable.loadPresetAlias(resource_dir + "/PresetAlias.whz");
    }

    public static void loadLanguage() {
        GlobalVariable.loadLanguageList(resource_dir + "/Language.whz");
        GlobalVariable.loadLanguages(resource_dir);
    }

    public static void loadCardDeck() {
        GlobalVariable.loadCardDeck(resource_dir + "/CardDeck.whz");
        GlobalVariable.loadGachaPool(resource_dir + "/GachaPool.whz");
    }

    public static void loadDefaultSetting() {
        GlobalVariable.loadDefaultSetting(
                resource_dir + "/GroupDefault.whz",
                resource_dir + "/UserDefault.whz");
    }

    public static void saveProfile() {
        Pool.unloadCharacters();
        Pool.unloadGroups();
        Pool.unloadUsers();
        GlobalVariable.saveUpdated(resource_dir + "/Updated.whz");
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