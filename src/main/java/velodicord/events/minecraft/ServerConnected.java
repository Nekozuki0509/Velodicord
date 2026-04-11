package velodicord.events.minecraft;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ServerConnected {
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        String playerName = event.getPlayer().getUsername();
        String targetServer = event.getServer().getServerInfo().getName();

        event.getPreviousServer().ifPresentOrElse(
                server -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("[%s]".formatted(playerName), AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[%s]".formatted(server.getServerInfo().getName()), DARK_GREEN))
                            .append(text(" から ", YELLOW))
                            .append(text("[%s]".formatted(targetServer), DARK_GREEN))
                            .append(text(" へ移動しました", YELLOW))
                    );
                    Discordbot.getNoticeChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[%s] から [%s] へ移動しました".formatted(server.getServerInfo().getName(), targetServer))
                            .setColor(Color.blue)
                            .setAuthor(playerName, null, "https://mc-heads.net/avatar/%s.png".formatted(playerName))
                            .build()).queue();
                },
                () -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("[%s]".formatted(playerName), AQUA))
                            .append(text(" が ", YELLOW))
                            .append(text("[%s]".formatted(targetServer), DARK_GREEN))
                            .append(text(" に参加しました", YELLOW))
                    );
                    Discordbot.getNoticeChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("[%s] に参加しました".formatted(targetServer))
                            .setColor(Color.blue)
                            .setAuthor(playerName, null, "https://mc-heads.net/avatar/%s.png".formatted(playerName))
                            .build()).queue();
                    String message = "%sが%sに参加しました".formatted(playerName, targetServer);
                    for (String word : Config.getDic().keySet()) {
                        message = message.replaceAll(word, Config.getDic().get(word));
                    }
                    Discordbot.sendvoicemessage(message, Discordbot.getDefaultSpeakerID());
                }
        );

        new Thread(() -> {
            java.util.List<TabListEntry> tabListEntries = new java.util.ArrayList<>(Velodicord.getVelodicord().getProxy().getAllPlayers().stream().map(player -> TabListEntry.builder()
                    .profile(player.getGameProfile())
                    .displayName(Component.text()
                            .append(text("[%s] ".formatted(player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : targetServer), DARK_GREEN))
                            .append(text("%s".formatted(player.getUsername())))
                            .build()
                    )
                    .tabList(player.getTabList())
                    .build()
            ).toList());

            tabListEntries.addAll(Velodicord.getBots().values().stream().map(botInfo -> TabListEntry.builder()
                    .profile(botInfo.gameProfile())
                    .displayName(Component.text()
                            .append(text("[%s] ".formatted(botInfo.server()), DARK_GREEN))
                            .append(text("(BOT) ", BLUE))
                            .append(text(botInfo.name()))
                            .build()
                    )
                    .tabList(Velodicord.getVelodicord().getProxy().getAllPlayers().stream().findAny().orElseThrow().getTabList())
                    .build()
            ).toList());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Velodicord.getVelodicord().getLogger().error("TabList更新スレッドが割り込まれました: {}", ExceptionUtils.getStackTrace(e));
            }

            Velodicord.getVelodicord().getProxy().getAllPlayers().forEach(player -> {
                player.getTabList().clearAll();
                player.getTabList().addEntries(tabListEntries);
            });
        }).start();
    }
}
