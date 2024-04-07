package velodicord;

import V4S4J.V4S4J.V4S4J;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
    public static Webhook webhook;
    public static String voicechannel;

    static void init() throws InterruptedException {
        jda = JDABuilder.createDefault(config.p.getProperty("BotToken"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new discord())
                .build();

        jda.awaitReady();

        jda.upsertCommand("player", "現在参加しているプレイヤー").queue();
        jda.upsertCommand("join", "ボイスチャンネルへの参加").queue();
        jda.upsertCommand("leave", "ボイスチャンネルからの退出").queue();
        jda.upsertCommand("showdic", "辞書に登録されている単語一覧").queue();
        jda.upsertCommand("adddic", "辞書に新たな単語を登録・登録されている単語の読み方を変更")
                .addOption(OptionType.STRING, "word", "登録したい単語", true)
                .addOption(OptionType.STRING, "read", "登録したい単語の読み方", true)
                .queue();
        jda.upsertCommand("removedic", "辞書に登録されている単語の削除")
                .addOption(OptionType.STRING, "word", "削除したい単語", true)
                .queue();
        jda.upsertCommand("setmain", "メインチャンネルを現在いるテキストチャンネルに設定する").queue();
        jda.upsertCommand("setlog", "ログチャンネルを現在いるテキストチャンネルに設定する").queue();
        jda.upsertCommand("setpos", "POSチャンネルを現在いるテキストチャンネルに設定する").queue();

        MainChannel = jda.getTextChannelById(config.p.getProperty("MainChannelId"));
        LogChannel = config.p.getProperty("LogChannelID").equals("000000")?MainChannel:jda.getTextChannelById(config.p.getProperty("LogChannelID"));
        PosChannel = config.p.getProperty("PosChannelID").equals("000000")?MainChannel:jda.getTextChannelById(config.p.getProperty("PosChannelID"));
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

    public static void sendvoicemessage(String message) {
        if (voicechannel == null) return;

        if (V4S4J.tts(message)) {
            PlayerManager.getInstance().loadAndPlay(MainChannel, "./result.wav");
        }
    }
}
