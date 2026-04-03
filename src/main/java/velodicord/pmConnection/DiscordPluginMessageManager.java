package velodicord.pmConnection;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static velodicord.Config.*;
import static velodicord.Discordbot.*;

public class DiscordPluginMessageManager extends PluginMessageManager {

    @Getter
    @Setter
    private TextChannel PMChannel;

    public DiscordPluginMessageManager() {
        super();

        this.PMChannel = Optional.ofNullable(Discordbot.getJda().getTextChannelById(Config.getConfig().get("PMChannelID"))).orElse(Discordbot.getMainChannel());

        try {
            sendMessage("ALL", "OK&%s&%s&%s&%s&%s&%s&%s".formatted(getNoticeChannel().getId(), getLogForumChannel().map(ForumChannel::getId).orElse(""), getCommandChannel(), Discordbot.getCommandRole().getId(), Files.readString(getIgnorecommandjson()), Files.readString(getDisadmincommandjson()), Files.readString(getMineadmincommandjson())));
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Failed to read command config files: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void sendMessage(String to, String msg) {
        PMChannel.sendMessage("%s&%s".formatted(to, msg)).complete();
    }
}
