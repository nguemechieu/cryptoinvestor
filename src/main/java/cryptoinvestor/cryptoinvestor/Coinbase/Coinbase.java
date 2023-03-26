package cryptoinvestor.cryptoinvestor.Coinbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cryptoinvestor.cryptoinvestor.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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


public class Coinbase extends Exchange {
    public static final Logger logger = LoggerFactory.getLogger(Coinbase.class);
    public static final String API_VERSION = "v2";


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Set<TradePair> tradePair=
            new HashSet<>() {{
                add(new TradePair("BTC", "USD"));
                add(new TradePair("BTC", "EUR"));
                add(new TradePair("ETH", "USD"));
                add(new TradePair("ETH", "EUR"));
                add(new TradePair("LTC", "USD"));
                add(new TradePair("LTC", "EUR"));
                add(new TradePair("BCH", "USD"));
                add(new TradePair("BCH", "EUR"));
                add(new TradePair("XRP", "USD"));
                add(new TradePair("XRP", "EUR"));
                add(new TradePair("ZEC", "USD"));
                add(new TradePair("ZEC", "EUR"));
                add(new TradePair("DASH", "USD"));
                add(new TradePair("DASH", "EUR"));
                add(new TradePair("ETC", "USD"));
                add(new TradePair("ETC", "EUR"));
                add(new TradePair("XMR", "USD"));
            }};
    private static final ExchangeWebSocketClient websocket = new CoinbaseWebSocketClient(tradePair);
    static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

    String apiKey;



    static TelegramClient telegramBot;
     static String account_id;

    public Coinbase(String account_id,String apiKey, String api_secret) throws NoSuchAlgorithmException {
        super(websocket);


        this.apiKey = apiKey;
        Coinbase.account_id =account_id;

        requestBuilder.header("CB-ACCESS-KEY", apiKey);
        requestBuilder.header("CB-ACCESS-PASSPHRASE", api_secret);
        requestBuilder.header("CB-ACCESS-SIGNATURE", timestampSignature(apiKey, api_secret));
        requestBuilder.header("CB-ACCESS-TIMESTAMP", new Date().toString());
        requestBuilder.header("CB-ACCESS-VERSION", API_VERSION);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Accept-Language", "en-US,en;q=0.9");
        requestBuilder.header("Origin", "https://www.coinbase.com");
        requestBuilder.header("Referer", "https://www.coinbase.com/");
        requestBuilder.header("Sec-Fetch-Dest", "empty");
        requestBuilder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
        requestBuilder.header("Sec-Fetch-Dest", "empty");
        requestBuilder.header("Sec-Fetch-Mode", "cors");
        requestBuilder.header(
                "Access-Control-Allow-Credentials",
                "true"
        );

        requestBuilder.header(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        logger.info("Coinbase initialized");

    }
    static HttpClient client =  HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();


//
//
//
//
//    Advanced Trade endpoint URL: /api/v3/brokerage/{resource}
//
//    API	Method	Resource	Required Scope
//    List Accounts	GET	/accounts	wallet:accounts:read
static String url = "https://api.coinbase.com/api/v3/brokerage/";
    @Contract(pure = true)
    public static @NotNull List<Account> getAccountsList() throws IOException, InterruptedException {

        requestBuilder.uri(URI.create(url + "accounts"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if(data.statusCode() != 200) {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();


        }  else {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            List<Account> accounts = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + accounts.size());

            ListView <Account> accountsListView = new ListView<>();
            for (Account account : accounts) {
                accountsListView.getItems().add(account);
            }
            accountsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue!= null) {
                    try {
                        Account account = getAccount(newValue.getId());
                        assert account != null;
                        account.setBalance(newValue.getBalance());
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

    @Contract(pure = true)
    public static @NotNull String getCoinbaseMessage() {
        return "Coinbase";
    }

    //    Get Account	GET	/accounts/:account_id	wallet:accounts:read
    static Account getAccount(String accountId) throws IOException, InterruptedException {

    new Account();
    Account account;
        requestBuilder.uri(URI.create(url + "accounts/" + account_id ));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(account_id));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if(data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            account = OBJECT_MAPPER.readValue(data.body(), Account.class);
            logger.info("Coinbase: " + account.toString());
        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return account;
    }
//    Create Order	POST	/orders	wallet:buys:create
    Order createOrder(@NotNull Order order) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "orders"));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(order.toString()));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if(data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            order = OBJECT_MAPPER.readValue(data.body(), Order.class);
            logger.info("Coinbase: " + order.toString());
        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return order;
    }
//    Cancel Orders	POST	/orders/batch_cancel	wallet:buys:create
    Order cancelOrder(String orderId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "orders/batch_cancel"));

        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(orderId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Order order;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            order = OBJECT_MAPPER.readValue(data.body(), Order.class);
            logger.info("Coinbase: " + order.toString());
        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return order;
    }
//    List Orders	GET	/orders/historical/batch	wallet:orders:read
    List<Order> getOrdersHistorical(String orderId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "orders/historical/batch"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Order> orders;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            orders = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + orders.size());
            ListView<Order> orderListView = new ListView<>();

            for (Order order : orders) {
                orderListView.getItems().add(order);
            }


            Stage stage = new Stage();
            Scene scene = new Scene(orderListView);
            stage.setScene(scene);
            stage.show();

        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return orders;
    }
//    List Fills	GET	/orders/historical/fills	wallet:transactions:read
    List<Fill> getFills() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "orders/historical/fills"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Fill> fills;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            fills = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + fills.size());
        }
        else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return fills;
    }
