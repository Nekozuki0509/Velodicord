package velodicord.events.minecraft;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class PlayerChat {
    private final OkHttpClient httpClient = new OkHttpClient();

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        String discord;
        String message = discord = event.getMessage();
        String japanese = Japanizer.japanize(message);
        Player player = event.getPlayer();
        String server = player.getCurrentServer().orElseThrow().getServerInfo().getName();
        TextComponent.Builder component = text()
                .append(text("[%s]".formatted(server), DARK_GREEN))
                .append(text("<%s> ".formatted(player.getUsername())));
        String cutmessage = message;
        for (String word : Config.getDic().keySet()) {
            cutmessage = cutmessage.replaceAll(word, Config.getDic().get(word));
        }
        cutmessage = cutmessage.replaceAll("~~(.*?)~~", "$1")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("__(.*?)__", "$1")
                .replaceAll("_(.*?)_", "$1")
                .replaceAll("```(.*?)```", "コード省略")
                .replaceAll("\\|\\|(.*?)\\|\\|", "ネタバレ")
                .replace("@", "アット");
        message = message.replaceAll("~~(.*?)~~", "<st>$1</st>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replaceAll("__(.*?)__", "<u>$1</u>")
                .replaceAll("_(.*?)_", "<i>$1</i>")
                .replaceAll("```(.*?)```", "$1")
                .replaceAll("\\|\\|(.*?)\\|\\|", "<ネタバレ>");

        if (Pattern.compile("\\[.*?]\\(https?://.*?\\)").matcher(cutmessage).find()) {
            cutmessage = cutmessage.replaceAll("\\[(.*?)]\\(https?://.*?\\)", "$1かっこゆーあーるえる");
        } else if (Pattern.compile("https?://\\S+").matcher(cutmessage).find()) {
            cutmessage = cutmessage.replaceAll("https?://\\S+", "ゆーあーるえる省略");
        }

        if (Pattern.compile("\\[.*?]\\(https?://.*?\\)").matcher(message).find()) {
            message = message.replaceAll("\\[(.*?)]\\((https?://.*?)\\)", "<blue><u><click:open_url:'$2'>$1");
        } else if (Pattern.compile("https?://\\S+").matcher(message).find()) {
            message = message.replaceAll("(https?://\\S+)", "<blue><u><click:open_url:'$1'>$1");
        }

        if (message.contains("@")) {
            for (Member member : Discordbot.getMainChannel().getMembers()) {
                String usernameMention = "@%s".formatted(member.getUser().getName());
                String displayNameMention = "@%s".formatted(member.getEffectiveName());

                message = message.replace(usernameMention, "<blue>%s</blue>".formatted(usernameMention));
                message = message.replace(displayNameMention, "<blue>%s</blue>".formatted(displayNameMention));

                discord = StringUtils.replaceIgnoreCase(discord, displayNameMention, member.getAsMention());
                discord = StringUtils.replaceIgnoreCase(discord, usernameMention, member.getAsMention());


                if (member.getNickname() != null) {
                    String nicknameMention = "@%s".formatted(member.getNickname());
                    discord = StringUtils.replaceIgnoreCase(discord, nicknameMention, member.getAsMention());
                    message = message.replace(nicknameMention, "<blue>%s</blue>".formatted(nicknameMention));
                }
            }
            for (Role role : Discordbot.getMainChannel().getGuild().getRoles()) {
                String roleMention = "@%s".formatted(role.getName());
                discord = StringUtils.replaceIgnoreCase(discord, roleMention, role.getAsMention());
                message = message.replace(roleMention, "<blue>%s</blue>".formatted(roleMention));
            }
            message = message.replace("@everyone", "<blue>@everyone</blue>");
            message = message.replace("@here", "<blue>@here</blue>");
        }
        component.append(MiniMessage.miniMessage().deserialize(message));
        discord = "[%s] %s".formatted(server, discord);
        if (!japanese.isEmpty() && !event.getMessage().contains("https://") && !event.getMessage().contains("http://") && !event.getMessage().contains("```")) {
            component.append(text("(%s)".formatted(japanese), GOLD));
            discord += "(%s)".formatted(japanese);
        }
        Velodicord.getVelodicord().getProxy().sendMessage(component);
        JsonObject body = new JsonObject();
        body.addProperty("content", discord);
        body.addProperty("username", player.getUsername());
        body.addProperty("avatar_url", "https://mc-heads.net/avatar/%s.png".formatted(player.getUsername()));
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new Gson().toJsonTree(Discordbot.getMentionable()).getAsJsonArray());
        body.add("allowed_mentions", allowedMentions);
        Request request = new Request.Builder()
                .url(Discordbot.getWebhook().getUrl())
                .post(RequestBody.create(MediaType.get("application/json"), body.toString()))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(() -> {
            try {
                Response response = httpClient.newCall(request).execute();
                response.close();
            } catch (Exception e) {
                Velodicord.getVelodicord().getLogger().error(ExceptionUtils.getStackTrace(e));
            }
        });
        executor.shutdown();

        String japanized = Japanizer.japanize(cutmessage);
        Discordbot.sendvoicemessage(japanized.isEmpty() ? cutmessage : japanized, Config.getMinespeaker().getOrDefault(event.getPlayer().getUniqueId().toString(), Discordbot.getDefaultSpeakerID()));
    }
}
