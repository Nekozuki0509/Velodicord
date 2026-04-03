package velodicord.pmConnection;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import velodicord.Discordbot;
import velodicord.Velodicord;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import static velodicord.Config.*;
import static velodicord.Discordbot.*;

public class WebSocketClient {

    private volatile WebSocket ws;

    private final String uri;

    private final WebSocketFactory factory;

    private boolean shouldReconnect = true;

    private volatile boolean connecting = false;

    private String serverName;

    public WebSocketClient(InetSocketAddress address, String serverName) {
        this.uri = "ws://%s:%d/ws/pm".formatted(address.getAddress().getHostAddress(), address.getPort() + Velodicord.getWebSocketPortIncrement());
        this.factory = new WebSocketFactory();
        this.serverName = serverName;

        Velodicord.getVelodicord().getLogger().info("WebSocketClient initialized with URI: {}", uri);
        startConnectLoop();
    }

    private void startConnectLoop() {
        new Thread(() -> {
            connecting = true;
            while (shouldReconnect) {
                try {
                    ws = factory.createSocket(uri)
                            .addListener(new WebSocketAdapter() {

                                @Override
                                public void onConnected(WebSocket ws, java.util.Map<String, java.util.List<String>> headers) {
                                    Velodicord.getVelodicord().getLogger().info("WebSocket connection established.");

                                    try {
                                        ws.sendText("OK&%s&%s&%s&%s&%s&%s&%s".formatted(getNoticeChannel().getId(), getLogForumChannel().map(ForumChannel::getId).orElse(""), getCommandChannel(), Discordbot.getCommandRole().getId(), Files.readString(getIgnorecommandjson()), Files.readString(getDisadmincommandjson()), Files.readString(getMineadmincommandjson())));
                                    } catch (IOException e) {
                                        Velodicord.getVelodicord().getLogger().error("Failed to read command config files: {}", ExceptionUtils.getStackTrace(e));
                                    }

                                    Discordbot.sendvoicemessage("%sが起動しました".formatted(serverName), getDefaultSpeakerID());
                                }

                                @Override
                                public void onTextMessage(WebSocket ws, String message) {
                                    PluginMessageManager.receive("VELOCITY&%s".formatted(message));
                                }

                                @Override
                                public void onDisconnected(WebSocket ws,
                                                           com.neovisionaries.ws.client.WebSocketFrame serverCloseFrame,
                                                           com.neovisionaries.ws.client.WebSocketFrame clientCloseFrame,
                                                           boolean closedByServer) {
                                    if (shouldReconnect) {
                                        Velodicord.getVelodicord().getLogger().info("WebSocket connection closed. try to reconnect...");
                                        reconnect(ws);
                                    } else {
                                        Velodicord.getVelodicord().getLogger().info("WebSocket connection closed.");
                                    }
                                }

                                @Override
                                public void onError(WebSocket ws, WebSocketException e) {
                                    Velodicord.getVelodicord().getLogger().error("WebSocket error occurred.: {}", ExceptionUtils.getStackTrace(e));
                                }
                            })
                            .connect();

                    connecting = false;
                    return;
                } catch (Exception e) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Velodicord.getVelodicord().getLogger().error("WebSocket connection interrupted while trying to connect.: {}", ExceptionUtils.getStackTrace(e));
                    }
                }
            }

            connecting = false;
        }, "websocket-connect").start();
    }

    public void reconnect(WebSocket oldws) {
        if (connecting) {
            return;
        }

        new Thread(() -> {
            connecting = true;
            while (shouldReconnect) {
                try {
                    ws = oldws.recreate().connect();
                    connecting = false;

                    try {
                        sendMessage("OK&%s&%s&%s&%s&%s&%s&%s".formatted(getNoticeChannel().getId(), getLogForumChannel().map(ForumChannel::getId).orElse(""), getCommandChannel(), Discordbot.getCommandRole().getId(), Files.readString(getIgnorecommandjson()), Files.readString(getDisadmincommandjson()), Files.readString(getMineadmincommandjson())));
                    } catch (IOException e) {
                        Velodicord.getVelodicord().getLogger().error("Failed to read command config files: {}", ExceptionUtils.getStackTrace(e));
                    }
                    return;
                } catch (Exception e) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Velodicord.getVelodicord().getLogger().error("WebSocket connection interrupted while trying to reconnect.: {}", ExceptionUtils.getStackTrace(e));
                    }
                }
            }
            connecting = false;
        }, "websocket-reconnect").start();
    }

    public void close() {
        shouldReconnect = false;
        if (ws != null) ws.disconnect();
    }

    public void sendMessage(String message) {
        try {
            if (ws != null && ws.isOpen()) {
                ws.sendText(message);
            }
        } catch (Exception e) {
            Velodicord.getVelodicord().getLogger().error("Failed to send WebSocket message.: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