//    Get Order	GET	/orders/historical/{order_id}	wallet:transactions:read
    Order getOrderHistorical(String orderId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "orders/historical/" + orderId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Order order;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            order = OBJECT_MAPPER.readValue(data.body(), Order.class);
            logger.info("Coinbase: " + order.toString());

            ListView<Order> orderListView=new ListView<>();
            orderListView.getItems().add(order);
            orderListView.getSelectionModel().select(order);
            logger.info("Coinbase: " + orderListView.getSelectionModel().getSelectedItem().toString());

            Stage stage = new Stage();
            stage.setScene(new Scene(orderListView));
            stage.setTitle("Coinbase");
            stage.setResizable(false);
            stage.show();


        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return order;
    }
//    List Products	GET	/products	wallet:user:read
    List<Product> getProducts() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "products"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Product> products;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            products = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + products.size());

            ListView<Product> productListView=new ListView<>();

            productListView.getItems().addAll(products);

            productListView.getSelectionModel().select(products.get(0));
            logger.info("Coinbase: " + productListView.getSelectionModel().getSelectedItem().toString());

            Stage stage = new Stage();
            stage.setScene(new Scene(productListView));
            stage.setTitle("Coinbase");

            stage.show();
        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return products;
    }
//    Get Product	GET	/products/{product_id}	wallet:user:read
    Product getProduct(String productId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "products/" + productId));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        Product product;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            product = OBJECT_MAPPER.readValue(data.body(), Product.class);
            logger.info("Coinbase: " + product.toString());
        } else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return product;
    }
//    Get Product Candles	GET	/products/{product_id}/candles	none
    List<Candle> getProductCandles(String productId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "products/" + productId + "/candles"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Candle> candles;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            candles = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + candles.size());
        }
        else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return candles;
    }
//    Get Market Trades	GET	/products/{product_id}/ticker	wallet:user:read
    List<Trade> getMarketTrades(String productId) throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "products/" + productId + "/ticker"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        List<Trade> trades;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            trades = OBJECT_MAPPER.readValue(data.body(), new TypeReference<>() {
            });
            logger.info("Coinbase: " + trades.size());
        }
        else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return trades;
    }
