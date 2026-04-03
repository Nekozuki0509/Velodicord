package velodicord.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Voicevox;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class SetspeakerCommand implements SimpleCommand {

    @Override
    public void execute(final Invocation invocation) {
        int id = Integer.parseInt(invocation.arguments()[0]);
        if (invocation.source() instanceof Player player) {
            Voicevox.getVoicevox().stream().filter(m -> m.id() == id).findFirst().ifPresentOrElse(
                    m -> {
                        Config.getMinespeaker().put(player.getUniqueId().toString(), id);
                        player.sendMessage(text()
                                .append(text(m.name(), AQUA))
                                .append(text("に設定しました"))
                                .build()
                        );
                        Discordbot.sendvoicemessage("%sに設定しました".formatted(Voicevox.getVoicevox().get(id)), id);
                    },
                    () -> player.sendMessage(text("%dを持つ話者はいません".formatted(id), RED))
            );
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        return List.of();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}