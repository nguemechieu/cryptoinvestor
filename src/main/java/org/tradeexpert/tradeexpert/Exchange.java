package org.tradeexpert.tradeexpert;


import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class Exchange {


    public abstract CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, String tradePair);

    public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt);

    public abstract CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
            String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

    public ExchangeWebSocketClient getWebsocketClient() {

        return new ExchangeWebSocketClient(
                URI.create("wss://api.oanda.com/ws/v1/"),
                null
        ) {
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


                Log.info(
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

    private Object getExchangeName() {
        return this.getClass().getSimpleName();
    }


    public abstract void onOpen(ServerHandshake handshake);

    public abstract void onMessage(String message);

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);
}
