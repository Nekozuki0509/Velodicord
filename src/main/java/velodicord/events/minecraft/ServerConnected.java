package velodicord.events.minecraft;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ServerConnected {
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        String player = event.getPlayer().getUsername();
        String targetServer = event.getServer().getServerInfo().getName();

        event.getPreviousServer().ifPresentOrElse(
                server -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("[%s]".formatted(player), AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[%s]".formatted(server.getServerInfo().getName()), DARK_GREEN))
                            .append(text(" から ", YELLOW))
                            .append(text("[%s]".formatted(targetServer), DARK_GREEN))
                            .append(text(" へ移動しました", YELLOW))
                    );
                    Discordbot.getNoticeChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[%s] から [%s] へ移動しました".formatted(server.getServerInfo().getName(), targetServer))
                            .setColor(Color.blue)
                            .setAuthor(player, null, "https://mc-heads.net/avatar/%s.png".formatted(player))
                            .build()).queue();
                },
                () -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("[%s]".formatted(player), AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[%s]".formatted(targetServer), DARK_GREEN))
                            .append(text(" に入室しました", YELLOW))
                    );
                    Discordbot.getNoticeChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[%s] に入室しました".formatted(targetServer))
                            .setColor(Color.blue)
                            .setAuthor(player, null, "https://mc-heads.net/avatar/%s.png".formatted(player))
                            .build()).queue();
                    String message = "%sが%sに入室しました".formatted(player, targetServer);
                    for (String word : Config.getDic().keySet()) {
                        message = message.replaceAll(word, Config.getDic().get(word));
                    }
                    Discordbot.sendvoicemessage(message, Discordbot.getDefaultSpeakerID());
                }
        );
    }
}
