package velodicord.events.minecraft;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class Disconnect {
    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        String player = event.getPlayer().getUsername();
        Velodicord.getVelodicord().getProxy().sendMessage(text()
                .append(text("[%s]".formatted(player), AQUA))
                .append(text(" が退出しました", YELLOW))
        );
        Discordbot.getNoticeChannel().sendMessageEmbeds(new EmbedBuilder()
                .setTitle("退出しました")
                .setColor(Color.blue)
                .setAuthor(player, null, "https://mc-heads.net/avatar/%s.png".formatted(player))
                .build()).queue();
        String message = "%sがマイクラサーバーから退出しました".formatted(player);
        for (String word : Config.getDic().keySet()) {
            message = message.replaceAll(word, Config.getDic().get(word));
        }
        Discordbot.sendvoicemessage(message, Discordbot.getDefaultSpeakerID());
    }
}
