package tradeexpert.tradeexpert;


import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.websocket.*;

public abstract class Exchange {

    private static final Logger Log = LoggerFactory.getLogger(Exchange.class);
    private final String urlize;

    public TelegramClient telegram;

    public Exchange(String ur, String token) throws TelegramApiException, IOException, ParseException, InterruptedException {
        this.urlize = ur;

        this.telegram = new TelegramClient(token);
        this.telegram.connect();



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
            public Session connectToServer(Object o, URI uri) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<?> aClass, URI uri) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfig, URI uri) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<? extends Endpoint> aClass, ClientEndpointConfig clientEndpointConfig, URI uri) throws DeploymentException, IOException {
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

    private @NotNull @Unmodifiable Object getExchangeName() {
        return this.getClass().getSimpleName();
    }


    public abstract void onOpen(ServerHandshake handshake);

    public abstract void onMessage(String message);

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);


}

