package velodicord.events.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import velodicord.DiscordCommandSource;
import velodicord.VOICEVOX;
import velodicord.discordbot;
import velodicord.log;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import static velodicord.Config.*;
import static velodicord.Velodicord.velodicord;
import static velodicord.discordbot.*;

public class SlashCommandInteraction extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!CommandChannel.equals(event.getChannelId())) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("不明なチャンネルです")
                    .build()
            ).setEphemeral(true).queue();
            return;
        } else if (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                disadmincommand.stream().anyMatch(event.getCommandString().substring(1)::startsWith)) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("このコマンドを実行するのに必要な権限がありません")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "join" -> {
                GuildVoiceState voiceState;
                if (voicechannel != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("もうすでに" + Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getAudioManager().getConnectedChannel()).getName() + "に接続しています")
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }
                if ((Objects.requireNonNull(voiceState = Objects.requireNonNull(event.getMember()).getVoiceState())).inAudioChannel()) {
                    Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(voiceState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.cyan)
                            .setTitle("[" + Objects.requireNonNull(voiceState.getChannel()).getName() + "]に接続しました")
                            .build()
                    ).queue();

                    voicechannel = voiceState.getChannel().getId();
                    sendvoicemessage("接続しました", DefaultSpeakerID);
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("接続中のボイスチャンネルが見つかりません")
                            .build()
                    ).setEphemeral(true).queue();
                }
            }

            case "leave" -> {
                if (voicechannel != null) {
                    Objects.requireNonNull(event.getGuild()).getAudioManager().closeAudioConnection();
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.orange)
                            .setTitle("切断しました")
                            .build()
                    ).queue();
                    voicechannel = null;
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("接続中のボイスチャンネルが見つかりません")
                            .build()
                    ).setEphemeral(true).queue();
                }
            }

            case "dic" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder builder = new StringBuilder();
                        dic.keySet().forEach(word -> builder.append("・ ").append(word).append(" -> ").append(dic.get(word)).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("辞書に登録されている単語一覧")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        String word = event.getOptions().get(0).getAsString();
                        String read = event.getOptions().get(1).getAsString();
                        dic.put(word, read);
                        dic = dic.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(String::length).reversed()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("単語を登録・変更しました")
                                .setDescription(word + " -> " + read)
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        String word = event.getOptions().get(0).getAsString();
                        dic.remove(word);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("単語を削除しました")
                                .setDescription(word)
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }
                }
            }

            case "ch" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> event.replyEmbeds(new EmbedBuilder()
                            .setTitle("設定されているチャンネル")
                            .setDescription(
                                    "ログチャンネル -> " + LogForumChannel.map(ForumChannel::getName).orElse("<<設定されていません>>") + "(" + LogForumChannel.map(ForumChannel::getId).orElse("<<設定されていません>>") + ")\n" +
                                            "メインチャンネル -> " + MainChannel.getName() + "(" + MainChannel.getId() + ")\n" +
                                            "PMチャンネル -> " + PMChannel.getName() + "(" + PMChannel.getId() + ")\n" +
                                            "通知チャンネル 　   -> " + NoticeChannel.getName() + "(" + NoticeChannel.getId() + ")\n" +
                                            "POSチャンネル 　   -> " + PosChannel.getName() + "(" + PosChannel.getId() + ")\n" +
                                            "コマンドチャンネル 　-> " + Objects.requireNonNull(jda.getTextChannelById(CommandChannel)).getName() + "(" + CommandChannel + ")"

                            )
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();

                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "log" -> {
                                LogForumChannel = Optional.of(event.getOptions().get(1).getAsChannel().asForumChannel());
                                config.put("LogChannelID", LogForumChannel.get().getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("ログチャンネルを" + LogForumChannel.get().getName() + "(" + LogForumChannel.get().getId() + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();

                                LogForumChannel.get().getThreadChannels()
                                        .stream().filter(thread -> "velocity".equals(thread.getName())).findFirst().ifPresentOrElse(
                                                log -> LogChannel = log,

                                                () -> LogChannel = LogForumChannel.get()
                                                        .createForumPost("velocity", MessageCreateData.fromContent("velocity server's log")).complete().getThreadChannel()
                                        );
                                if (log == null) (log = new Thread(new log(true))).start();
                                else if (!log.isAlive()) (log = new Thread(new log(false))).start();
                            }

                            case "main" -> {
                                String lm = MainChannel.getId();
                                MainChannel = event.getOptions().get(1).getAsChannel().asTextChannel();
                                config.put("MainChannelID", MainChannel.getId());
                                if (lm.equals(NoticeChannel.getId())) {
                                    NoticeChannel = MainChannel;
                                    config.put("NoticeChannelID", NoticeChannel.getId());
                                }
                                if (lm.equals(PMChannel.getId())) {
                                    PMChannel = MainChannel;
                                    config.put("PMChannelID", PMChannel.getId());
                                }
                                if (lm.equals(PosChannel.getId())) {
                                    PosChannel = MainChannel;
                                    config.put("PosChannelID", PosChannel.getId());
                                }
                                if (lm.equals(CommandChannel)) {
                                    CommandChannel = MainChannel.getId();
                                    config.put("CommandChannelID", CommandChannel);
                                }

                                String webhookname = "Velodicord";
                                MainChannel.getGuild().retrieveWebhooks().complete().forEach(webhook -> {
                                    if (webhookname.equals(webhook.getName())) discordbot.webhook = webhook;
                                });

                                if (webhook == null) {
                                    webhook = MainChannel.createWebhook(webhookname).complete();
                                }

                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("メインチャンネルを" + MainChannel.getName() + "(" + MainChannel.getId() + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();

                                discordbot.PMChannel.sendMessage("ALL&OK&" + NoticeChannel.getId() + "&" + LogForumChannel.map(ForumChannel::getId).orElse("") + "&" + CommandChannel + "&" + CommandRole.getId()).queue();
                            }

                            case "pm" -> {
                                PMChannel = event.getOptions().get(1).getAsChannel().asTextChannel();
                                config.put("PMChannelID", PMChannel.getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("PMチャンネルを" + PMChannel.getName() + "(" + PMChannel.getId() + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "notice" -> {
                                NoticeChannel = event.getOptions().get(1).getAsChannel().asTextChannel();
                                config.put("NoticeChannelID", NoticeChannel.getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("通知チャンネルを" + NoticeChannel.getName() + "(" + NoticeChannel.getId() + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "pos" -> {
                                PosChannel = event.getOptions().get(1).getAsChannel().asTextChannel();
                                config.put("PosChannelID", PosChannel.getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("POSチャンネルを" + PosChannel.getName() + "(" + PosChannel.getId() + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "command" -> {
                                CommandChannel = event.getOptions().get(1).getAsChannel().asTextChannel().getId();
                                config.put("CommandChannelID", CommandChannel);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("コマンドチャンネルを" + event.getOptions().get(1).getAsChannel().asTextChannel().getName() + "(" + CommandChannel + ")に設定しました")
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }
                        }
                    }

                    case "del_log" -> {
                        log.interrupt();
                        config.put("LogChannelID", "000000");
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("ログチャンネルを削除しました")
                                .setColor(Color.red)
                                .build()
                        ).queue();
                    }
                }
            }

            case "commandrole" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> event.replyEmbeds(new EmbedBuilder()
                            .setTitle("設定されているロール")
                            .setDescription(CommandRole.getName() + "(" + CommandRole.getId() + ")")
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();

                    case "set" -> {
                        CommandRole = event.getOptions().get(0).getAsRole();
                        config.put("CommandRoleID", CommandRole.getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("コマンドロールを" + CommandRole.getName() + "(" + CommandRole.getId() + ")に設定しました")
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }
                }
            }

            case "detectbot" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder bots = new StringBuilder();
                        detectbot.forEach(id -> bots.append("・ ").append(Objects.requireNonNull(jda.getUserById(id)).getName()).append("(").append(id).append(")\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されている発言を無視しないbot一覧")
                                .setDescription(bots)
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        User bot = event.getOptions().get(0).getAsUser();
                        detectbot.add(bot.getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(bot.getName() + "(" + bot.getId() + ")を登録しました")
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        User bot = event.getOptions().get(0).getAsUser();
                        detectbot.remove(bot.getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(bot.getName() + "(" + bot.getId() + ")を削除しました")
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }
                }
            }

            case "speaker" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "all" -> {
                        StringBuilder speakers = new StringBuilder();
                        VOICEVOX.voicevox.keySet().forEach(id -> speakers.append("・ ").append(VOICEVOX.voicevox.get(id)).append(" & ").append(id).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("話者の種類とそのID")
                                .setDescription(speakers)
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "your" -> {
                        int id = disspeaker.getOrDefault(event.getUser().getId(), DefaultSpeakerID);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(event.getUser().getName() + "の話者")
                                .setDescription(VOICEVOX.voicevox.get(id) + "(" + id + ")")
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "default" -> event.replyEmbeds(new EmbedBuilder()
                            .setTitle("デフォルトの話者")
                            .setDescription(VOICEVOX.voicevox.get(DefaultSpeakerID) + "(" + DefaultSpeakerID + ")")
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();

                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "your" -> {
                                int id = event.getOptions().get(1).getAsInt();
                                if (VOICEVOX.voicevox.containsKey(id)) {
                                    disspeaker.put(event.getUser().getId(), id);
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(VOICEVOX.voicevox.get(id) + "に設定しました")
                                            .setColor(Color.blue)
                                            .build()
                                    ).setEphemeral(true).queue();
                                    sendvoicemessage(VOICEVOX.voicevox.get(id) + "に設定しました", id);
                                } else {
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(id + "を持つ話者はいません")
                                            .setColor(Color.red)
                                            .build()
                                    ).setEphemeral(true).queue();
                                }
                            }

                            case "default" -> {
                                int id = event.getOptions().get(1).getAsInt();
                                if (VOICEVOX.voicevox.containsKey(id)) {
                                    config.put("DefaultSpeakerID", String.valueOf(id));
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(VOICEVOX.voicevox.get(id) + "に設定しました")
                                            .setColor(Color.blue)
                                            .build()
                                    ).queue();
                                    sendvoicemessage(VOICEVOX.voicevox.get(id) + "に設定しました", id);
                                } else {
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(id + "を持つ話者はいません")
                                            .setColor(Color.red)
                                            .build()
                                    ).setEphemeral(true).queue();
                                }
                            }
                        }
                    }
                }
            }

            case "ignorecommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder builder = new StringBuilder();
                        ignorecommand.forEach(command -> builder.append("・ ").append(command).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されている通知しないコマンド一覧")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        String command = event.getOptions().get(0).getAsString();
                        ignorecommand.add(command);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("コマンドを登録しました")
                                .setDescription(command)
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        String command = event.getOptions().get(0).getAsString();
                        ignorecommand.remove(command);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("コマンドを削除しました")
                                .setDescription(command)
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }
                }
            }

            case "mentionable" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder builder = new StringBuilder();
                        mentionable.forEach(mention -> builder.append("・ ").append(mention).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されているメンション可能ロール")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "set" -> {
                        StringBuilder builder = new StringBuilder();
                        mentionable.clear();
                        mentionable.addAll(event.getOptions().stream().map(OptionMapping::getAsString).toList());
                        mentionable.forEach(mention -> builder.append("・ ").append(mention).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("メンション可能ロールを設定しました")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }
                }
            }

            case "server" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "info" -> event.replyEmbeds(new EmbedBuilder()
                            .setTitle("velocity info")
                            .setDescription("```\n\n" +
                                    "( " + velodicord.proxy.getPlayerCount() + " )人のプレイヤーがオンライン\n\n" +
                                    "使用メモリ:\n" +
                                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB / " +
                                    Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB\n```"
                            )
                            .setColor(Color.green)
                            .build()
                    ).queue();

                    case "command" -> {
                        if (!"velocity".equals(event.getOptions().get(0).getAsString())) return;

                        String command = event.getOptions().get(1).getAsString();

                        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(CommandRole) &&
                                mineadmincommand.stream().anyMatch(command::startsWith)) {
                            event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setTitle("このコマンドを実行するのに必要な権限がありません")
                                    .build()
                            ).setEphemeral(true).queue();
                            return;
                        }

                        if (!(Boolean) velodicord.proxy.getCommandManager().executeAsync(new DiscordCommandSource(event), command).join()) {
                            event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setTitle("このコマンドは存在しません")
                                    .build()
                            ).setEphemeral(true).queue();
                        }
                    }
                }
            }

            case "admincommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder builder = new StringBuilder("discordの管理者コマンド\n");
                        disadmincommand.forEach(command -> builder.append("・ ").append(command).append("\n"));
                        builder.append("\nマイクラの管理者コマンド\n");
                        mineadmincommand.forEach(command -> builder.append("・ ").append(command).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されている管理者コマンドコマンド一覧")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        String command = event.getOptions().get(1).getAsString();

                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> {
                                disadmincommand.add(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("discordの管理者コマンドを登録しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "マイクラ" -> {
                                mineadmincommand.add(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("マイクラの管理者コマンドを登録しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }
                        }
                    }

                    case "del" -> {
                        String command = event.getOptions().get(1).getAsString();

                        switch (event.getOptions().get(0).getAsString()) {
                            case "discord" -> {
                                disadmincommand.remove(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("discordの管理者コマンドを削除しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "マイクラ" -> {
                                mineadmincommand.remove(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("discordの管理者コマンドを削除しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }
                        }
                    }
                }
            }
        }
    }
}
