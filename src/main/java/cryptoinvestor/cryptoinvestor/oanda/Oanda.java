package cryptoinvestor.cryptoinvestor.oanda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cryptoinvestor.cryptoinvestor.*;
import cryptoinvestor.cryptoinvestor.Coinbase.Candle;
import cryptoinvestor.cryptoinvestor.Coinbase.Fill;
import cryptoinvestor.cryptoinvestor.Currency;
import cryptoinvestor.cryptoinvestor.Coinbase.TransactionSummary;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class Oanda extends Exchange {
    public static final Logger logger = LoggerFactory.getLogger(Oanda.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final List<String> tradePairs = getTradePair();
    static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    private final ArrayList<OrderCancelTransaction> getOrderCancelTransaction = new ArrayList<>();
    static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    String account_id;
    String url;
    ArrayList<Double> voulumeSoFarList = new ArrayList<>();
    String apiKey;
    //    Advanced Trade endpoint URL: /api/v3/brokerage/{resource}
//
//    API	Method	Resource	Required Scope
//    List Accounts	GET	/accounts	wallet:accounts:read
    private String api_secret;
    List<OrderCreateTransaction> getOrderCreateTransaction = new ArrayList<>();
    String orderBookUrl = "https://api-fxtrade.oanda.com/v3/instruments/USD_JPY/orderBook";
    String positionBookUrl = "https://api-fxtrade.oanda.com/v3/instruments/USD_JPY/positionBook";

    public Oanda(String account_id, String apiKey)
            throws TelegramApiException, IOException, NoSuchAlgorithmException, InterruptedException {
        super(webSocketUrl(account_id, apiKey));

        this.url = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id;
        this.account_id = account_id;
        requestBuilder.header("Authorization", "Bearer " + apiKey);
        this.apiKey = apiKey;
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Origin", "https://api-fxtrade.oanda.com");
        requestBuilder.header("Referer", "https://api-fxtrade.oanda.com/v3/accounts/" + account_id);
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

    private static @NotNull ExchangeWebSocketClient webSocketUrl(String accountId, String apiKey) {
        OandaWebSocket oandaWebSocket = new OandaWebSocket(accountId);
        oandaWebSocket.addHeader("Authorization", "Bearer " + apiKey);
        oandaWebSocket.addHeader("Content-Type:", "application/octet-stream");
        oandaWebSocket.addHeader("Origin", "https://api-fxtrade.oanda.com");
        oandaWebSocket.addHeader("Referer", "https://api-fxtrade.oanda.com/v3/accounts/" + accountId);
        oandaWebSocket.addHeader("Sec-Fetch-Dest", "empty");
        oandaWebSocket.addHeader("Sec-Fetch-Mode", "cors");
        oandaWebSocket.addHeader("Sec-Fetch-Site", "same-origin");
        oandaWebSocket.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        oandaWebSocket.addHeader("Access-Control-Allow-Credentials", "true");
        oandaWebSocket.addHeader(
                "Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept"
        );
        oandaWebSocket.addHeader(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        return oandaWebSocket;
    }

    //
//
//    API	Method	Resource	Required Scope
//    List Accounts	GET	/accounts	wallet:accounts:read
    public ArrayList<Account> listAccounts() throws IOException, InterruptedException {
        ArrayList<Account> accounts;
        requestBuilder.uri(URI.create(url));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            accounts = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Oanda: " + accounts.size());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );
            return null;
        }
        return accounts;
    }

    //    Get Order	GET	/orders/historical/{order_id}	wallet:transactions:read
    public ArrayList<Order> listOrdersHistorical() throws IOException, InterruptedException {
        ArrayList<Order> orders;

        requestBuilder.uri(URI.create(url + "/orders/historical/batch"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            orders = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Oanda: " + orders.size());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return orders;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Contract(pure = true)
    public @NotNull List<Account> getAccountsList() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(getUrl() + "/accounts"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (data.statusCode() != 200) {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );


        } else {
            logger.info("   Oanda: " + data.statusCode() + " " + data.body());
            List<Account> accounts = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });


            logger.info("Oanda: " + accounts.size());

            ListView<Account> accountsListView = new ListView<>();
            for (Account account : accounts) {
                accountsListView.getItems().add(account);
            }
            accountsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    try {
                        Account account = getAccount(newValue.getId());
                        Objects.requireNonNull(account).setBalance(newValue.getBalance());
                        account.setAvailable(newValue.getAvailable());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return accounts;
        }
        return new ArrayList<>();

    }
//    Get Transactions Summary	GET	/transaction_summary	wallet:transactions:read

    //    Get Account	GET	/accounts/:account_id	wallet:accounts:read
    @Nullable
    Account getAccount(String accountId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(accountId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Account account;
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            account = OBJECT_MAPPER.readValue(data.body(), Account.class);
            logger.info("Oanda: " + account.toString());
        } else {
            logger.error("Oana: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return account;
    }
//    See Also:
//
//    API Key Authentication
//    Pro API Mapping
//    Was this helpful?
//
//
//
//    Previous
//    Migrating from Pro
//            Next
//    Authenticating Messages
//    Advanced Trade Endpoi


    @Override
    public String getName() {
        return
                "Oanda";
    }

    @Override
    public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle() {
        return null;
    }

    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return new CoinbaseCandleDataSupplier(secondsPerCandle, tradePair) {
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

    //    Cancel Orders	POST	/orders/batch_cancel	wallet:buys:create
    public Order cancelOrder(String orderId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "/cancel"));

        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(orderId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Order order;
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            order = OBJECT_MAPPER.readValue(data.body(), Order.class);
            logger.info("Oanda: " + order.toString());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return order;
    }

    //    Get Product	GET	/products/{product_id}	wallet:user:read
    Product getProduct(String productId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "/instrument/" + productId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Product product;
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            product = OBJECT_MAPPER.readValue(data.body(), Product.class);
            logger.info("Oanda: " + product.toString());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return product;
    }

    //    Get Product Candles	GET	/products/{product_id}/candles	none
    List<Candle> getProductCandles(String productId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "/products/" + productId + "/candles"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Candle> candles;
        if (data.statusCode() == 200) {
            logger.info("OANDA: " + data.statusCode() + " " + data.body());
            candles = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Oanda: " + candles.size());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return candles;
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

    private @NotNull String granularityToString(int actualGranularity) {

        //"https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?count=6&price=M&granularity=" + granularity + "&from=" + "2016-10-17T15:16:40.000000000Z";

        String x;
        String str;
        if (actualGranularity < 3600) {
            x = String.valueOf(actualGranularity / 60);
            str = "M";
        } else if (actualGranularity < 86400) {
            x = String.valueOf((actualGranularity / 3600));
            str = "H";
        } else if (actualGranularity < 604800) {
            x = "";//String.valueOf(secondsPerCandle / 86400);
            str = "D";
        } else if (actualGranularity < 2592000) {
            x = String.valueOf((actualGranularity / 604800));
            str = "W";
        } else {
            x = String.valueOf((actualGranularity * 7 / 2592000 / 7));
            str = "M";
        }
        return str + x;

    }


    @Override
    public String getSymbol() {
        return tradePairs.toString();
    }

    TransactionSummary getTransactionSummary() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "/transaction_summary"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        TransactionSummary transactionSummary;
        if (data.statusCode() == 200) {
            logger.info("Oanda: " + data.statusCode() + " " + data.body());
            transactionSummary = OBJECT_MAPPER.readValue(data.body(), TransactionSummary.class);
            logger.info("Oanda: " + transactionSummary.toString());
        } else {
            logger.error("Oanda: " + data.statusCode() + " " + data.body());
            new Message(
                    Message.MessageType.ERROR,
                    data.statusCode() + " " + data.body()
            );

            return null;
        }
        return transactionSummary;
    }

    @Override
    public ExchangeWebSocketClient getWebsocketClient() {
        return null;
    }

    public Set<Integer> getSupportedGranularities() {


        return Set.of(60, 60 * 2, 60 * 5, 60 * 30, 3600, 3600 * 4, 3600 * 6, 3600 * 12, 3600 * 24 * 7, 3600 * 24 * 7 * 4);

    }

    /**
     * Fetches the recent trades for the given trade pair from  {@code stopAt} till now (the current time).
     * <p>
     * This method only needs to be implemented to support live syncing.
     */
    @Override
    public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt,
                                                                 boolean isAutoTrade) {
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

            String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id +
                    "/trades?instrument=" +
                    tradePair.toString('_');
            //  uriStr += "products/" + tradePair.toString('-') + "/trades";


            requestBuilder.uri(URI.create(uriStr));

            HttpResponse<String> response;
            try {
                response = client.send(requestBuilder.build()
                        ,
                        HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            Log.info("oanda response headers: ", response.headers().toString());
            if (response.headers().firstValue("date").isEmpty()) {
                futureResult.completeExceptionally(new RuntimeException(
                        "Oanda trades response did not contain header \"date\": " + response));

            }
            afterCursor.setValue((int) Date.from(Instant.parse(response.headers().firstValue("date").get())).getTime());
            JsonNode tradesResponse;
            try {
                tradesResponse = OBJECT_MAPPER.readTree(response.body());
                logger.info("Oanda trade: " + tradesResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (tradesResponse.get("trades").isEmpty()) {

                try {
                    double price = getLivePrice(tradePair);


                    double distance = (price - getLivePrice(tradePair) / 2);
                    double sl = price - distance;
                    double tp = price + distance;

                    createOrder(
                            tradePair, Side.BUY,
                            ENUM_ORDER_TYPE.STOP_LOSS,
                            price,
                            tradesResponse.get("size").get(0).asDouble(),
                            new Date(), sl, tp, 0


                    );
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
            } else if (tradesResponse.has("message")) {


                new Message(
                        Message.MessageType.ERROR,
                        tradesResponse.get("message").asText()
                );


            } else {

                for (int j = 0; j < tradesResponse.size(); j++) {
                    JsonNode trade = tradesResponse.get(j);
                    Instant time = Instant.from(ISO_INSTANT.parse(trade.get("time").asText()));
                    if (time.compareTo(stopAt) <= 0) {
                        futureResult.complete(tradesBeforeStopTime);
                        return;
                    } else {
                        try {


                            logger.info("Oanda trade: " + trade);
                            Trade trade0 = new Trade(tradePair,
                                    trade.get("price").asDouble(),
                                    trade.get("size").asDouble(),
                                    Side.getSide(trade.get("side").asText()),
                                    trade.get("lastTransactionID").asLong(), time.getEpochSecond());

                            trade0.setLastTransactionID(trade.get("lastTransactionID").asLong());
                            trade0.setPrice(trade.get("price").asDouble());
                            trade0.setSize(trade.get("units").asDouble());
                            trade0.setSide(Side.getSide(trade.get("side").asText()));
                            trade0.setInstrument(tradePair.toString('_'));
                            trade0.setTradeID(getOrderCreateTransaction.size() + 1);
                            trade0.setAccountID(account_id);
                            trade0.setTime(time.getEpochSecond());

                            trade0.run();
                            tradesBeforeStopTime.add(trade0);
                        } catch (IOException |
                                 InterruptedException |
                                 ParseException |
                                 URISyntaxException e) {
                            throw new RuntimeException(e);
                        }

                        logger.info("Oanda trade: " + trade);
                    }
                }
            }


//


        });
        return futureResult;

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

    /**
     * This method only needs to be implemented to support live syncing.
     */
    @Override
    public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
            TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
        String startDateString1 = ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
                currentCandleStartedAt, ZoneOffset.UTC));
        long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
        // Get the closest supported granularity to the ideal granularity.
        int actualGranularity = getSupportedGranularities().stream()
                .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
                .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));

        return client.sendAsync(
                        requestBuilder
                                .uri(URI.create(
                                        "https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') +
                                                "/candles?count=" + 200 + "&price=M&granularity=" + granularityToString(actualGranularity)
                                ))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Oanda response2: ", response);
                    JsonNode res;
                    try {
                        res = OBJECT_MAPPER.readTree(response);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }

                    if (res.isEmpty()) {
                        return Optional.empty();
                    }


                    // Iterator<JsonNode> candleItr = res.elements();

                    logger.info("currCandle: " + res.elements());
                    int currentTill = -1;
                    double openPrice = -1;
                    double highSoFar = -1;
                    double lowSoFar = Double.MAX_VALUE;
                    double volumeSoFar = 0;
                    double lastTradePrice = -1;

                    int openTime = 0;

                    for (JsonNode res0 : res.get("candles")) {
                        for (JsonNode currCandle : res0.get("mid")) {

                            // logger.info("currCandle: " + candleItr.next().toString());


                            if (res0.has("time")) {

                                logger.info("currCandle: " + currCandle + " ");
                                int time = Instant.from(ISO_INSTANT.parse(res0.get("time").asText())).getNano();
                                logger.info("time: " + Date.from(Instant.ofEpochSecond(
                                        res0.findValue("time").asLong() / 1000L)));


                                currentTill = time;
                                if (currCandle.has("c")) lastTradePrice = currCandle.findValue("c").asDouble();

                                if (currCandle.has("o")) openPrice = currCandle.findValue("o").asDouble();

                                if (openPrice > highSoFar) {
                                    highSoFar = openPrice;
                                }
                                if (currCandle.has("l")) lowSoFar = currCandle.findValue("l").asDouble();

                                if (currCandle.has("o")) {
                                    if (currCandle.findValue("o").asDouble() < lowSoFar) {
                                        lowSoFar = currCandle.findValue("o").asDouble();
                                    }
                                }

                                volumeSoFar += res0.findValue("volume").asDouble();

                                openTime = (int) (currentCandleStartedAt.toEpochMilli() / 1000L);

                            } else {
                                logger.info("No time in response " + currCandle);
                                return Optional.empty();
                            }
                        }
                    }
                    return Optional.of(new InProgressCandleData(openTime, openPrice, highSoFar, lowSoFar,
                            currentTill, lastTradePrice, volumeSoFar));

                });
    }

    @Override
    public String getTradeId() {
        return null;
    }

    @Override
    public double getLivePrice(@NotNull TradePair tradePair) {

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        requestBuilder.header("Content-Type", "application/octet-stream");
        //  Access-Control-Allow-Headers: Authorization, Content-Type, Accept-Datetime-Format, OANDA-Agent
        requestBuilder.header(
                "OANDA-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"
        );


        requestBuilder.uri(URI.create("https://stream-fxtrade.oanda.com/v3/accounts/" + account_id + "/pricing/stream?instruments=" + tradePair.toString('_')));
        //"https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?count=6&price=M&granularity=" + granularityToString(actualGranularity) + "&from=" + "2016-10-17T15:16:40.000000000Z"));

        final Product[] product = new Product[1];
        client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
                //.thenAccept(System.out::println)
                .exceptionally(throwable -> {

                    logger.error("Error getting live price", throwable);
                    return null;
                }).thenApply(response -> {
                    logger.info("Response: " + response);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    mapper.registerModule(new JavaTimeModule());
                    JsonNode res = null;
//                    {"asks":[{"liquidity":10000000,"price":"1.11704"},{"liquidity":10000000,"price":"1.11706"}],"bids":                        [{"liquidity":10000000,"price":"1.11690"},{"liquidity":10000000,"price":"1.11688"}],"closeoutAsk":"1.11708","closeoutBid":"1.11686","instrument":"EUR_USD","status":"tradeable","time":"2016-09-20T15:05:47.960449532Z"}
//                    {"asks":[{"liquidity":1000000,"price":"1.32149"},{"liquidity":2000000,"price":"1.32150"},{"liquidity":5000000,"price":"1.32151"},{"liquidity":10000000,"price":"1.32153"}],"bids":[{"liquidity":1000000,"price":"1.32128"},{"liquidity":2000000,"price":"1.32127"},{"liquidity":5000000,"price":"1.32126"},{"liquidity":10000000,"price":"1.32124"}],"closeoutAsk":"1.32153","closeoutBid":"1.32124","instrument":"USD_CAD","status":"tradeable","time":"2016-09-20T15:05:48.157162748Z"}
//                    {"asks":[{"liquidity":1000000,"price":"1.32145"},{"liquidity":2000000,"price":"1.32146"},{"liquidity":5000000,"price":"1.32147"},{"liquidity":10000000,"price":"1.32149"}],"bids":[{"liquidity":1000000,"price":"1.32123"},{"liquidity":2000000,"price":"1.32122"},{"liquidity":5000000,"price":"1.32121"},{"liquidity":10000000,"price":"1.32119"}],"closeoutAsk":"1.32149","closeoutBid":"1.32119","instrument":"USD_CAD","status":"tradeable","time":"2016-09-20T15:05:48.272079801Z"}
//                    {"asks":[{"liquidity":1000000,"price":"1.32147"},{"liquidity":2000000,"price":"1.32148"},{"liquidity":5000000,"price":"1.32149"},{"liquidity":10000000,"price":"1.32151"}],"bids":[{"liquidity":1000000,"price":"1.32126"},{"liquidity":2000000,"price":"1.32125"},{"liquidity":5000000,"price":"1.32124"},{"liquidity":10000000,"price":"1.32122"}],"closeoutAsk":"1.32151","closeoutBid":"1.32122","instrument":"USD_CAD","status":"tradeable","time":"2016-09-20T15:05:48.540813660Z"}
//                    {"time":"2016-09-20T15:05:50.163791738Z","type":"HEARTBEAT"}
                    try {
                        res = mapper.readTree(response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    logger.info("Response: " + res);
                    assert res != null;
                    if (res.has("asks") && res.has("bids")) {
                        ArrayNode asks = (ArrayNode) res.get("asks");
                        ArrayNode bids = (ArrayNode) res.get("bids");
                        if (asks.size() > 0 && bids.size() > 0) {
                            List<Fill> asksList = new ArrayList<>();
                            List<Fill> bidsList = new ArrayList<>();
                            for (int i = 0; i < asks.size(); i++) {
                                JsonNode ask = asks.get(i);
                                JsonNode bid = bids.get(i);
                                asksList.add(new Fill(ask.get("liquidity").asInt(), ask.get("price").asDouble()));


                                bidsList.add(new Fill(bid.get("liquidity").asInt(), bid.get("price").asDouble()));

                                voulumeSoFarList.add(ask.get("bucketWidth").asDouble());

                                logger.info("volumeList: " + voulumeSoFarList);

                            }
                            logger.info("tradePair: " + tradePair.toString('_'));
                            logger.info("Asks: " + asksList);
                            logger.info("Bids: " + bidsList);
                            product[0] = new Product(asksList, bidsList, tradePair);


                            logger.info("product: " + product[0]);
                            return product[0];
                        }
                    } else {
                        logger.error("Error getting live price");
                    }
                    return null;

                });
        return product[0].getLivePrice();


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
    public ArrayList<Double> getVolume() {
        return voulumeSoFarList;
    }

    // Get all orders
    //       GET
    //https://api.exchange.coinbase.com/orders

    @Override
    public String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    }


    //  Get single order
    //      GET
    //https://api.exchange.coinbase.com/orders/{order_id}


    public void getOrder(String orderId) throws IOException, InterruptedException {
        String uriStr = url + "orders/" + orderId;
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
    // Cancel an order
    //       DELETE
    //https://api.exchange.coinbase.com/orders/{order_id}

    public void cancelOrder(long orderId) throws IOException, InterruptedException {

        String uriStr = url + "orders/" + orderId;
        requestBuilder.DELETE();
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

    @Override
    public void cancelAllOrders() {

    }

    @Override
    public void cancelAllOpenOrders() {

    }

    @Override
    public ListView<Order> getOrderView() {


        return new ListView<>();

    }

    @Override
    public String getOrderId() {
        return Long.toString(System.currentTimeMillis());

    }

    @Override
    public void withdraw(Double value) {


    }

    public Node getAllOrders() throws IOException, InterruptedException {
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/orders";

        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        JSONObject jsonObject;
        if (response.statusCode() == 200) {
            jsonObject = new JSONObject(response.body());

            System.out.println(jsonObject.toString(4));

            jsonObject.put(
                    "orders",
                    jsonObject.getJSONArray("orders").toString()
            );


        } else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
        }

        return null;
    }

    @Override
    public List<OrderBook> getOrderBook(@NotNull TradePair tradePair) throws IOException, InterruptedException {

        requestBuilder.uri(URI.create(
                orderBookUrl
        ));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());

        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            System.out.println(jsonObject.toString(4));
            JSONArray jsonArray = jsonObject.getJSONArray("orderbooks");
            System.out.println(jsonArray.toString(4));
            List<OrderBook> orderBooks = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                OrderBook orderBook = new OrderBook(jsonObject1.names());
                orderBooks.add(orderBook);
                System.out.println("OrderBook :" + orderBooks);
            }
        }

        return null;
    }

    public void getOrderHistory(@NotNull TradePair tradePair) throws IOException, InterruptedException {
        String uriStr =
                url + "orders/";
        requestBuilder.uri(URI.create(uriStr));
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());

        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            System.out.println(jsonObject.toString(4));
            JSONArray jsonArray = jsonObject.getJSONArray("orders");
            System.out.println(jsonArray.toString(4));
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                Order order = new Order(jsonObject1.names());
                orders.add(order);
                System.out.println(orders);
            }


        } else {
            System.out.println(response.statusCode());
            System.out.println(response.body());
        }
    }

    @Override
    public void createOrder(@NotNull TradePair tradePair, @NotNull Side side, @NotNull ENUM_ORDER_TYPE orderType, double price, double size,
                            @NotNull Date timestamp, double stopLoss, double takeProfit, double takeProfitPrice) throws IOException, InterruptedException {
//
//        curl: Create EUR/USD Market Order to sell 100 units
//        curl: Create a Take Profit Order @ 1.6000 for Trade with ID 6368
//        curl: Create a Limit Order for -1000 USD_CAD @ 1.5000 with a Stop Loss on Fill @ 1.7000 and a Take Profit @ 1.14530
//        curl: Create an Entry Order for 10000 EUR_CAD @ 1.2000 with Client Extensions

        if (orderType == ENUM_ORDER_TYPE.MARKET && Side.SELL == side) {
            createSellOrder(tradePair, price, size);
        }
        if (orderType == ENUM_ORDER_TYPE.MARKET && Side.BUY == side) {
            createBuyOrder(tradePair, side, price, size, timestamp);
        }
        if (orderType == ENUM_ORDER_TYPE.LIMIT && Side.SELL == side) {
            createSellLimitOrder(tradePair, side, price, size, timestamp);
        }
        if (orderType == ENUM_ORDER_TYPE.LIMIT && Side.BUY == side) {
            createBuyLimitOrder(tradePair, side, price, size, takeProfit, stopLoss);
        }
        if (orderType == ENUM_ORDER_TYPE.TAKE_PROFIT && Side.SELL == side) {
            createSellTakeProfitOrder(tradePair, side, price, size, takeProfit, stopLoss);
        }
        if (orderType == ENUM_ORDER_TYPE.TAKE_PROFIT && Side.BUY == side) {
            createBuyTakeProfitOrder(tradePair, side, price, size, takeProfit, stopLoss);
        }
        if (orderType == ENUM_ORDER_TYPE.ENTRY && Side.SELL == side) {
            createSellEntryOrder(tradePair, price, size, takeProfit, stopLoss);
        }
        if (orderType == ENUM_ORDER_TYPE.ENTRY && Side.BUY == side) {
            createBuyEntryOrder(tradePair, side, price, size, takeProfit, stopLoss);
        }


    }

    private void createBuyTakeProfitOrder(@NotNull TradePair tradePair, Side side, double price, double size, double takeProfit, double stopLoss) throws IOException, InterruptedException {
        //   curl: Create a Buy Take Profit Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.POST(

                HttpRequest.BodyPublishers.ofString(
                        "{\n" +
                                "  \"order\": {\n" +
                                "    \"price\": \"" + price + "\",\n" +
                                "    \"stopLossOnFill\": {\n" +
                                "      \"timeInForce\": \"GTC\",\n" +
                                "      \"price\": \"" + stopLoss + "\"\n" +
                                "    },\n" +
                                "    \"takeProfitOnFill\": {\n" +
                                "      \"timeInForce\": \"GTC\",\n" +
                                "      \"price\": \"" + takeProfit + "\"\n" +
                                "    },\n" +
                                "    \"timeInForce\": \"GTC\",\n" +
                                "    \"instrument\": \"" + symbol + "\",\n" +
                                "    \"units\": \"" + size + "\",\n" +
                                "    \"type\": \"TAKE_PROFIT\"\n" +
                                "  }\n" +
                                "}"
                ));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            new Message(
                    Message.MessageType.ERROR,
                    "Error creating order: " + response.body()
            )
            ;
        } else {
            JSONObject jsonObject = new JSONObject(response.body());


            System.out.println(jsonObject.toString(4));
        }
    }

    private void createSellTakeProfitOrder(@NotNull TradePair tradePair, Side side, double price, double size, double takeProfit, double stopLoss) throws IOException, InterruptedException {
        //   curl: Create a Sell Take Profit Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = url + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.POST(

                HttpRequest.BodyPublishers.ofString(
                        "{\n" +
                                "  \"order\": {\n" +
                                "    \"price\": \"" + price + "\",\n" +
                                "    \"stopLossOnFill\": {\n" +
                                "      \"timeInForce\": \"GTC\",\n" +
                                "      \"price\": \"" + stopLoss + "\"\n" +
                                "    },\n" +
                                "    \"takeProfitOnFill\": {\n" +
                                "      \"timeInForce\": \"GTC\",\n" +
                                "      \"price\": \"" + takeProfit + "\"\n" +
                                "    },\n" +
                                "    \"timeInForce\": \"GTC\",\n" +
                                "    \"instrument\": \"" + symbol + "\",\n" +
                                "    \"units\": \"" + size + "\",\n" +
                                "    \"type\": \"TAKE_PROFIT\"\n" +
                                "  }\n" +
                                "}"
                ));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("order error " + response.body());

            new Message(
                    Message.MessageType.ERROR,
                    "Error creating order: " + response.body()
            )
            ;
        }
        JSONObject jsonObject = new JSONObject(response.body());
        System.out.println(jsonObject.toString(4));

    }

    private void createBuyLimitOrder(TradePair tradePair, Side side, double price, double size, double takeProfit, double stopLoss) {
        //   curl: Create a Buy Limit Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = url + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
//
//        body=$(cat << EOF
//        {
//            "order": {
//            "price": "1.5000",
//                    "stopLossOnFill": {
//                "timeInForce": "GTC",
//                        "price": "1.7000"
//            },
//            "takeProfitOnFill": {
//                "price": "1.14530"
//            },
//            "timeInForce": "GTC",
//                    "instrument": "USD_CAD",
//                    "units": "-1000",
//                    "type": "LIMIT",
//                    "positionFill": "DEFAULT"
//        }
//        }
//        EOF
//)
        requestBuilder.POST(

                HttpRequest.BodyPublishers.ofString(
                        "{\n" +
                                "  \"order\": {\n" +
                                "    \"price\": \"" + price + "\",\n" +
                                "    \"stopLossOnFill\": {\n" +
                                "      \"timeInForce\": \"GTC\",\n" +
                                "      \"price\": \"" + stopLoss + "\"\n" +
                                "    },\n" +
                                "    \"takeProfitOnFill\": {\n" +
                                "      \"price\": \"" + takeProfit + "\"\n" +
                                "    },\n" +
                                "    \"timeInForce\": \"GTC\",\n" +
                                "    \"instrument\": \"" + symbol + "\",\n" +
                                "    \"units\": \"" + size + "\",\n" +
                                "    \"type\": \"LIMIT\",\n" +
                                "    \"positionFill\": \"DEFAULT\"\n" +
                                "  }\n" +
                                "}"
                ));
        requestBuilder.uri(URI.create(uriStr));
        HttpResponse<String> response;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            new Message(

                    Message.MessageType.ERROR,
                    "Error creating order: " + response.body()
            );


        } else {
            JSONObject jsonObject = new JSONObject(response.body());


            System.out.println(jsonObject.toString(4));
        }


    }

    private void createSellLimitOrder(TradePair tradePair, Side side, double price, double size, Date timestamp) {
        //   curl: Create a Sell Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = url + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
//        body=$(cat << EOF
//        {
//            "order": {
//            "price": "1.5000",
//                    "stopLossOnFill": {
//                "timeInForce": "GTC",
//                        "price": "1.7000"
//            },
//            "takeProfitOnFill": {
//                "price": "1.14530"
//            },
//            "timeInForce": "GTC",
//                    "instrument": "USD_CAD",
//                    "units": "-1000",
//                    "type": "LIMIT",
//                    "positionFill": "DEFAULT"
//        }
//        }
//        EOF
//)
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                "{\n" +
                        "    \"order\": {\n" +
                        "        \"price\": \"" + price + "\",\n" +
                        "        \"timeInForce\": \"FOK\",\n" +
                        "        \"instrument\": \"" + symbol + "\",\n" +
                        "        \"units\": \"" + size + "\",\n" +
                        "        \"type\": \"LIMIT\",\n" +
                        "        \"positionFill\": \"DEFAULT\"\n" +
                        "    }\n" +
                        "}"));

        HttpResponse<String> response = null;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("order error " + response.body());

            new Message(
                    Message.MessageType.ERROR,
                    "Error creating order: " + response.body()
            );
        }
        JSONObject jsonObject = new JSONObject(response.body());
        System.out.println(jsonObject.toString(4));


    }

    private void createBuyOrder(TradePair tradePair, Side side, double price, double size, Date timestamp) {
        //   curl: Create a Buy Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                "{\n" +
                        "    \"order\": {\n" +
                        "        \"price\": \"" + price + "\",\n" +
                        "        \"timeInForce\": \"FOK\",\n" +
                        "        \"instrument\": \"" + symbol + "\",\n" +
                        "        \"units\": \"" + size + "\",\n" +
                        "        \"type\": \"MARKET\",\n" +
                        "        \"positionFill\": \"DEFAULT\"\n" +
                        "    }\n" +
                        "}"));
        HttpResponse<String> response = null;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("order error " + response.body());

            new Message(
                    Message.MessageType.ERROR,
                    "Error creating order: " + response.body()
            );
        }
        JSONObject jsonObject = new JSONObject(response.body());
        System.out.println(jsonObject.toString(4));
