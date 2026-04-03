package velodicord.pmConnection;

import velodicord.Velodicord;

import java.util.HashMap;
import java.util.Map;

public class WebSocketPluginMessageManager extends PluginMessageManager {

    private final Map<String, WebSocketClient> clients = new HashMap<>();

    public WebSocketPluginMessageManager() {
        super();

        Velodicord.getVelodicord().getProxy().getAllServers().forEach(server -> clients.put(server.getServerInfo().getName(), new WebSocketClient(server.getServerInfo().getAddress(), server.getServerInfo().getName())));
    }

    public void closeAll() {
        clients.forEach((name, client) -> client.close());
    }

    @Override
    public void sendMessage(String to, String msg) {
        if (to.equals("ALL")) {
            clients.values().forEach(client -> client.sendMessage(msg));
        } else {
            clients.get(to).sendMessage(msg);
        }
    }
}
