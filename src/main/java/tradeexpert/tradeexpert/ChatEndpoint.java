package tradeexpert.tradeexpert;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value="/chat/{username}")
public class ChatEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);


    private static final Set<ChatEndpoint> chatEndpoints
            = new CopyOnWriteArraySet<>();
    private static final HashMap<String, String> users = new HashMap<>();
    private final javax.websocket.Session session;

    public ChatEndpoint(javax.websocket.Session session) {
        this.session = session;
    }

    @OnOpen
    public void onOpen(
            @NotNull javax.websocket.Session session,
            @PathParam("username") String username) {


        chatEndpoints.add(this);
        users.put(session.getId(), username);

        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");
        broadcast(message);
    }

    @OnMessage
    public void onMessage(@NotNull javax.websocket.Session session,@NotNull Message message)
            throws IOException, EncodeException {

        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void onClose(@NotNull javax.websocket.Session session) throws IOException, EncodeException {

        chatEndpoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(javax.websocket.Session session, @NotNull Throwable throwable) {
        logger.error(session+"  "+throwable.getMessage());
        throwable.printStackTrace();
        // Do error handling here
    }

    private static void broadcast(Message message) {

        chatEndpoints.forEach(endpoint -> {
            synchronized (
                    endpoint.session
            ) {
                try {
                    endpoint.session.getBasicRemote().
                            sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}