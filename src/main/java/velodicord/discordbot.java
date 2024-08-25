package velodicord;

import V4S4J.V4S4J.V4S4J;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import velodicord.events.discord.CommandAutoCompleteInteraction;
import velodicord.events.discord.GuildVoiceUpdate;
import velodicord.events.discord.MessageReceived;
import velodicord.events.discord.SlashCommandInteraction;
import velodicord.lavaplayer.PlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static velodicord.Config.*;


public class discordbot {

    public static JDA jda;

    public static Optional<ForumChannel> LogForumChannel;

    public static ThreadChannel LogChannel;

    public static TextChannel MainChannel;

    public static TextChannel PMChannel;

    public static TextChannel NoticeChannel;

    public static TextChannel PosChannel;

    public static String CommandChannel;

    public static String voicechannel;

    public static Role CommandRole;

    public static int DefaultSpeakerID;

    public static Webhook webhook;

    public static List<String> mentionable = new ArrayList<>();

    public static Thread log;

    static void init() throws InterruptedException {

        jda = JDABuilder.createDefault(Config.config.get("BotToken"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new GuildVoiceUpdate(), new MessageReceived(), new SlashCommandInteraction(), new CommandAutoCompleteInteraction())
                .build();

        jda.awaitReady();
        jda.updateCommands().addCommands(
                Commands.slash("join", "ボイスチャンネルへの参加"),
                Commands.slash("leave", "ボイスチャンネルからの退出"),
                Commands.slash("dic", "辞書関係")
                        .addSubcommands(
                                new SubcommandData("show", "辞書に登録されている単語"),
                                new SubcommandData("add", "辞書に新たな単語を登録・登録されている単語の読み方を変更(正規表現可)")
                                        .addOption(OptionType.STRING, "word", "登録したい単語", true)
                                        .addOption(OptionType.STRING, "read", "登録したい単語の読み方", true),
                                new SubcommandData("del", "辞書に登録されている単語の削除")
                                        .addOption(OptionType.STRING, "word", "削除したい単語", true, true)
                        ),
                Commands.slash("ch", "チャンネル関連")
                        .addSubcommands(
                                new SubcommandData("show", "設定されているチャンネル"),
                                new SubcommandData("set", "チャンネルを設定")
                                        .addOption(OptionType.STRING, "name", "設定したいチャンネル名", true, true)
                                        .addOption(OptionType.CHANNEL, "channel", "設定したいチャンネル", true),
                                new SubcommandData("del_log", "ログチャンネルを削除")
                        ),
                Commands.slash("commandrole", "コマンドロール関連")
                        .addSubcommands(
                                new SubcommandData("show", "設定されているロール"),
                                new SubcommandData("set", "ロールを設定")
                                        .addOption(OptionType.ROLE, "role", "設定したいロール", true)
                        ),
                Commands.slash("detectbot", "発言を無視しないbot関連")
                        .addSubcommands(
                                new SubcommandData("show", "登録されている発言を無視しないbot"),
                                new SubcommandData("add", "新たに発言を無視しないbotを登録")
                                        .addOption(OptionType.USER, "bot", "登録したいbot", true),
                                new SubcommandData("del", "登録されている発言を無視しないbotの削除")
                                        .addOption(OptionType.USER, "bot", "削除したいbot", true)
                        ),
                Commands.slash("speaker", "話者関連")
                        .addSubcommandGroups(new SubcommandGroupData("show", "話者")
                                .addSubcommands(
                                        new SubcommandData("all", "話者の種類とID"),
                                        new SubcommandData("your", "設定されている話者"),
                                        new SubcommandData("default", "デフォルトの話者")
                                )
                        )
                        .addSubcommands(new SubcommandData("set", "話者を設定")
                                .addOption(OptionType.STRING, "which", "どの話者", true, true)
                                .addOption(OptionType.INTEGER, "id", "話者の名前", true, true)
                        ),
                Commands.slash("ignorecommand", "通知しないコマンド関連")
                        .addSubcommands(
                                new SubcommandData("show", "登録されている通知しないコマンド"),
                                new SubcommandData("add", "新たに通知しないコマンドを登録")
                                        .addOption(OptionType.STRING, "command", "登録したいコマンド", true),
                                new SubcommandData("del", "登録されている通知しないコマンドの削除")
                                        .addOption(OptionType.STRING, "command", "削除したいコマンド", true, true)
                        ),
                Commands.slash("mentionable", "メンション可能ロール関係")
                        .addSubcommands(
                                new SubcommandData("show", "登録されているメンション可能ロール"),
                                new SubcommandData("set", "メンション可能ロールの設定")
                                        .addOption(OptionType.STRING, "role1", "設定したいロール", false, true)
                                        .addOption(OptionType.STRING, "role2", "設定したいロール", false, true)
                                        .addOption(OptionType.STRING, "role3", "設定したいロール", false, true)
                        ),
                Commands.slash("server", "マイクラサーバー関連")
                        .addSubcommands(
                                new SubcommandData("info", "各サーバーの情報"),
                                new SubcommandData("command", "マイクラコマンド実行")
                                        .addOption(OptionType.STRING, "name", "実行するコマンドのサーバー名", true, true)
                                        .addOption(OptionType.STRING, "command", "実行するコマンド", true, true)
                        ),
                Commands.slash("admincommand", "管理者コマンド関連")
                        .addSubcommands(
                                new SubcommandData("show", "登録されている管理者コマンド"),
                                new SubcommandData("add", "新たに管理者コマンドを登録")
                                        .addOption(OptionType.STRING, "which", "どのコマンド", true, true)
                                        .addOption(OptionType.STRING, "command", "登録したいコマンド", true),
                                new SubcommandData("del", "登録されている管理者コマンドの削除")
                                        .addOption(OptionType.STRING, "which", "どのコマンド", true, true)
                                        .addOption(OptionType.STRING, "command", "削除したいコマンド", true, true)
                        )
        ).queue();

        LogForumChannel = Optional.ofNullable(jda.getForumChannelById(Config.config.get("LogChannelID")));

        LogForumChannel.ifPresent(forum -> {
            forum.getThreadChannels().stream().filter(thread -> "velocity".equals(thread.getName())).findFirst().ifPresentOrElse(
                    log -> LogChannel = log,

                    () -> LogChannel = LogForumChannel.get().createForumPost("velocity", MessageCreateData.fromContent("velocity server's log"))
                            .complete().getThreadChannel()
            );
            (log = new Thread(new log(true))).start();
        });

        MainChannel = Optional.ofNullable(jda.getTextChannelById(Config.config.get("MainChannelID"))).orElseThrow();
        PMChannel = Optional.ofNullable(jda.getTextChannelById(Config.config.get("PMChannelID"))).orElse(MainChannel);
        NoticeChannel = Optional.ofNullable(jda.getTextChannelById(Config.config.get("NoticeChannelID"))).orElse(MainChannel);
        PosChannel = Optional.ofNullable(jda.getTextChannelById(Config.config.get("PosChannelID"))).orElse(MainChannel);
        CommandChannel = Optional.ofNullable(jda.getTextChannelById(Config.config.get("CommandChannelID"))).orElse(MainChannel).getId();

        CommandRole = Optional.ofNullable(jda.getRoleById(Config.config.get("CommandRoleID"))).orElseThrow();

        DefaultSpeakerID = Integer.parseInt(Config.config.get("DefaultSpeakerID"));

        String webhookname = "Velodicord";
        MainChannel.retrieveWebhooks().complete().forEach(webhook -> {
            if (webhookname.equals(webhook.getName())) discordbot.webhook = webhook;
        });

        if (webhook == null) {
            webhook = MainChannel.createWebhook(webhookname).complete();
        }

        discordbot.PMChannel.sendMessage("ALL&OK&" + NoticeChannel.getId() + "&" + LogForumChannel.map(ForumChannel::getId).orElse("") + "&" + CommandChannel + "&" + CommandRole.getId())
                .setFiles(FileUpload.fromData(ignorecommandjson.toFile()), FileUpload.fromData(disadmincommandjson.toFile()), FileUpload.fromData(mineadmincommandjson.toFile())).queue();
    }

    public static void sendvoicemessage(String message, int speaker) {
        if (voicechannel == null) return;
        String path = String.valueOf(Config.dataDirectory.resolve("result.wav"));
        if (V4S4J.tts(message, path, speaker)) {
            PlayerManager.getInstance().loadAndPlay(MainChannel, path);
        }
    }
}
