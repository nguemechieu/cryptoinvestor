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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import org.java_websocket.handshake.ServerHandshake;
import org.tradeexpert.tradeexpert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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


public class Binance extends Exchange {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String apiKey;

    public static final String API_URL = "https://api.binance.com";
    public static final String API_VERSION = "v1";


    String apiSecret;
    String apiPass;


    public Binance(
            String apiKey,
            String apiSecret,
            String apiPass
    ) throws Exception {

        if (apiKey == null || apiSecret == null || apiPass == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill in all the required fields",
                    ButtonType.OK);
            alert.setTitle("Binance");
            alert.setHeaderText("Please fill in all the required fields");
            alert.showAndWait();

            throw new Exception("apiKey, apiSecret and apiPass are required");
        } else {
            Binance.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.apiPass = apiPass;
        }

    }

    public Binance() {

    }


    public static String getApiKey() {
        return apiKey;
    }


    public static String getVersion() {
        return API_VERSION;
    }


    public static void createMarketOrder(String tradePair, String type, String side, double size) {
    }

    public static void createMarketOrder(String tradePair, String side, double size) {
    }

    public Node start() {

        return new VBox();
    }

    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, String tradePair) {
        return null;
    }

    @Override
    public CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
        return null;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }


    abstract class BinanceU extends Exchange {   // private static final URI urO=URI.create("wss://stream.binance.us:9443");


        private static String x;
        private static String str;

        BinanceU() {
             // This argument is for creating a WebSocket client for live trading data.
        }

        @Override
        public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, String tradePair) {
            return new BinanceCandleDataSupplier(secondsPerCandle, tradePair);
        }

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
                        uriStr += "?after=" + afterCursor.get();
                    }
                    try {
                        HttpRequest.Builder req = HttpRequest.newBuilder();
                        req.header("Accept", "application/json");
                        req.header("Authorization", Binance.getApiKey());
                        req.uri(new URI(uriStr));
                        HttpResponse<String> response = HttpClient.newHttpClient().send(
                                req.build(),
                                HttpResponse.BodyHandlers.ofString());

                        Log.info("response headers: " + response.headers(), news.toString());
                        if (response.headers().firstValue("cb-after").isEmpty()) {
                            futureResult.completeExceptionally(new RuntimeException(
                                    "Binance Us trades response did not contain header \"cb-after\": " + response));
                            return;
                        }

                        afterCursor.setValue(Integer.valueOf((response.headers().firstValue(" cb-after").get())));

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
                                            DefaultMoney.ofFiat(trade.get("price").asText(), tradePair.substring(4, tradePair.length() - 1)),
                                            DefaultMoney.ofCrypto(trade.get("qty").asText(), tradePair.substring(0, 3)),
                                            Side.getSide(trade.get("isBuyerMaker").asText()), trade.get("id").asLong(), time));
//                                    "id": 981492,
//                                            "price": "0.00380100",
//                                            "qty": "0.22000000",
//                                            "quoteQty": "0.00083622",
//                                            "time": 1637128016269,
//                                            "isBuyerMaker": false,
//                                            "isBestMatch": true
                                }
                            }
                        }
                    } catch (IOException | InterruptedException ex) {
                        Log.error("ex: " + ex);
                    } catch (URISyntaxException e) {
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
            return HttpClient.newHttpClient().sendAsync(

                            HttpRequest.newBuilder()
                                    .uri(URI.create("https://api.binance.us/api/v3/klines?symbol=" + tradePair + "&interval=" + timeFrame))
                                    .GET().build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                        Log.info("Binance us Response: " + response, news.toString());
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

                                currentTill = currCandle.get(0).asInt();
                                lastTradePrice = currCandle.get(4).asDouble();
                                foundFirst = true;

                                continue;

                            } else {
                                if (!foundFirst) {
                                    // FIXME: Why are we only using the first sub-candle here?
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






        public static class BinanceCandleDataSupplier extends CandleDataSupplier {
            private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            private static final int EARLIEST_DATA = 1422144000; // roughly the first trade
            private static final Set<Integer> GRANULARITIES = Set.of(60, 60 * 5, 60 * 15, 60 * 30, 3600, 3600 * 2, 3600 * 3, 3600 * 4, 3600 * 6, 3600 * 24, 3600 * 24 * 7, 3600 * 24 * 7 * 4, 3600 * 24 * 365);

            BinanceCandleDataSupplier(int secondsPerCandle, String tradePair) {
                super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
            }

            @Override
            public Set<Integer> getSupportedGranularities() {
                return new TreeSet<>(GRANULARITIES);
            }

            @Override
            public List<CandleData> getCandleData() {
                return new ArrayList<>();
            }

            @Override
            public CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CandleData>>>>>>>>>>>> fetchCandleDataForInProgressCandle(String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
                return null;
            }

            @Override
            public CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt) {
                return null;
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

//

                Log.info("Start date: " + startDateString, news.toString())
                ;//
//                ;
                Log.info("End date: " + endDateString, news.toString());

                Log.info("TradePair " + tradePair, news.toString());
                Log.info("Second per Candle: " + secondsPerCandle, news.toString());

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
                    // signal more data is false

                    out.println("startTime: " + startTime + " is false");
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }

                return HttpClient.newHttpClient().sendAsync(
                                HttpRequest.newBuilder()
                                        .uri(URI.create(uriStr))
                                        .GET().build(),
                                HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenApply(response -> {
                            Log.info("Binance us Response: " + response, news.toString());
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
                                    //        JSON [1632614400000,"42695.8400","43957.8200","40192.1600","43216.3600","1119.97070800",1632700799999,"47701882.7039",50948,"514.17724000","21953536.9128","0"]
                                    candleData.add(new CandleData(candle.get(1).asDouble(),  // open price
                                            candle.get(4).asDouble(),  // close price
                                            candle.get(2).asDouble(),  // high price
                                            candle.get(3).asDouble(),  // low price
                                            candle.get(0).asInt(),     // open time
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
}