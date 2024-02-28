package velodicord;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import org.slf4j.Logger;


import static com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Plugin(
        id = "velodicord",
        name = "velodicord",
        version = BuildConstants.VERSION
)
public class Velodicord {

    @Getter
    private final Logger logger;

    @Getter
    private final ProxyServer proxy;

    @Inject
    public Velodicord(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent eve) throws ClassNotFoundException {

        Class.forName("org.apache.commons.lang3.StringUtils");

        proxy.getEventManager().register(this, ListenerBoundEvent.class, event -> {

        });

        proxy.getEventManager().register(this, ListenerCloseEvent.class, event -> {

        });

        proxy.getEventManager().register(this, ProxyShutdownEvent.class, event -> {

        });

        proxy.getEventManager().register(this, DisconnectEvent.class, event -> {
            proxy.sendMessage(text()
                    .append(text("["+event.getPlayer().getUsername()+"]", AQUA))
                    .append(text("が退出しました", YELLOW))
            );
        });

        proxy.getEventManager().register(this, ServerConnectedEvent.class, event -> {
            String player = event.getPlayer().getUsername();
            String targetServer = event.getServer().getServerInfo().getName();

            event.getPreviousServer().ifPresentOrElse(
                    server -> proxy.sendMessage(text()
                            .append(text("["+player+"]", AQUA))
                            .append(text("が", YELLOW))
                            .append(text("["+server.getServerInfo().getName()+"]", DARK_GREEN))
                            .append(text("から", YELLOW))
                            .append(text("["+targetServer+"]", DARK_GREEN))
                            .append(text("へ移動しました", YELLOW))
                    ),
                    () -> proxy.sendMessage(text()
                            .append(text("["+player+"]", AQUA))
                            .append(text("が入室しました", YELLOW))
                    )
            );
        });

        proxy.getEventManager().register(this, PlayerChatEvent.class, PostOrder.FIRST, event -> {
            TextComponent.Builder component = text()
                    .append(text("["+event.getPlayer().getCurrentServer().get().getServerInfo().getName()+"]", DARK_GREEN))
                    .append(text("<"+event.getPlayer().getUsername()+"> "))
                    .append(text(event.getMessage()));
            if (!Japanizer.japanize(event.getMessage()).isEmpty()) component.append(text("("+Japanizer.japanize(event.getMessage())+")", GOLD));
            proxy.sendMessage(component);
            event.setResult(denied());
        });
    }

}
