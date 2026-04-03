package velodicord.commands;

import com.velocitypowered.api.command.RawCommand;
import net.kyori.adventure.text.TextComponent;
import velodicord.Velodicord;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;

public class PlayerlistCommand implements RawCommand {
    @Override
    public void execute(Invocation invocation) {
        TextComponent.Builder players = text();
        Velodicord.getVelodicord().getProxy().getAllPlayers().forEach(player -> players
                .append(text("・"))
                .append(text("[%s]".formatted(player.getCurrentServer().get().getServerInfo().getName()), DARK_GREEN))
                .append(text(player.getUsername() + "\n")));
        Velodicord.getVelodicord().getProxy().sendMessage(players.build());
    }
}
