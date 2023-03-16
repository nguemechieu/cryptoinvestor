package tradeexpert.tradeexpert.oanda;

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
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;


public class Oanda extends Exchange {


    private static String accountID;
    protected String API_KEY;

    //"wss://api-fxtrade.oanda.com/v3/accounts/";
    private static final Accounts accounts = new Accounts();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    public Oanda(String api_key, String accountID,String token) throws IOException, ParseException, InterruptedException, TelegramApiException {
        super("wss://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/pricing/stream", token);
        this.API_KEY = api_key;
        Oanda.accountID = accountID;
       accounts.setAccountID(accountID);



        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);



//        HTTP/1.1 200 OK
//        Access-Control-Allow-Headers: Authorization, Content-Type, Accept-Datetime-Format
//        Content-Encoding: gzip
//        Transfer-Encoding: chunked
//        Server: openresty/1.7.0.1
//        Connection: keep-alive
//        Link: <https://api-fxtrade.oanda.com/v3/accounts/<ACCOUNT>/trades?beforeID=6397&instrument=USD_CAD>; rel="next"
//        Date: Wed, 22 Jun 2016 18:41:48 GMT
//        Access-Control-Allow-Origin: *
//        Access-Control-Allow-Methods: PUT, PATCH, POST, GET, OPTIONS, DELETE
//        Content-Type: application/json
           requestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.header("Accept-Datetime-Format", "Wed, 22 Jun 2016 18:41:48 GMT");
           requestBuilder.header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept-Datetime-Format");

        requestBuilder.header("Server", "openresty/1.7.0.1");

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        requestBuilder.header("Content-Encoding", "gzip");





    }

    public static double getMarginPercent() {
        return accounts.getMarginPercent();
    }

    public static double getBalance() {
        return accounts.getBalance();
    }

    public static double getOpen() {
        return accounts.getOpen();
    }

    public static double getHigh() {
        return
                Math.max(accounts.getHigh(), accounts.getOpen());
    }

    public static double getLow() {
        return
                Math.min(accounts.getLow(), accounts.getOpen());
    }

    public static double getFreeMargin() {
        return
                Math.max(accounts.getFreeMargin(), accounts.getOpen());
    }

    public static double getProfit() {
        return accounts.getProfit();
    }

    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return
                new OandaCandleDataSupplier(secondsPerCandle, tradePair) {
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
                String uriStr = "https://api-fxtrade.oanda.com/";
                uriStr += "v3/accounts/" + accountID + "/trades?beforeID=63997&instrument="+ tradePair.toString('_');


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
                                "Oanda trades response did not contain header \"CB-AFTER\": " + response));
                        return;
                    }

                    afterCursor.setValue(Integer.valueOf((response.headers().firstValue("CB-AFTER").get())));

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


        String x;
        String str;
        if (secondsPerCandle < 3600) {
            x = String.valueOf(secondsPerCandle / 60);
            str = "M";
        } else if (secondsPerCandle < 86400) {
            x = String.valueOf((secondsPerCandle / 3600));
            str = "H";
        } else if (secondsPerCandle < 604800) {
            x = "";//String.valueOf(secondsPerCandle / 86400);
            str = "D";
        } else if (secondsPerCandle < 2592000) {
            x = String.valueOf((secondsPerCandle / 604800));
            str = "W";
        } else {
            x = String.valueOf((secondsPerCandle * 7 / 2592000 / 7));
            str = "M";
        }


        String granularity = str + x;
           return HttpClient.newHttpClient().sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create(String.format(
                                        "https://api-fxtrade.oanda.com/v3/accounts/"+accountID+"instrument/" + tradePair.toString('_') + "/candles?price=BA&from=2016-10-17T15%3A00%3A00.000000000Z&granularity=" + granularity




        , actualGranularity, startDateString)))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Oanda response: " , response);
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





    private @NotNull JSONObject getJSON(@NotNull TradePair tradePair) {

        JSONObject jsonObject = new JSONObject();
        try {
            URL url = new URL("https://api-fxtrade.oanda.com/v3/accounts/instruments/"+tradePair.toString('_')+"/candles?price=BA&from=2016-");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10)");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);//    API key as a string
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

    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(
                "Connection closed: " + code + " " + reason + " " + remote
        );


    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error");

    }

    public void createMarketOrder(@NotNull TradePair tradePair, String side, double size) {


        JSONObject jsonObject = getJSON(tradePair);

        String uriStr = "https://api.pro.coinbase.com/" +
                "products/" + tradePair.toString('_') + "/orders" +
                "?side=" + side +
                "&type=market" +
                "&quantity=" + size +
                "&price=" + jsonObject.getJSONObject("data").getJSONObject("rates").getDouble("USD");

        System.out.println(uriStr);




    }




    public static abstract class OandaCandleDataSupplier extends CandleDataSupplier {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        private static final int EARLIEST_DATA = 1422144000; // roughly the first trade

        OandaCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
            super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
        }


        @Override
        public List<CandleData> getCandleData() {
            return new ArrayList<>();
        }

        @Override
        public Future<List<CandleData>> get() {
            String uriStr ;/// "https://api-fxtrade.oanda.com/v3/accounts/" + accountID +
                    //"/trades/instruments" + tradePair.toString('_');
            uriStr="https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?beforeID=6397&instruments="+ tradePair.toString('_');
            requestBuilder.header("Link", "<https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?beforeID=6397&instrument="+ tradePair.toString('_')+"&rel=\"next\"");

            if (endTime.get() == -1) {
                endTime.set((int) (Instant.now().toEpochMilli() / 1000L));
            }

            String endDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .format(LocalDateTime.ofEpochSecond(endTime.get(), 0, ZoneOffset.UTC));

            int startTime = Math.max(endTime.get() - (numCandles * secondsPerCandle), EARLIEST_DATA);
            String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .format(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC));




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
                        Log.info("Oanda response-->: " , response);
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