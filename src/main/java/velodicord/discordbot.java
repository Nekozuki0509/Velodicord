package velodicord;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dejvokep.boostedyaml.route.Route;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import velodicord.events.discord;
import velodicord.lavaplayer.PlayerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


import static velodicord.Velodicord.velodicord;

public class discordbot {
    public static JDA jda;
    public static TextChannel textChannel;
    public static Webhook webhook;
    public static String voicechannel;

    static void init() throws InterruptedException {
        jda = JDABuilder.createDefault(velodicord.config.getString(Route.from("BotToken")))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new discord())
                .build();

        jda.awaitReady();

        jda.upsertCommand("player", "現在参加しているプレイヤー").queue();
        jda.upsertCommand("join", "ボイスチャンネルへの参加").queue();
        jda.upsertCommand("leave", "ボイスチャンネルからの退出").queue();
        jda.upsertCommand("showdic", "辞書に登録されている単語一覧").queue();
        jda.upsertCommand("adddic", "辞書に新たな単語を登録・登録されている単語の読み方を変更")
                .addOption(OptionType.STRING, "word", "登録したい単語", true)
                .addOption(OptionType.STRING, "read", "登録したい単語の読み方", true)
                .queue();
        jda.upsertCommand("removedic", "辞書に登録されている単語の削除")
                .addOption(OptionType.STRING, "word", "削除したい単語", true)
                .queue();

        textChannel = jda.getTextChannelById(velodicord.config.getString(Route.from("ChannelId")));
        if (textChannel == null) {
            throw new NullPointerException("チャンネルIDが不正です");
        }

        String webhookname = "Velodicord";
        Objects.requireNonNull(textChannel).getGuild().retrieveWebhooks().complete().forEach(webhook -> {
            if (webhookname.equals(webhook.getName())) discordbot.webhook = webhook;
        });

        if (webhook == null) {
            webhook = textChannel.createWebhook(webhookname).complete();
        }
    }

    public static void sendvoicemessage(String message) {
        if (voicechannel == null) return;

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.tts.quest/v3/voicevox/synthesis?text="+
                    URLEncoder.encode(message, StandardCharsets.UTF_8)+"&speaker=3").openConnection();

            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonNode first = velodicord.mapper.readTree(response.toString());

            StringBuffer response2;
            do {
                HttpURLConnection status = (HttpURLConnection) new URL(first.get("audioStatusUrl").asText()).openConnection();

                status.setRequestMethod("GET");

                String inputLine2;
                BufferedReader in2 = new BufferedReader(new InputStreamReader(status.getInputStream()));
                response2 = new StringBuffer();
                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                in2.close();
            } while (!velodicord.mapper.readTree(response2.toString()).get("isAudioReady").asBoolean());

            PlayerManager.getInstance().loadAndPlay(textChannel, first.get("mp3DownloadUrl").asText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
