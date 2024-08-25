package velodicord.events.discord;

import com.velocitypowered.proxy.command.VelocityCommandManager;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import velodicord.Config;
import velodicord.DiscordCommandSource;
import velodicord.VOICEVOX;
import velodicord.Velodicord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static velodicord.discordbot.CommandChannel;

public class CommandAutoCompleteInteraction extends ListenerAdapter {

    List<String> chs = new ArrayList<>(Arrays.asList("log", "main", "pm", "notice", "pos", "command"));

    List<String> speakers = new ArrayList<>(Arrays.asList("your", "default"));

    List<String> mention = new ArrayList<>(Arrays.asList("users", "roles", "everyone"));

    List<String> dismine = new ArrayList<>(Arrays.asList("discord", "マイクラ"));

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!CommandChannel.equals(event.getChannelId())) return;
        switch (event.getName()) {
            case "dic" -> {
                if ("del".equals(event.getSubcommandName())) {
                    List<Command.Choice> options = Config.dic.keySet().stream()
                            .filter(word -> word.contains(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word)).toList();

                    if (options.size() > 25) {
                        options = options.subList(0, 23);
                        options.add(new Command.Choice("...", "..."));
                    }

                    event.replyChoices(options).queue();
                }
            }

            case "ch" -> {
                if ("set".equals(event.getSubcommandName()) && "name".equals(event.getFocusedOption().getName())) {
                    event.replyChoices(chs.stream()
                            .filter(ch -> ch.contains(event.getFocusedOption().getValue()))
                            .map(ch -> new Command.Choice(ch, ch)).toList()).queue();
                }
            }

            case "speaker" -> {
                switch (event.getFocusedOption().getName()) {
                    case "which" -> event.replyChoices(speakers.stream()
                            .filter(speaker -> speaker.contains(event.getFocusedOption().getValue()))
                            .map(speaker -> new Command.Choice(speaker, speaker)).toList()).queue();

                    case "id" -> {
                        List<Command.Choice> options = VOICEVOX.voicevox.keySet().stream()
                                .filter(id -> id.toString().contains(event.getFocusedOption().getValue())
                                        || VOICEVOX.voicevox.get(id).contains(event.getFocusedOption().getValue()))
                                .map(id -> new Command.Choice(VOICEVOX.voicevox.get(id), id.toString())).toList();

                        if (options.size() > 25) {
                            options = options.subList(0, 23);
                            options.add(new Command.Choice("...", "..."));
                        }

                        event.replyChoices(options).queue();
                    }
                }
            }

            case "ignorecommand" -> {
                if ("del".equals(event.getSubcommandName())) {
                    List<Command.Choice> options = Config.ignorecommand.stream()
                            .filter(command -> command.contains(event.getFocusedOption().getValue()))
                            .map(command -> new Command.Choice(command, command)).toList();

                    if (options.size() > 25) {
                        options = options.subList(0, 23);
                        options.add(new Command.Choice("...", "..."));
                    }

                    event.replyChoices(options).queue();
                }
            }

            case "mentionable" -> {
                if ("set".equals(event.getSubcommandName())) {
                    event.replyChoices(mention.stream()
                            .filter(m -> m.contains(event.getFocusedOption().getValue()))
                            .map(m -> new Command.Choice(m, m)).toList()).queue();
                }
            }

            case "server" -> {
                if (!"command".equals(event.getSubcommandName())) return;
                switch (event.getFocusedOption().getName()) {
                    case "name" -> {
                        List<String> servers = new ArrayList<>(Velodicord.velodicord.proxy.getAllServers().stream().map(server -> server.getServerInfo().getName()).toList());
                        servers.add("velocity");
                        event.replyChoices(servers.stream()
                                .filter(server -> server.contains(event.getFocusedOption().getValue()))
                                .map(server -> new Command.Choice(server, server)).toList()).queue();
                    }

                    case "command" -> {
                        if (!"velocity".equals(event.getOptions().get(0).getAsString())) return;

                        List<Command.Choice> options = ((VelocityCommandManager) Velodicord.velodicord.proxy.getCommandManager())
                                .offerSuggestions(new DiscordCommandSource(null), event.getFocusedOption().getValue()).join()
                                .stream().map(s -> new Command.Choice(s, s)).toList();

                        if (options.size() > 25) {
                            options = options.subList(0, 23);
                            options.add(new Command.Choice("...", "..."));
                        }

                        event.replyChoices(options).queue();
                    }
                }
            }

            case "admincommand" -> {
                switch (event.getFocusedOption().getName()) {
                    case "which" -> event.replyChoices(dismine.stream()
                            .filter(disOrMine -> disOrMine.contains(event.getFocusedOption().getValue()))
                            .map(disOrMine -> new Command.Choice(disOrMine, disOrMine)).toList()).queue();

                    case "command" -> {
                        if (!"del".equals(event.getSubcommandName())) return;

                        event.replyChoices(("discord".equals(event.getOptions().get(0).getAsString()) ? Config.disadmincommand : Config.mineadmincommand).stream()
                                .filter(command -> command.contains(event.getFocusedOption().getValue()))
                                .map(command -> new Command.Choice(command, command)).toList()).queue();
                    }
                }
            }
        }
    }
}
