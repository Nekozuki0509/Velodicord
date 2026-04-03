package velodicord.events.minecraft;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Config;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static velodicord.Config.*;


public class ListenerClose {
    @Subscribe(order = PostOrder.FIRST)
    public void onListenerClose(ListenerCloseEvent event) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.getConfigjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getConfig(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのconfigを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.getDicjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getDic(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのdicを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.getDetectbotjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getDetectbot(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのdetectbotを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(getIgnorecommandjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getIgnorecommand(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのignorecommandを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(getDisadmincommandjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getDisadmincommand(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのdisadmincommandを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(getMineadmincommandjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getMineadmincommand(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのmineadmincommandを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.getDisspeakerjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getDisspeaker(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのdisspeakerを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(Config.getMinespeakerjson())), StandardCharsets.UTF_8))) {
            getGson().toJson(getMinespeaker(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのminespeakerを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(String.valueOf(getMentionablejson())), StandardCharsets.UTF_8))) {
            getGson().toJson(Discordbot.getMentionable(), writer);
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("Velodicordのmentionableを保存できませんでした: {}", ExceptionUtils.getStackTrace(e));
        }
        Discordbot.getNoticeChannel().sendMessage("\uD83D\uDED1velocityサーバーが停止しました").complete();
        Discordbot.getJda().shutdown();
    }
}
