package velodicord.events;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import velodicord.Config;
import velodicord.VOICEVOX;
import velodicord.discordbot;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import static velodicord.Velodicord.velodicord;
import static velodicord.discordbot.*;

public class discord extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!(event.getAuthor().isBot() && !Config.detectbot.contains(event.getAuthor().getId())) && event.getChannel().getId().equals(MainChannel.getId())) {
            String message = event.getMessage().getContentDisplay();
            String japanese;
            if (!(japanese=(!(japanese=Japanizer.japanize(message)).isEmpty()?"("+japanese+")":"")).isEmpty() && !message.contains("https://") && !message.contains("```")) MainChannel.sendMessage(message+japanese).queue();
            String cutmessage = message;
            for (String word : Config.dic.keySet()) {
                cutmessage = cutmessage.replace(word, Config.dic.get(word));
            }
            cutmessage = cutmessage.replace("~~", "").replace("**", "").replace("__", "").replaceAll("\\|\\|(.*?)\\|\\|", "ネタバレ");
            String mmessage = message.replace("~~", "").replace("**", "").replace("__", "").replaceAll("\\|\\|(.*?)\\|\\|", "<ネタバレ>");
            if (!event.getMessage().getAttachments().isEmpty()) {
                if (cutmessage.isEmpty()) {
                    mmessage = "添付ファイル";
                    cutmessage = "<添付ファイル>";
                } else {
                    cutmessage += "ぷらす添付ファイル";
                    mmessage += "<+添付ファイル>";
                }
            }
            velodicord.getProxy().sendMessage(text()
                    .append(text("[discord]", DARK_GREEN))
                    .append(text("<"+event.getAuthor().getName()+"> "))
                    .append(text(mmessage))
                    .append(text(!(mmessage=Japanizer.japanize(mmessage)).isEmpty()?"("+mmessage+")":"", GOLD))
            );
            if (cutmessage.contains("https://")) {
                sendvoicemessage("ゆーあーるえる省略", Config.disspeaker.getOrDefault(event.getAuthor().getId(), Integer.valueOf(Config.config.get("DefaultSpeakerID"))));
                return;
            } else if (cutmessage.contains("```")) {
                sendvoicemessage("コード省略", Config.disspeaker.getOrDefault(event.getAuthor().getId(), Integer.valueOf(Config.config.get("DefaultSpeakerID"))));
                return;
            }
            cutmessage = cutmessage.replace("@", "アット");
            String cutjapanese = !(cutjapanese=Japanizer.japanize(cutmessage)).isEmpty()?"("+cutjapanese+")":"";
            sendvoicemessage(cutmessage+cutjapanese, Config.disspeaker.getOrDefault(event.getAuthor().getId(), Integer.valueOf(Config.config.get("DefaultSpeakerID"))));
        }
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelUnion;
        if (voicechannel == null) return;
        if (event.getMember().getUser().isBot() && !Config.detectbot.contains(event.getMember().getId())) return;
        if ((channelUnion=event.getChannelJoined()) != null && voicechannel.equals(channelUnion.getId())) {
            String message = event.getMember().getEffectiveName()+"がボイスチャンネルに参加しました";
            for (String word : Config.dic.keySet()) {
                message = message.replace(word, Config.dic.get(word));
            }
            sendvoicemessage(message, Integer.parseInt(Config.config.get("DefaultSpeakerID")));
        } else if ((channelUnion=event.getChannelLeft()) != null && voicechannel.equals(channelUnion.getId())) {
            String message = event.getMember().getEffectiveName()+"がボイスチャンネルから退出しました";
            for (String word : Config.dic.keySet()) {
                message = message.replace(word, Config.dic.get(word));
            }
            sendvoicemessage(message, Integer.parseInt(Config.config.get("DefaultSpeakerID")));
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getChannel().getId().equals(MainChannel.getId())) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.red)
                    .setTitle("不明なチャンネルです")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "setmain" -> {
                MainChannel = event.getOptions().get(0).getAsChannel().asTextChannel();
                Config.config.put("MainChannelID", MainChannel.getId());
                if (MainChannel.getId().equals(LogChannel.getId())) {
                    LogChannel = MainChannel;
                    Config.config.put("LogChannelID", LogChannel.getId());
                }
                if (MainChannel.getId().equals(PosChannel.getId())) {
                    PosChannel = MainChannel;
                    Config.config.put("PosChannelID", PosChannel.getId());
                }
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("メインチャンネルを" + MainChannel.getName() + "(" + MainChannel.getId() + ")に設定しました")
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "setlog" -> {
                LogChannel = event.getOptions().get(0).getAsChannel().asTextChannel();
                Config.config.put("LogChannelID", LogChannel.getId());
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("ログチャンネルを" + LogChannel.getName() + "(" + LogChannel.getId() + ")に設定しました")
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "setpos" -> {
                PosChannel = event.getOptions().get(0).getAsChannel().asTextChannel();
                Config.config.put("PosChannelID", PosChannel.getId());
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("POSチャンネルを" + PosChannel.getName() + "(" + PosChannel.getId() + ")に設定しました")
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "join" -> {
                GuildVoiceState voiceState;
                if (voicechannel != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.red)
                            .setTitle("もうすでに"+event.getGuild().getAudioManager().getConnectedChannel().getName()+"に接続しています")
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }
                if ((voiceState=event.getMember().getVoiceState()).inAudioChannel()) {
                    event.getGuild().getAudioManager().openAudioConnection(voiceState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                    event.replyEmbeds(new EmbedBuilder()
                            .setColor(Color.cyan)
                            .setTitle("["+voiceState.getChannel().getName()+"]に接続しました")
                            .build()
                    ).queue();

                    voicechannel = voiceState.getChannel().getId();
                    sendvoicemessage("接続しました", Integer.parseInt(Config.config.get("DefaultSpeakerID")));
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
                    event.getGuild().getAudioManager().closeAudioConnection();
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

            case "player" -> {
                StringBuilder players = new StringBuilder();
                velodicord.getProxy().getAllPlayers().forEach(player -> players.append("・[").append(player.getCurrentServer().get().getServerInfo().getName()).append("]").append(player.getUsername()).append("\n"));
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("現在参加しているプレーヤー")
                        .setDescription(players.toString())
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "showdic" -> {
                StringBuilder builder = new StringBuilder();
                Config.dic.keySet().forEach(word -> builder.append("・ ").append(word).append(" -> ").append(Config.dic.get(word)).append("\n"));
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("辞書に登録されている単語一覧")
                        .setDescription(builder.toString())
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "adddic" -> {
                String word = event.getOptions().get(0).getAsString();
                String read = event.getOptions().get(1).getAsString();
                Config.dic.put(word, read);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("単語を登録・変更しました")
                        .setDescription(word + " -> " + read)
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "deletedic" -> {
                String word = event.getOptions().get(0).getAsString();
                Config.dic.remove(word);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("単語を削除しました")
                        .setDescription(word)
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "showchannel" ->
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("設定されているチャンネル")
                        .setDescription("メインチャンネル -> " + MainChannel.getName() + "(" + MainChannel.getId() + ")\n" +
                                        "ログチャンネル 　-> " + LogChannel.getName() + "(" + LogChannel.getId() + ")\n" +
                                        "POSチャンネル 　-> " + PosChannel.getName() + "(" + PosChannel.getId() + ")"
                        )
                        .setColor(Color.blue)
                        .build()
                ).queue();

            case "showdetectbot" -> {
                StringBuilder bots = new StringBuilder();
                Config.detectbot.forEach(id -> bots.append("・ ").append(Objects.requireNonNull(jda.getUserById(id)).getName()).append("(").append(id).append(")\n"));
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("登録されている発言を無視しないbot一覧")
                        .setDescription(bots)
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "adddetectbot" -> {
                String bot = event.getOptions().get(0).getAsUser().getId();
                Config.detectbot.add(bot);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(event.getOptions().get(0).getAsUser().getName() + "(" + bot + ")を登録しました")
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "deletedetectbot" -> {
                String bot = event.getOptions().get(0).getAsUser().getId();
                Config.detectbot.remove(bot);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(event.getOptions().get(0).getAsUser().getName() + "(" + bot + ")を削除しました")
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "showspeaker" -> {
                StringBuilder speakers = new StringBuilder();
                VOICEVOX.voicevox.keySet().forEach(id -> speakers.append("・ ").append(VOICEVOX.voicevox.get(id)).append(" : ").append(id).append("\n"));
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("話者の種類とそのID")
                        .setDescription(speakers)
                        .setColor(Color.blue)
                        .build()
                ).queue();
            }

            case "setspeaker" -> {
                int id = event.getOptions().get(0).getAsInt();
                if (VOICEVOX.voicevox.containsKey(id)) {
                    Config.disspeaker.put(event.getUser().getId(), id);
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(VOICEVOX.voicevox.get(id) + "に設定しました")
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();
                    discordbot.sendvoicemessage(VOICEVOX.voicevox.get(id) + "に設定しました", id);
                } else {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(id + "を持つ話者はいません")
                            .setColor(Color.red)
                            .build()
                    ).setEphemeral(true).queue();
                }
            }

            case "setdefaultspeaker" -> {
                int id = event.getOptions().get(0).getAsInt();
                if (VOICEVOX.voicevox.containsKey(id)) {
                    Config.config.put("DefaultSpeakerID", String.valueOf(id));
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(VOICEVOX.voicevox.get(id) + "に設定しました")
                            .setColor(Color.blue)
                            .build()
                    ).setEphemeral(true).queue();
                    discordbot.sendvoicemessage(VOICEVOX.voicevox.get(id) + "に設定しました", id);
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
