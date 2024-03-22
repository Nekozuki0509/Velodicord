package velodicord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.slf4j.Logger;
import velodicord.commands.PlayerlistCommand;
import velodicord.commands.ServerCommand;
import velodicord.events.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static velodicord.discordbot.*;

@Plugin(
        id = "velodicord",
        name = "velodicord",
        version = BuildConstants.VERSION
)
public class Velodicord {

    public final Logger logger;

    @Getter
    final ProxyServer proxy;

    YamlDocument config;

    @DataDirectory
    Path dataDirectory;

    public Path dicjson;

    public static Velodicord velodicord;

    public final ObjectMapper mapper = new ObjectMapper();

    public Map<String, String> dic;

    @Inject
    public Velodicord(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.dicjson = dataDirectory.resolve("dic.json");
        velodicord = this;

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent proxyInitializeEvent) throws InterruptedException, IOException {
        if (Files.notExists(dataDirectory))
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }

        if (Files.notExists(dicjson))
            Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/dic.json")), dicjson);

        TypeReference<HashMap<String, String>> reference = new TypeReference<>() {
        };
        dic = mapper.readValue(mapper.readTree(new File(String.valueOf(dicjson))).toString(), reference);

        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yaml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yaml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Velodicordのconfigを読み込めませんでした");
            Optional<PluginContainer> container = proxy.getPluginManager().getPlugin("velodicord");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        discordbot.init();

        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.create("velocity", "fabdicord"));

        textChannel.sendMessage("✅velocityサーバーが起動しました").queue();

        proxy.getEventManager().register(this, ProxyShutdownEvent.class, PostOrder.LAST, event -> jda.shutdown());

        proxy.getEventManager().register(this, new ListenerClose());

        proxy.getEventManager().register(this, new Disconnect());

        proxy.getEventManager().register(this, new ServerConnected());

        proxy.getEventManager().register(this, new PlayerChat());

        proxy.getEventManager().register(this, new PluginMessage());

        String[] serverNames = proxy.getAllServers().stream().map(server -> server.getServerInfo().getName()).toArray(String[]::new);
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta server = commandManager.metaBuilder(serverNames[0]).aliases(serverNames).plugin(this).build();
        CommandMeta playerlist = commandManager.metaBuilder("playerlist").plugin(this).build();
        commandManager.register(server, new ServerCommand());
        commandManager.register(playerlist, new PlayerlistCommand());
    }
}
