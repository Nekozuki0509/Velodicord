package velodicord.events.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import velodicord.Config;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

import static velodicord.Discordbot.*;

public class GuildVoiceUpdate extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelUnion;
        if (event.getMember().getUser().isBot() && !Config.getDetectbot().contains(event.getMember().getId())) return;
        if (getVoicechannel() == null) return;

        if ((channelUnion = event.getChannelJoined()) != null && getVoicechannel().equals(channelUnion.getId())) {
            String message = "%sがボイスチャンネルに参加しました".formatted(event.getMember().getEffectiveName());
            for (String word : Config.getDic().keySet()) {
                message = message.replaceAll(word, Config.getDic().get(word));
            }
            sendvoicemessage(message, getDefaultSpeakerID());
        } else if ((channelUnion = event.getChannelLeft()) != null && getVoicechannel().equals(channelUnion.getId())) {
            if (Objects.requireNonNull(getJda().getVoiceChannelById(getVoicechannel())).getMembers().stream().noneMatch(member -> !member.getUser().isBot() || Config.getDetectbot().contains(member.getId()))) {
                event.getGuild().getAudioManager().closeAudioConnection();
                getMainChannel().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.orange)
                        .setTitle("切断しました")
                        .build()
                ).queue();
                setVoicechannel(null);
                return;
            }
            String message = "%sがボイスチャンネルから退出しました".formatted(event.getMember().getEffectiveName());
            for (String word : Config.getDic().keySet()) {
                message = message.replaceAll(word, Config.getDic().get(word));
            }
            sendvoicemessage(message, getDefaultSpeakerID());
        }
    }
}
