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
    private final String apiKey;


    public String getAccountID() {
        return accountID;
    }

    private final String accountID;
   public static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
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
                        60, 60 * 5, 60 * 15, 3600, 3600 * 6, 3600 * 24,
                        3600 * 24 * 7, 3600 * 24 * 30, 3600 * 24 * 30 * 7, 3600 * 24 * 30 * 365
                ));

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
                String uriStr = "https://api-fxtrade.oanda.com/v3/";
                uriStr += "/instruments/"+tradePair.getBaseCurrency().getCode()+"/candles?count=6&price=M&granularity=M30";

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

    public JSONObject getJSON(TradePair tradePair) {

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

            String uriStr ="https://api-fxtrade.oanda.com/v3/instruments/"+tradePair.toString('_')+"/candles?count=6&price=M&granularity=M30";

         if (startTime == EARLIEST_DATA) {
//                // signal more data is false
               return CompletableFuture.completedFuture(Collections.emptyList());
        }
            requestBuilder.uri(URI.create(uriStr));
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