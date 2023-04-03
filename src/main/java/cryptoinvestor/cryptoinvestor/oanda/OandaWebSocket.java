package cryptoinvestor.cryptoinvestor.oanda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cryptoinvestor.cryptoinvestor.*;
import javafx.util.Pair;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class OandaWebSocket extends ExchangeWebSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(cryptoinvestor.cryptoinvestor.Coinbase.CoinbaseWebSocketClient.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Account connectionEstablished;


//    curl \
//            -H "Authorization: Bearer <AUTHENTICATION TOKEN>" \
//            "https://stream-fxtrade.oanda.com/v3/accounts/<ACCOUNT>/pricing/stream?instruments=EUR_USD%2CUSD_CAD"

    public OandaWebSocket(Account connectionEstablished) {
        super(URI.create("ws://api-fxtrade.oanda.com/v3/accounts/001-001-2783446-002/pricing/stream?instruments=" + "EUR_USD&USD_CAD"), new Draft_6455());


        this.connectionEstablished = connectionEstablished;
    }

    @Override
    public void onMessage(String message) {
        JsonNode messageJson;
        try {
            messageJson = OBJECT_MAPPER.readTree(message);
        } catch (JsonProcessingException ex) {
            logger.error("ex: ", ex);
            throw new RuntimeException(ex);
        }

        if (messageJson.has("event") && messageJson.get("event").asText().equalsIgnoreCase("info")) {
            connectionEstablished.setValue(true);
            logger.info(
                    "oanda websocket client: connection established with account: " +
                            messageJson.get("account_id").asText()
            );
        }

        TradePair tradePair = null;
        try {
            tradePair = parseTradePair(messageJson);
        } catch (CurrencyNotFoundException exception) {
            logger.error("oanda websocket client: could not initialize trade pair: " +
                    messageJson.get("product_id").asText(), exception);
        }

        Side side = messageJson.has("side") ? Side.getSide(messageJson.get("side").asText()) : null;

        switch (messageJson.get("type").asText()) {
            case "heartbeat" ->
                    sendText(OBJECT_MAPPER.createObjectNode().put("type", "heartbeat").put("on","").toPrettyString(),false);
            case "match" -> {
                if (liveTradeConsumers.containsKey(tradePair)) {
                    assert tradePair != null;
                    Trade newTrade;
                    try {
                        newTrade = new Trade(tradePair,
                                messageJson.get("price").asDouble(),

                                messageJson.get("size").asDouble(),

                                side, messageJson.at("trade_id").asLong(),
                                Instant.from(ISO_INSTANT.parse(messageJson.get("time").asText())));
                    } catch (IOException | InterruptedException | ParseException |
                             URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    liveTradeConsumers.get(tradePair).acceptTrades(Collections.singletonList(newTrade));
                }
            }
            case "error" -> throw new IllegalArgumentException("Error on Oanda websocket client: " +
                    messageJson.get("message").asText());
            default -> throw new IllegalStateException("Unhandled message type on Gdax websocket client: " +
                    messageJson.get("type").asText());
        }
    }

    private @NotNull TradePair parseTradePair(@NotNull JsonNode messageJson) throws CurrencyNotFoundException {
        final String productId = messageJson.get("product_id").asText();
        final String[] products = productId.split("-");
        TradePair tradePair;
        if (products[0].equalsIgnoreCase("BTC")) {
            tradePair = TradePair.parse(productId, "-", new Pair<>(CryptoCurrency.class, FiatCurrency.class));
        } else {
            // products[0] == "ETH"
            if (products[1].equalsIgnoreCase("usd")) {
                tradePair = TradePair.parse(productId, "-", new Pair<>(CryptoCurrency.class, FiatCurrency.class));
            } else {
                // productId == "ETH-BTC"
                tradePair = TradePair.parse(productId, "-", new Pair<>(CryptoCurrency.class, CryptoCurrency.class));
            }
        }

        return tradePair;
    }


    @Override
    public void streamLiveTrades(@NotNull Set<TradePair> tradePairs, LiveTradesConsumer liveTradesConsumer) {

        sendText(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("product_id", tradePairs.toString()).toPrettyString(), false);
        liveTradeConsumers.put(tradePairs.iterator().next(), liveTradesConsumer);

    }


    @Override
    public void streamLiveTrades(@NotNull TradePair tradePair, LiveTradesConsumer liveTradesConsumer) {
        liveTradeConsumers.put(tradePair, liveTradesConsumer);

    }

    @Override
    public void stopStreamLiveTrades(TradePair tradePair) {
        liveTradeConsumers.remove(tradePair);
    }

    @Override
    public boolean supportsStreamingTrades(TradePair tradePair) {

        return !liveTradeConsumers.containsKey(tradePair);
    }

    @Override
    public boolean isStreamingTradesSupported(TradePair tradePair) {
        return false;
    }

    @Override
    public boolean isStreamingTradesEnabled(TradePair tradePair) {
        return false;
    }

    @Override
    @NotNull
    public URI getURI() {
        return
                URI.create("wss://stream-fxtrade.oanda.com/v3/accounts/001-001-2783446-002/pricing/stream?instruments=" + "EUR_USD&USD_CAD");
    }


    @Override
    public void request(long n) {
        logger.info("oanda websocket client: request: " + n);


    }

    @Override
    public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
        logger.debug("sendText: {}", data);
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
        logger.debug("abort");

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("oanda websocket client: connection closed with code: " + code + ", reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {

        logger.error("onError: ", ex);

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("oanda websocket client: connection established with account: " +
                serverHandshake.getHttpStatusMessage());
    }

    @Override
    public long getDefaultAsyncSendTimeout() {
        return 0;
    }

    @Override
    public void setAsyncSendTimeout(long timeout) {
        logger.debug("setAsyncSendTimeout: {}", timeout);


    }

    @Override
    public Session connectToServer(Object endpoint, ClientEndpointConfig path) {
        return null;
    }

    @Override
    public Session connectToServer(Class<?> annotatedEndpointClass, URI path) {
        return null;
    }

    @Override
    public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfiguration, URI path) {
        return null;
    }

    @Override
    public Session connectToServer(Class<? extends Endpoint> endpoint, ClientEndpointConfig clientEndpointConfiguration, URI path) {
        return null;
    }

    @Override
    public long getDefaultMaxSessionIdleTimeout() {
        return 0;
    }

    @Override
    public void setDefaultMaxSessionIdleTimeout(long timeout) {
        logger.debug("setDefaultMaxSessionIdleTimeout: {}", timeout);

    }

    @Override
    public int getDefaultMaxBinaryMessageBufferSize() {
        return 0;
    }

    @Override
    public void setDefaultMaxBinaryMessageBufferSize(int max) {

    }

    @Override
    public int getDefaultMaxTextMessageBufferSize() {
        return 0;
    }

    @Override
    public void setDefaultMaxTextMessageBufferSize(int max) {

    }

    @Override
    public Set<Extension> getInstalledExtensions() {
        return null;
    }
}
