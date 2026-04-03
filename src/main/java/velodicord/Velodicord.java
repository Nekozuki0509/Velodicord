package velodicord;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import velodicord.commands.PlayerlistCommand;
import velodicord.commands.ServerCommand;
import velodicord.commands.SetspeakerCommand;
import velodicord.events.minecraft.Disconnect;
import velodicord.events.minecraft.ListenerClose;
import velodicord.events.minecraft.PlayerChat;
import velodicord.events.minecraft.ServerConnected;
import velodicord.pmConnection.DiscordPluginMessageManager;
import velodicord.pmConnection.PluginMessageManager;
import velodicord.pmConnection.WebSocketPluginMessageManager;

import java.nio.file.Path;

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

    @Getter
    private static Velodicord velodicord;

    @Getter
    private static PluginMessageManager PMManager;

    @Getter
    private static int WebSocketPortIncrement;

    @Inject
    public Velodicord(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        Config.setDataDirectory(dataDirectory);
        Config.setConfigjson(dataDirectory.resolve("config.json"));
        Config.setDicjson(dataDirectory.resolve("dic.json"));
        Config.setDetectbotjson(dataDirectory.resolve("detectbot.json"));
        Config.setIgnorecommandjson(dataDirectory.resolve("ignorecommand.json"));
        Config.setDisadmincommandjson(dataDirectory.resolve("disadmincommand.json"));
        Config.setMineadmincommandjson(dataDirectory.resolve("mineadmincommand.json"));
        Config.setDisspeakerjson(dataDirectory.resolve("disspeaker.json"));
        Config.setMinespeakerjson(dataDirectory.resolve("minespeaker.json"));
        Config.setMentionablejson(dataDirectory.resolve("mentionable.json"));
        velodicord = this;

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init();

        Discordbot.init();

        Discordbot.getNoticeChannel().sendMessage("✅velocityサーバーが起動しました").queue();

        proxy.getEventManager().register(this, new ListenerClose());

        proxy.getEventManager().register(this, new Disconnect());

        proxy.getEventManager().register(this, new ServerConnected());

        proxy.getEventManager().register(this, new PlayerChat());

        proxy.getEventManager().register(this, ProxyShutdownEvent.class, e -> {
            if (PMManager instanceof WebSocketPluginMessageManager manager) manager.closeAll();
        });

        WebSocketPortIncrement = Integer.parseInt(Config.getConfig().get("WebSocketPortIncrement"));

        PMManager = Config.getConfig().get("PMType").equals("1") ? new DiscordPluginMessageManager() : new WebSocketPluginMessageManager();

        CommandManager commandManager = proxy.getCommandManager();

        String[] serverNames = proxy.getAllServers().stream().map(server -> server.getServerInfo().getName()).toArray(String[]::new);

        CommandMeta server = commandManager.metaBuilder(serverNames[0]).aliases(serverNames).plugin(this).build();
        CommandMeta playerlist = commandManager.metaBuilder("playerlist").plugin(this).build();
        CommandMeta setspeaker = commandManager.metaBuilder("speaker").plugin(this).build();

        commandManager.register(server, new ServerCommand());
        commandManager.register(playerlist, new PlayerlistCommand());
        commandManager.register(setspeaker, new SetspeakerCommand());
    }
}
