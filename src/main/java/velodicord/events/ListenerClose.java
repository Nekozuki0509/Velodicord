package velodicord.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import velodicord.discordbot;

import java.io.File;
import java.io.IOException;

import static velodicord.Velodicord.velodicord;

public class ListenerClose {
    @Subscribe(order = PostOrder.FIRST)
    public void onListenerClose(ListenerCloseEvent event) {
        try {
            velodicord.mapper.writerWithDefaultPrettyPrinter().writeValue(new File(String.valueOf(velodicord.dicjson)), velodicord.dic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        discordbot.textChannel.sendMessage("❌velocityサーバーが停止しました").queue();
    }
}
