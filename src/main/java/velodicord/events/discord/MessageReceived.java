package velodicord.events.discord;

import com.github.ucchyocean.lc3.japanize.Japanizer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import velodicord.Config;
import velodicord.Velodicord;
import velodicord.pmConnection.DiscordPluginMessageManager;
import velodicord.pmConnection.PluginMessageManager;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static velodicord.Discordbot.*;

public class MessageReceived extends ListenerAdapter {
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!(event.getAuthor().isBot() && !Config.getDetectbot().contains(event.getAuthor().getId())) && (event.getChannel().getId().equals(getMainChannel().getId()) || event.getChannel().getId().equals(getVoicechannel()))) {
            String message = event.getMessage().getContentDisplay();
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
            Velodicord.getVelodicord().getProxy().sendMessage(text()
                    .append(text("[discord]", DARK_GREEN))
                    .append(text("<%s> ".formatted(event.getAuthor().getName())))
                    .append(MiniMessage.miniMessage().deserialize(mmessage))
                    .append(text(!(mmessage = Japanizer.japanize(mmessage)).isEmpty() && !message.contains("https://") && !message.contains("http://") && !message.contains("```") ? "(%s)".formatted(mmessage) : "", GOLD))
                    .append(text(temp, BLUE))
            );

            String japanized = Japanizer.japanize(cutmessage);
            sendvoicemessage(japanized.isEmpty() ? cutmessage : japanized, Config.getDisspeaker().getOrDefault(event.getAuthor().getId(), getDefaultSpeakerID()));
        } else if (Velodicord.getPMManager() instanceof DiscordPluginMessageManager manager && event.getChannel().getId().equals(manager.getPMChannel().getId())) {
            PluginMessageManager.receive(event.getMessage().getContentDisplay());
        }
    }
}
