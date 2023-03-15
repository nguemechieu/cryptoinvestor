package tradeexpert.tradeexpert.Coinbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import tradeexpert.tradeexpert.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
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


public class Coinbase extends Exchange {


    public static final String API_URL = "https://api.coinbase.com/v2/exchange-rates?currency=BTC";
    public static final String API_VERSION = "v2";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String ur0 = "wss://ws-direct.exchange.coinbase.com";


    protected String PASSPHRASE = "w73hzit0cgl";
    protected String API_SECRET = "FEXDflwq+XnAU2Oussbk1FOK7YM6b9A4qWbCw0TWSj0xUBCwtZ2V0MVaJIGSjWWtp9PjmR/XMQoH9IZ9GTCaKQ==";
    String API_KEY0 = "39ed6c9ec56976ad7fcab4323ac60dac";



    static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

    public Coinbase() {
        super(ur0);



        requestBuilder.header("CB-ACCESS-KEY", API_KEY0);
        requestBuilder.header("CB-ACCESS-PASSPHRASE", PASSPHRASE);
       // requestBuilder.header("CB-ACCESS-SIGNATURE", timestampSignature(API_KEY0, PASSPHRASE));
        requestBuilder.header("CB-ACCESS-TIMESTAMP", Date.from(Instant.now()).toString());
        requestBuilder.header("CB-ACCESS-VERSION", API_VERSION);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        requestBuilder.header("Origin", "https://www.coinbase.com");
        requestBuilder.header("Referer", "https://www.coinbase.com/");
        requestBuilder.header("Sec-Fetch-Dest", "empty");
        requestBuilder.header("Sec-Fetch-Mode", "cors");

    }

    @Override
    public CoinbaseCandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return
                new CoinbaseCandleDataSupplier(secondsPerCandle, tradePair) {
                    @Override
                    public CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
                        return null;
                    }

                    @Override
                    public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
                        return null;
                    }
                };
    }

