package org.tradeexpert.tradeexpert.BinanceUs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.tradeexpert.tradeexpert.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.tradeexpert.tradeexpert.NewsManager.news;


public class BinanceUs extends Exchange {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String apiKey;
    public static final String API_URL =  "wss://stream.binance.us:9443";
    public static final String API_VERSION = "v1";
    static String apiSecret;
    String apiPass;

    public BinanceUs(
            String apiKey,
            String apiSecret,
            String apiPass
    ) throws Exception {
        super(
                API_URL
        );


        if (apiKey == null || apiSecret == null || apiPass == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill in all the required fields",
                    ButtonType.OK);
            alert.setTitle("Binance");
            alert.setHeaderText("Please fill in all the required fields");
            alert.showAndWait();

            throw new Exception("apiKey, apiSecret and apiPass are required");
        } else {
            BinanceUs.apiKey = apiKey;
            BinanceUs.apiSecret = apiSecret;
            this.apiPass = apiPass;
        }

    }

    public static String getVersion() {
        return API_VERSION;
    }

    public static void createMarketOrder(String tradePair, String side, double size) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(side);


    }
    public static void createLimitOrder(String tradePair, String side, double size, double price) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(side);
        Objects.requireNonNull(size);
        Objects.requireNonNull(price);
    }
    public static void createStopLimitOrder(String tradePair, String side, double size, double price, double stopPrice) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(side);
        Objects.requireNonNull(size);
        Objects.requireNonNull(price);
        Objects.requireNonNull(stopPrice);
    }
    public static void createStopMarketOrder(String tradePair, String side, double size, double price, double stopPrice) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(side);
        Objects.requireNonNull(size);
        Objects.requireNonNull(price);
        Objects.requireNonNull(stopPrice);
    }
    public static void createStopLimitMarketOrder(String tradePair, String side, double size, double price, double stopPrice, double stopLimitPrice) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(side);
        Objects.requireNonNull(size);
        Objects.requireNonNull(price);
        Objects.requireNonNull(stopPrice);
        Objects.requireNonNull(stopLimitPrice);
    }

    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, String tradePair) {
        return
                new BinanceCandleDataSupplier(secondsPerCandle, tradePair);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to Binance " +handshake);

    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from Binance");
        out.println("Code: " + code+ " Reason: " + reason+ " Remote: " + remote);


    }

    @Override
    public void onError(@NotNull Exception ex) {
        ex.printStackTrace();

    }

        private static String x;
        private static String str;


        /**
         * Fetches the recent trades for the given trade pair from  {@code stopAt} till now (the current time).
         * <p>
         * This method only needs to be implemented to support live syncing.
         */
        @Override
        public CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt) {
            Objects.requireNonNull(tradePair);
            Objects.requireNonNull(stopAt);

            if (stopAt.isAfter(Instant.now())) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }

            CompletableFuture<List<Trade>> futureResult = new CompletableFuture<>();

            // It is not easy to fetch trades concurrently because we need to get the "cb-after" header after each request.
            CompletableFuture.runAsync(() -> {
                IntegerProperty afterCursor = new SimpleIntegerProperty(-1);
                List<Trade> tradesBeforeStopTime = new ArrayList<>();
                for (int i = 0; !futureResult.isDone(); i++) {
                    String uriStr = "https://api.binance.us/api/v3/trades?symbol=" + tradePair;

                    if (i != 0) {
                        uriStr += "?date=" + afterCursor.get();
                    }
                    try {
                        HttpRequest.Builder req = HttpRequest.newBuilder();
                        req.header("Accept", "application/json");
                        req.header("X-MBX-APIKEY", BinanceUs.apiKey);
                        req.header("X-MBX-APISECRET", BinanceUs.apiSecret);
                        req.header("Authorization", BinanceUs.apiKey);
                        req.uri(URI.create(uriStr));

                        HttpResponse<String> response = HttpClient.newHttpClient().send(
                                req.build(),
                                HttpResponse.BodyHandlers.ofString());

                        Log.info("response headers: " , String.valueOf(response.headers()));
                        if (response.headers().firstValue("date").isEmpty()) {
                            futureResult.completeExceptionally(new RuntimeException(
                                    "Binance Us trades response did not contain header \"date\": " + response));
                            return;
                        }

                        afterCursor.setValue(Integer.valueOf((response.headers().firstValue(" date").get())));

                        JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());
                        if (!tradesResponse.isArray()) {
                            futureResult.completeExceptionally(new RuntimeException(
                                    "Trades response was not an array!"));
                        }
                        if (tradesResponse.isEmpty()) {
                            futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
                        } else {
                            for (int j = 0; j < tradesResponse.size(); j++) {
                                JsonNode trade = tradesResponse.get(j);
                                Instant time = Instant.from(ISO_INSTANT.parse(trade.get("time").asText()));
                                if (time.compareTo(stopAt) <= 0) {
                                    futureResult.complete(tradesBeforeStopTime);

                                    break;
                                } else {
                                    tradesBeforeStopTime.add(new Trade(tradePair,
                                            DefaultMoney.ofFiat(trade.get("price").asText(), tradePair.substring(3, tradePair.length() - 1)),
                                            DefaultMoney.ofCrypto(trade.get("qty").asText(), tradePair.substring(0, 3)),
                                            Side.getSide(trade.get("isBuyerMaker").asText()), trade.get("id").asLong(), time));
                                }
                            }
                        }
                    } catch (IOException | InterruptedException ex) {
                        Log.error("ex: " + ex);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return futureResult;
        }

        /**
         * This method only needs to be implemented to support live syncing.
         */
        @Override
        public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
                String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
             String timeFrame = x + str;
            HttpRequest.Builder req=
                    HttpRequest.newBuilder();
            req.header("Accept", "application/json");
            req.header("Authorization", BinanceUs.apiKey);
            req.uri(URI.create("https://api.binance.us/api/v3/klines?symbol=" + tradePair + "&interval=" + timeFrame + "&limit=1000"));
            return HttpClient.newHttpClient().sendAsync(

                          req.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                        Log.info("Binance us Response: " ,response);
                        JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }

                        if (res.isEmpty()) {
                            return Optional.empty();
                        }

                        JsonNode currCandle;
                        Iterator<JsonNode> candleItr = res.iterator();
                        int currentTill = -1;
                        double openPrice = -1;
                        double highSoFar = -1;
                        double lowSoFar = Double.MAX_VALUE;
                        double volumeSoFar = 0;
                        double lastTradePrice = -1;
                        boolean foundFirst = false;
                        while (candleItr.hasNext()) {
                            currCandle = candleItr.next();
                            if (currCandle.get(0).asInt() < currentCandleStartedAt.getEpochSecond() ||
                                    currCandle.get(0).asInt() >= currentCandleStartedAt.getEpochSecond() +
                                            secondsPerCandle) {
                                currentTill = currCandle.get(0).asInt();
                                lastTradePrice = currCandle.get(4).asDouble();
                                foundFirst = true;
                                continue;
                            } else {
                                if (!foundFirst) {
                                    currentTill = currCandle.get(0).asInt();
                                    lastTradePrice = currCandle.get(4).asDouble();
                                    foundFirst = true;
                                }
                            }

                            openPrice = currCandle.get(1).asDouble();

                            if (currCandle.get(2).asDouble() > highSoFar) {
                                highSoFar = currCandle.get(2).asDouble();
                            }

                            if (currCandle.get(3).asDouble() < lowSoFar) {
                                lowSoFar = currCandle.get(3).asDouble();
                            }
                            volumeSoFar += currCandle.get(5).asDouble();
                        }
                        int openTime = (int) (currentCandleStartedAt.toEpochMilli() / 1000L);
                        return Optional.of(new InProgressCandleData(openTime, openPrice, highSoFar, lowSoFar,
                                currentTill, lastTradePrice, volumeSoFar));
                    });

        }






        public class BinanceCandleDataSupplier extends CandleDataSupplier {
            private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            private static final int EARLIEST_DATA = 1422144000; // roughly the first trade
            private static final Set<Integer> GRANULARITIES = Set.of(60, 60 * 5, 60 * 15, 60 * 30, 3600, 3600 * 2, 3600 * 3, 3600 * 4, 3600 * 6, 3600 * 24, 3600 * 24 * 7, 3600 * 24 * 7 * 4, 3600 * 24 * 365);

            BinanceCandleDataSupplier(int secondsPerCandle, String tradePair) {
                super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
                endTime.set(-1);

            }

            @Override
            public Set<Integer> getSupportedGranularities() {
                return new TreeSet<>(GRANULARITIES);
            }

            @Override
            public List<CandleData> getCandleData() {
                return null;
            }


            @Override
            public CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(
                    String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
                String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
                        currentCandleStartedAt, ZoneOffset.UTC));
                long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
                // Get the closest supported granularity to the ideal granularity.
                int actualGranularity = getCandleDataSupplier(secondsPerCandle, tradePair).getSupportedGranularities().stream()
                        .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
                        .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));
                // TODO: If actualGranularity = secondsPerCandle there are no sub-candles to fetch and we must get all the
                //  data for the current live syncing candle from the raw trades method.
                String timeFrame=
                        actualGranularity == secondsPerCandle? "1m" :
                                actualGranularity == secondsPerCandle * 5? "5m" :
                                        actualGranularity == secondsPerCandle * 15? "15m" :
                                                actualGranularity == secondsPerCandle * 30? "30m" :
                                                        actualGranularity == secondsPerCandle * 60? "1h" :
                                                                actualGranularity == secondsPerCandle * 60 * 5? "5h" :
                                                                        actualGranularity == secondsPerCandle * 60 * 15? "15h" :
                                                                                actualGranularity == secondsPerCandle * 60 * 30? "30h" :
                                                                                        actualGranularity == secondsPerCandle * 60 * 60? "1d" :
                                                                                                actualGranularity == secondsPerCandle * 60 * 60 * 24? "2" :
                                                                                                        String.valueOf(actualGranularity == secondsPerCandle * 60 * 60 * 24);

                return HttpClient.newHttpClient().sendAsync(
                                HttpRequest.newBuilder()
                                        .uri(URI.create(String.format(
                                                 "https://api.binance.us/api/v3/klines?symbol=" +
                                                        tradePair + "&interval=" + timeFrame)))
                                        .GET().build(),
                                HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenApply(response -> {
                            Log.info("coinbase response: " + response, news.toString());
                            JsonNode res;
                            try {
                                res = OBJECT_MAPPER.readTree(response);
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }

                            if (res.isEmpty()) {
                                return Optional.empty();
                            }

                            JsonNode currCandle;
                            Iterator<JsonNode> candleItr = res.iterator();
                            int currentTill = -1;
                            double openPrice = -1;
                            double highSoFar = -1;
                            double lowSoFar = Double.MAX_VALUE;
                            double volumeSoFar = 0;
                            double lastTradePrice = -1;
                            boolean foundFirst = false;
                            while (candleItr.hasNext()) {
                                currCandle = candleItr.next();
                                if (currCandle.get(0).asInt() < currentCandleStartedAt.getEpochSecond() ||
                                        currCandle.get(0).asInt() >= currentCandleStartedAt.getEpochSecond() +
                                                secondsPerCandle) {
                                    // skip this sub-candle if it is not in the parent candle's duration (this is just a
                                    // sanity guard) TODO(mike): Consider making this a "break;" once we understand why
                                    //  Coinbase is  not respecting start/end times
                                   // continue;
                                    break;
                                } else {
                                    if (!foundFirst) {
                                        // FIXME: Why are we only using the first sub-candle here?
                                        currentTill = currCandle.get(0).asInt();
                                        lastTradePrice = currCandle.get(4).asDouble();
                                        foundFirst = true;
                                    }
                                }

                                openPrice = currCandle.get(3).asDouble();

                                if (currCandle.get(2).asDouble() > highSoFar) {
                                    highSoFar = currCandle.get(2).asDouble();
                                }

                                if (currCandle.get(1).asDouble() < lowSoFar) {
                                    lowSoFar = currCandle.get(1).asDouble();
                                }

                                volumeSoFar += currCandle.get(5).asDouble();
                            }

                            int openTime = (int) (currentCandleStartedAt.toEpochMilli() / 1000L);

                            return Optional.of(new InProgressCandleData(openTime, openPrice, highSoFar, lowSoFar,
                                    currentTill, lastTradePrice, volumeSoFar));
                        });
            }



            public CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt) {
                Objects.requireNonNull(tradePair);
                Objects.requireNonNull(stopAt);
                if (stopAt.isAfter(Instant.now())) {
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }

                CompletableFuture<List<Trade>> futureResult = new CompletableFuture<>();

                // It is not easy to fetch trades concurrently because we need to get the "cb-after" header after each request.
                CompletableFuture.runAsync(() -> {
                    IntegerProperty afterCursor = new SimpleIntegerProperty(-1);
                    List<Trade> tradesBeforeStopTime = new ArrayList<>();

                    for (int i = 0; !futureResult.isDone(); i++) {
                        String uriStr = "https://api.binance.us/api/v3";

                        uriStr += "/trades/instruments" + tradePair;

                        if (i != 0) {
                            uriStr += "?cb-after=" + afterCursor.get();

}
                        try {
                            HttpRequest.Builder re = HttpRequest.newBuilder();
                            re.header("Authorization", "Bearer " + apiKey);
                            re.header(
                                    "Content-Type",
                                    "application/json"
                            );
                            re.header(
                                    "Accept",
                                    "application/json"
                            );
                            re.uri(URI.create(uriStr));
                            HttpResponse<String> response = HttpClient.newHttpClient().send(re.build(), HttpResponse.BodyHandlers.ofString());

                            Log.info("Binance response headers: " , String.valueOf(response.headers()));
                            if (response.headers().firstValue("cb-after").isEmpty()) {
                                futureResult.completeExceptionally(new RuntimeException(
                                        "Binance us  trades response did not contain header \"cb-after\": " + response));
                                return;
                            }

                            afterCursor.setValue(Integer.valueOf((response.headers().firstValue("cb-after").toString())));

                            JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());

                            if (!tradesResponse.isArray()) {
                                futureResult.completeExceptionally(new RuntimeException(
                                        "Oanda trades response was not an array!"));
                            }
                            if (tradesResponse.isEmpty()) {
                                futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
                            } else {
                                for (int j = 0; j < tradesResponse.size(); j++) {
                                    JsonNode trade = tradesResponse.get(j);

                                    Instant time = Instant.from(Instant.ofEpochSecond(Trade.candle.getOpenTime()));
                                    if (time.compareTo(stopAt) <= 0) {
                                        futureResult.complete(tradesBeforeStopTime);
                                        break;
                                    } else {
                                        tradesBeforeStopTime.add(new Trade(tradePair,
                                                DefaultMoney.ofFiat(trade.get("price").asText(), tradePair),
                                                DefaultMoney.ofCrypto(trade.get("size").asText(), tradePair),
                                                Side.getSide(trade.get("side").asText()), trade.get("trade_id").asLong(), time));
                                    }
                                }
                            }
                        } catch (IOException | InterruptedException ex) {
                            Log.error("ex: " + ex);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                return futureResult;
            }

            @Override
            public Future<List<CandleData>> get() {
                if (endTime.get() == -1) {
                    endTime.set((int) (Instant.now().toEpochMilli() / 1000L));
                }

                String endDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .format(LocalDateTime.ofEpochSecond(endTime.get(), 0, ZoneOffset.UTC));
                int startTime = Math.max(endTime.get() - (numCandles * secondsPerCandle), EARLIEST_DATA);
                String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .format(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC));
                Log.info("Start date: " + startDateString,"");
                Log.info("End date: " + endDateString, "");
                Log.info("TradePair " + tradePair, "");
                Log.info("Second per Candle: " + secondsPerCandle, "");
                if (secondsPerCandle < 3600) {
                    x = String.valueOf(secondsPerCandle / 60);
                    str = "m";
                } else if (secondsPerCandle < 86400) {
                    x = String.valueOf((secondsPerCandle / 3600));
                    str = "h";
                } else if (secondsPerCandle < 604800) {
                    x = String.valueOf(secondsPerCandle / 86400);
                    str = "d";
                } else if (secondsPerCandle < 2592000) {
                    x = String.valueOf((secondsPerCandle / 604800));
                    str = "w";
                } else {
                    x = String.valueOf((secondsPerCandle * 7 / 2592000 / 7));
                    str = "M";
                }
                String timeFrame = x + str;
                out.println("timeframe: " + timeFrame);
                String uriStr = "https://api.binance.us/api/v3/klines?symbol=" +
                        tradePair + "&interval=" + timeFrame;
                if (startTime == EARLIEST_DATA) {
                    out.println("startTime: " + startTime + " is false");
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }

                HttpRequest.Builder req=
                        HttpRequest.newBuilder()
                              .uri(URI.create(uriStr))
                              .header("Accept", "application/json")
                              .header("X-MBX-APIKEY", "9jnerlff23u8ed01np9g6ysbhsh0dvcs")
                              .header("Content-Type", "application/json")
                              .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");

                return HttpClient.newHttpClient().sendAsync(
                               req.build(),
                                HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenApply(response -> {
                            Log.info("Binance us Response: " , response);
                            JsonNode res;
                            try {
                                res = OBJECT_MAPPER.readTree(response);
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }

                            if (!res.isEmpty()) {
                                // Remove the current in-progress candle
                                if (res.get(0).asInt() + secondsPerCandle > endTime.get()) {
                                    ((ArrayNode) res).remove(0);
                                }
                                List<CandleData> candleData = new ArrayList<>();

                                for (JsonNode candle : res) {
                                    out.println("JSON " + candle);
                                    candleData.add(new CandleData(candle.get(1).asDouble(),  // open price
                                            candle.get(4).asDouble(),  // close price
                                            candle.get(2).asDouble(),  // high price
                                            candle.get(3).asDouble(),  // low price
                                            (int) Long.parseLong( candle.get(0).asText()),     // open time
                                            candle.get(5).asDouble())   // volume
                                    );
                                    endTime.set(candle.get(0).asInt());
                                    Log.info("Candle D binance " + candleData, news.toString());
                                }
                                candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                                return candleData;
                            } else {
                                return Collections.emptyList();
                            }

                        });
            }
        }

}