//    Get Transactions Summary	GET	/transaction_summary	wallet:transactions:read

    TransactionSummary getTransactionSummary() throws IOException, InterruptedException {
        requestBuilder.uri(URI.create(url + "transaction_summary"));
        HttpResponse<String> data = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        TransactionSummary transactionSummary;
        if (data.statusCode() == 200) {
            logger.info("Coinbase: " + data.statusCode() + " " + data.body());
            transactionSummary = OBJECT_MAPPER.readValue(data.body(), TransactionSummary.class);
            logger.info("Coinbase: " + transactionSummary.toString());
        }
        else {
            logger.error("Coinbase: " + data.statusCode() + " " + data.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Coinbase Error");
            alert.setHeaderText(null);
            alert.setContentText(data.body());
            alert.showAndWait();
            return null;
        }
        return transactionSummary;
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
//    Advanced Trade Endpoints





















    @Override
    public String getName() {
        return
                "COINBASE";
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



    private @Nullable String timestampSignature(
            String apiKey,
            String passphrase
    ) throws NoSuchAlgorithmException {
        Objects.requireNonNull(apiKey);
        Objects.requireNonNull(passphrase);

        String timestamp = new Date().toString();
        String stringToSign = timestamp + "\n" + apiKey + "\n" + passphrase;

//        try {
//            byte[] hash = MessageDigest.getInstance("SHA-256").digest(stringToSign.getBytes());
//            return Base64.getEncoder().encodeToString(hash);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }

return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(stringToSign.getBytes()));
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

                    Log.info("response headers: ", response.headers().toString());
                    if (response.headers().firstValue("CB-AFTER").isEmpty()) {
                        futureResult.completeExceptionally(new RuntimeException(
                                "Coinbase trades response did not contain header \"CB-AFTER\": " + response));
                        return;
                    }

                    afterCursor.setValue(Integer.valueOf((response.headers().firstValue("CB-AFTER").get())));

                    JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());

                    if (!tradesResponse.isArray()) {
                        futureResult.completeExceptionally(new RuntimeException("coinbase trades response was not an array!"));


                    } else if (tradesResponse.isEmpty()) {
                        futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
                    } else if (tradesResponse.has("message")) {


                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Coinbase Error");
                        alert.setHeaderText("Coinbase Error");
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
        String startDateString1 = ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
                currentCandleStartedAt, ZoneOffset.UTC));
        long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
        // Get the closest supported granularity to the ideal granularity.
        int actualGranularity = getCandleDataSupplier(secondsPerCandle, tradePair).getSupportedGranularities().stream()
                .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
                .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));

        return client.sendAsync(
                        HttpRequest.newBuilder()
                                .uri(URI.create(String.format(
                                        "https://api.pro.coinbase.com/products/%s/candles?granularity=%s&start=%s",
                                        tradePair.toString('-'), actualGranularity, startDateString1)))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Coinbase response: ", response);
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


    public JSONObject getJSON() {

        JSONObject jsonObject = new JSONObject();
        try {

          requestBuilder.uri(URI.create("https://api.coinbase.com/v2/exchange-rates"));
          HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
          System.out.println(response.body());
          System.out.println(response.statusCode());
          System.out.println(response.headers());

          if (response.statusCode()!= 200) {
              logger.info(
                      String.format("Coinbase response: %d %s", response.statusCode(), response.body())
              );
          }else {
              jsonObject.put("rates", response.body());
          }




        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        out.println(jsonObject.toString(4));
        return jsonObject;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {


        System.out.println("Connected");
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4) + " " + handshake);

    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
        JSONObject jsonObject = getJSON();
        System.out.println(jsonObject.toString(4));

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

    @Override
    public String getSymbol() {
        return tradePair.toString();
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

    // Get all orders
     //       GET
    //https://api.exchange.coinbase.com/orders

    public void getAllOrders() throws IOException, InterruptedException {
        String uriStr = "https://api.exchange.coinbase.com/orders";

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


  //  Get single order
      //      GET
    //https://api.exchange.coinbase.com/orders/{order_id}


    public  void getOrder(String orderId) throws IOException, InterruptedException {
        String uriStr = "https://api.exchange.coinbase.com/orders/" + orderId;
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

    public  void cancelOrder(long orderId) throws IOException, InterruptedException {

        String uriStr = "https://api.exchange.coinbase.com/orders/" + orderId;
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
        String uriStr = "https://api.coinbase.com/api/v3/brokerage/orders";
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

        String symbol = tradePair.toString('-');

        String uriStr = "https://api.coinbase.com/api/v3/brokerage/orders";

        String data=
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

        }else {
            JSONObject jsonObject = new JSONObject(response.body());


            System.out.println(jsonObject.toString(4));
        }





    }
public void CloseAllOrders() throws IOException, InterruptedException {
        String uriStr ="https://api.coinbase.com/api/v3/brokerage/orders/batch_cancel";
        System.out.println(uriStr);
        HttpRequest.Builder request = HttpRequest.newBuilder();
        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.DELETE();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        System.out.println(response.body());
        if (response.statusCode()!= 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            telegramBot.sendMessage("Error: " + response.body());
        }
        else {
            JSONObject jsonObject = new JSONObject(response.body());

            telegramBot.sendMessage(jsonObject.toString(4));
            System.out.println(jsonObject.toString(4));
        }

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
            return client.sendAsync(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {


                        Log.info("coinbase response: ", response);
                        JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);

 if (res.has("message")) {

    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(res.get("message").asText());
    alert.showAndWait();
}

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
                                logger.info(
                                        "CandleData: " + candleData
                                );
                            }

                            candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                            return candleData;
                        } else {
                            logger.info("Coinbase Empty response");
                            return Collections.emptyList();
                        }
                    });
        }

        public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

        public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
    }

}