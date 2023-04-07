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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;


public class BinanceUsWebSocket extends ExchangeWebSocketClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Logger logger = LoggerFactory.getLogger(cryptoinvestor.cryptoinvestor.Coinbase.CoinbaseWebSocketClient.class);


    public BinanceUsWebSocket(@NotNull TradePair tradePair) {
        super(
                URI.create(
                        "wss://stream.binance.com:9443/ws/" + tradePair.getBaseCurrency().getCode().toLowerCase() + tradePair.getCounterCurrency().getCode().toLowerCase()
                )

                , new Draft_6455());


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

        if (messageJson.has("e") && messageJson.get("e").asText().equalsIgnoreCase("info")) {

            logger.info("BinanceUsWebSocket: " + messageJson);

        }

//
//            "e": "executionReport",        // Event type
//                    "E": 1499405658658,            // Event time
//                    "s": "ETHBTC",                 // Symbol
//                    "c": "mUvoqJxFIILMdfAW5iGSOW", // Client order ID
//                    "S": "BUY",                    // Side
//                    "o": "LIMIT",                  // Order type
//                    "f": "GTC",                    // Time in force
//                    "q": "1.00000000",             // Order quantity
//                    "p": "0.10264410",             // Order price
//                    "P": "0.00000000",             // Stop price
//                    "d": 4,                        // Trailing Delta; This is only visible if the order was a trailing stop order.
//                    "F": "0.00000000",             // Iceberg quantity
//                    "g": -1,                       // OrderListId
//                    "C": "",                       // Original client order ID; This is the ID of the order being canceled
//                    "x": "NEW",                    // Current execution type
//                    "X": "NEW",                    // Current order status
//                    "r": "NONE",                   // Order reject reason; will be an error code.
//                    "i": 4293153,                  // Order ID
//                    "l": "0.00000000",             // Last executed quantity
//                    "z": "0.00000000",             // Cumulative filled quantity
//                    "L": "0.00000000",             // Last executed price
//                    "n": "0",                      // Commission amount
//                    "N": null,                     // Commission asset
//                    "T": 1499405658657,            // Transaction time
//                    "t": -1,                       // Trade ID
//                    "I": 8641984,                  // Ignore
//                    "w": true,                     // Is the order on the book?
//                    "m": false,                    // Is this trade the maker side?
//                    "M": false,                    // Ignore
//                    "O": 1499405658657,            // Order creation time
//                    "Z": "0.00000000",             // Cumulative quote asset transacted quantity
//                    "Y": "0.00000000",             // Last quote asset transacted quantity (i.e. lastPrice * lastQty)
//                    "Q": "0.00000000",             //Quote Order Quantity
//                    "V": "selfTradePreventionMode",
//                    "D": "trailing_time",          // (Appears if the trailing stop order is active)
//                    "W": "workingTime"             // (Appears if the order is working on the order book)
//            "u":12332                      // tradeGroupId (Appear if the order has expired due to STP)
//            "v":122                        // preventedMatchId (Appear if the order has expired due to STP)
//            "U":2039                       // counterOrderId (Appear if the order has expired due to STP)
//            "A":"1.00000000"               // preventedQuantity(Appear if the order has expired due to STP )
//            "B":"2.00000000"               // lastPreventedQuantity(Appear if the order has expired due to STP)


        Side side = messageJson.has("S") ? Side.getSide(messageJson.get("S").asText()) : null;
        logger.info("BinanceUsWebSocket: " + messageJson + " " + side);
//            switch (messageJson.asText()) {
//                case "heartbeat" ->
//                        send(OBJECT_MAPPER.createObjectNode().put("type", "heartbeat").put("on", "false").toPrettyString());
//                case "match" -> {

        String symb = messageJson.get("s").asText();

        TradePair tradePair = null;
        if (symb.contains("USDT")) {
            symb = symb.replace("USDT", "");
            tradePair = new TradePair(symb, "USDT");
        } else if (symb.contains("USD") && symb.length() > 3) {
            symb = symb.replace("USD", "");
            tradePair = new TradePair(symb, "USD");
        } else if (symb.subSequence(3, symb.length()).equals("BTC")) {
            symb = symb.replace("BTC", "");
            tradePair = new TradePair(symb, "BTC");
        }


        logger.info("BinanceUsWebSocket: " + messageJson + " " + tradePair);


        if (liveTradeConsumers.containsKey(tradePair)) {
            Trade newTrade;
            try {

                newTrade = new Trade(tradePair,
                        messageJson.get("p").asDouble(),

                        messageJson.get("q").asDouble(),

                        side, messageJson.at("E").asLong(),
                        Date.from(Instant.from(ISO_INSTANT.parse(messageJson.get("t").asText()))).getTime());

                logger.info(
                        "BinanceUs websocket client: received trade: " + newTrade
                );
            } catch (IOException | InterruptedException | URISyntaxException |
                     ParseException e) {
                throw new RuntimeException(e);
            }
            logger.info("BinanceUs websocket client: received trade: " + newTrade);
            List<Trade> newTrades = new ArrayList<>();
            newTrades.add(newTrade);
            liveTradeConsumers.get(tradePair).acceptTrades(newTrades);
        }
        //}
        //case "error" -> throw new IllegalArgumentException("Error on Binance websocket client: " +
        //       messageJson.get("message").asText());
        // default -> throw new IllegalStateException("Unhandled message type on Gdax websocket client: " +
        //     messageJson.get("type").asText());
        //}

    }

    private @NotNull TradePair parseTradePair(@NotNull JsonNode messageJson) throws CurrencyNotFoundException {
        String productId0 = messageJson.get("s").asText();
        String productId1;
        if (productId0.contains("USDT") || productId0.contains("USD")) {
            productId0 = productId0.split("USDT")[0];
            productId1 = productId0 + "-" + "USDT";

            return TradePair.parse(productId1, "-", new Pair<>(CryptoCurrency.class, FiatCurrency.class));
        } else if (productId0.contains("BTC")) {
            productId0 = productId0.split("BTC")[0];
            productId1 = productId0 + "-" + "BTC";
            return TradePair.parse(productId1, "-", new Pair<>(CryptoCurrency.class, CryptoCurrency.class));
        } else throw new CurrencyNotFoundException(CurrencyType.CRYPTO, productId0);

    }

    @Override
    public void streamLiveTrades(@NotNull Set<TradePair> tradePairs, LiveTradesConsumer liveTradesConsumer) {

        tradePairs = tradePairs.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        send(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("s", tradePairs.stream().map(TradePair::toString).collect(Collectors.joining())).toPrettyString());
        liveTradeConsumers.put(
                (TradePair) tradePairs,
                liveTradesConsumer);

    }

    @Override
    public void streamLiveTrades(@NotNull TradePair tradePair, LiveTradesConsumer liveTradesConsumer) {
        send(OBJECT_MAPPER.createObjectNode().put("type", "subscribe")
                .put("s", tradePair.toString('-')).toPrettyString());
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
    public double getPrice(TradePair tradePair) {
        return 0;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("onClose: code: {}, reason: {}, remote: {}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("ex: ", ex);

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("onOpen: {}", serverHandshake);
    }
}