//        {
//            "orderCreateTransaction": {
//            "accountID": "001-001-2783446-002",
//                    "reason": "CLIENT_ORDER",
//                    "requestID": "25063993365991929",
//                    "instrument": "EUR_HKD",
//                    "id": "143215",
//                    "time": "2023-04-06T04:13:08.773992216Z",
//                    "units": "1000",
//                    "batchID": "143215",
//                    "type": "MARKET_ORDER",
//                    "userID": 2783446,
//                    "timeInForce": "FOK",
//                    "positionFill": "DEFAULT"
//        },
//            "orderCancelTransaction": {
//            "accountID": "001-001-2783446-002",
//                    "reason": "INSUFFICIENT_MARGIN",
//                    "orderID": "143215",
//                    "requestID": "25063993365991929",
//                    "id": "143216",
//                    "time": "2023-04-06T04:13:08.773992216Z",
//                    "batchID": "143215",
//                    "type": "ORDER_CANCEL",
//                    "userID": 2783446
//        },
//            "lastTransactionID": "143216",
//                "relatedTransactionIDs": [
//            "143215",
//                    "143216"
//    ]
//        }

        String orderId = jsonObject.getJSONObject("orderCreateTransaction").getString("id");
        System.out.println(orderId);
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("id"));
        System.out.println(jsonObject.getJSONObject("lastTransactionID"));
        System.out.println(jsonObject.getJSONObject("relatedTransactionIDs"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getLong("orderID"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getLong("requestID"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getLong("id"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("time"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("batchID"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("type"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("userID"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("timeInForce"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("positionFill"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("reason"));
        System.out.println(jsonObject.getJSONObject("orderCancelTransaction").getString("instrument"));
        OrderCreateTransaction orderCreateTransaction = new OrderCreateTransaction(jsonObject.getJSONObject("orderCreateTransaction"));
        OrderCancelTransaction orderCancelTransaction = new OrderCancelTransaction(jsonObject.getJSONObject("orderCancelTransaction"));

        getOrderCreateTransaction.add(orderCreateTransaction);
        getOrderCancelTransaction.add(orderCancelTransaction);

    }

    private void createSellOrder(@NotNull TradePair tradePair, double price, double size) {
        //   curl: Create a Sell Order for 100 units

        String symbol = tradePair.toString('_');
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/orders/";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
//        body=$(cat << EOF
//        {
//            "order": {
//            "units": "-100",
//                    "instrument": "EUR_USD",
//                    "timeInForce": "FOK",
//                    "type": "MARKET",
//                    "positionFill": "DEFAULT"
//        }
//        }
//        EOF
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                "{\n" +
                        "    \"order\": {\n" +
                        "        \"price\": \"" + price + "\",\n" +
                        "        \"timeInForce\": \"FOK\",\n" +
                        "        \"instrument\": \"" + symbol + "\",\n" +
                        "        \"units\": \"" + size + "\",\n" +
                        "        \"type\": \"MARKET\",\n" +
                        "        \"positionFill\": \"DEFAULT\"\n" +
                        "    }\n" +
                        "}"));
        HttpResponse<String> response;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("Order error " + response.body());


            Stage stage = new Stage();
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(new VBox(new Label("order error "), new TextArea(
                    response.body()
            )));
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm());

            dialogPane.getButtonTypes().addAll(
                    ButtonType.OK,
                    ButtonType.CANCEL
            );
            dialogPane.getStyleClass().add("dialog");
            dialogPane.setPrefSize(400, 200);
            dialogPane.setMaxSize(400, 200);
            dialogPane.setMinSize(400, 200);
            dialogPane.setMaxWidth(400);
            dialogPane.setMaxHeight(200);
            dialogPane.setMinWidth(400);
            dialogPane.setMinHeight(200);

            dialogPane.setOpacity(0.8);
            dialogPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(23,23,189,0.7)"), CornerRadii.EMPTY,
                    Insets.EMPTY)));
            Scene scene = new Scene(dialogPane, 800, 600);
            stage.setScene(scene);
            stage.show();


        }
        JSONObject jsonObject = new JSONObject(response.body());
        System.out.println(jsonObject.toString(4));


        OrderCreateTransaction orderCreateTransaction = new OrderCreateTransaction(jsonObject.getJSONObject("orderCreateTransaction"));
        OrderCancelTransaction orderCancelTransaction = new OrderCancelTransaction(jsonObject.getJSONObject("orderCancelTransaction"));
        getOrderCreateTransaction.add(orderCreateTransaction);
        getOrderCancelTransaction.add(orderCancelTransaction);


        logger.info(

                "Order Create Transaction: " + orderCreateTransaction + "\n" +
                        "Order Cancel Transaction: " + orderCancelTransaction + "\n" +
                        "Last Transaction ID: " + jsonObject.getJSONObject("lastTransactionID").toString() + "\n" +
                        "Related Transaction IDs: " + jsonObject.getJSONObject("relatedTransactionIDs").toString(4) + "\n" +
                        "Order ID: " + jsonObject.getJSONObject("orderCancelTransaction").getString("orderID") + "\n" +
                        "Request ID: " + jsonObject.getJSONObject("orderCancelTransaction").getString("requestID") + "\n" +
                        "ID: " + jsonObject.getJSONObject("orderCancelTransaction").getString("id") + "\n" +
                        "Time: " + jsonObject.getJSONObject("orderCancelTransaction").getString("time") + "\n"
        );

    }

    private void createSellEntryOrder(@NotNull TradePair tradePair, double price, double units, double takeProfit, double stopLoss) {
        //   curl: Create a Sell Entry Order for 10000 EUR_CAD

        String symbol = tradePair.toString('_');
        String uriStr = url + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
//
//        body=$(cat << EOF
//        {
//            "order": {
//            "price": "1.2000",
//                    "timeInForce": "GTC",
//                    "instrument": "EUR_CAD",
//                    "units": "10000",
//                    "clientExtensions": {
//                "comment": "New idea for trading",
//                        "tag": "strategy_9",
//                        "id": "my_order_100"
//            },
//            "type": "MARKET_IF_TOUCHED",
//                    "positionFill": "DEFAULT"
//        }
//        }
//        EOF
//)

        String strategy = "CryptoInvestor strategy";

        requestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(
                        "{\n" +
                                "    \"order\": {\n" +
                                "        \"price\": \"" + price + "\",\n" +
                                "        \"timeInForce\": \"GTC\",\n" +
                                "        \"instrument\": \"" + symbol + "\",\n" +
                                "        \"units\": \"" + units + "\",\n" +
                                "        \"clientExtensions\": {\n" +
                                "            \"comment\": \"" + strategy + "\",\n" +
                                "            \"tag\": \"strategy_9\",\n" +
                                "            \"id\": \"" + getOrderId() + "\"\n" +
                                "        },\n" +
                                "        \"type\": \"MARKET_IF_TOUCHED\",\n" +
                                "        \"positionFill\": \"DEFAULT\"\n" +
                                "    }\n" +
                                "}"));


        HttpResponse<String> response;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("order error " + response.body());
            new Message(
                    Message.MessageType.ERROR,
                    response.body()
            );
        }
        JSONObject jsonObject = new JSONObject(response.body());
        System.out.println(jsonObject.toString(4));
        String orderId = jsonObject.getString("id");
        System.out.println(orderId);
        System.out.println(jsonObject.getString("status"));
        System.out.println(jsonObject.getString("clientExtensions"));
        System.out.println(jsonObject.getString("timeInForce"));
        System.out.println(jsonObject.getString("side"));
        System.out.println(jsonObject.getString("price"));
        System.out.println(jsonObject.getString("quantity"));
        System.out.println(jsonObject.getString("filledQuantity"));
        System.out.println(jsonObject.getString("remainingQuantity"));
        System.out.println(jsonObject.getString("fills"));

    }


    public @NotNull List<Currency> getAvailableSymbols() {
        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/instruments";
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
        System.out.println("Oanda Instruments " + response.body());


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
                5,
                1,
                5,
                50,
                100.000,
                100.000,
                5,
                "",
                "t1"
        );
        out.println("Instruments " + instruments);
        ArrayList<Currency> instrumentsList = new ArrayList<>();

        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());

        } else {


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

    //Get Account
    @Override
    public Account getAccounts() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + account_id));
        requestBuilder.GET();
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        JSONObject jsonObject = new JSONObject(response.body());
        if (response.statusCode() == 200) {

            // {"account":{"guaranteedStopLossOrderMode":"DISABLED","hedgingEnabled":false,"id":"001-001-2783446-002","createdTime":"2019-04-30T02:39:18.895364468Z","currency":"USD","createdByUserID":2783446,"alias":"MT4","marginRate":"0.02","lastTransactionID":"143166","balance":"51.4613","openTradeCount":1,"openPositionCount":1,"pendingOrderCount":0,"pl":"-914.0620","resettablePL":"-914.0620","resettablePLTime":"0","financing":"-13.1795","commission":"0.2672","dividendAdjustment":"0","guaranteedExecutionFees":"0.0000","orders":[],"positions":[{"instrument":"EUR_USD","long":{"units":"0","pl":"-99.1666","resettablePL":"-99.1666","financing":"-7.7397","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"-2300","averagePrice":"1.08814","pl":"-133.7496","resettablePL":"-133.7496","financing":"2.0142","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","tradeIDs":["143152"],"unrealizedPL":"8.7400"},"pl":"-232.9162","resettablePL":"-232.9162","financing":"-5.7255","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"8.7400","marginUsed":"49.8741"},{"instrument":"EUR_GBP","long":{"units":"0","pl":"0.3154","resettablePL":"0.3154","financing":"-0.0105","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.5702","resettablePL":"-0.5702","financing":"-0.0024","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2548","resettablePL":"-0.2548","financing":"-0.0129","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_AUD","long":{"units":"0","pl":"-7.9081","resettablePL":"-7.9081","financing":"-0.0016","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4026","resettablePL":"0.4026","financing":"0.0018","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-7.5055","resettablePL":"-7.5055","financing":"0.0002","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CAD","long":{"units":"0","pl":"-5.4214","resettablePL":"-5.4214","financing":"-0.5173","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.3465","resettablePL":"-1.3465","financing":"-0.0027","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.7679","resettablePL":"-6.7679","financing":"-0.5200","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_SGD","long":{"units":"0","pl":"-1.3650","resettablePL":"-1.3650","financing":"-0.0024","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.5949","resettablePL":"-1.5949","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.9599","resettablePL":"-2.9599","financing":"-0.0025","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CHF","long":{"units":"0","pl":"2.9766","resettablePL":"2.9766","financing":"-0.1421","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.2801","resettablePL":"0.2801","financing":"-0.0021","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"3.2567","resettablePL":"3.2567","financing":"-0.1442","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_JPY","long":{"units":"0","pl":"-1.0765","resettablePL":"-1.0765","financing":"-0.0975","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-10.0630","resettablePL":"-10.0630","financing":"-0.0131","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.1395","resettablePL":"-11.1395","financing":"-0.1106","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_NZD","long":{"units":"0","pl":"2.8772","resettablePL":"2.8772","financing":"-0.2898","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-138.8771","resettablePL":"-138.8771","financing":"0.6645","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-135.9999","resettablePL":"-135.9999","financing":"0.3747","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1659","resettablePL":"-0.1659","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1659","resettablePL":"-0.1659","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_CZK","long":{"units":"0","pl":"-2.1723","resettablePL":"-2.1723","financing":"-0.0078","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1557","resettablePL":"-0.1557","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.3280","resettablePL":"-2.3280","financing":"-0.0078","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_NOK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1526","resettablePL":"-0.1526","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1526","resettablePL":"-0.1526","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_SEK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0180","resettablePL":"-0.0180","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0180","resettablePL":"-0.0180","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_TRY","long":{"units":"0","pl":"-0.0959","resettablePL":"-0.0959","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0959","resettablePL":"-0.0959","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"EUR_ZAR","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0682","resettablePL":"-0.0682","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0682","resettablePL":"-0.0682","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CAD","long":{"units":"0","pl":"-29.8908","resettablePL":"-29.8908","financing":"-0.9440","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-44.8502","resettablePL":"-44.8502","financing":"-0.1419","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-74.7410","resettablePL":"-74.7410","financing":"-1.0859","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_SGD","long":{"units":"0","pl":"0.6893","resettablePL":"0.6893","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"0.6893","resettablePL":"0.6893","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CHF","long":{"units":"0","pl":"-12.2150","resettablePL":"-12.2150","financing":"0.3309","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"1.8985","resettablePL":"1.8985","financing":"-0.3812","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-10.3165","resettablePL":"-10.3165","financing":"-0.0503","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_JPY","long":{"units":"0","pl":"-84.0042","resettablePL":"-84.0042","financing":"0.3929","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-35.5903","resettablePL":"-35.5903","financing":"-1.7775","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-119.5945","resettablePL":"-119.5945","financing":"-1.3846","commission":"0.1172","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_HKD","long":{"units":"0","pl":"-0.3687","resettablePL":"-0.3687","financing":"-0.1380","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0547","resettablePL":"0.0547","financing":"-0.0055","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.3140","resettablePL":"-0.3140","financing":"-0.1435","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CZK","long":{"units":"0","pl":"-2.7163","resettablePL":"-2.7163","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.7163","resettablePL":"-2.7163","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_DKK","long":{"units":"0","pl":"-0.0077","resettablePL":"-0.0077","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0077","resettablePL":"-0.0077","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_MXN","long":{"units":"0","pl":"-0.0951","resettablePL":"-0.0951","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0564","resettablePL":"-0.0564","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1515","resettablePL":"-0.1515","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_NOK","long":{"units":"0","pl":"-0.0108","resettablePL":"-0.0108","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0108","resettablePL":"-0.0108","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_PLN","long":{"units":"0","pl":"-0.0991","resettablePL":"-0.0991","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0991","resettablePL":"-0.0991","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_SEK","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0132","resettablePL":"-0.0132","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0132","resettablePL":"-0.0132","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_THB","long":{"units":"0","pl":"-0.1990","resettablePL":"-0.1990","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1990","resettablePL":"-0.1990","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"USD_CNH","long":{"units":"0","pl":"-1.0486","resettablePL":"-1.0486","financing":"-0.0074","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-10.7225","resettablePL":"-10.7225","financing":"-0.0092","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.7711","resettablePL":"-11.7711","financing":"-0.0166","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_USD","long":{"units":"0","pl":"-10.0528","resettablePL":"-10.0528","financing":"-0.8398","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-23.1159","resettablePL":"-23.1159","financing":"-0.0970","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-33.1687","resettablePL":"-33.1687","financing":"-0.9368","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_AUD","long":{"units":"0","pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0075","resettablePL":"-0.0075","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1144","resettablePL":"-0.1144","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_CAD","long":{"units":"0","pl":"-5.2250","resettablePL":"-5.2250","financing":"-0.0355","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-3.0620","resettablePL":"-3.0620","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-8.2870","resettablePL":"-8.2870","financing":"-0.0355","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_SGD","long":{"units":"0","pl":"-0.8250","resettablePL":"-0.8250","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.8250","resettablePL":"-0.8250","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_CHF","long":{"units":"0","pl":"-4.4219","resettablePL":"-4.4219","financing":"0.0005","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1570","resettablePL":"-0.1570","financing":"-0.0687","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-4.5789","resettablePL":"-4.5789","financing":"-0.0682","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_JPY","long":{"units":"0","pl":"-9.7444","resettablePL":"-9.7444","financing":"-0.0043","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4019","resettablePL":"0.4019","financing":"-0.0653","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-9.3425","resettablePL":"-9.3425","financing":"-0.0696","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_NZD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2934","resettablePL":"-0.2934","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2934","resettablePL":"-0.2934","financing":"0.0000","commission":"0.1500","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_PLN","long":{"units":"0","pl":"-0.5359","resettablePL":"-0.5359","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2639","resettablePL":"-0.2639","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.7998","resettablePL":"-0.7998","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"GBP_ZAR","long":{"units":"0","pl":"-0.0420","resettablePL":"-0.0420","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0168","resettablePL":"-0.0168","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0588","resettablePL":"-0.0588","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_USD","long":{"units":"0","pl":"-26.7262","resettablePL":"-26.7262","financing":"-0.6898","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-43.7439","resettablePL":"-43.7439","financing":"-0.1078","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-70.4701","resettablePL":"-70.4701","financing":"-0.7976","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_CAD","long":{"units":"0","pl":"-2.9320","resettablePL":"-2.9320","financing":"-0.0339","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-18.4886","resettablePL":"-18.4886","financing":"-0.0171","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-21.4206","resettablePL":"-21.4206","financing":"-0.0510","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_SGD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-2.6495","resettablePL":"-2.6495","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-2.6495","resettablePL":"-2.6495","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_CHF","long":{"units":"0","pl":"-8.4263","resettablePL":"-8.4263","financing":"0.0082","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.2083","resettablePL":"0.2083","financing":"-0.0090","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-8.2180","resettablePL":"-8.2180","financing":"-0.0008","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_JPY","long":{"units":"0","pl":"-2.4061","resettablePL":"-2.4061","financing":"-0.1419","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-9.0972","resettablePL":"-9.0972","financing":"-0.6077","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.5033","resettablePL":"-11.5033","financing":"-0.7496","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_NZD","long":{"units":"0","pl":"-6.1148","resettablePL":"-6.1148","financing":"-0.0231","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-11.3032","resettablePL":"-11.3032","financing":"-0.0377","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-17.4180","resettablePL":"-17.4180","financing":"-0.0608","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"AUD_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0008","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.1069","resettablePL":"-0.1069","financing":"-0.0008","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_SGD","long":{"units":"0","pl":"0.0036","resettablePL":"0.0036","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.2758","resettablePL":"-0.2758","financing":"-0.0006","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.2722","resettablePL":"-0.2722","financing":"-0.0006","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_CHF","long":{"units":"0","pl":"-2.7829","resettablePL":"-2.7829","financing":"0.0009","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-2.8108","resettablePL":"-2.8108","financing":"-0.0095","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-5.5937","resettablePL":"-5.5937","financing":"-0.0086","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_JPY","long":{"units":"0","pl":"-2.5502","resettablePL":"-2.5502","financing":"0.0110","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-4.1264","resettablePL":"-4.1264","financing":"-0.0157","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.6766","resettablePL":"-6.6766","financing":"-0.0047","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CAD_HKD","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.9540","resettablePL":"-0.9540","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.9540","resettablePL":"-0.9540","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"SGD_CHF","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-1.8765","resettablePL":"-1.8765","financing":"-0.0040","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.8765","resettablePL":"-1.8765","financing":"-0.0040","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"SGD_JPY","long":{"units":"0","pl":"-11.6988","resettablePL":"-11.6988","financing":"-0.0266","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.4127","resettablePL":"0.4127","financing":"-0.0052","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-11.2861","resettablePL":"-11.2861","financing":"-0.0318","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CHF_JPY","long":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0281","resettablePL":"0.0281","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"0.0281","resettablePL":"0.0281","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"CHF_ZAR","long":{"units":"0","pl":"-0.0021","resettablePL":"-0.0021","financing":"-0.0002","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0021","resettablePL":"-0.0021","financing":"-0.0002","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_USD","long":{"units":"0","pl":"-33.4490","resettablePL":"-33.4490","financing":"-0.5699","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-35.2630","resettablePL":"-35.2630","financing":"-0.7206","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-68.7120","resettablePL":"-68.7120","financing":"-1.2905","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_CAD","long":{"units":"0","pl":"-7.7127","resettablePL":"-7.7127","financing":"-0.0620","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"3.8895","resettablePL":"3.8895","financing":"-0.1901","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-3.8232","resettablePL":"-3.8232","financing":"-0.2521","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_CHF","long":{"units":"0","pl":"-5.9925","resettablePL":"-5.9925","financing":"0.0123","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.4028","resettablePL":"-0.4028","financing":"-0.0003","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-6.3953","resettablePL":"-6.3953","financing":"0.0120","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_JPY","long":{"units":"0","pl":"-2.0866","resettablePL":"-2.0866","financing":"0.0016","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.3974","resettablePL":"0.3974","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.6892","resettablePL":"-1.6892","financing":"0.0016","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"NZD_HKD","long":{"units":"0","pl":"0.0232","resettablePL":"0.0232","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"-0.0515","resettablePL":"-0.0515","financing":"-0.0001","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-0.0283","resettablePL":"-0.0283","financing":"-0.0001","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},{"instrument":"TRY_JPY","long":{"units":"0","pl":"-1.1650","resettablePL":"-1.1650","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"short":{"units":"0","pl":"0.0000","resettablePL":"0.0000","financing":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"},"pl":"-1.1650","resettablePL":"-1.1650","financing":"0.0000","commission":"0.0000","dividendAdjustment":"0.0000","guaranteedExecutionFees":"0.0000","unrealizedPL":"0.0000"}],"trades":[{"id":"143152","instrument":"EUR_USD","price":"1.08814","openTime":"2023-03-31T13:18:05.967088687Z","initialUnits":"-2300","initialMarginRequired":"50.0581","state":"OPEN","currentUnits":"-2300","realizedPL":"0.0000","financing":"0.0619","dividendAdjustment":"0.0000","clientExtensions":{"id":"140953421","tag":"0"},"unrealizedPL":"8.7400","marginUsed":"49.8741"}],"unrealizedPL":"8.7400","NAV":"60.2013","marginUsed":"49.8741","marginAvailable":"10.3272","positionValue":"2493.7060","marginCloseoutUnrealizedPL":"9.0160","marginCloseoutNAV":"60.4773","marginCloseoutMarginUsed":"49.8741","marginCloseoutPositionValue":"2493.7060","marginCloseoutPercent":"0.41234","withdrawalLimit":"10.3272","marginCallMarginUsed":"49.8741","marginCallPercent":"0.82467"},"lastTransactionID":"143166"}


            Account account = new Account(jsonObject.getJSONObject("account"));
            logger.info("account: " + account);
            logger.info("Commission " + account.commission);

            System.out.println(jsonObject.getJSONObject("account").getString("currency"));


        }
        return null;
    }

    private void createBuyEntryOrder(TradePair tradePair, Side side, double price, double size, double takeProfit,
                                     double stopLoss) {
        //    curl: Create a Buy Entry Order for 10000 EUR_CAD
//        body=$(cat << EOF
//        {
//            "order": {
//            "price": "1.2000",
//                    "timeInForce": "GTC",
//                    "instrument": "EUR_CAD",
//                    "units": "10000",
//                    "clientExtensions": {
//                "comment": "New idea for trading",
//                        "tag": "strategy_9",
//                        "id": "my_order_100"
//            },
//            "type": "MARKET_IF_TOUCHED",
//                    "positionFill": "DEFAULT"
//        }
//        }
//        EOF
//)
//
//        curl \
//        -X POST \
//        -H "Content-Type: application/json" \
//        -H "Authorization: Bearer <AUTHENTICATION TOKEN>" \
//        -d "$body" \
//        "https://api-fxtrade.oanda.com/v3/accounts/<ACCOUNT>/orders"

        String symbol = tradePair.toString('_');
        String uriStr = url + "/orders";
        System.out.println(uriStr);
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString("{\"order\": {\"price\": \""
                + price + "\", \"timeInForce\": \"GTC\", \"instrument\": \"" + symbol + "\", \"units\": \"" + size + "\", \"clientExtensions\": {\"comment\": \"New idea for trading\", \"tag\": \"strategy_9\", \"id\": \"my_order_100\"}, \"type\": \"MARKET_IF_TOUCHED\"," +
                "\"positionFill\": \"DEFAULT\"}}"));
        HttpResponse<String> response;
        try {
            response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200 || response.statusCode() != 201) {
            System.out.println(response.statusCode());
            System.out.println("order error " + response.body());

            new Message(
                    Message.MessageType.ERROR,
                    "Order Error" +
                            response.body()
            );
        } else {
            JSONObject jsonObject = new JSONObject(response.body());

            System.out.println(jsonObject.toString(4));
        }


    }

    public void CloseAllOrders() throws IOException, InterruptedException {
        String uriStr = url + "/cancel-all";
        System.out.println(uriStr);
        HttpRequest.Builder request = HttpRequest.newBuilder();
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.DELETE();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());

            new Message(
                    Message.MessageType.ERROR,
                    "Order Error" +
                            response.body()
            );

        } else {
            JSONObject jsonObject = new JSONObject(response.body());

            //   TelegramClient.sendMessage(jsonObject.toString(4));
            System.out.println(jsonObject.toString(4));
        }

    }


    @Override
    public void closeAllOrders() throws IOException, InterruptedException {
        String url1 = url + "/products/cancel";

        requestBuilder.uri(URI.create(url1));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.body());

    }

    @Override
    public List<String> getTradePair() throws IOException, InterruptedException {

        String uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + account_id + "/instruments";
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
        System.out.println("Oanda Instruments " + response.body());


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
                5,
                1,
                5,
                50,
                100.000,
                100.000,
                5,
                "",
                "t1"
        );
        out.println("Instruments " + instruments);


        List<String> data = new ArrayList<>();
        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());

        } else {


            JsonNode jsonNode;
            try {
                jsonNode = new ObjectMapper().readTree(response.body());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode instrumentsNode = jsonNode.get("instruments");
            // String name, String type, String displayName, int pipLocation, int displayPrecision, int tradeUnitsPrecision, int minimumTradeSize, int maximumTrailingStopDistance, int minimumTrailingStopDistance, double maximumPositionSize, double maximumOrderUnits, double marginRate, String guaranteedStopLossOrderMode, String tags
            logger.info("Instruments:  " + instrumentsNode.toString());

            for (JsonNode instrumentNode : instrumentsNode) {

                String symb = instrumentNode.get("displayName").asText();

                if (symb.contains("/") || symb.contains("_")) {
                    String[] symb1 = symb.split("/");
                    String[] symb2 = symb1[1].split("_");
                    logger.info(symb1[0] + " " + symb2[0]);
                    symb = symb1[0] + "/" + symb2[0];
                } else {
                    logger.info(
                            "Error: " + symb + " is not a valid instrument"
                    );

                }
                System.out.println(symb);
                String symb1 = symb.split("/")[0];
                String symb2 = symb.split("/")[1];

                data.add(
                        symb1 + "/" + symb2
                );

            }
        }
        logger.info(data.toString());

        return data;
    }

    @Override
    public void connect(String text, String text1, String userIdText) throws IOException, InterruptedException {

        apiKey = text;
        api_secret = text1;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void getPositionBook(TradePair tradePair) throws IOException, InterruptedException {
        String url1 = positionBookUrl;
        requestBuilder.uri(URI.create(url1));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.body());
        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            logger.info(jsonObject.toString());

            Account account = new Account(jsonObject.getJSONObject("account"));
            logger.info("account: " + account);
            logger.info("Commission " + account.commission);

            System.out.println(jsonObject.getJSONObject("account").getString("currency"));
        } else {

            new Message(
                    Message.MessageType.ERROR,
                    "Error getting position book\n" +
                            "Status Code: " + response.statusCode() + "\n" +
                            "Response: " + response.body()
            );

        }


    }

    @Override
    public void getOpenOrder(TradePair tradePair) throws IOException, InterruptedException {
        String url1 = url + "orders/" + tradePair.toString('_');
        requestBuilder.uri(URI.create(url1));
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.body());
        if (response.statusCode() == 200) {
            JSONObject jsonObject = new JSONObject(response.body());
            logger.info(jsonObject.toString());

            Account account = new Account(jsonObject.getJSONObject("account"));
            logger.info("account: " + account);
            logger.info("Commission " + account.commission);

            System.out.println(jsonObject.getJSONObject("account").getString("currency"));
        } else {


            new Message(
                    Message.MessageType.ERROR,
                    "Error getting open order\n" +
                            "Status Code: " + response.statusCode() + "\n" +
                            "Response: " + response.body());

        }
    }

    public String getApi_secret() {
        return api_secret;
    }

    public void setApi_secret(String api_secret) {
        this.api_secret = api_secret;
    }

    static abstract class CoinbaseCandleDataSupplier extends CandleDataSupplier {
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


            String granularity = null;
            String uriStr = "";//"https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?count=6&price=M&granularity=" + granularity + "&from=" + "2016-10-17T15:16:40.000000000Z";

            String x;
            String str;
            //  secondsPerCandle = actualGranularity;
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
            granularity = str + x;

            uriStr = "https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') +
                    "/candles?count=" + numCandles + "&price=M&granularity=" + granularity;

            //  String uriStr = "https://api.pro.coinbase.com/" +
//                    "products/" + tradePair.toString('-') + "/candles" +
//                    "?granularity=" + secondsPerCandle +
//                    "&start=" + startDateString +
//                    "&end=" + endDateString;

            if (startTime == EARLIEST_DATA) {
                // signal more data is false
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            requestBuilder.uri(URI.create(uriStr));
            //requestBuilder.header("CB-AFTER", String.valueOf(afterCursor.get()));

//            Request
//            Request Parameters
//            Name	Located In	Type	Description
//            Authorization	header	string	The authorization bearer token previously obtained by the client [required]
//            Accept-Datetime-Format	header	AcceptDatetimeFormat	Format of DateTime fields in the request and response.
//                    accountID	path	AccountID	Account Identifier [required]
//            instrument	path	InstrumentName	Name of the Instrument [required]
//            price	query	PricingComponent	The Price component(s) to get candlestick data for. [default=M]
//            granularity	query	CandlestickGranularity	The granularity of the candlesticks to fetch [default=S5]
//            count	query	integer	The number of candlesticks to return in the response. Count should not be specified if both the start and end parameters are provided, as the time range combined with the granularity will determine the number of candlesticks to return. [default=500, maximum=5000]
//            from	query	DateTime	The start of the time range to fetch candlesticks for.
//            to	query	DateTime	The end of the time range to fetch candlesticks for.
//            smooth	query	boolean	A flag that controls whether the candlestick is “smoothed” or not. A smoothed candlestick uses the previous candle’s close price as its open price, while an unsmoothed candlestick uses the first price from its time range as its open price. [default=False]
//            includeFirst	query	boolean	A flag that controls whether the candlestick that is covered by the from time should be included in the results. This flag enables clients to use the timestamp of the last completed candlestick received to poll for future candlesticks but avoid receiving the previous candlestick repeatedly. [default=True]
//            dailyAlignment	query	integer	The hour of the day (in the specified timezone) to use for granularities that have daily alignments. [default=17, minimum=0, maximum=23]
//            alignmentTimezone	query	string	The timezone to use for the dailyAlignment parameter. Candlesticks with daily alignment will be aligned to the dailyAlignment hour within the alignmentTimezone. Note that the returned times will still be represented in UTC. [default=America/New_York]
//            weeklyAlignment	query	WeeklyAlignment	The day of the week used for granularities that have weekly alignment. [default=Friday]
//            units	query	DecimalNumber	The number of units used to calculate the volume-weighted average bid and ask prices in the returned candles. [default=1]
//


            boolean smooth = true;
            boolean includeFirst = true;
            String dailyAlignment = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    + "T00:00:00Z";
            String alignmentTimezone = "America/New_York";
            String weeklyAlignment = "Wednesday";
            int units = 2;
            String body =
                    "{\n" +
                            "  \"instrument\": \"" + tradePair.toString('_') + "\",\n" +
                            "  \"price\": \"" + granularity + "\",\n" +
                            "  \"count\": " + numCandles + ",\n" +
                            "  \"from\": \"" + startDateString + "\",\n" +
                            "  \"to\": \"" + endDateString + "\",\n" +
                            "  \"smooth\": " + smooth + ",\n" +
                            "  \"includeFirst\": " + includeFirst + ",\n" +
                            "  \"dailyAlignment\": " + dailyAlignment + ",\n" +
                            "  \"alignmentTimezone\": \"" + alignmentTimezone + "\",\n" +
                            "  \"weeklyAlignment\": \"" + weeklyAlignment + "\",\n" +
                            "  \"units\": " + units + "\n" +
                            "}";

            return client.sendAsync(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                        Log.info("Oanda response: ", response);
                        JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);

                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }

                        List<CandleData> candleData = null;
                        if (!res.isEmpty()) {

                            logger.info("Oanda candle: " + res);


                            // Remove the current in-progress candle
                            logger.info("Removing in-progress candle for " + tradePair.toString('_') + " " + startTime);
                            //  logger.info("Oanda size " + res.get("candles").size());

                            int time = (int) Date.from(Instant.parse(res.findValue("time").asText())).getTime();//("time").asText())).getTime();
                            logger.info("Oanda time " + time);

                            ArrayNode candles = (ArrayNode) res.get("candles");
                            if (time + secondsPerCandle > endTime.get()) {
                                candles.remove(res.get("candles").size() - 1);

                                logger.info("Oanda remove " + res.findValue("time").asText());
                            }
                            endTime.set(startTime);
                            candleData = new ArrayList<>();


                            for (JsonNode candle : candles) {
                                double volume = candle.findValue("volume").asDouble();
                                logger.info("Oanda volume " + volume);
                                candleData.add(new CandleData(
                                        candle.get("mid").get("o").asDouble(),  // open price
                                        candle.get("mid").get("c").asDouble(),  // close price
                                        candle.get("mid").get("h").asDouble(),  // high price
                                        candle.get("mid").get("l").asDouble(),  // low price

                                        time,  // time
                                        volume
                                ));
                                logger.info(
                                        "CandleData: " + candleData
                                );

                            }
                            candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                        } else {
                            logger.info("No candles");


                        }
                        return candleData;

                    });
        }

        public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

        public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
    }

}