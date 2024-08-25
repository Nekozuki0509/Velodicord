package velodicord;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordCommandSource implements ConsoleCommandSource {

    private final SlashCommandInteractionEvent event;

    private StringBuilder output = new StringBuilder("```\n");

    private long lastOutputMillis = 0L;

    public DiscordCommandSource(SlashCommandInteractionEvent event) {
        this.event = event;
        if (event != null) event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.yellow)
                .setTitle("実行しています...")
                .build()
        ).queue();
    }

    @Override
    public Tristate getPermissionValue(String s) {
        return Tristate.TRUE;
    }

    @Override
    public void sendMessage(@NonNull Identity identity, @NonNull Component message) {
        long currentOutputMillis = System.currentTimeMillis();
        if (this.output.length() > 1500) {
            this.output.append("```");
            this.event.getChannel().sendMessage(this.output.toString()).queue();
            this.output = new StringBuilder("```\n");
        } else {
            this.output.append(PlainTextComponentSerializer.plainText().serialize(message)).append("\n");
        }

        if (currentOutputMillis - this.lastOutputMillis > 50L) {
            (new Thread(() -> {
                (new Timer()).schedule(new TimerTask() {
                    public void run() {
                        DiscordCommandSource.this.output.append("```");
                        DiscordCommandSource.this.event.getChannel().sendMessage(DiscordCommandSource.this.output.toString()).queue();
                        DiscordCommandSource.this.output = new StringBuilder("```\n");
                    }
                }, 51L);
            })).start();
        }

        this.lastOutputMillis = currentOutputMillis;
    }

    @Override
    public void sendMessage(@NonNull Identity identity, @NonNull Component message, @NonNull MessageType type) {
        sendMessage(identity, message);
    }
}
