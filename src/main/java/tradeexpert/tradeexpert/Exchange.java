package tradeexpert.tradeexpert;


import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class Exchange {

    private static final Logger logger = LoggerFactory.getLogger(Exchange.class);
    private final String urlize;

    public TelegramClient telegram;
    private SocketFactory socket;
    private boolean isOpen;

    public Exchange(String ur, String token) throws TelegramApiException, IOException, ParseException, InterruptedException {
        this.urlize = ur;

        this.telegram = new TelegramClient(token);
        this.telegram.connect();

     logger.info("Connected to " + urlize);

    }

    public abstract CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair);

    public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);

    public abstract CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
            TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

    public ExchangeWebSocketClient getWebsocketClient() {

        return new ExchangeWebSocketClient(
                URI.create(urlize),
                new Draft_6455()
        ) {
            @Override
            public long getDefaultAsyncSendTimeout() {
                return 0;
            }

            @Override
            public void setAsyncSendTimeout(long l) {


            }

            @Override
            public Session connectToServer(Object o, URI uri) {

                return new Session() {
                    private long maxIdleTimeout;

                    @Override
                    public WebSocketContainer getContainer() {
                        return null;
                    }

                    @Override
                    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {

                        logger.info("Adding message handler ");


                    }

                    @Override
                    public Set<MessageHandler> getMessageHandlers() {
                        return null;
                    }

                    @Override
                    public void removeMessageHandler(MessageHandler listener) {

                    }

                    @Override
                    public String getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public String getNegotiatedSubprotocol() {
                        return null;
                    }

                    @Override
                    public List<Extension> getNegotiatedExtensions() {
                        return null;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public boolean isOpen() {
                        return false;
                    }

                    @Override
                    public long getMaxIdleTimeout() {
                        return 0;
                    }

                    @Override
                    public void setMaxIdleTimeout(long timeout) {
                        this.maxIdleTimeout = timeout;

                    }

                    @Override
                    public void setMaxBinaryMessageBufferSize(int max) {

                    }

                    @Override
                    public int getMaxBinaryMessageBufferSize() {
                        return 0;
                    }

                    @Override
                    public void setMaxTextMessageBufferSize(int max) {

                    }

                    @Override
                    public int getMaxTextMessageBufferSize() {
                        return 0;
                    }

                    @Override
                    public RemoteEndpoint.Async getAsyncRemote() {
                        return null;
                    }

                    @Override
                    public RemoteEndpoint.Basic getBasicRemote() {
                        return null;
                    }

                    @Override
                    public String getId() {
                        return null;
                    }

                    @Override
                    public void close() {
                        Exchange.this.close();
                        logger.info("Connection closed");

                    }

                    @Override
                    public void close(CloseReason closeReason) {

                    }

                    @Override
                    public URI getRequestURI() {
                        return null;
                    }

                    @Override
                    public Map<String, List<String>> getRequestParameterMap() {
                        return null;
                    }

                    @Override
                    public String getQueryString() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getPathParameters() {
                        return null;
                    }

                    @Override
                    public Map<String, Object> getUserProperties() {
                        return null;
                    }

                    @Override
                    public Principal getUserPrincipal() {
                        return null;
                    }

                    @Override
                    public Set<Session> getOpenSessions() {
                        return null;
                    }

                    @Override
                    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) throws IllegalStateException {

                    }

                    @Override
                    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) throws IllegalStateException {

                    }
                };
            }

            @Override
            public Session connectToServer(Class<?> aClass, URI uri) {
                return null;
            }

            @Override
            public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfig, URI uri) {
                return null;
            }

            @Override
            public Session connectToServer(Class<? extends Endpoint> aClass, ClientEndpointConfig clientEndpointConfig, URI uri) throws IOException {
                //Connect to the endpoint


                socket = SocketFactory.getDefault();

                socket.createSocket(String.valueOf(uri), 8080);
                try {
                    socket.wait(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            return null;
            }
            @Override
            public long getDefaultMaxSessionIdleTimeout() {
                return 0;
            }

            @Override
            public void setDefaultMaxSessionIdleTimeout(long l) {

            }

            @Override
            public int getDefaultMaxBinaryMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxBinaryMessageBufferSize(int i) {

            }

            @Override
            public int getDefaultMaxTextMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxTextMessageBufferSize(int i) {

            }

            @Override
            public Set<Extension> getInstalledExtensions() {
                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {




               Log.info(
                       String.format("Exchange %s sent text message %s",
                                getExchangeName(), data),"text"+data

               );


                return null;


            }

            @Override
            public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {



                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {




                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
                return null;
            }

            @Override
            public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {


                logger.info(
                        String.format("Exchange %s closed connection with status code %d and reason %s",
                                getExchangeName(), statusCode, reason),"status"+statusCode
                );


                return null;
            }

            @Override
            public String getSubprotocol() {
                return null;
            }

            @Override
            public boolean isOutputClosed() {
                return false;
            }

            @Override
            public boolean isInputClosed() {
                return false;
            }

            @Override
            public void abort() {

            }
        };
    }

    private void close() {
        this.isOpen = false;
    }


    private @NotNull @Unmodifiable Object getExchangeName() {
        return this.getClass().getSimpleName();
    }


    public abstract void onOpen(ServerHandshake handshake);

    public abstract void onMessage(String message);

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);


    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}

