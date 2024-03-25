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

import javax.annotation.Nonnull;
import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import static velodicord.Velodicord.velodicord;
import static velodicord.discordbot.*;

public class discord extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getChannel().getId().equals(textChannel.getId())) {
            String message = event.getMessage().getContentDisplay();
            String japanese;
            if (!(japanese=(!(japanese=Japanizer.japanize(message)).isEmpty()?"("+japanese+")":"")).isEmpty() && !message.contains("https://") && !message.contains("```")) textChannel.sendMessage(message+japanese).queue();
            String cutmessage = message;
            for (String word : velodicord.dic.keySet()) {
                cutmessage = cutmessage.replace(word, velodicord.dic.get(word));
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
                sendvoicemessage("ゆーあーるえる省略");
                return;
            } else if (cutmessage.contains("```")) {
                sendvoicemessage("コード省略");
                return;
            }
            cutmessage = cutmessage.replace("@", "アット");
            String cutjapanese = !(cutjapanese=Japanizer.japanize(cutmessage)).isEmpty()?"("+cutjapanese+")":"";
            sendvoicemessage(cutmessage+cutjapanese);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelUnion;
        if (voicechannel == null) return;
        if (event.getMember().getUser().isBot()) return;
        if ((channelUnion=event.getChannelJoined()) != null && voicechannel.equals(channelUnion.getId())) {
            String message = event.getMember().getEffectiveName()+"がボイスチャンネルに参加しました";
            for (String word : velodicord.dic.keySet()) {
                message = message.replace(word, velodicord.dic.get(word));
            }
            sendvoicemessage(message);
        } else if ((channelUnion=event.getChannelLeft()) != null && voicechannel.equals(channelUnion.getId())) {
            String message = event.getMember().getEffectiveName()+"がボイスチャンネルから退出しました";
            for (String word : velodicord.dic.keySet()) {
                message = message.replace(word, velodicord.dic.get(word));
            }
            sendvoicemessage(message);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getChannel().getId().equals(textChannel.getId())) {
            switch (event.getName()) {
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
                        sendvoicemessage("接続しました");
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
                    velodicord.dic.keySet().forEach(word -> builder.append("・ ").append(word).append(" -> ").append(velodicord.dic.get(word)).append("\n"));
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
                    velodicord.dic.put(word, read);
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("単語を登録・変更しました")
                            .setDescription(word + " -> " + read)
                            .setColor(Color.blue)
                            .build()
                    ).queue();
                }

                case "removedic" -> {
                    String word = event.getOptions().get(0).getAsString();
                    velodicord.dic.remove(word);
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("単語を削除しました")
                            .setDescription(word)
                            .setColor(Color.blue)
                            .build()
                    ).queue();
                }
            }
        }else event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.red)
                .setTitle("不明なチャンネルです")
                .build()
        ).setEphemeral(true).queue();
    }
}
