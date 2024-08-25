package velodicord;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import velodicord.commands.PlayerlistCommand;
import velodicord.commands.ServerCommand;
import velodicord.commands.SetspeakerCommand;
import velodicord.events.minecraft.*;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "velodicord",
        name = "velodicord",
        version = BuildConstants.VERSION
)
public class Velodicord {

    public final Logger logger;

    public final ProxyServer proxy;

    public static Velodicord velodicord;

    @Inject
    public Velodicord(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        Config.dataDirectory = dataDirectory;
        Config.configjson = dataDirectory.resolve("config.json");
        Config.dicjson = dataDirectory.resolve("dic.json");
        Config.detectbotjson = dataDirectory.resolve("detectbot.json");
        Config.ignorecommandjson = dataDirectory.resolve("ignorecommand.json");
        Config.disadmincommandjson = dataDirectory.resolve("disadmincommand.json");
        Config.mineadmincommandjson = dataDirectory.resolve("mineadmincommand.json");
        Config.disspeakerjson = dataDirectory.resolve("disspeaker.json");
        Config.minespeakerjson = dataDirectory.resolve("minespeaker.json");
        Config.mentionablejson = dataDirectory.resolve("mentionable.json");
        velodicord = this;

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws InterruptedException, IOException {
        Config.init();

        discordbot.init();

        discordbot.NoticeChannel.sendMessage("✅velocityサーバーが起動しました").queue();

        proxy.getEventManager().register(this, new ListenerClose());

        proxy.getEventManager().register(this, new Disconnect());

        proxy.getEventManager().register(this, new ServerConnected());

        proxy.getEventManager().register(this, new PlayerChat());

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
