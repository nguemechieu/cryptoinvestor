package cryptoinvestor.cryptoinvestor.oanda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cryptoinvestor.cryptoinvestor.Currency;
import cryptoinvestor.cryptoinvestor.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static javax.swing.UIManager.put;


public class Oanda extends Exchange {
    public static final Logger logger = LoggerFactory.getLogger(Oanda.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Set<TradePair> tradePair;

    static {

            tradePair = new HashSet<>(
                    List.of(
                            new TradePair("EUR", "USD")
                    )
            );

    }

    private static final ExchangeWebSocketClient websocke =
            new OandaWebSocket(
                    tradePair
            );
    public static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();


    public String getAccountID() {
        return accountID;
    }
    private String apiKey;
    private String accountID;
    private static final HttpClient client=         HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    ConcurrentHashMap<Order, Order> orders = new ConcurrentHashMap<>();

    @Contract(pure = true)
    public static @NotNull String getCoinbaseMessage() {
        return
                "{\"method\":\"get_exchange_rates\",\"params\":{},\"id\":1}";
    }

    @Override
    public String getName() {
        return
                "OANDA";
    }

    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return new OandaCandleDataSupplier(secondsPerCandle, tradePair) {
            @Override
            public CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
                return null;
            }

            @Override
            public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
                return null;
            }

