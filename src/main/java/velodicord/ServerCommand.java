// Source code is decompiled from a .class file using FernFlower decompiler.
package velodicord;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.awt.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ServerCommand implements RawCommand {
    public ServerCommand() {
    }

    @Override
    public void execute(RawCommand.Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("プレイヤーしかこのコマンドを使えません").color(TextColor.color(Color.RED.getRGB())));
        } else {
            String commandName = invocation.alias();
            RegisteredServer server = Velodicord.velodicord.getProxy().getServer(commandName).orElse(null);
            if (server == null) {
                player.sendMessage(Component.text("サーバーが見つかりません").color(TextColor.color(Color.RED.getRGB())));
            } else {
                player.createConnectionRequest(server).fireAndForget();
            }
        }
    }
}
