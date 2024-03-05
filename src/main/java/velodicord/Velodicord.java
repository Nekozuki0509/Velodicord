package velodicord;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.player.TabListEntry;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;


import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Plugin(
        id = "velodicord",
        name = "velodicord",
        version = BuildConstants.VERSION
)
public class Velodicord extends ListenerAdapter {

    private final Logger logger;

    @Getter
    final ProxyServer proxy;

    private static YamlDocument config;

    private static JDA jda;

    private static TextChannel textChannel;

    private Webhook webhook;

    private final OkHttpClient httpClient = new OkHttpClient();

    @DataDirectory
    Path dataDirectory;

    static Velodicord velodicord;

    private Map<String, String> players = new HashMap<>();

    @Inject
    public Velodicord(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        velodicord = this;

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent proxyInitializeEvent) throws InterruptedException {
        if (Files.notExists(dataDirectory))
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }

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
        } catch (IOException e){
            logger.error("Velodicordのconfigを読み込めませんでした");
            Optional<PluginContainer> container = proxy.getPluginManager().getPlugin("velodicord");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        try {
            jda = JDABuilder.createDefault(config.getString(Route.from("BotToken")))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(this)
                    .build();
        } catch (LoginException e) {
            logger.error("discord botにログインできませんでした:\n"+e);
        }

        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.create("velocity", "fabdicord"));

        jda.awaitReady();

        textChannel = jda.getTextChannelById(config.getString(Route.from("ChannelId")));
        if (textChannel == null) {
            throw new NullPointerException("チャンネルIDが不正です");
        }

        String webhookname = "Velodicord";
        Objects.requireNonNull(textChannel).getGuild().retrieveWebhooks().complete().forEach(webhook -> {
            if (webhookname.equals(webhook.getName())) this.webhook = webhook;
        });

        if (webhook == null) {
            webhook = textChannel.createWebhook(webhookname).complete();
        }

        textChannel.sendMessage("✅velocityサーバーが起動しました").queue();

        proxy.getEventManager().register(this, ListenerCloseEvent.class, PostOrder.FIRST, event ->
                textChannel.sendMessage("❌velocityサーバーが停止しました").queue());

        proxy.getEventManager().register(this, ProxyShutdownEvent.class, PostOrder.LAST, event -> jda.shutdown());

        proxy.getEventManager().register(this, DisconnectEvent.class, PostOrder.FIRST, event -> {
            String player = event.getPlayer().getUsername();
            proxy.sendMessage(text()
                    .append(text("["+player+"]", AQUA))
                    .append(text("が退出しました", YELLOW))
            );
            textChannel.sendMessage(new EmbedBuilder()
                    .setTitle("退出しました")
                    .setColor(Color.blue)
                    .setAuthor(player, null, "https://mc-heads.net/avatar/"+player+".png")
                    .build()).queue();
            players.remove(player);
            tabrefresh();
        });

        proxy.getEventManager().register(this, ServerConnectedEvent.class, event -> {
            String player = event.getPlayer().getUsername();
            String targetServer = event.getServer().getServerInfo().getName();

            event.getPreviousServer().ifPresentOrElse(
                    server -> {
                        proxy.sendMessage(text()
                                .append(text("["+player+"]", AQUA))
                                .append(text("が", YELLOW))
                                .append(text("["+server.getServerInfo().getName()+"]", DARK_GREEN))
                                .append(text("から", YELLOW))
                                .append(text("["+targetServer+"]", DARK_GREEN))
                                .append(text("へ移動しました", YELLOW))
                        );
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("["+server.getServerInfo().getName()+"]から["+targetServer+"]へ移動しました")
                                .setColor(Color.blue)
                                .setAuthor(player, null, "https://mc-heads.net/avatar/"+player+".png")
                                .build()).queue();
                    },
                    () -> {
                        proxy.sendMessage(text()
                                .append(text("["+player+"]", AQUA))
                                .append(text("が", YELLOW))
                                .append(text("["+targetServer+"]", DARK_GREEN))
                                .append(text("に入室しました", YELLOW))
                        );
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("["+targetServer+"]に入室しました")
                                .setColor(Color.blue)
                                .setAuthor(player, null, "https://mc-heads.net/avatar/"+player+".png")
                                .build()).queue();
                    }
            );
            players.put(player, targetServer);
            tabrefresh();
        });

        proxy.getEventManager().register(this, PlayerChatEvent.class, PostOrder.FIRST, event -> {
            String discord;
            String message = discord = event.getMessage();
            Player player = event.getPlayer();
            String server = player.getCurrentServer().get().getServerInfo().getName();
            TextComponent.Builder component = text()
                    .append(text("["+server+"]", DARK_GREEN))
                    .append(text("<"+player.getUsername()+"> "));
            String japanese = Japanizer.japanize(message);
            if (message.contains("@")){
                for (Member member : textChannel.getMembers()) {
                    String usernameMention = "@" + member.getUser().getName();
                    String displayNameMention = "@" + member.getEffectiveName();

                    discord = StringUtils.replaceIgnoreCase(discord, usernameMention, member.getAsMention());
                    message = message.replace(usernameMention, "<blue>"+usernameMention+"</blue>");

                    discord = StringUtils.replaceIgnoreCase(discord, displayNameMention, member.getAsMention());
                    message = message.replace(displayNameMention, "<blue>"+displayNameMention+"</blue>");

                    if (member.getNickname() != null) {
                        String nicknameMention = "@" + member.getNickname();
                        discord = StringUtils.replaceIgnoreCase(discord, nicknameMention, member.getAsMention());
                        message = message.replace(nicknameMention, "<blue>"+nicknameMention+"</blue>");
                    }
                }
                for (Role role : textChannel.getGuild().getRoles()) {
                    String roleMention = "@" + role.getName();
                    discord = StringUtils.replaceIgnoreCase(discord, roleMention, role.getAsMention());
                    message = message.replace(roleMention, "<blue>"+roleMention+"</blue>");
                }
                message = message.replace("@everyone", "<blue>@everyone</blue>");
                message = message.replace("@here", "<blue>@here</blue>");
            }
            component.append(MiniMessage.miniMessage().deserialize(message));
            discord = "[" + server + "] " + discord;
            if (!japanese.isEmpty()) {
                component.append(text("(" + japanese + ")", GOLD));
                discord += "(" + japanese + ")";
            }
            proxy.sendMessage(component);
            JsonObject body = new JsonObject();
            body.addProperty("content", discord);
            body.addProperty("username", player.getUsername());
            body.addProperty("avatar_url", "https://mc-heads.net/avatar/"+player.getUsername()+".png");
            JsonObject allowedMentions = new JsonObject();
            allowedMentions.add("parse", new Gson().toJsonTree(new ArrayList<>(List.of("everyone", "users", "roles"))).getAsJsonArray());
            body.add("allowed_mentions", allowedMentions);
            Request request = new Request.Builder()
                    .url(webhook.getUrl())
                    .post(RequestBody.create(MediaType.get("application/json"), body.toString()))
                    .build();

            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.submit(() -> {
                try {
                    Response response = httpClient.newCall(request).execute();
                    response.close();
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            });
            executor.shutdown();
        });

        proxy.getEventManager().register(this, PluginMessageEvent.class, event -> {
            if (!event.getIdentifier().equals(MinecraftChannelIdentifier.create("velocity", "fabdicord"))) return;
            String[] data = new String(event.getData(), StandardCharsets.UTF_8).split(":");
            switch (data[0]) {
                //death type:server:player:dim:xyz:message
                case "DEATH" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[1]+"]の["+data[3]+data[4]+"]で死亡しました")
                        .setDescription(data[5])
                        .setColor(Color.red)
                        .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                        .build()).queue();

                //advancement type:server:player:title:description
                case "ADVANCEMENT" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[1]+"]で["+data[3]+"]を達成しました")
                        .setDescription(data[4])
                        .setColor(Color.green)
                        .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                        .build()).queue();

                //command type:server:player:command
                case "COMMAND" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[1]+"]で["+data[3]+"]を実行しました")
                        .setColor(Color.yellow)
                        .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                        .build()).queue();

                //pos type:server:player:dim:xyz
                case "POS" -> {
                        proxy.sendMessage(text()
                                .append(text("<"+data[2]+"> ", BLUE))
                                .append(text("POS:[", GOLD))
                                .append(text("["+data[1]+"]", DARK_GREEN))
                                .append(text(data[3], GREEN))
                                .append(text(data[4], AQUA))
                                .append(text("]", GOLD))
                                .build());
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("POS:[["+data[1]+"]"+data[3]+data[4]+"]")
                                .setColor(Color.cyan)
                                .setAuthor(data[2], null, "https://mc-heads.net/avatar/"+data[2]+".png")
                                .build()).queue();
                }
            }
        });

        jda.upsertCommand("player", "現在参加しているプレイヤー").queue();

        String[] serverNames = proxy.getAllServers().stream().map(server -> server.getServerInfo().getName()).toArray(String[]::new);
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta server = commandManager.metaBuilder(serverNames[0]).aliases(serverNames).plugin(this).build();
        CommandMeta playerlist = commandManager.metaBuilder("playerlist").plugin(this).build();
        commandManager.register(server, new ServerCommand());
        commandManager.register(playerlist, new PlayerlistCommand());
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getChannel().getId().equals(textChannel.getId())) {
            String message = event.getMessage().getContentDisplay();
            String japanese = !(japanese=Japanizer.japanize(message)).isEmpty()?"("+japanese+")":"";
            proxy.sendMessage(text()
                    .append(text("[discord]", DARK_GREEN))
                    .append(text("<"+event.getAuthor().getName()+"> "))
                    .append(text(message))
                    .append(text(japanese, GOLD))
            );
            textChannel.sendMessage(japanese).queue();
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if ("player".equals(event.getName()) && event.getChannel().getId().equals(textChannel.getId())){
            event.reply("現在参加しているプレーヤー").queue();
            StringBuilder players = new StringBuilder();
            proxy.getAllPlayers().forEach(player -> players.append("・[").append(player.getCurrentServer().get().getServerInfo().getName()).append("]").append(player.getUsername()).append("\n"));
            event.getChannel().sendMessage(new EmbedBuilder()
                    .setDescription(players.toString())
                    .setColor(Color.blue)
                    .build()
            ).queue();
            return;
        }
        event.reply("不明なコマンド・チャンネルです").setEphemeral(false).queue();
    }

    public void tabrefresh() {
        for (Player player : this.proxy.getAllPlayers()) {
            for (Player player1 : this.proxy.getAllPlayers()) {
                if (!player.getTabList().containsEntry(player1.getUniqueId())) {
                    player.getTabList().addEntry(
                            TabListEntry.builder()
                                    .displayName(text()
                                            .append(text("["+players.get(player1.getUsername())+"]", DARK_GREEN))
                                            .append(text(player1.getUsername()))
                                            .build()
                                    )
                                    .profile(player1.getGameProfile())
                                    .gameMode(0)
                                    .tabList(player.getTabList())
                                    .build()
                    );
                }
            }

            for (TabListEntry entry : player.getTabList().getEntries()) {
                UUID uuid = entry.getProfile().getId();
                Optional<Player> playerOptional = proxy.getPlayer(uuid);
                if (playerOptional.isPresent()) {
                    entry.setLatency((int) player.getPing() * 1000);
                } else {
                    player.getTabList().removeEntry(uuid);
                }
            }
        }
    }
}
