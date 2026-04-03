package velodicord.events.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import velodicord.*;
import velodicord.pmConnection.DiscordPluginMessageManager;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import static velodicord.Config.*;
import static velodicord.Discordbot.*;

public class SlashCommandInteraction extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!getCommandChannel().equals(event.getChannelId())) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("不明なチャンネルです")
                    .build()
            ).setEphemeral(true).queue();
            return;
        } else if (!Objects.requireNonNull(event.getMember()).getRoles().contains(getCommandRole()) &&
                getDisadmincommand().stream().anyMatch(event.getCommandString().substring(1)::startsWith)) {
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
                if (getVoicechannel() != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("もうすでに%sに接続しています".formatted(Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getAudioManager().getConnectedChannel()).getAsMention()))
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }
                if ((Objects.requireNonNull(voiceState = Objects.requireNonNull(event.getMember()).getVoiceState())).inAudioChannel()) {
                    Objects.requireNonNull(event.getGuild()).getAudioManager().openAudioConnection(Objects.requireNonNull(voiceState.getChannel()));
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.cyan)
                            .setTitle("[%s]に接続しました".formatted(voiceState.getChannel().getAsMention()))
                            .build()
                    ).queue();

                    setVoicechannel(voiceState.getChannel().getId());
                    sendvoicemessage("接続しました", getDefaultSpeakerID());
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("接続中のボイスチャンネルが見つかりません")
                            .build()
                    ).setEphemeral(true).queue();
                }
            }

            case "leave" -> {
                if (getVoicechannel() != null) {
                    Objects.requireNonNull(event.getGuild()).getAudioManager().closeAudioConnection();
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.orange)
                            .setTitle("切断しました")
                            .build()
                    ).queue();
                    setVoicechannel(null);
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
                        getDic().keySet().forEach(word -> builder.append("・ ").append(word).append(" -> ").append(getDic().get(word)).append("\n"));
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
                        getDic().put(word, read);
                        setDic(getDic().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(String::length).reversed()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("単語を登録・変更しました")
                                .setDescription("%s -> %s".formatted(word, read))
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        String word = event.getOptions().get(0).getAsString();
                        getDic().remove(word);
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
                    case "show" -> {
                        StringBuilder builder = new StringBuilder();
                        builder.append("ログチャンネル -> ").append(getLogForumChannel().map(ForumChannel::getName).orElse("<<設定されていません>>")).append("(").append(getLogForumChannel().map(ForumChannel::getId).orElse("<<設定されていません>>")).append(")\n")
                                .append("メインチャンネル -> ").append(getMainChannel().getName()).append("(").append(getMainChannel().getId()).append(")\n")
                                .append("通知チャンネル 　   -> ").append(getNoticeChannel().getName()).append("(").append(getNoticeChannel().getId()).append(")\n")
                                .append("POSチャンネル 　   -> ").append(getPosChannel().getName()).append("(").append(getPosChannel().getId()).append(")\n")
                                .append("コマンドチャンネル 　-> ").append(Objects.requireNonNull(getJda().getTextChannelById(getCommandChannel())).getName()).append("(").append(getCommandChannel()).append(")\n");

                        if (Velodicord.getPMManager() instanceof DiscordPluginMessageManager manager)
                            builder.append("PMチャンネル -> ").append(manager.getPMChannel().getName()).append("(").append(manager.getPMChannel().getId()).append(")");

                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("設定されているチャンネル")
                                .setDescription(
                                        builder.toString()
                                )
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "log" -> {
                                setLogForumChannel(Optional.of(event.getOptions().get(1).getAsChannel().asForumChannel()));
                                getConfig().put("LogChannelID", getLogForumChannel().get().getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("ログチャンネルを%s(%s)に設定しました".formatted(getLogForumChannel().get().getName(), getLogForumChannel().get().getId()))
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();

                                getLogForumChannel().get().getThreadChannels()
                                        .stream().filter(thread -> "velocity".equals(thread.getName())).findFirst().ifPresentOrElse(
                                                Discordbot::setLogChannel,

                                                () -> setLogChannel(getLogForumChannel().get()
                                                        .createForumPost("velocity", MessageCreateData.fromContent("velocity server's log")).complete().getThreadChannel())
                                        );
                                if (getLog() == null) {
                                    setLog(new Thread(new Log(true)));
                                    getLog().start();
                                } else if (!getLog().isAlive()) {
                                    setLog(new Thread(new Log(false)));
                                    getLog().start();
                                }
                            }

                            case "main" -> {
                                String lm = getMainChannel().getId();
                                setMainChannel(event.getOptions().get(1).getAsChannel().asTextChannel());
                                getConfig().put("MainChannelID", getMainChannel().getId());
                                if (lm.equals(getNoticeChannel().getId())) {
                                    setNoticeChannel(getMainChannel());
                                    getConfig().put("NoticeChannelID", getNoticeChannel().getId());
                                }
                                if (lm.equals(getPosChannel().getId())) {
                                    setPosChannel(getMainChannel());
                                    getConfig().put("PosChannelID", getPosChannel().getId());
                                }
                                if (lm.equals(getCommandChannel())) {
                                    setCommandChannel(getMainChannel().getId());
                                    getConfig().put("CommandChannelID", getCommandChannel());
                                }
                                if (Velodicord.getPMManager() instanceof DiscordPluginMessageManager manager && lm.equals(manager.getPMChannel().getId())) {
                                    manager.setPMChannel(getMainChannel());
                                    getConfig().put("PMChannelID", getMainChannel().getId());
                                }

                                String webhookname = "Velodicord";
                                getMainChannel().getGuild().retrieveWebhooks().complete().forEach(webhook -> {
                                    if (webhookname.equals(webhook.getName())) Discordbot.setWebhook(webhook);
                                });

                                if (getWebhook() == null) {
                                    setWebhook(getMainChannel().createWebhook(webhookname).complete());
                                }

                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("メインチャンネルを%s(%s)に設定しました".formatted(getMainChannel().getName(), getMainChannel().getId()))
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();

                                Velodicord.getPMManager().sendMessage("ALL", "OK&%s&%s&%s&%s".formatted(getNoticeChannel().getId(), getLogForumChannel().map(ForumChannel::getId).orElse(""), getCommandChannel(), getCommandRole().getId()));
                            }

                            case "pm" -> {
                                if (Velodicord.getPMManager() instanceof DiscordPluginMessageManager manager) {
                                    TextChannel pmtmp = event.getOptions().get(1).getAsChannel().asTextChannel();

                                    manager.setPMChannel(pmtmp);
                                    getConfig().put("PMChannelID", pmtmp.getId());
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle("PMチャンネルを%s(%s)に設定しました".formatted(pmtmp.getName(), pmtmp.getId()))
                                            .setColor(Color.blue)
                                            .build()
                                    ).queue();
                                } else {
                                    event.replyEmbeds(new EmbedBuilder()
                                            .setTitle("PMチャンネルはWebSocket版では設定できません")
                                            .setColor(Color.red)
                                            .build()
                                    ).queue();
                                }
                            }

                            case "notice" -> {
                                setNoticeChannel(event.getOptions().get(1).getAsChannel().asTextChannel());
                                getConfig().put("NoticeChannelID", getNoticeChannel().getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("通知チャンネルを%s(%s)に設定しました".formatted(getNoticeChannel().getName(), getNoticeChannel().getId()))
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "pos" -> {
                                setPosChannel(event.getOptions().get(1).getAsChannel().asTextChannel());
                                getConfig().put("PosChannelID", getPosChannel().getId());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("POSチャンネルを%s(%s)に設定しました".formatted(getPosChannel().getName(), getPosChannel().getId()))
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "command" -> {
                                setCommandChannel(event.getOptions().get(1).getAsChannel().asTextChannel().getId());
                                getConfig().put("CommandChannelID", getCommandChannel());
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("コマンドチャンネルを%s(%s)に設定しました".formatted(event.getOptions().get(1).getAsChannel().asTextChannel().getName(), getCommandChannel()))
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }
                        }
                    }

                    case "del_log" -> {
                        getLog().interrupt();
                        getConfig().put("LogChannelID", "000000");
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
                            .setDescription("%s(%s)".formatted(getCommandRole().getName(), getCommandRole().getId()))
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();

                    case "set" -> {
                        setCommandRole(event.getOptions().get(0).getAsRole());
                        getConfig().put("CommandRoleID", getCommandRole().getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("コマンドロールを%s(%s)に設定しました".formatted(getCommandRole().getName(), getCommandRole().getId()))
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
                        getDetectbot().forEach(id -> bots.append("・ ").append(Objects.requireNonNull(getJda().getUserById(id)).getName()).append("(").append(id).append(")\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されている発言を無視しないbot一覧")
                                .setDescription(bots)
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        User bot = event.getOptions().get(0).getAsUser();
                        getDetectbot().add(bot.getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("%s(%s)を登録しました".formatted(bot.getName(), bot.getId()))
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        User bot = event.getOptions().get(0).getAsUser();
                        getDetectbot().remove(bot.getId());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("%s(%s)を削除しました".formatted(bot.getName(), bot.getId()))
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
                        Voicevox.getVoicevox().forEach(m -> speakers.append("・ ").append(m.name()).append(" & ").append(m.id()).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("話者の種類とそのID")
                                .setDescription(speakers)
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "your" -> {
                        int id = getDisspeaker().getOrDefault(event.getUser().getId(), getDefaultSpeakerID());
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("%sの話者".formatted(event.getUser().getName()))
                                .setDescription("%s(%d)".formatted(Voicevox.getVoicevox().stream().filter(m -> m.id() == id).findFirst().get().name(), id))
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "default" -> event.replyEmbeds(new EmbedBuilder()
                            .setTitle("デフォルトの話者")
                            .setDescription("%s(%d)".formatted(Voicevox.getVoicevox().stream().filter(m -> m.id() == getDefaultSpeakerID()).findFirst().get(), getDefaultSpeakerID()))
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();

                    case "set" -> {
                        switch (event.getOptions().get(0).getAsString()) {
                            case "your" -> {
                                int id = event.getOptions().get(1).getAsInt();
                                Voicevox.getVoicevox().stream().filter(m -> m.id() == id).findFirst().ifPresentOrElse(
                                        m -> {
                                            getDisspeaker().put(event.getUser().getId(), id);
                                            event.replyEmbeds(new EmbedBuilder()
                                                    .setTitle("%sに設定しました".formatted(m.name()))
                                                    .setColor(Color.blue)
                                                    .build()
                                            ).setEphemeral(true).queue();
                                            sendvoicemessage("%sに設定しました".formatted(m.name()), id);
                                        },

                                        () -> event.replyEmbeds(new EmbedBuilder()
                                                .setTitle("%dを持つ話者はいません".formatted(id))
                                                .setColor(Color.red)
                                                .build()
                                        ).setEphemeral(true).queue()
                                );
                            }

                            case "default" -> {
                                int id = event.getOptions().get(1).getAsInt();
                                Voicevox.getVoicevox().stream().filter(m -> m.id() == id).findFirst().ifPresentOrElse(
                                        m -> {
                                            getConfig().put("DefaultSpeakerID", String.valueOf(id));
                                            event.replyEmbeds(new EmbedBuilder()
                                                    .setTitle("%sに設定しました".formatted(m.name()))
                                                    .setColor(Color.blue)
                                                    .build()
                                            ).queue();
                                            sendvoicemessage("%sに設定しました".formatted(m.name()), id);
                                        },

                                        () -> event.replyEmbeds(new EmbedBuilder()
                                                .setTitle("%dを持つ話者はいません".formatted(id))
                                                .setColor(Color.red)
                                                .build()
                                        ).setEphemeral(true).queue()
                                );
                            }
                        }
                    }
                }
            }

            case "ignorecommand" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "show" -> {
                        StringBuilder builder = new StringBuilder();
                        getIgnorecommand().forEach(command -> builder.append("・ ").append(command).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されている通知しないコマンド一覧")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "add" -> {
                        String command = event.getOptions().get(0).getAsString();
                        getIgnorecommand().add(command);
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("コマンドを登録しました")
                                .setDescription(command)
                                .setColor(Color.blue)
                                .build()
                        ).queue();
                    }

                    case "del" -> {
                        String command = event.getOptions().get(0).getAsString();
                        getIgnorecommand().remove(command);
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
                        getMentionable().forEach(mention -> builder.append("・ ").append(mention).append("\n"));
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("登録されているメンション可能ロール")
                                .setDescription(builder.toString())
                                .setColor(Color.blue)
                                .build()
                        ).setEphemeral(true).queue();
                    }

                    case "set" -> {
                        StringBuilder builder = new StringBuilder();
                        getMentionable().clear();
                        getMentionable().addAll(event.getOptions().stream().map(OptionMapping::getAsString).toList());
                        getMentionable().forEach(mention -> builder.append("・ ").append(mention).append("\n"));
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
                            .setDescription("```\n\n( %d )人のプレイヤーがオンライン\n\n使用メモリ:\n%d MB / %d MB\n```".formatted(Velodicord.getVelodicord().getProxy().getPlayerCount(), (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024, Runtime.getRuntime().totalMemory() / 1024 / 1024)
                            )
                            .setColor(Color.green)
                            .build()
                    ).queue();

                    case "command" -> {
                        if (!"velocity".equals(event.getOptions().get(0).getAsString())) return;

                        String command = event.getOptions().get(1).getAsString();

                        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(getCommandRole()) &&
                                getMineadmincommand().stream().anyMatch(command::startsWith)) {
                            event.replyEmbeds(new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setTitle("このコマンドを実行するのに必要な権限がありません")
                                    .build()
                            ).setEphemeral(true).queue();
                            return;
                        }

                        if (!(Boolean) Velodicord.getVelodicord().getProxy().getCommandManager().executeAsync(new DiscordCommandSource(event), command).join()) {
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
                        getDisadmincommand().forEach(command -> builder.append("・ ").append(command).append("\n"));
                        builder.append("\nマイクラの管理者コマンド\n");
                        getMineadmincommand().forEach(command -> builder.append("・ ").append(command).append("\n"));
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
                                getDisadmincommand().add(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("discordの管理者コマンドを登録しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "マイクラ" -> {
                                getMineadmincommand().add(command);
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
                                getDisadmincommand().remove(command);
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle("discordの管理者コマンドを削除しました")
                                        .setDescription(command)
                                        .setColor(Color.blue)
                                        .build()
                                ).queue();
                            }

                            case "マイクラ" -> {
                                getMineadmincommand().remove(command);
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
