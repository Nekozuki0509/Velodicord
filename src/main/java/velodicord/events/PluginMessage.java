package velodicord.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.dv8tion.jda.api.EmbedBuilder;
import velodicord.discordbot;

import java.awt.*;
import java.nio.charset.StandardCharsets;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

import static velodicord.Velodicord.velodicord;
import static velodicord.discordbot.MainChannel;

public class PluginMessage {
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(MinecraftChannelIdentifier.create("velocity", "fabdicord"))) return;
        String[] data = new String(event.getData(), StandardCharsets.UTF_8).split(":");
        switch (data[0]) {
            //death type:server:player:dim:xyz:message
            case "DEATH" -> discordbot.LogChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("["+data[1]+"]の["+data[3]+data[4]+"]で死亡しました")
                    .setDescription(data[5])
                    .setColor(Color.red)
                    .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                    .build()).queue();

            //advancement type:server:player:title:description
            case "ADVANCEMENT" -> discordbot.LogChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("["+data[1]+"]で["+data[3]+"]を達成しました")
                    .setDescription(data[4])
                    .setColor(Color.green)
                    .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                    .build()).queue();

            //command type:server:player:command
            case "COMMAND" -> discordbot.LogChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("["+data[1]+"]で["+data[3]+"]を実行しました")
                    .setColor(Color.yellow)
                    .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                    .build()).queue();

            //pos type:server:player:dim:xyz
            case "POS" -> {
                velodicord.getProxy().sendMessage(text()
                        .append(text("<"+data[2]+"> ", BLUE))
                        .append(text("POS:[", GOLD))
                        .append(text("["+data[1]+"]", DARK_GREEN))
                        .append(text(data[3], GREEN))
                        .append(text(data[4], AQUA))
                        .append(text("]", GOLD))
                        .build());
                MainChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("POS:[["+data[1]+"]"+data[3]+data[4]+"]")
                        .setColor(Color.cyan)
                        .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                        .build()).queue();
            }

            //npos type:server:player:dim:xyz:name
            case "NPOS" -> {
                velodicord.getProxy().sendMessage(text()
                        .append(text("<"+data[2]+"> ", BLUE))
                        .append(text(data[5]+":[", GOLD))
                        .append(text("["+data[1]+"]", DARK_GREEN))
                        .append(text(data[3], GREEN))
                        .append(text(data[4], AQUA))
                        .append(text("]", GOLD))
                        .build());
                MainChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle(data[5]+":[["+data[1]+"]"+data[3]+data[4]+"]")
                        .setColor(Color.cyan)
                        .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                        .build()).queue();
                if (!discordbot.PosChannel.getId().equals(MainChannel.getId())) {
                    discordbot.PosChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle(data[5]+":[["+data[1]+"]"+data[3]+data[4]+"]")
                            .setColor(Color.cyan)
                            .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                            .build()).queue();
                }
            }
        }
    }
}
