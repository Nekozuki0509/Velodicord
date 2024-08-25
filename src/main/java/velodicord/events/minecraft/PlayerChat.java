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
import velodicord.discordbot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static velodicord.Velodicord.velodicord;

public class PlayerChat {
    public final OkHttpClient httpClient = new OkHttpClient();

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        String discord;
        String message = discord = event.getMessage();
        String japanese = Japanizer.japanize(message);
        Player player = event.getPlayer();
        String server = player.getCurrentServer().orElseThrow().getServerInfo().getName();
        TextComponent.Builder component = text()
                .append(text("[" + server + "]", DARK_GREEN))
                .append(text("<" + player.getUsername() + "> "));
        String cutmessage = message;
        for (String word : Config.dic.keySet()) {
            cutmessage = cutmessage.replaceAll(word, Config.dic.get(word));
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

        String cutjapanese = !(cutjapanese = Japanizer.japanize(cutmessage)).isEmpty() ? "(" + cutjapanese + ")" : "";
        String voice = cutmessage + cutjapanese;
        if (message.contains("@")) {
            for (Member member : discordbot.MainChannel.getMembers()) {
                String usernameMention = "@" + member.getUser().getName();
                String displayNameMention = "@" + member.getEffectiveName();

                message = message.replace(usernameMention, "<blue>" + usernameMention + "</blue>");
                message = message.replace(displayNameMention, "<blue>" + displayNameMention + "</blue>");

                discord = StringUtils.replaceIgnoreCase(discord, displayNameMention, member.getAsMention());
                discord = StringUtils.replaceIgnoreCase(discord, usernameMention, member.getAsMention());


                if (member.getNickname() != null) {
                    String nicknameMention = "@" + member.getNickname();
                    discord = StringUtils.replaceIgnoreCase(discord, nicknameMention, member.getAsMention());
                    message = message.replace(nicknameMention, "<blue>" + nicknameMention + "</blue>");
                }
            }
            for (Role role : discordbot.MainChannel.getGuild().getRoles()) {
                String roleMention = "@" + role.getName();
                discord = StringUtils.replaceIgnoreCase(discord, roleMention, role.getAsMention());
                message = message.replace(roleMention, "<blue>" + roleMention + "</blue>");
            }
            message = message.replace("@everyone", "<blue>@everyone</blue>");
            message = message.replace("@here", "<blue>@here</blue>");
        }
        component.append(MiniMessage.miniMessage().deserialize(message));
        discord = "[" + server + "] " + discord;
        if (!japanese.isEmpty() && !event.getMessage().contains("https://") && !event.getMessage().contains("http://") && !event.getMessage().contains("```")) {
            component.append(text("(" + japanese + ")", GOLD));
            discord += "(" + japanese + ")";
        }
        velodicord.proxy.sendMessage(component);
        JsonObject body = new JsonObject();
        body.addProperty("content", discord);
        body.addProperty("username", player.getUsername());
        body.addProperty("avatar_url", "https://mc-heads.net/avatar/" + player.getUsername() + ".png");
        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new Gson().toJsonTree(discordbot.mentionable).getAsJsonArray());
        body.add("allowed_mentions", allowedMentions);
        Request request = new Request.Builder()
                .url(discordbot.webhook.getUrl())
                .post(RequestBody.create(MediaType.get("application/json"), body.toString()))
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(() -> {
            try {
                Response response = httpClient.newCall(request).execute();
                response.close();
            } catch (Exception e) {
                velodicord.logger.error(ExceptionUtils.getStackTrace(e));
            }
        });
        executor.shutdown();
        discordbot.sendvoicemessage(voice, Config.minespeaker.getOrDefault(event.getPlayer().getUniqueId().toString(), discordbot.DefaultSpeakerID));
    }
}
