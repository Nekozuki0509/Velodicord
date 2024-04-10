package velodicord.events;

import V4S4J.V4S4J.V4S4J;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import velodicord.config;
import velodicord.discordbot;

import java.io.File;
import java.io.IOException;


public class ListenerClose {
    @Subscribe(order = PostOrder.FIRST)
    public void onListenerClose(ListenerCloseEvent event) {
        try {
            config.mapper.writerWithDefaultPrettyPrinter().writeValue(new File(String.valueOf(config.dicjson)), config.dic);
            config.mapper.writerWithDefaultPrettyPrinter().writeValue(new File(String.valueOf(config.configjson)), config.config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        discordbot.LogChannel.sendMessage("❌velocityサーバーが停止しました").queue();
        V4S4J.fin();
    }
}
