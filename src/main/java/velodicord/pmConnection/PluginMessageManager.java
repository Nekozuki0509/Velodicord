package velodicord.pmConnection;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static velodicord.Config.*;
import static velodicord.Discordbot.*;

public abstract class PluginMessageManager {

    public static void receive(String msg) {
        String[] data = msg.split("&");

        //to:what:data
        if ("VELOCITY".equals(data[0])) {
            switch (data[1]) {
                case "OK" -> {
                    try {
                        Velodicord.getPMManager().sendMessage(data[2], "RESOK&%s&%s&%s&%s&%s&%s&%s".formatted(getNoticeChannel().getId(), getLogForumChannel().map(ForumChannel::getId).orElse(""), getCommandChannel(), getCommandRole().getId(), Files.readString(getIgnorecommandjson()), Files.readString(getDisadmincommandjson()), Files.readString(getMineadmincommandjson())));
                    } catch (IOException e) {
                        Velodicord.getVelodicord().getLogger().error("Failed to read json: {}", ExceptionUtils.getStackTrace(e));
                    }
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("✅ "))
                            .append(text("[%s]".formatted(data[2]), DARK_GREEN))
                            .append(text(" が起動しました", YELLOW))
                    );

                    Discordbot.sendvoicemessage("%sが起動しました".formatted(data[2]), getDefaultSpeakerID());

                    Arrays.stream(data[3].split(",")).forEach(playerName -> {
                        if (playerName.isEmpty()) return;
                        GameProfile gameProfile = GameProfile.forOfflinePlayer("%s%s".formatted(data[2], playerName));
                        Velodicord.getBots().put(gameProfile.getId(), new Velodicord.BotInfo(playerName, data[2], gameProfile));
                    });
                }

                case "RESOK" -> Arrays.stream(data[3].split(",")).forEach(playerName -> {
                    if (playerName.isEmpty()) return;
                    GameProfile gameProfile = GameProfile.forOfflinePlayer("%s%s".formatted(data[2], playerName));
                    Velodicord.getBots().put(gameProfile.getId(), new Velodicord.BotInfo(playerName, data[2], gameProfile));
                });

                case "FIN" -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("\uD83D\uDED1 "))
                            .append(text("[%s]".formatted(data[2]), DARK_GREEN))
                            .append(text(" が停止しました", YELLOW))
                    );

                    Discordbot.sendvoicemessage("%sが停止しました".formatted(data[2]), getDefaultSpeakerID());
                }

                case "SEND" ->
                        Velodicord.getVelodicord().getProxy().sendMessage(MiniMessage.miniMessage().deserialize(data[2]));

                case "READ" -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(MiniMessage.miniMessage().deserialize(data[2]));
                    String message = data[3];
                    for (String word : Config.getDic().keySet()) {
                        message = message.replaceAll(word, Config.getDic().get(word));
                    }
                    Discordbot.sendvoicemessage(message, getDefaultSpeakerID());
                }

                case "POS" -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("<%s> ".formatted(data[3]), BLUE))
                            .append(text("POS:[", GOLD))
                            .append(text("[%s]".formatted(data[2]), DARK_GREEN))
                            .append(text(data[4], GREEN))
                            .append(text(data[5], AQUA))
                            .append(text("]", GOLD))
                            .build());
                    getMainChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("POS:[[%s]%s%s]".formatted(data[2], data[4], data[5]))
                            .setColor(Color.cyan)
                            .setAuthor(data[3], null, "https://mc-heads.net/avatar/%s.png".formatted(data[3]))
                            .build()).queue();
                }

                case "NPOS" -> {
                    Velodicord.getVelodicord().getProxy().sendMessage(text()
                            .append(text("<%s> ".formatted(data[3]), BLUE))
                            .append(text("%s:[".formatted(data[6]), GOLD))
                            .append(text("[%s]".formatted(data[2]), DARK_GREEN))
                            .append(text(data[4], GREEN))
                            .append(text(data[5], AQUA))
                            .append(text("]", GOLD))
                            .build());
                    getMainChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("%s:[[%s]%s%s]".formatted(data[6], data[2], data[4], data[5]))
                            .setColor(Color.cyan)
                            .setAuthor(data[3], null, "https://mc-heads.net/avatar/%s.png".formatted(data[3]))
                            .build()).queue();
                    if (!Discordbot.getPosChannel().getId().equals(getMainChannel().getId())) {
                        Discordbot.getPosChannel().sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("%s:[[%s]%s%s]".formatted(data[6], data[2], data[4], data[5]))
                                .setColor(Color.cyan)
                                .setAuthor(data[3], null, "https://mc-heads.net/avatar/%s.png".formatted(data[3]))
                                .build()).queue();
                    }
                }

                case "JOIN" -> {
                    String ServerName = data[2];
                    String PN = data[3];

                    if (Velodicord.getVelodicord().getProxy().getAllPlayers().isEmpty()) return;

                    new Thread(() -> {
                        GameProfile gameProfile = GameProfile.forOfflinePlayer("%s%s".formatted(ServerName, PN));
                        Velodicord.getBots().put(gameProfile.getId(), new Velodicord.BotInfo(PN, ServerName, gameProfile));

                        java.util.List<TabListEntry> tabListEntries = new java.util.ArrayList<>(Velodicord.getVelodicord().getProxy().getAllPlayers().stream().map(player -> TabListEntry.builder()
                                .profile(player.getGameProfile())
                                .displayName(Component.text()
                                        .append(text("[%s] ".formatted(player.getCurrentServer().get().getServerInfo().getName()), DARK_GREEN))
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

                case "DISCONNECT" -> {
                    String ServerName = data[2];
                    String playerName = data[3];
                    GameProfile gameProfile = GameProfile.forOfflinePlayer("%s%s".formatted(ServerName, playerName));

                    Velodicord.getBots().remove(gameProfile.getId());

                    Velodicord.getVelodicord().getProxy().getAllPlayers().forEach(player -> player.getTabList().removeEntry(gameProfile.getId()));
                }

                default -> Velodicord.getVelodicord().getLogger().error("Received unexpected message: {}", msg);
            }
        }
    }

    public abstract void sendMessage(String to, String msg);
}
