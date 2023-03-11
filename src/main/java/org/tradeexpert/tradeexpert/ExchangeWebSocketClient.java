package org.tradeexpert.tradeexpert;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import  org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.TAG;


public abstract class ExchangeWebSocketClient implements WebSocket {
    protected final BooleanProperty connectionEstablished;
    protected final Map<String, LiveTradesConsumer> liveTradeConsumers = new ConcurrentHashMap<>();
    protected final Map<String, LiveOrdersConsumer> liveOrderConsumers = new ConcurrentHashMap<>();
    protected final Map<String, LiveOrderBookConsumers> liveOrderBookConsumers = new ConcurrentHashMap<>();
    protected final Map<String, LiveTickerConsumer> liveTickerConsumers = new ConcurrentHashMap<>();
    protected final Map<String, News> liveNewsConsumers = new ConcurrentHashMap<>();

    protected final CountDownLatch webSocketInitializedLatch = new CountDownLatch(1);


    public ExchangeWebSocketClient(URI clientUri, Draft clientDraft) {
        this.connectionEstablished = new SimpleBooleanProperty(false);
        this.connectionEstablished.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                webSocketInitializedLatch.countDown();
                Log.info("","Connection established");
            }
        });
        this.connect(clientUri, clientDraft);
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
                Log.info(String.valueOf(TAG),"Connection established");
            }

            @Override
            public void onMessage(String s) {
                Log.info(String.valueOf(TAG),"Received message: " + s);

                org.slf4j.Logger logger = (org.slf4j.Logger) Logger.getLogger(s);
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
                Log.info(String.valueOf(TAG),"Connection closed");
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

                e.printStackTrace();


            }
        };
        webSocketClient.connect();
    }

    public CountDownLatch getInitializationLatch() {
        return webSocketInitializedLatch;
    }

    public void streamLiveTrades(String tradePair, LiveTradesConsumer liveTradesConsumer) {
        liveTradeConsumers.put(tradePair, liveTradesConsumer);
        if (connectionEstablished.get()) {
            liveTradesConsumer.onConnectionEstablished();
        }
        else {
            liveTradesConsumer.onConnectionFailed();

            CompletableFuture.runAsync(() -> {
                try {
                    webSocketInitializedLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (connectionEstablished.get()) {
                    liveTradesConsumer.onConnectionEstablished();
                }
                else {
                    liveTradesConsumer.onConnectionFailed();
                }
            });
        }

    }


    public boolean supportsStreamingTrades(String tradePair) {

        return liveTradeConsumers.containsKey(tradePair);


    }


    @Contract(" -> new")
    @NotNull URI getURI() {
        return URI.create("wss://" + getURI().getHost() + ":" + getURI().getPort() + "/ws");
    }








    public abstract CompletableFuture<WebSocket> sendPong(ByteBuffer message);

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



}