//    private @Nullable String timestampSignature(
//            String apiKey,
//            String passphrase
//    ) {
//        Objects.requireNonNull(apiKey);
//        Objects.requireNonNull(passphrase);
//
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
//        String stringToSign = timestamp + "\n" + apiKey + "\n" + passphrase;
//
//        try {
//            byte[] hash = MessageDigest.getInstance("SHA-256").digest(stringToSign.getBytes());
//            return Base64.getEncoder().encodeToString(hash);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//
//    }



    /**
     * Fetches the recent trades for the given trade pair from  {@code stopAt} till now (the current time).
     * <p>
     * This method only needs to be implemented to support live syncing.
     */
    @Override
    public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(stopAt);

        if (stopAt.isAfter(Instant.now())) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        CompletableFuture<List<Trade>> futureResult = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            IntegerProperty afterCursor = new SimpleIntegerProperty(0);
            List<Trade> tradesBeforeStopTime = new ArrayList<>();

            // For Public Endpoints, our rate limit is 3 requests per second, up to 6 requests per second in
            // burst.
            // We will know if we get rate limited if we get a 429 response code.
            for (int i = 0; !futureResult.isDone(); i++) {
                String uriStr = "https://api.pro.coinbase.com/";
                uriStr += "products/" + tradePair.toString('-') + "/trades";

                if (i != 0) {
                    uriStr += "?after=" + afterCursor.get();
                }
                requestBuilder.uri(URI.create(uriStr));
                try {
                    HttpResponse<String> response = HttpClient.newHttpClient().send(requestBuilder.build()
                            ,
                            HttpResponse.BodyHandlers.ofString());

                    Log.info("response headers: " , response.headers().toString());
                    if (response.headers().firstValue("CB-AFTER").isEmpty()) {
                        futureResult.completeExceptionally(new RuntimeException(
                                "Coinbase trades response did not contain header \"CB-AFTER\": " + response));
                        return;
                    }

                    afterCursor.setValue(Integer.valueOf((response.headers().firstValue("CB-AFTER").get())));

                    JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());

                    if (!tradesResponse.isArray()) {
                        futureResult.completeExceptionally(new RuntimeException(
                                "coinbase trades response was not an array!"));
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
                                        DefaultMoney.ofFiat(trade.get("price").asText(), String.valueOf(tradePair.getCounterCurrency())),
                                        DefaultMoney.ofCrypto(trade.get("size").asText(), String.valueOf(tradePair.getBaseCurrency())),
                                        Side.getSide(trade.get("side").asText()), trade.get("trade_id").asLong(), time));
                            }
                        }
                    }
                } catch (IOException | InterruptedException ex) {
                    Log.error("ex: " + ex);
                    futureResult.completeExceptionally(ex);
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
            TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
        String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
                currentCandleStartedAt, ZoneOffset.UTC));
        long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
        // Get the closest supported granularity to the ideal granularity.
        int actualGranularity = getCandleDataSupplier(secondsPerCandle, tradePair).getSupportedGranularities().stream()
                .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
                .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));
        // TODO: If actualGranularity = secondsPerCandle there are no sub-candles to fetch and we must get all the
        //  data for the current live syncing candle from the raw trades method.
        return HttpClient.newHttpClient().sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create(String.format(
                                        "https://api.pro.coinbase.com/products/%s/candles?granularity=%s&start=%s",
                                        tradePair.toString('-'), actualGranularity, startDateString)))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Coinbase response: " , response);
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
                              //  Coinbase is  not respecting start/end times
                            continue;

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





    private @NotNull JSONObject getJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            URL url = new URL("https://api.coinbase.com/v2/exchange-rates");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10)");
            conn.setRequestProperty("CB-ACCESS-KEY", API_KEY0);//    API key as a string
            String timestamp = new Date().toString();

            conn.setRequestProperty("CB-ACCESS-SIGN", timestamp + "GET" + url);
            //"base64-encoded signature (see Signing a Message)");
            conn.setRequestProperty("CB-ACCESS-TIMESTAMP", new Date().toString());//    Timestamp for your request


            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine())!= null) {
                response.append(inputLine);

            }
            in.close();

            out.println(response);
            //Put data into json file
            jsonObject = new JSONObject(response.toString());
            out.println(jsonObject.toString(4));

            String rates;
            if (jsonObject.has("data")) {
                JSONObject dat =new JSONObject(jsonObject.getJSONObject("data").toString(4));
                if (dat.has("rates")) {
                    rates=dat.getJSONObject("rates").toString(4);
                    out.println(rates);
                }

            }







        }
        catch (IOException e) {
            e.printStackTrace();
        }
        out.println(jsonObject.toString(4));
        return jsonObject;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {


        System.out.println("Connected");
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4) + " "+ handshake);

    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4) );

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected");
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4));

    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error");
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4));

    }

    public void createMarketOrder(TradePair tradePair, String side, double size) {
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4));

        String uriStr = "https://api.pro.coinbase.com/" +
                "products/" + tradePair + "/orders" +
                "?side=" + side +
                "&type=market" +
                "&quantity=" + size +
                "&price=" + jsonObject.getJSONObject("data").getJSONObject("rates").getDouble("USD");

        System.out.println(uriStr);




    }



    public static abstract class CoinbaseCandleDataSupplier extends CandleDataSupplier {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        private static final int EARLIEST_DATA = 1422144000; // roughly the first trade

        CoinbaseCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
            super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
        }


        @Override
        public List<CandleData> getCandleData() {
            return new ArrayList<>();
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

            String uriStr = "https://api.pro.coinbase.com/" +
                    "products/" + tradePair.toString('-') + "/candles" +
                    "?granularity=" + secondsPerCandle +
                    "&start=" + startDateString +
                    "&end=" + endDateString;

            if (startTime == EARLIEST_DATA) {
                // signal more data is false
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            requestBuilder.uri(URI.create(uriStr));
            //requestBuilder.header("CB-AFTER", String.valueOf(afterCursor.get()));
            return HttpClient.newHttpClient().sendAsync(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                        Log.info("coinbase response: " , response);
                        JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }

                        if (!res.isEmpty()) {
                            // Remove the current in-progress candle
                            if (res.get(0).get(0).asInt() + secondsPerCandle > endTime.get()) {
                                ((ArrayNode) res).remove(0);
                            }
                            endTime.set(startTime);

                            List<CandleData> candleData = new ArrayList<>();
                            for (JsonNode candle : res) {
                                candleData.add(new CandleData(
                                        candle.get(3).asDouble(),  // open price
                                        candle.get(4).asDouble(),  // close price
                                        candle.get(2).asDouble(),  // high price
                                        candle.get(1).asDouble(),  // low price
                                        candle.get(0).asInt(),     // open time
                                        candle.get(5).asDouble()   // volume
                                ));
                            }
                            candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                            return candleData;
                        } else {
                            return Collections.emptyList();
                        }
                    });
        }

        public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

        public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
    }

}