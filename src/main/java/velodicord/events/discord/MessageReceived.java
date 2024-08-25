package velodicord.events.discord;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.minimessage.MiniMessage;
import velodicord.Config;
import velodicord.discordbot;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static velodicord.Config.*;
import static velodicord.Velodicord.velodicord;
import static velodicord.discordbot.*;

public class MessageReceived extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!(event.getAuthor().isBot() && !Config.detectbot.contains(event.getAuthor().getId())) && event.getChannel().getId().equals(MainChannel.getId())) {
            String message = event.getMessage().getContentDisplay();
            String japanese;
            if (!(japanese = (!(japanese = Japanizer.japanize(message)).isEmpty() ? "(" + japanese + ")" : "")).isEmpty()
                    && !message.contains("https://") && !message.contains("http://") && !message.contains("```"))
                event.getMessage().reply(message + japanese).queue();
            String cutmessage = message;
            for (String word : dic.keySet()) {
                cutmessage = cutmessage.replaceAll(word, dic.get(word));
            }
            cutmessage = cutmessage.replaceAll("~~(.*?)~~", "$1")
                    .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                    .replaceAll("__(.*?)__", "$1")
                    .replaceAll("_(.*?)_", "$1")
                    .replaceAll("```(.*?)```", "コード省略")
                    .replaceAll("\\|\\|(.*?)\\|\\|", "ネタバレ")
                    .replace("@", "アット");
            String mmessage = message.replaceAll("~~(.*?)~~", "<st>$1</st>")
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

            if (Pattern.compile("\\[.*?]\\(https?://.*?\\)").matcher(mmessage).find()) {
                mmessage = mmessage.replaceAll("\\[(.*?)]\\((https?://.*?)\\)", "<blue><u><click:open_url:'$2'>$1");
            } else if (Pattern.compile("https?://\\S+").matcher(mmessage).find()) {
                mmessage = mmessage.replaceAll("(https?://\\S+)", "<blue><u><click:open_url:'$1'>$1");
            }

            String temp = "";
            if (!event.getMessage().getAttachments().isEmpty()) {
                if (cutmessage.isEmpty()) {
                    cutmessage = "添付ファイル";
                    temp = "<添付ファイル>";
                } else {
                    cutmessage += "ぷらす添付ファイル";
                    temp += "<+添付ファイル>";
                }
            }
            velodicord.proxy.sendMessage(text()
                    .append(text("[discord]", DARK_GREEN))
                    .append(text("<" + event.getAuthor().getName() + "> "))
                    .append(MiniMessage.miniMessage().deserialize(mmessage))
                    .append(text(!(mmessage = Japanizer.japanize(mmessage)).isEmpty() && !message.contains("https://") && !message.contains("http://") && !message.contains("```") ? "(" + mmessage + ")" : "", GOLD))
                    .append(text(temp, BLUE))
            );

            String cutjapanese = !(cutjapanese = Japanizer.japanize(cutmessage)).isEmpty() ? "(" + cutjapanese + ")" : "";
            sendvoicemessage(cutmessage + cutjapanese, Config.disspeaker.getOrDefault(event.getAuthor().getId(), DefaultSpeakerID));
        } else if (event.getChannel().getId().equals(PMChannel.getId())) {
            //to:what:data
            String[] data = event.getMessage().getContentDisplay().split("&");
            if ("VELOCITY".equals(data[0])) {
                switch (data[1]) {
                    case "OK" -> {
                        PMChannel.sendMessage(data[2] + "&OK&" + NoticeChannel.getId() + "&" + LogForumChannel.map(ForumChannel::getId).orElse("") + "&" + CommandChannel + "&" + CommandRole.getId())
                                .setFiles(FileUpload.fromData(ignorecommandjson.toFile()), FileUpload.fromData(disadmincommandjson.toFile()), FileUpload.fromData(mineadmincommandjson.toFile())).queue();
                        velodicord.proxy.sendMessage(text()
                                .append(text("✅ "))
                                .append(text("[" + data[2] + "]", DARK_GREEN))
                                .append(text(" が起動しました", YELLOW))
                        );

                        discordbot.sendvoicemessage(data[2] + "が起動しました", DefaultSpeakerID);
                    }

                    case "FIN" -> {
                        velodicord.proxy.sendMessage(text()
                                .append(text("\uD83D\uDED1 "))
                                .append(text("[" + data[2] + "]", DARK_GREEN))
                                .append(text(" が停止しました", YELLOW))
                        );

                        discordbot.sendvoicemessage(data[2] + "が停止しました", DefaultSpeakerID);
                    }

                    case "SEND" -> velodicord.proxy.sendMessage(MiniMessage.miniMessage().deserialize(data[2]));

                    case "READ" -> {
                        velodicord.proxy.sendMessage(MiniMessage.miniMessage().deserialize(data[2]));
                        String message = data[3];
                        for (String word : Config.dic.keySet()) {
                            message = message.replaceAll(word, Config.dic.get(word));
                        }
                        discordbot.sendvoicemessage(message, DefaultSpeakerID);
                    }
                }
            }
        }
    }
}
