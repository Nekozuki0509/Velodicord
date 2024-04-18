package velodicord.events;

import V4S4J.V4S4J.V4S4J;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import velodicord.Config;
import velodicord.discordbot;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static velodicord.Config.*;


public class ListenerClose {
    @Subscribe(order = PostOrder.FIRST)
    public void onListenerClose(ListenerCloseEvent event) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.dicjson)), StandardCharsets.UTF_8))) {
            gson.toJson(dic, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.detectbotjson)), StandardCharsets.UTF_8))) {
            gson.toJson(detectbot, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(ignorecommandjson)), StandardCharsets.UTF_8))) {
            gson.toJson(ignorecommand, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.disspeakerjson)), StandardCharsets.UTF_8))) {
            gson.toJson(disspeaker, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.minespeakerjson)), StandardCharsets.UTF_8))) {
            gson.toJson(minespeaker, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.configjson)), StandardCharsets.UTF_8))) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        discordbot.LogChannel.sendMessage("❌velocityサーバーが停止しました").queue();
        V4S4J.fin();
    }
}
