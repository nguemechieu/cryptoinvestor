package cryptoinvestor.cryptoinvestor.BinanceUs;

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
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;


public class BinanceUsWebSocket  extends ExchangeWebSocketClient {



        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final CompletableFuture<Boolean> connectionEstablished = new CompletableFuture<>();

    private static final Logger logger = LoggerFactory.getLogger(cryptoinvestor.cryptoinvestor.Coinbase.CoinbaseWebSocketClient.class);


    public BinanceUsWebSocket(Set<TradePair> tradePairs) {
        super(URI.create(
                "wss://stream.binance.us:9443/ws"
        ), new Draft_6455());
        logger.info("Binance us websocket client initialized");
    }

    @Override
    public void onMessage(String message) {
        JsonNode messageJson;
        try {
            messageJson = OBJECT_MAPPER.readTree(message);
            if (messageJson.has("event") && messageJson.get("event").asText().equalsIgnoreCase("ping")) {
                sendText(OBJECT_MAPPER.createObjectNode().put("type", "pong").toPrettyString(),false);
                logger.info(
                        "BinanceUs  websocket client: Pong received"
                );
            }

        } catch (JsonProcessingException ex) {
            logger.error("ex: ", ex);
            throw new RuntimeException(ex);
        }

        if (messageJson.has("event") && messageJson.get("event").asText().equalsIgnoreCase("info")) {

            if (messageJson.has("product_id") && messageJson.get("product_id").asText().equalsIgnoreCase("BTCUSD")) {
                connectionEstablished.complete(true);
            }
        }


        TradePair tradePair = null;
        try {
            tradePair = parseTradePair(messageJson);
            logger.info(
                    "BinanceUs  websocket client: received trade pair: " + tradePair
            );
        } catch (CurrencyNotFoundException exception) {
            logger.error("coinbase websocket client: could not initialize trade pair: " +
                    messageJson.get("product_id").asText(), exception);
        }

        Side side = messageJson.has("side") ? Side.getSide(messageJson.get("side").asText()) : null;

        switch (messageJson.get("type").asText()) {
            case "heartbeat" ->
                    sendText(OBJECT_MAPPER.createObjectNode().put("type", "heartbeat").put("on", "false").toPrettyString(),true);
            case "match" -> {
                if (liveTradeConsumers.containsKey(tradePair)) {
                    assert tradePair != null;
                    Trade newTrade ;
                    try {
                        newTrade = new Trade(tradePair,
                                DefaultMoney.of(new BigDecimal(messageJson.get("price").asText()),
                                        tradePair.getCounterCurrency()),
                                DefaultMoney.of(new BigDecimal(messageJson.get("size").asText()),
                                        tradePair.getBaseCurrency()),
                                side, messageJson.at("trade_id").asLong(),
                                Instant.from(ISO_INSTANT.parse(messageJson.get("time").asText())));
                    } catch (TelegramApiException | IOException | InterruptedException e) {
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

    private @NotNull TradePair parseTradePair(@NotNull JsonNode messageJson) throws CurrencyNotFoundException {
        final String productId = messageJson.get("product_id").asText();
        final String[] products = productId.split("/");
        TradePair tradePair;
        if (products[0].equalsIgnoreCase("BTC")) {
            tradePair = TradePair.parse(productId, "/", new Pair<>(CryptoCurrency.class, FiatCurrency.class));
        } else {
            // products[0] == "ETH"
            if (products[1].equalsIgnoreCase("usd")) {
                tradePair = TradePair.parse(productId, "/", new Pair<>(CryptoCurrency.class, FiatCurrency.class));
            } else {
                // productId == "ETH-BTC"
                tradePair = TradePair.parse(productId, "/", new Pair<>(CryptoCurrency.class, CryptoCurrency.class));
            }
        }

        return tradePair;
    }

    @Override
    public void streamLiveTrades(@NotNull TradePair tradePair, LiveTradesConsumer liveTradesConsumer) {
        sendText(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("product_id", tradePair.toString()).toPrettyString(),true);
        liveTradeConsumers.put(tradePair, liveTradesConsumer);
    }

    @Override
    public void streamLiveTrades(@NotNull Set<TradePair> tradePairs, LiveTradesConsumer liveTradesConsumer) {
        for (TradePair tradePair : tradePairs) {
            streamLiveTrades(tradePair, liveTradesConsumer);
        }

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
    public void onClose(int code, String reason, boolean remote) {}

    @Override
    public void onError(Exception ex) {

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {}

    @Override
    public long getDefaultAsyncSendTimeout() {
        return 0;
    }

    @Override
    public void setAsyncSendTimeout(long timeout) {

    }

    @Override
    public Session connectToServer(Object endpoint, URI path) throws DeploymentException, IOException {
        return null;
    }

    @Override
    public Session connectToServer(Class<?> annotatedEndpointClass, URI path) throws DeploymentException, IOException {
        return null;
    }

    @Override
    public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfiguration, URI path) throws DeploymentException, IOException {
        return null;
    }

    @Override
    public Session connectToServer(Class<? extends Endpoint> endpoint, ClientEndpointConfig clientEndpointConfiguration, URI path) throws DeploymentException, IOException {
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
        return
                Collections.emptySet();
    }


}
