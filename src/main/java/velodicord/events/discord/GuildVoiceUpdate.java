package velodicord.events.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import velodicord.Config;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

import static velodicord.Config.dic;
import static velodicord.discordbot.*;

public class GuildVoiceUpdate extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelUnion;
        if (event.getMember().getUser().isBot() && !Config.detectbot.contains(event.getMember().getId())) return;
        if (voicechannel == null) {
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(voiceState).getChannel());
            event.getGuild().getAudioManager().setSelfDeafened(true);
            MainChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.cyan)
                    .setTitle("[" + Objects.requireNonNull(voiceState.getChannel()).getName() + "]に接続しました")
                    .build()
            ).queue();

            voicechannel = voiceState.getChannel().getId();
            sendvoicemessage("接続しました", DefaultSpeakerID);
            return;
        }
        if ((channelUnion = event.getChannelJoined()) != null && voicechannel.equals(channelUnion.getId())) {
            String message = event.getMember().getEffectiveName() + "がボイスチャンネルに参加しました";
            for (String word : dic.keySet()) {
                message = message.replaceAll(word, dic.get(word));
            }
            sendvoicemessage(message, DefaultSpeakerID);
        } else if ((channelUnion = event.getChannelLeft()) != null && voicechannel.equals(channelUnion.getId())) {
            if (Objects.requireNonNull(jda.getVoiceChannelById(voicechannel)).getMembers().stream().noneMatch(member -> !member.getUser().isBot() || Config.detectbot.contains(member.getId()))) {
                event.getGuild().getAudioManager().closeAudioConnection();
                MainChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.orange)
                        .setTitle("切断しました")
                        .build()
                ).queue();
                voicechannel = null;
                return;
            }
            String message = event.getMember().getEffectiveName() + "がボイスチャンネルから退出しました";
            for (String word : dic.keySet()) {
                message = message.replaceAll(word, dic.get(word));
            }
            sendvoicemessage(message, DefaultSpeakerID);
        }
    }
}
