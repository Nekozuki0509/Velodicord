package velodicord.commands;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.concurrent.CompletableFuture;
import java.util.List;

import com.velocitypowered.api.proxy.Player;
import velodicord.Config;
import velodicord.VOICEVOX;
import velodicord.discordbot;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class SetspeakerCommand implements SimpleCommand {

    @Override
    public void execute(final Invocation invocation) {
        int id = Integer.parseInt(invocation.arguments()[0]);
        if (invocation.source() instanceof Player player) {
            if (VOICEVOX.voicevox.containsKey(id)) {
                Config.disspeaker.put(player.getUniqueId().toString(), id);
                player.sendMessage(text()
                        .append(text(VOICEVOX.voicevox.get(id), AQUA))
                        .append(text("に設定しました"))
                        .build()
                );
                discordbot.sendvoicemessage(VOICEVOX.voicevox.get(id) + "に設定しました", id);
            } else {
                player.sendMessage(text(id + "を持つ話者はいません", RED));
            }
        }
    }
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
    }
    @Override
    public List<String> suggest(final Invocation invocation) {
        return VOICEVOX.voicevox.keySet().stream().map(id -> Integer.toString(id)).toList();
    }
    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}