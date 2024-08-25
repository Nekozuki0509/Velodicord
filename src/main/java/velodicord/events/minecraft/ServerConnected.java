package velodicord.events.minecraft;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import velodicord.Config;
import velodicord.discordbot;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static velodicord.Velodicord.velodicord;

public class ServerConnected {
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        String player = event.getPlayer().getUsername();
        String targetServer = event.getServer().getServerInfo().getName();

        event.getPreviousServer().ifPresentOrElse(
                server -> {
                    velodicord.proxy.sendMessage(text()
                            .append(text("[" + player + "]", AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[" + server.getServerInfo().getName() + "]", DARK_GREEN))
                            .append(text(" から ", YELLOW))
                            .append(text("[" + targetServer + "]", DARK_GREEN))
                            .append(text(" へ移動しました", YELLOW))
                    );
                    discordbot.NoticeChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[" + server.getServerInfo().getName() + "] から [" + targetServer + "] へ移動しました")
                            .setColor(Color.blue)
                            .setAuthor(player, null, "https://mc-heads.net/avatar/" + player + ".png")
                            .build()).queue();
                },
                () -> {
                    velodicord.proxy.sendMessage(text()
                            .append(text("[" + player + "]", AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[" + targetServer + "]", DARK_GREEN))
                            .append(text(" に入室しました", YELLOW))
                    );
                    discordbot.NoticeChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[" + targetServer + "] に入室しました")
                            .setColor(Color.blue)
                            .setAuthor(player, null, "https://mc-heads.net/avatar/" + player + ".png")
                            .build()).queue();
                    String message = player + "が" + targetServer + "に入室しました";
                    for (String word : Config.dic.keySet()) {
                        message = message.replaceAll(word, Config.dic.get(word));
                    }
                    discordbot.sendvoicemessage(message, discordbot.DefaultSpeakerID);
                }
        );
    }
}
