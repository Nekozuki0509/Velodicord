package velodicord;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
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
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Plugin(
        id = "velodicord",
        name = "velodicord",
        version = BuildConstants.VERSION
)
public class Velodicord extends ListenerAdapter {

    @Getter
    private final Logger logger;

    @Getter
    private final ProxyServer proxy;

    @Getter
    private static YamlDocument config;

    private static final Map<String,Map<String, UUID>> bots = new HashMap<>();

    private static JDA jda;

    private static TextChannel textChannel;

    private Webhook webhook;

    private final OkHttpClient httpClient = new OkHttpClient();


    @Inject
    public Velodicord(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;

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

        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yaml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
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

        logger.info("Velodicord loaded");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent proxyInitializeEvent) {

        proxy.getEventManager().register(this, ListenerBoundEvent.class, PostOrder.FIRST, event ->
                textChannel.sendMessage("✅velocityサーバーが起動しました").queue()
        );

        proxy.getEventManager().register(this, ListenerCloseEvent.class, PostOrder.FIRST, event ->
                textChannel.sendMessage("❌velocityサーバーが停止しました").queue()
        );

        proxy.getEventManager().register(this, ProxyShutdownEvent.class, PostOrder.FIRST, event ->
                jda.shutdownNow()
        );

        proxy.getEventManager().register(this, DisconnectEvent.class, PostOrder.FIRST, event -> {
            Player player = event.getPlayer();
            proxy.sendMessage(text()
                    .append(text("["+player.getUsername()+"]", AQUA))
                    .append(text("が退出しました", YELLOW))
            );
            textChannel.sendMessage(new EmbedBuilder()
                    .setTitle("["+player.getUsername()+"]が退出しました")
                    .setColor(Color.blue)
                    .setThumbnail("https://crafatar.com/avatars/"+player.getUniqueId()+"?overlay")
                    .build()).queue();
        });

        proxy.getEventManager().register(this, ServerConnectedEvent.class, PostOrder.FIRST, event -> {
            Player player = event.getPlayer();
            String targetServer = event.getServer().getServerInfo().getName();

            event.getPreviousServer().ifPresentOrElse(
                    server -> {
                        proxy.sendMessage(text()
                                .append(text("["+player.getUsername()+"]", AQUA))
                                .append(text("が", YELLOW))
                                .append(text("["+server.getServerInfo().getName()+"]", DARK_GREEN))
                                .append(text("から", YELLOW))
                                .append(text("["+targetServer+"]", DARK_GREEN))
                                .append(text("へ移動しました", YELLOW))
                        );
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("["+player.getUsername()+"]が["+server.getServerInfo().getName()+"]から["+targetServer+"]へ移動しました")
                                .setColor(Color.blue)
                                .setThumbnail("https://crafatar.com/avatars/"+player.getUniqueId()+"?overlay")
                                .build()).queue();
                    },
                    () -> {
                        proxy.sendMessage(text()
                                .append(text("["+player.getUsername()+"]", AQUA))
                                .append(text("が入室しました", YELLOW))
                        );
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("["+player.getUsername()+"]が入室しました")
                                .setColor(Color.blue)
                                .setThumbnail("https://crafatar.com/avatars/"+player.getUniqueId()+"?overlay")
                                .build()).queue();
                    }
            );

            proxy.getAllPlayers().forEach(player1 ->
                    player1.getTabList().getEntry(player.getUniqueId()).get().setDisplayName(text()
                            .append(text("["+targetServer+"]", DARK_GREEN))
                            .append(text(player.getUsername()))
                            .build()
                    )
            );
        });

        proxy.getEventManager().register(this, PlayerChatEvent.class, PostOrder.FIRST, event -> {
            String discord;
            String message = discord = event.getMessage();
            Player player = event.getPlayer();
            String server = player.getCurrentServer().get().getServerInfo().getName();
            TextComponent.Builder component = text()
                    .append(text("["+server+"]", DARK_GREEN))
                    .append(text("<"+player.getUsername()+"> "));
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
            String japanese;
            if (!(japanese=Japanizer.japanize(message)).isEmpty()) {
                component.append(text("(" + japanese + ")", GOLD));
                discord += "(" + japanese + ")";
            }
            proxy.sendMessage(component);
            JsonObject body = new JsonObject();
            body.addProperty("content", discord);
            body.addProperty("username", player.getUsername());
            body.addProperty("avatar_url", "https://crafatar.com/avatars/"+player.getUniqueId()+"?overlay");
            JsonObject allowedMentions = new JsonObject();
            allowedMentions.add("parse", new Gson().toJsonTree("[\"everyone\", \"users\", \"roles\"]").getAsJsonArray());
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

        event.setResult(denied());
        });

        proxy.getEventManager().register(this, PluginMessageEvent.class, event -> {
            if (!event.getIdentifier().equals(MinecraftChannelIdentifier.create("velocity", "Fabdicord"))) return;
            //(dis)connect type:server:player
            //death type:server:player:dim:x:y:z:message
            //advancement type:server:player:title:description
            //command type:server:player:command
            String[] data = new String(event.getData(), StandardCharsets.UTF_8).split(":");
            switch (data[0]) {
                case "CONNECT" -> {
                    if (proxy.getAllPlayers().stream().noneMatch(player -> player.getUsername().equals(data[2]))) {
                        TabListEntry tabListEntry = TabListEntry.builder()
                                .displayName(text()
                                        .append(text("[" + data[1] + "]", DARK_GREEN))
                                        .append(text("[bot]", DARK_BLUE))
                                        .append(text(data[2]))
                                        .build()
                                )
                                .build();
                        proxy.getAllPlayers().forEach(player ->
                                player.getTabList().addEntry(tabListEntry)
                        );
                        bots.put(data[2], new HashMap<String, UUID>(){
                            {
                                put(data[1], tabListEntry.getProfile().getId());
                            };
                        });
                        proxy.sendMessage(text()
                                .append(text("["+data[2]+"(bot)]", AQUA))
                                .append(text("が", YELLOW))
                                .append(text(data[1], DARK_GREEN))
                                .append(text("に入室しました", YELLOW))
                        );
                        textChannel.sendMessage(new EmbedBuilder()
                                .setTitle("["+data[2]+"(bot)]が"+data[1]+"入室しました")
                                .setColor(Color.blue)
                                .build()).queue();
                    }
                }

                case "DISCONNECT" -> {
                    proxy.getAllPlayers().forEach(player -> player.getTabList().removeEntry(bots.get(data[2]).values().stream().findFirst().get()));
                    proxy.sendMessage(text()
                            .append(text("["+data[2]+"(bot)]", AQUA))
                            .append(text("が退出しました", YELLOW))
                    );
                    bots.remove(data[2]);
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle("["+data[2]+"(bot)]が退出しました")
                            .setColor(Color.blue)
                            .build()).queue();
                }

                case "DEATH" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[2]+"]が"+data[1]+"の"+data[3]+"(x:"+data[4]+", y:"+data[5]+", z:"+data[6]+"で死亡しました")
                        .setDescription(data[7])
                        .setColor(Color.red)
                        .build()).queue();

                case "ADVANCEMENT" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[2]+"]は"+data[1]+"で["+data[3]+"]を達成しました")
                        .setDescription(data[4])
                        .setColor(Color.green)
                        .build()).queue();

                case "COMMAND" -> textChannel.sendMessage(new EmbedBuilder()
                        .setTitle("["+data[2]+"]が"+data[1]+"で["+data[3]+"]を実行しました")
                        .setColor(Color.yellow)
                        .build()).queue();
            }
        });

        jda.upsertCommand("player", "現在サーバーに入ってるプレイヤー一覧").queue();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            String japanese;
            proxy.sendMessage(text()
                    .append(text("[discord]", DARK_GREEN))
                    .append(text("<"+event.getAuthor().getName()+"> "))
                    .append(text(event.getMessage().getContentDisplay()))
                    .append(text(!(japanese=Japanizer.japanize(event.getMessage().getContentDisplay())).isEmpty()?"("+japanese+")":"", GOLD))
            );
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String command = event.getName();
        if ("player".equals(command)){
            StringBuilder players = new StringBuilder();
            proxy.getAllPlayers().forEach(player -> players.append("[").append(player.getCurrentServer()).append("]").append(player.getUsername()));
            bots.keySet().forEach(player -> bots.get(player).keySet().forEach(server -> players.append("[").append(server).append("][bot]").append(player)));
            textChannel.sendMessage(new EmbedBuilder()
                    .setTitle("現在参加しているプレーヤー一覧")
                    .setDescription(players.toString())
                    .setColor(Color.blue)
                    .build()
            ).queue();
        }
    }
}
