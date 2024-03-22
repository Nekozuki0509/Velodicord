package velodicord.commands;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import velodicord.Velodicord;

import java.awt.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;

public class PlayerlistCommand implements RawCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(text("プレイヤーしかこのコマンドを使えません").color(TextColor.color(Color.RED.getRGB())));
        } else {
            TextComponent.Builder players = text();
            Velodicord.velodicord.getProxy().getAllPlayers().forEach(player -> players
                    .append(text("・"))
                    .append(text("["+player.getCurrentServer().get().getServerInfo().getName()+"]", DARK_GREEN))
                    .append(text(player.getUsername()+"\n")));
            Velodicord.velodicord.getProxy().sendMessage(players.build());
        }
    }
}
