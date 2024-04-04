package velodicord.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import velodicord.VoiceVox;
import velodicord.config;
import velodicord.discordbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static velodicord.config.dataDirectory;
import static velodicord.discordbot.*;

public class ListenerClose {
    @Subscribe(order = PostOrder.FIRST)
    public void onServerConnected(ListenerCloseEvent event) {
        try {
            config.mapper.writerWithDefaultPrettyPrinter().writeValue(new File(String.valueOf(config.dicjson)), config.dic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        discordbot.LogChannel.sendMessage("❌velocityサーバーが停止しました").queue();
        config.p.setProperty("MainChannelId", MainChannel.getId());
        config.p.setProperty("LogChannelID", LogChannel.getId());
        config.p.setProperty("PosChannelID", PosChannel.getId());
        try {
            config.p.store(new FileWriter(String.valueOf(dataDirectory.resolve("config.properties"))), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VoiceVox.fin();
    }
}
