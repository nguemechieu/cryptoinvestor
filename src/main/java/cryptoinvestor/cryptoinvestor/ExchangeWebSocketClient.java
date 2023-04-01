package cryptoinvestor.cryptoinvestor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExchangeWebSocketClient implements javax.websocket.WebSocketContainer {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeWebSocketClient.class);
    protected final BooleanProperty connectionEstablished;
    protected final Map<TradePair, LiveTradesConsumer> liveTradeConsumers = new ConcurrentHashMap<>();


    protected final CountDownLatch webSocketInitializedLatch = new CountDownLatch(1);


    public ExchangeWebSocketClient(URI clientUri, Draft clientDraft) {
        this.connectionEstablished = new SimpleBooleanProperty(false);
        this.connectionEstablished.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                webSocketInitializedLatch.countDown();
                Log.info("", "Connection established");
            }
        });
        this.connect(clientUri, clientDraft);
        logger.info("Connection established " + this.connectionEstablished.get()+ " " + this.webSocketInitializedLatch);

    }

    private void connect(URI clientUri, Draft clientDraft) {

        WebSocketClient webSocketClient = new WebSocketClient(clientUri, clientDraft) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                connectionEstablished.set(true);
                CompletableFuture.runAsync(() -> {
                    try {
                        webSocketInitializedLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (connectionEstablished.get()) {
                        connectionEstablished.set(false);
                    }
                });
                logger.info("Connection established " + this.getURI().toString());
            }

            @Override
            public void onMessage(String s) {

                logger.info("Received message: " + s);


            }

            @Override
            public void onClose(int i, String s, boolean b) {
                connectionEstablished.set(false);

                CompletableFuture.runAsync(() -> {
                    try {
                        webSocketInitializedLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (connectionEstablished.get()) {
                        connectionEstablished.set(false);

                    }
                });

            }

            @Override
            public void onError(@NotNull Exception e) {
                connectionEstablished.set(false);
                Log.error("WebSocketClient error (" + getURI().getHost() + "): " + e);
                CompletableFuture.runAsync(() -> {
                    try {
                        webSocketInitializedLatch.await();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();

                    }
                    if (connectionEstablished.get()) {
                        connectionEstablished.set(false);
                    }
                });


            }
        };
        webSocketClient.connect();
        if (webSocketClient.getConnection().isOpen()) logger.info("Connection established ");
        else logger.info("Connection closed ");
    }

    public CountDownLatch getInitializationLatch() {
        return webSocketInitializedLatch;
    }

    public abstract void onMessage(String message) throws TelegramApiException, IOException, InterruptedException, ParseException, URISyntaxException;

    public void streamLiveTrades(TradePair tradePair, LiveTradesConsumer liveTradesConsumer) throws IOException, InterruptedException, ParseException {
        liveTradeConsumers.put(tradePair, liveTradesConsumer);
        if (connectionEstablished.get()) {
            liveTradesConsumer.onConnectionEstablished();

        } else {
            liveTradesConsumer.onConnectionFailed();

            CompletableFuture.runAsync(() -> {
                try {
                    webSocketInitializedLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (connectionEstablished.get()) {
                    try {
                        liveTradesConsumer.onConnectionEstablished();
                    } catch (IOException | InterruptedException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        liveTradesConsumer.onConnectionFailed();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

    }




    public abstract void streamLiveTrades(@NotNull Set<TradePair> tradePairs, LiveTradesConsumer liveTradesConsumer);

    public abstract void stopStreamLiveTrades(TradePair tradePair);

    public boolean supportsStreamingTrades(TradePair tradePair) {return liveTradeConsumers.containsKey(tradePair);}


    protected abstract @NotNull URI getURI();

    public void request(long n) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(n);
                webSocketInitializedLatch.await();
                if (connectionEstablished.get()) {
                    connectionEstablished.set(false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectionEstablished.set(!connectionEstablished.get());
        });
    }


    public abstract CompletableFuture<WebSocket> sendText(CharSequence data, boolean last);

    public abstract CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last);

    public abstract CompletableFuture<WebSocket> sendPing(ByteBuffer message);

    public abstract CompletableFuture<WebSocket> sendPong(ByteBuffer message);

    public abstract CompletableFuture<WebSocket> sendClose(int statusCode, String reason);

    public abstract String getSubprotocol();

    public abstract boolean isOutputClosed();

    public abstract boolean isInputClosed();

    public abstract void abort();

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);

    public abstract void onOpen(ServerHandshake serverHandshake);
}