            @Override
            public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
                return null;
            }
        };
    }

    @Override
    public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle() {
        return null;
    }

    @Override
    public Set<Integer> getSupportedGranularities() {
        return
                new HashSet<>(Arrays.asList(
                        60, 60 * 5, 60 * 30, 3600, 3600 * 2, 3600 * 4,
                        3600 * 6, 3600 * 12, 3600 * 24, 3600 * 24 * 7, 3600 * 24 * 7 * 4,
                        3600 * 24 * 7 * 4 * 12));

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

            String x;
            String str;
            int secondsPerCandle = getSupportedGranularities().iterator().next();
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


//
            out.println("timeframe: " + granularity);
            // For Public Endpoints, our rate limit is 3 requests per second, up to 6 requests per second in
            // burst.
            // We will know if we get rate limited if we get a 429 response code.
            for (int i = 0; !futureResult.isDone(); i++) {
                String uriStr = "https://api-fxtrade.oanda.com/v3";
                uriStr += "/instruments/" + tradePair.getBaseCurrency().getCode() + "/candles?count=6&price=M&granularity=" + granularity;

                if (i != 0) {
                    uriStr += "?after=" + afterCursor.get();
                }
                requestBuilder.uri(URI.create(uriStr));
                try {
                    HttpResponse<String> response = HttpClient.newHttpClient().send(requestBuilder.build()
                            ,
                            HttpResponse.BodyHandlers.ofString());

                    Log.info("response headers: ", response.headers().toString());
                    if (response.headers().firstValue("CB-AFTER").isEmpty()) {
                        futureResult.completeExceptionally(new RuntimeException(
                                "Oanda trades response did not contain header \"CB-AFTER\": " + response));
                        return;
                    }

                    afterCursor.setValue(Integer.valueOf((response.headers().firstValue("CB-AFTER").get())));

                    JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());

                    if (!tradesResponse.isArray()) {
                        futureResult.completeExceptionally(new RuntimeException("Oanda trades response was not an array!"));


                    } else if (tradesResponse.isEmpty()) {
                        futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
                    } else if (tradesResponse.has("message")) {


                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Oanda Error");
                        alert.setHeaderText("Oanda Error");
                        alert.setContentText(tradesResponse.get("message").asText());
                        alert.showAndWait();


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
                } catch (TelegramApiException | ParseException | URISyntaxException e) {
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
            @NotNull TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
        String startDateString1 = ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
                currentCandleStartedAt, ZoneOffset.UTC));
        long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
        // Get the closest supported granularity to the ideal granularity.
        int actualGranularity = getSupportedGranularities().stream()
                .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
                .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));

        return client.sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create("https://api-fxtrade.oanda.com/v3/instruments/"+tradePair.toString('_')+"/candles?count=6&price=M&granularity=M30"))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Coinbase response: ", response);
                    JsonNode res;
                    try {
                        res = OBJECT_MAPPER.readTree(response);
                        if (res.has("message")) {


                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Oanda Error");
                            alert.setHeaderText("Oanda Error");
                            alert.setContentText(res.get("message").asText());
                            alert.showAndWait();


                        }

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


    public Oanda(String apiKey, String accountID)
            throws TelegramApiException, IOException, NoSuchAlgorithmException {
        super(websocke);


        this.accountID = accountID;
        requestBuilder.header("Authorization", "Bearer " + apiKey);
        this.apiKey = apiKey;
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Origin", "https://api-fxtrade.oanda.com");
        requestBuilder.header("Referer", "https://api-fxtrade.oanda.com/v3/accounts/" + accountID );
        requestBuilder.header("Sec-Fetch-Dest", "empty");
        requestBuilder.header("Sec-Fetch-Mode", "cors");
        requestBuilder.header("Sec-Fetch-Site", "same-origin");
        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        requestBuilder.header("Access-Control-Allow-Credentials", "true");
        requestBuilder.header(
                "Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept"
        );
        requestBuilder.header(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );


        logger.info("Oanda initialized");

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
        System.out.println("Disconnected");


    }

    @Override
    public void onError(Exception ex) {
        System.out.println("Error");

    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public String getPrice() {
        return null;
    }

    @Override
    public String getVolume() {
        return null;
    }

    @Override
    public String getOpen() {
        return null;
    }

    @Override
    public String getHigh() {
        return null;
    }

    @Override
    public String getLow() {
        return null;
    }

    @Override
    public String getClose() {
        return null;
    }

    @Override
    public String getTimestamp() {
        return null;
    }

    @Override
    public String getTradeId() {
        return null;
    }

    @Override
    public String getOrderId() {
        return null;
    }

    @Override
    public String getTradeType() {
        return null;
    }

    @Override
    public String getSide() {
        return null;
    }

    @Override
    public String getExchange() {
        return null;
    }

    @Override
    public String getCurrency() {
        return null;
    }

    @Override
    public String getAmount() {
        return null;
    }

    @Override
    public String getFee() {
        return null;
    }

    @Override
    public String getAvailable() {
        return null;
    }

    @Override
    public String getBalance() {
        return null;
    }

    @Override
    public String getPending() {
        return null;
    }

    @Override
    public String getTotal() {
        return null;
    }

    @Override
    public String getDeposit() {
        return null;
    }

    @Override
    public String getWithdraw() {
        return null;
    }

    @Override
    public void deposit(Double value) {

    }

    @Override
    public void withdraw(Double value) {


    }

    public JSONObject getJSON(@NotNull TradePair tradePair) {

        JSONObject jsonObject = new JSONObject();
        try {
            String url = "https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?count=6&price=M&granularity=M30";
            HttpsURLConnection conn = (HttpsURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(
                    "Authorization",
                    "Bearer " + apiKey);

            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10)");
            String timestamp = new Date().toString();


            conn.setRequestProperty("CB-ACCESS-SIGN", timestamp + "GET" + url);
            //"base64-encoded signature (see Signing a Message)");
            conn.setRequestProperty("CB-ACCESS-TIMESTAMP", new Date().toString());//    Timestamp for your request


            conn.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);

            }
            in.close();

            out.println(response);
            //Put data into json file
            jsonObject = new JSONObject(response.toString());
            out.println(jsonObject.toString(4));


        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(jsonObject.toString(4));
        return jsonObject;
    }

    // Get all orders
    //       GET
    public Node getAllOrders() throws IOException, InterruptedException {
        String uriStr =
                "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders";


        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        JSONObject jsonObject = new JSONObject(response.body());
        if (response.statusCode() == 200) {

            System.out.println(jsonObject.toString(4));
        } else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
        }


        JSONArray dat = jsonObject.getJSONArray("orders");

        ObservableList<Order> ob =
                FXCollections.observableArrayList();
        for (int i = 0; i < dat.length(); i++) {
            JSONObject obj = dat.getJSONObject(i);

            String triggerCondition, createTime,//": "2023-03-30T20:00:00.583871679Z",
                    price,//": "1.08063",
                    clientTradeID,//": "140930333",

                    state,//": "PENDING",

                    timeInForce,//": "GTC",
                    tradeID;//": "14311
            Order order = new Order(
                    obj.getString("clientTradeID"),
                    obj.getString("triggerCondition"),
                    obj.getString("createTime"),
                    obj.getString("price"),
                    obj.getString("clientTradeID"),
                    obj.getString("state"),
                    obj.getString("timeInForce"),
                    obj.getString("tradeID")
            );
            orders.put(order, order);
            ob.add(order);
        }

        return new ListView<>(ob);


    }

    //Get Account
    @Override
    public Account getAccounts() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID));
        requestBuilder.GET();
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        JSONObject jsonObject = new JSONObject(response.body());
        if (response.statusCode() == 200) {

            // {"account":{"guaranteedStopLossOrderMode":"DISABLED","hedgingEnabled":false,"id":"001-001-2783446-002","createdTime":"2019-04-30T02:39:18.895364468Z","currency":"USD","createdByUserID":2783446,"alias":"MT4","marginRate":"0.02","lastTransactionID":"143166","balance":"51.4613","openTradeCount":1,"openPositionCount":1,"pendingOrderCount":0,"pl":"-914.0620","resettablePL":"-914.0620","resettablePLTime":"0","financing":"-13.1795","commission":"0.2672","dividendAdjustment":"0","guaranteedExecutionFees":"0.0000","orders":[],"positions":[{"instrument":"EUR_USD","long":{"units":"0","pl":"-99.1666","resettablePL":"-99.1666","financing":"-7.7397","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"-2300","averagePrice":"1.08814","pl":"-133.7496","resettablePL":"-133.7496","financing":"2.0142","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","tradeIDs":["143152"],"unrealizedPL":"8.7400"},"pl":"-232.9162","resettablePL":"-232.9162","financing":"-5.7255","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"8.7400","marginUsed":"49.8741"},{"instrument":"EUR_GBP","long":{"units":"0","pl":"0.3154","resettablePL":"0.3154","financing":"-0.0105","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.5702","resettablePL":"-0.5702","financing":"-0.0024","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2548","resettablePL":"-0.2548","financing":"-0.0129","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_AUD","long":{"units":"0","pl":"-7.9081","resettablePL":"-7.9081","financing":"-0.0016","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4026","resettablePL":"0.4026","financing":"0.0018","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-7.5055","resettablePL":"-7.5055","financing":"0.0002","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CAD","long":{"units":"0","pl":"-5.4214","resettablePL":"-5.4214","financing":"-0.5173","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.3465","resettablePL":"-1.3465","financing":"-0.0027","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.7679","resettablePL":"-6.7679","financing":"-0.5200","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_SGD","long":{"units":"0","pl":"-1.3650","resettablePL":"-1.3650","financing":"-0.0024","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.5949","resettablePL":"-1.5949","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.9599","resettablePL":"-2.9599","financing":"-0.0025","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CHF","long":{"units":"0","pl":"2.9766","resettablePL":"2.9766","financing":"-0.1421","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.2801","resettablePL":"0.2801","financing":"-0.0021","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"3.2567","resettablePL":"3.2567","financing":"-0.1442","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_JPY","long":{"units":"0","pl":"-1.0765","resettablePL":"-1.0765","financing":"-0.0975","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-10.0630","resettablePL":"-10.0630","financing":"-0.0131","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.1395","resettablePL":"-11.1395","financing":"-0.1106","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_NZD","long":{"units":"0","pl":"2.8772","resettablePL":"2.8772","financing":"-0.2898","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-138.8771","resettablePL":"-138.8771","financing":"0.6645","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-135.9999","resettablePL":"-135.9999","financing":"0.3747","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1659","resettablePL":"-0.1659","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1659","resettablePL":"-0.1659","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CZK","long":{"units":"0","pl":"-2.1723","resettablePL":"-2.1723","financing":"-0.0078","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1557","resettablePL":"-0.1557","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.3280","resettablePL":"-2.3280","financing":"-0.0078","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_NOK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1526","resettablePL":"-0.1526","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1526","resettablePL":"-0.1526","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_SEK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0180","resettablePL":"-0.0180","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0180","resettablePL":"-0.0180","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_TRY","long":{"units":"0","pl":"-0.0959","resettablePL":"-0.0959","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0959","resettablePL":"-0.0959","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_ZAR","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0682","resettablePL":"-0.0682","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0682","resettablePL":"-0.0682","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CAD","long":{"units":"0","pl":"-29.8908","resettablePL":"-29.8908","financing":"-0.9440","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-44.8502","resettablePL":"-44.8502","financing":"-0.1419","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-74.7410","resettablePL":"-74.7410","financing":"-1.0859","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_SGD","long":{"units":"0","pl":"0.6893","resettablePL":"0.6893","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"0.6893","resettablePL":"0.6893","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CHF","long":{"units":"0","pl":"-12.2150","resettablePL":"-12.2150","financing":"0.3309","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"1.8985","resettablePL":"1.8985","financing":"-0.3812","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-10.3165","resettablePL":"-10.3165","financing":"-0.0503","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_JPY","long":{"units":"0","pl":"-84.0042","resettablePL":"-84.0042","financing":"0.3929","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-35.5903","resettablePL":"-35.5903","financing":"-1.7775","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-119.5945","resettablePL":"-119.5945","financing":"-1.3846","commission":"0.1172","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_HKD","long":{"units":"0","pl":"-0.3687","resettablePL":"-0.3687","financing":"-0.1380","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0547","resettablePL":"0.0547","financing":"-0.0055","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.3140","resettablePL":"-0.3140","financing":"-0.1435","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CZK","long":{"units":"0","pl":"-2.7163","resettablePL":"-2.7163","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.7163","resettablePL":"-2.7163","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_DKK","long":{"units":"0","pl":"-0.0077","resettablePL":"-0.0077","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0077","resettablePL":"-0.0077","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_MXN","long":{"units":"0","pl":"-0.0951","resettablePL":"-0.0951","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0564","resettablePL":"-0.0564","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1515","resettablePL":"-0.1515","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_NOK","long":{"units":"0","pl":"-0.0108","resettablePL":"-0.0108","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0108","resettablePL":"-0.0108","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_PLN","long":{"units":"0","pl":"-0.0991","resettablePL":"-0.0991","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0991","resettablePL":"-0.0991","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_SEK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0132","resettablePL":"-0.0132","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0132","resettablePL":"-0.0132","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_THB","long":{"units":"0","pl":"-0.1990","resettablePL":"-0.1990","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1990","resettablePL":"-0.1990","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CNH","long":{"units":"0","pl":"-1.0486","resettablePL":"-1.0486","financing":"-0.0074","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-10.7225","resettablePL":"-10.7225","financing":"-0.0092","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.7711","resettablePL":"-11.7711","financing":"-0.0166","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_USD","long":{"units":"0","pl":"-10.0528","resettablePL":"-10.0528","financing":"-0.8398","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-23.1159","resettablePL":"-23.1159","financing":"-0.0970","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-33.1687","resettablePL":"-33.1687","financing":"-0.9368","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_AUD","long":{"units":"0","pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0075","resettablePL":"-0.0075","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1144","resettablePL":"-0.1144","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_CAD","long":{"units":"0","pl":"-5.2250","resettablePL":"-5.2250","financing":"-0.0355","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-3.0620","resettablePL":"-3.0620","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-8.2870","resettablePL":"-8.2870","financing":"-0.0355","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_SGD","long":{"units":"0","pl":"-0.8250","resettablePL":"-0.8250","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.8250","resettablePL":"-0.8250","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_CHF","long":{"units":"0","pl":"-4.4219","resettablePL":"-4.4219","financing":"0.0005","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1570","resettablePL":"-0.1570","financing":"-0.0687","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-4.5789","resettablePL":"-4.5789","financing":"-0.0682","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_JPY","long":{"units":"0","pl":"-9.7444","resettablePL":"-9.7444","financing":"-0.0043","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4019","resettablePL":"0.4019","financing":"-0.0653","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-9.3425","resettablePL":"-9.3425","financing":"-0.0696","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_NZD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2934","resettablePL":"-0.2934","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2934","resettablePL":"-0.2934","financing":"0.0000","commission":"0.1500","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_PLN","long":{"units":"0","pl":"-0.5359","resettablePL":"-0.5359","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2639","resettablePL":"-0.2639","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.7998","resettablePL":"-0.7998","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_ZAR","long":{"units":"0","pl":"-0.0420","resettablePL":"-0.0420","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0168","resettablePL":"-0.0168","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0588","resettablePL":"-0.0588","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_USD","long":{"units":"0","pl":"-26.7262","resettablePL":"-26.7262","financing":"-0.6898","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-43.7439","resettablePL":"-43.7439","financing":"-0.1078","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-70.4701","resettablePL":"-70.4701","financing":"-0.7976","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_CAD","long":{"units":"0","pl":"-2.9320","resettablePL":"-2.9320","financing":"-0.0339","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-18.4886","resettablePL":"-18.4886","financing":"-0.0171","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-21.4206","resettablePL":"-21.4206","financing":"-0.0510","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_SGD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-2.6495","resettablePL":"-2.6495","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.6495","resettablePL":"-2.6495","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_CHF","long":{"units":"0","pl":"-8.4263","resettablePL":"-8.4263","financing":"0.0082","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.2083","resettablePL":"0.2083","financing":"-0.0090","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-8.2180","resettablePL":"-8.2180","financing":"-0.0008","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_JPY","long":{"units":"0","pl":"-2.4061","resettablePL":"-2.4061","financing":"-0.1419","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-9.0972","resettablePL":"-9.0972","financing":"-0.6077","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.5033","resettablePL":"-11.5033","financing":"-0.7496","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_NZD","long":{"units":"0","pl":"-6.1148","resettablePL":"-6.1148","financing":"-0.0231","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-11.3032","resettablePL":"-11.3032","financing":"-0.0377","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-17.4180","resettablePL":"-17.4180","financing":"-0.0608","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0008","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0008","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_SGD","long":{"units":"0","pl":"0.0036","resettablePL":"0.0036","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2758","resettablePL":"-0.2758","financing":"-0.0006","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2722","resettablePL":"-0.2722","financing":"-0.0006","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_CHF","long":{"units":"0","pl":"-2.7829","resettablePL":"-2.7829","financing":"0.0009","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-2.8108","resettablePL":"-2.8108","financing":"-0.0095","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-5.5937","resettablePL":"-5.5937","financing":"-0.0086","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_JPY","long":{"units":"0","pl":"-2.5502","resettablePL":"-2.5502","financing":"0.0110","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-4.1264","resettablePL":"-4.1264","financing":"-0.0157","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.6766","resettablePL":"-6.6766","financing":"-0.0047","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.9540","resettablePL":"-0.9540","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.9540","resettablePL":"-0.9540","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"SGD_CHF","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.8765","resettablePL":"-1.8765","financing":"-0.0040","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.8765","resettablePL":"-1.8765","financing":"-0.0040","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"SGD_JPY","long":{"units":"0","pl":"-11.6988","resettablePL":"-11.6988","financing":"-0.0266","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4127","resettablePL":"0.4127","financing":"-0.0052","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.2861","resettablePL":"-11.2861","financing":"-0.0318","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CHF_JPY","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0281","resettablePL":"0.0281","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"0.0281","resettablePL":"0.0281","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CHF_ZAR","long":{"units":"0","pl":"-0.0021","resettablePL":"-0.0021","financing":"-0.0002","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0021","resettablePL":"-0.0021","financing":"-0.0002","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_USD","long":{"units":"0","pl":"-33.4490","resettablePL":"-33.4490","financing":"-0.5699","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-35.2630","resettablePL":"-35.2630","financing":"-0.7206","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-68.7120","resettablePL":"-68.7120","financing":"-1.2905","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_CAD","long":{"units":"0","pl":"-7.7127","resettablePL":"-7.7127","financing":"-0.0620","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"3.8895","resettablePL":"3.8895","financing":"-0.1901","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-3.8232","resettablePL":"-3.8232","financing":"-0.2521","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_CHF","long":{"units":"0","pl":"-5.9925","resettablePL":"-5.9925","financing":"0.0123","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.4028","resettablePL":"-0.4028","financing":"-0.0003","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.3953","resettablePL":"-6.3953","financing":"0.0120","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_JPY","long":{"units":"0","pl":"-2.0866","resettablePL":"-2.0866","financing":"0.0016","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.3974","resettablePL":"0.3974","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.6892","resettablePL":"-1.6892","financing":"0.0016","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_HKD","long":{"units":"0","pl":"0.0232","resettablePL":"0.0232","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0515","resettablePL":"-0.0515","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0283","resettablePL":"-0.0283","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"TRY_JPY","long":{"units":"0","pl":"-1.1650","resettablePL":"-1.1650","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.1650","resettablePL":"-1.1650","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"}],"trades":[{"id":"143152","instrument":"EUR_USD","price":"1.08814","openTime":"2023-03-31T13:18:05.967088687Z","initialUnits":"-2300","initialMarginRequired":"50.0581","state":"OPEN","currentUnits":"-2300","realizedPL":"0.0000","financing":"0.0619","dividendAdjustment":"0.0000","clientExtensions":{"id":"140953421","tag":"0"},"unrealizedPL":"8.7400","marginUsed":"49.8741"}],"unrealizedPL":"8.7400","NAV":"60.2013","marginUsed":"49.8741","marginAvailable":"10.3272","positionValue":"2493.7060","marginCloseoutUnrealizedPL":"9.0160","marginCloseoutNAV":"60.4773","marginCloseoutMarginUsed":"49.8741","marginCloseoutPositionValue":"2493.7060","marginCloseoutPercent":"0.41234","withdrawalLimit":"10.3272","marginCallMarginUsed":"49.8741","marginCallPercent":"0.82467"},"lastTransactionID":"143166"}


            Account account = new Account(jsonObject.getJSONObject("account"));


            System.out.println(jsonObject.getJSONObject("account").getString("currency"));


        }
        return null;
    }


    //  Get single order
    //      GET
    //https://api-fxtrade.oanda.com/v3/accounts/orders/{order_id}


    public  void getOrder(String orderId) throws IOException, InterruptedException {
        String uriStr = "https://api-fxtrade.oanda.com/v3/account/orders/" + orderId;

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            System.out.println(jsonObject.toString(4));

            System.out.println(jsonObject.getJSONObject("data").getString("status"));
        } else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
            logger.error("Error getting order: " + response.body());
        }

    }
    // Cancel an order
    //       DELETE


    public void cancelOrder(String orderId) throws IOException, InterruptedException {

        String uriStr =
                "https://api-fxtrade.oanda.com/v3/accounts/orders/" + orderId + "/cancel";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.DELETE();
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            System.out.println(jsonObject.toString(4));
        }
        else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
        }
    }
    private void  getOrderHistory(@NotNull TradePair tradePair) throws IOException, InterruptedException {
        String symbol = tradePair.toString('_');
        String uriStr =
                "https://api-fxtrade.oanda.com/v3/accounts/orders?product_id=" + symbol + "&limit=100";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            System.out.println(jsonObject.toString(4));
        } else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
        }
    }


    public void createOrder(@NotNull TradePair tradePair, @NotNull Side side, @NotNull ENUM_ORDER_TYPE orderType, double price, double size,
                            @NotNull Date timestamp, double stopLoss, double takeProfit, double takeProfitPrice) throws IOException, InterruptedException {
        // JSONObject jsonObject = getJSON();
        //   System.out.println(jsonObject.toString(4));

        String symbol = tradePair.toString('_');

        String uriStr = "https://api-fxtrade.oanda.com/v3/account/orders";

        String data =
                String.format(
                        "{\"product_id\": \"%s\", \"side\": \"%s\", \"type\": \"%s\", \"quantity\": %f, \"price\": %f, \"stop-loss\": %f, \"take-profit\": %f, \"take-profit-price\": %f, \"timestamp\": \"%s\"}",
                        symbol, side, orderType, size, price, stopLoss, takeProfit, takeProfitPrice,
                        timestamp);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        data = String.format(data, orderType, side, price);

        System.out.println(uriStr);
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                data
        ));
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();

        }


    }


    public void createOrder(@NotNull TradePair tradePair, Side buy, ENUM_ORDER_TYPE trailingStopSell, double quantity, int i, @NotNull Date timestamp, long orderID, double stopPrice, double takeProfitPrice) throws IOException, InterruptedException {
        JSONObject jsonObject = getJSON(tradePair);
        System.out.println(jsonObject.toString(4));

        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders";
        String params = String.format(
                "{\"order_id\": \"%s\", \"side\": \"%s\", \"type\": \"%s\", \"quantity\": %f, \"stop-price\": %f, \"take-profit-price\": %f, \"timestamp\": \"%s\", \"order-id\": %d}",
                tradePair.toString('_'), buy, trailingStopSell, quantity, stopPrice, takeProfitPrice,
                timestamp, orderID);


        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                params
        ));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }

    public void createOrder(TradePair tradePair, POSITION_FILL defaultFill, double price, ENUM_ORDER_TYPE market, Side buy, double quantity, double stopPrice, double takeProfitPrice) {
    }

    @Override
    public void closeAllOrders() {

    }

    @Override
    public List<TradePair> getTradePair() throws IOException, InterruptedException, ParseException, URISyntaxException {
  ArrayList<TradePair> tradePairs = new ArrayList<>();

  for (Currency currency : getAvailableSymbols()) {
      TradePair tradePair = new TradePair(currency.getSymbol(), "USD");
      tradePairs.add(tradePair);

  }
  return tradePairs;
    }

    @Override
    public void connect(String text, String text1, String userIdText) {
        System.out.println(text);
        System.out.println(text1);
        System.out.println(userIdText);
        this.apiKey = text;

        this.accountID = userIdText;

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void cancelOrder(long orderID) throws IOException, InterruptedException {

    }

    @Override
    public void cancelAllOrders() {

    }

    @Override
    public void cancelAllOpenOrders() {

    }

    @Override
    public ListView<Order> getOrderView() throws IOException, InterruptedException, ParseException, URISyntaxException {
        return null;
    }

//    @Override
//    public ListView<Order> getOrderView() throws IOException, ParseException, URISyntaxException, InterruptedException {
//        ListView<Order> orderView = new ListView<>();
//        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/"+accountID+"/orders";
//        System.out.println(uriStr);
//        requestBuilder.uri(URI.create(uriStr));
//        requestBuilder.GET();
//        HttpResponse<String> response;
//        try {
//            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//
//            throw new RuntimeException(e);
//        }
//        System.out.println(response.statusCode());
//        System.out.println(response.body());
//        if (response.statusCode()!= 200) {
//            System.out.println(response.statusCode());
//            System.out.println(response.body());
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText(null);
//            alert.setContentText(response.body());
//            alert.showAndWait();
//        }else {
//            JSONObject jsonObject = new JSONObject(response.body());
//            JSONArray jsonArray = jsonObject.getJSONArray("orders");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                Order order = new Order(jsonArray.getJSONObject(i));
//                orderView.getItems().add(order);
//            }
//        }
//
//        return orderView;
//    }

    @Override
    public List<Objects> getOrderBook() {
        return null;
    }


    public @NotNull List<Currency> getAvailableSymbols() {
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instruments";
        System.out.println(uriStr);

        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.GET();
        HttpResponse<String> response;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());


//        "instruments": [
//        {
//            "displayName": "USD/THB",
//                "displayPrecision": 3,
//                "marginRate": "0.05",
//                "maximumOrderUnits": "100000000",
//                "maximumPositionSize": "0",
//                "maximumTrailingStopDistance": "100.000",
//                "minimumTradeSize": "1",
//                "minimumTrailingStopDistance": "0.050",
//                "name": "USD_THB",
//                "pipLocation": -2,
//                "tradeUnitsPrecision": 0,
//                "type": "CURRENCY"
//        },

Instruments instruments = new Instruments(
        "USD_THB",
        "CURRENCY",
        "USD/THB",
        -2,
        0,
        100000000,
        1,
        5,
        50,
        100.000,
        100.000,
        5,
        "",
        ""
);
        out.println("Instruments " + instruments);
ArrayList<Currency> instrumentsList = new ArrayList<>();

        if (response.statusCode()!= 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());

        }else {


            JsonNode jsonNode ;
            try {
                jsonNode = new ObjectMapper().readTree(response.body());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode instrumentsNode = jsonNode.get("instruments");
           // String name, String type, String displayName, int pipLocation, int displayPrecision, int tradeUnitsPrecision, int minimumTradeSize, int maximumTrailingStopDistance, int minimumTrailingStopDistance, double maximumPositionSize, double maximumOrderUnits, double marginRate, String guaranteedStopLossOrderMode, String tags
            logger.info("Instruments:  " + instrumentsNode.toString());
            for(JsonNode instrumentNode : instrumentsNode) {
                instrumentsList.add(new Currency(CurrencyType.FIAT,
                        instrumentNode.get("displayName").asText(),
                        instrumentNode.get("name").asText(),
                        instrumentNode.get("name").asText(),
                        instrumentNode.get("displayPrecision").asInt(),
                        instrumentNode.get("name").asText(),""
                ) {
                    @Override
                    public int compareTo(@NotNull Currency o) {
                        return 0;
                    }

                    @Override
                    public int compareTo(java.util.@NotNull Currency o) {
                        return 0;
                    }
                });
            }

        }


        return instrumentsList;
    }

    public String getApiKey() {
        return apiKey;
    }

    static abstract class OandaCandleDataSupplier extends CandleDataSupplier {
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
            if (endTime.get() == -1) {
                endTime.set((int) (Instant.now().toEpochMilli() / 1000L));
            }

            String endDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .format(LocalDateTime.ofEpochSecond(endTime.get(), 0, ZoneOffset.UTC));

            int startTime = Math.max(endTime.get() - (numCandles * secondsPerCandle), EARLIEST_DATA);
            String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .format(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC));

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
            String uriStr = "https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?price=M&granularity=" + granularity +
                    "&from=" + startDateString + "&to=" + endDateString;
            logger.info("uriStr: " + uriStr);


            //    String uriStr ="https://api-fxtrade.oanda.com/v3/instruments/"+tradePair.toString('_')+"/candles?count=6&price=M&granularity="+granularity+"&from="+startDateString+"&to="+endDateString;

            if (startTime == EARLIEST_DATA) {
//                // signal more data is false
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            requestBuilder.uri(URI.create(uriStr));
            requestBuilder.GET();
            //requestBuilder.header("CB-AFTER", String.valueOf(afterCursor.get()));
            return client.sendAsync(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {

                        JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);
                            logger.info(
                                    "Oanda response :--> " + res.toString()
                            );


                            if (!res.isEmpty()) {
                                int time = (int) Date.from(Instant.parse(res.get("time").asText())).getTime();
                                logger.info("time: " + time);

                                // Remove the current in-progress candle
                                if (time + secondsPerCandle > endTime.get()) {
                                    ((ArrayNode) res).remove(0);
                                }
                                endTime.set(startTime);
                                List<CandleData> candleData = new ArrayList<>();
                                for (JsonNode candle : res.get("candles").get("mid")) {
                                    candleData.add(new CandleData(
                                            candle.get("o").asDouble(),  // open price
                                            candle.get("c").asDouble(),  // close price
                                            candle.get("h").asDouble(),  // high price
                                            candle.get("l").asDouble(),  // low price
                                            time,     // open time
                                            res.get("candle").get("volume").asDouble()   // volume
                                    ));
                                    out.println("CandleData " + candleData);
                                    logger.info(
                                            "CandleData: " + candleData
                                    );
                                }

                                candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                                return candleData;
                            } else {
                                logger.info("Oanda Empty response");
                                return Collections.emptyList();
                            }
                        } catch (Exception ex) {
                           logger.error(ex.getMessage());
                            return Collections.emptyList();
                        }




                                            });
        }

        public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

        public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
    }

}