package cryptoinvestor.cryptoinvestor.Coinbase;

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

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;


public class CoinbaseWebSocketClient extends ExchangeWebSocketClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Logger logger = LoggerFactory.getLogger(CoinbaseWebSocketClient.class);

    public CoinbaseWebSocketClient(URI webSocketClientUri) {
        super(webSocketClientUri, new Draft_6455());

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


        TradePair tradePair = null;
        try {
            tradePair = parseTradePair(messageJson);
        } catch (CurrencyNotFoundException exception) {
            logger.error("coinbase websocket client: could not initialize trade pair: " +
                    messageJson.get("product_id").asText(), exception);
        }

        Side side = messageJson.has("side") ? Side.getSide(messageJson.get("side").asText()) : null;
        logger.info(
                "coinbase websocket client: received trade pair: " + tradePair +
                        ", side: " + side + ", message: " + messageJson.toPrettyString()
        );
        switch (messageJson.get("type").asText()) {
            case "heartbeat" ->
                    send(OBJECT_MAPPER.createObjectNode().put("type", "heartbeat").put("on", "false").toPrettyString());
            case "match" -> {
                if (liveTradeConsumers.containsKey(tradePair)) {
                    Trade newTrade;
                    try {

                        newTrade = new Trade(tradePair,
                                messageJson.get("price").asDouble()
                                ,
                                messageJson.get("size").asDouble(),

                                side, messageJson.at("trade_id").asLong(),
                                Instant.from(ISO_INSTANT.parse(messageJson.get("time").asText())));
                        logger.info("coinbase websocket client: received trade: " + newTrade);

                    } catch (IOException | InterruptedException | URISyntaxException |
                             ParseException e) {
                        throw new RuntimeException(e);
                    }
                    liveTradeConsumers.get(tradePair).acceptTrades(Collections.singletonList(newTrade));
                }
            }
            case "error" -> throw new IllegalArgumentException("Error on Coinbase websocket client: " +
                    messageJson.get("message").asText());
            default -> throw new IllegalStateException("Unhandled message type on Gdax websocket client: " +
                    messageJson.get("type").asText());
        }
    }

    private @NotNull TradePair parseTradePair(JsonNode messageJson) throws CurrencyNotFoundException {


        logger.info(
                String.valueOf(messageJson)
        );

        final String productId = messageJson.get("product_id").asText();

        logger.info("coinbase websocket client: parsing trade pair: " + productId);
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

        tradePairs = tradePairs.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        send(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("product_id", tradePairs.stream().map(TradePair::toString).collect(Collectors.joining())).toPrettyString());
        liveTradeConsumers.put(
                (TradePair) tradePairs,
                liveTradesConsumer);

    }

    @Override
    public void streamLiveTrades(@NotNull TradePair tradePair, LiveTradesConsumer liveTradesConsumer) {
        send(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("product_id", tradePair.toString('-')).toPrettyString());
        liveTradeConsumers.put(
                tradePair,
                liveTradesConsumer);


    }

    @Override
    public void stopStreamLiveTrades(TradePair tradePair) {
        liveTradeConsumers.remove(tradePair);
    }

    @Override
    public boolean supportsStreamingTrades(TradePair tradePair) {
        return false;
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
    public void request(long n) {

    }

    @Override
    public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
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

    }

    @Override
    public long getDefaultAsyncSendTimeout() {
        return 0;
    }

    @Override
    public void setAsyncSendTimeout(long timeout) {
        throw new UnsupportedOperationException();

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

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info(
                "coinbase websocket client: closed connection with code: " + code + ", reason: " + reason + ", remote: " + remote
        );
    }

    @Override
    public void onError(@NotNull Exception ex) {
        ex.printStackTrace();

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        send(OBJECT_MAPPER.createObjectNode().put("type", "heartbeat").put("on", "true").toPrettyString());

        for (Map.Entry<TradePair, LiveTradesConsumer> entry : liveTradeConsumers.entrySet()) {
            entry.getValue().acceptTrades(Collections.emptyList());
        }
        liveTradeConsumers.clear();
        for (Currency currency : CurrencyDataProvider.getInstance()) {


            send(OBJECT_MAPPER.createObjectNode().put("type", "subscribe").put("product_id", currency.getCode() + "-USD").toPrettyString());
        }
    }
}