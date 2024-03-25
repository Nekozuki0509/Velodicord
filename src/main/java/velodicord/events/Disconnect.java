package velodicord.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import velodicord.discordbot;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;


import static velodicord.Velodicord.velodicord;

public class Disconnect {
    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        String player = event.getPlayer().getUsername();
        velodicord.getProxy().sendMessage(text()
                .append(text("["+player+"]", AQUA))
                .append(text("が退出しました", YELLOW))
        );
        discordbot.textChannel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("退出しました")
                .setColor(Color.blue)
                .setAuthor(player, null, "https://mc-heads.net/avatar/"+player+".png")
                .build()).queue();
        String message = player+"がマイクラサーバーから退出しました";
        for (String word : velodicord.dic.keySet()) {
            message = message.replace(word, velodicord.dic.get(word));
        }
        discordbot.sendvoicemessage(message);
    }
}
