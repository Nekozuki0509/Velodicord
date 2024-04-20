package velodicord;

import V4S4J.V4S4J.V4S4J;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import velodicord.events.discord;
import velodicord.lavaplayer.PlayerManager;

import java.util.Objects;


public class discordbot {
    public static JDA jda;
    public static TextChannel MainChannel;
    public static TextChannel LogChannel;
    public static TextChannel PosChannel;
    public static TextChannel CommandChannel;
    public static Webhook webhook;
    public static String voicechannel;

    static void init() throws InterruptedException {
        jda = JDABuilder.createDefault(Config.config.get("BotToken"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new discord())
                .build();

        jda.awaitReady();
        jda.updateCommands().addCommands(
                Commands.slash("player", "現在参加しているプレイヤー一覧"),
                Commands.slash("join", "ボイスチャンネルへの参加"),
                Commands.slash("leave", "ボイスチャンネルからの退出"),
                Commands.slash("showdic", "辞書に登録されている単語一覧"),
                Commands.slash("adddic", "辞書に新たな単語を登録・登録されている単語の読み方を変更")
                        .addOption(OptionType.STRING, "word", "登録したい単語", true)
                        .addOption(OptionType.STRING, "read", "登録したい単語の読み方", true),
                Commands.slash("deletedic", "辞書に登録されている単語の削除")
                        .addOption(OptionType.STRING, "word", "削除したい単語", true),
                Commands.slash("setmain", "メインチャンネルを設定")
                        .addOption(OptionType.CHANNEL, "textchannel", "設定したいテキストチャンネル", true),
                Commands.slash("setlog", "ログチャンネルを設定")
                        .addOption(OptionType.CHANNEL, "textchannel", "設定したいテキストチャンネル", true),
                Commands.slash("setpos", "POSチャンネルを設定")
                        .addOption(OptionType.CHANNEL, "textchannel", "設定したいテキストチャンネル", true),
                Commands.slash("setcommand", "コマンドチャンネルを設定")
                        .addOption(OptionType.CHANNEL, "textchannel", "設定したいテキストチャンネル", true),
                Commands.slash("showchannel", "設定されているチャンネル"),
                Commands.slash("showdetectbot", "登録されている発言を無視しないbot一覧"),
                Commands.slash("adddetectbot", "新たに発言を無視しないbotを登録")
                        .addOption(OptionType.USER, "bot", "登録したいbot", true),
                Commands.slash("deletedetectbot", "登録されている発言を無視しないbotの削除")
                        .addOption(OptionType.USER, "bot", "削除したいbot", true),
                Commands.slash("showspeaker", "話者の種類とID"),
                Commands.slash("setspeaker", "話者を設定")
                        .addOption(OptionType.INTEGER, "id", "話者のID", true),
                Commands.slash("setdefaultspeaker", "デフォルトの話者を設定")
                        .addOption(OptionType.INTEGER, "id", "話者のID", true),
                Commands.slash("showignorecommand", "登録されている通知しないコマンド一覧"),
                Commands.slash("addignorecommand", "新たに通知しないコマンドを登録")
                        .addOption(OptionType.STRING, "command", "コマンド", true),
                Commands.slash("deleteignorecommand", "登録されている通知しないコマンドの削除")
                        .addOption(OptionType.STRING, "command", "削除したいコマンド", true)
        ).queue();

        MainChannel = jda.getTextChannelById(Config.config.get("MainChannelID"));
        LogChannel = Config.config.get("LogChannelID").equals("000000")?MainChannel:jda.getTextChannelById(Config.config.get("LogChannelID"));
        PosChannel = Config.config.get("PosChannelID").equals("000000")?MainChannel:jda.getTextChannelById(Config.config.get("PosChannelID"));
        CommandChannel = Config.config.get("CommandChannelID").equals("000000")?MainChannel:jda.getTextChannelById(Config.config.get("CommandChannelID"));
        if (MainChannel == null) {
            throw new NullPointerException("チャンネルIDが不正です");
        }

        String webhookname = "Velodicord";
        Objects.requireNonNull(MainChannel).getGuild().retrieveWebhooks().complete().forEach(webhook -> {
            if (webhookname.equals(webhook.getName())) discordbot.webhook = webhook;
        });

        if (webhook == null) {
            webhook = MainChannel.createWebhook(webhookname).complete();
        }
    }

    public static void sendvoicemessage(String message, int speaker) {
        if (voicechannel == null) return;
        String path = String.valueOf(Config.dataDirectory.resolve("result.wav"));
        if (V4S4J.tts(message, path, speaker)) {
            PlayerManager.getInstance().loadAndPlay(MainChannel, path);
        }
    }
}
