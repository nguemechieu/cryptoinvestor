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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static java.lang.System.out;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class Oanda extends Exchange {


    public double dividendAdjustment;
    public double unrealizedPL;
    public double resettablePL;
    public int units;
    public double financing;
    public double guaranteedExecutionFees;
    public double pl;
    public double margin;
    public double commission;

    public double marginPercentage;
    public double commissionAmount;



    public double getPrice(@NotNull TradePair tradePair) throws IOException, InterruptedException {


        //GET	/v3/accounts/{accountID}/pricing

        String url = "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/pricing?instruments=" + tradePair.toString('_');


        requestBuilder.timeout(Duration.ofSeconds(5));
        requestBuilder.uri(URI.create(url));
        client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.registerModule(new JavaTimeModule());
        JsonNode node = mapper.readTree(response.toString());
        logger.info("price "+node.toString());
        return node.get("price").asDouble();

    }

    static HttpClient client;
    static HttpRequest.Builder requestBuilder=HttpRequest.newBuilder();

    public static @NotNull ArrayList<String> getInstruments() throws IOException, InterruptedException {

        //GET    /v3/accounts/{accountID}/instruments


        //requestBuilder.uri(URI.create("https://api-fxtrade.oanda.com/v3/accounts/"+accountID+"/instruments/"+tradePair.toString('_'))); ;
HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instruments").openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + getAPI_KEY());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        ArrayList<String> instruments=new ArrayList<>();
        while ((inputLine = in.readLine())!= null) {

            out.println(inputLine);
            instruments.add(inputLine);
        }
        in.close();

        JsonNode node=new ObjectMapper().readTree(String.valueOf(instruments));
        for (JsonNode instrument : node) {
out.println(instrument.toString());
            instruments.add(instrument.asText());

        }







        return instruments;
    }



    public double getDividendAdjustment() {
        return dividendAdjustment;
    }

    public void setDividendAdjustment(double dividendAdjustment) {
        this.dividendAdjustment = dividendAdjustment;
    }

    public double getUnrealizedPL() {
        return unrealizedPL;
    }

    public void setUnrealizedPL(double unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    public double getResettablePL() {
        return resettablePL;
    }

    public void setResettablePL(double resettablePL) {
        this.resettablePL = resettablePL;
    }

    @Override
    public String toString() {
        return "MYSHORT{" +
                "dividendAdjustment=" + dividendAdjustment +
                ", unrealizedPL=" + unrealizedPL +
                ", resettablePL=" + resettablePL +
                ", units=" + units +
                ", financing=" + financing +
                ", guaranteedExecutionFees=" + guaranteedExecutionFees +
                ", pl=" + pl +
                '}';
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getFinancing() {
        return financing;
    }

    public void setFinancing(double financing) {
        this.financing = financing;
    }

    public double getGuaranteedExecutionFees() {
        return guaranteedExecutionFees;
    }

    public void setGuaranteedExecutionFees(double guaranteedExecutionFees) {
        this.guaranteedExecutionFees = guaranteedExecutionFees;
    }

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }
    private static final Logger logger = LoggerFactory.getLogger(Oanda.class);


    private static String accountID;
    protected static String API_KEY;

    //"wss://api-fxtrade.oanda.com/v3/accounts/";
    private static final Accounts accounts = new Accounts();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public Oanda(@NotNull TradePair tradePair, String api_key, String accountID, String token) throws IOException, TelegramApiException, InterruptedException {
        super("ws://api-fxtrade.oanda.com", api_key, token);
        API_KEY = api_key;

       Exchange. tradePair =tradePair;
       Oanda.accountID = accountID;

        accounts.setAccountID(accountID);
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + api_key);


//        HTTP/1.1 200 OK
//        Access-Control-Allow-Headers: Authorization, Content-Type, Accept-Datetime-Format
//        Content-Encoding: gzip
//        Transfer-Encoding: chunked
//        Server: openresty/1.7.0.1
//        Connection: keep-alive
//
        int beforeID = 32134;
 //requestBuilder.header("<Link:<https://api-fxtrade.oanda.com/v3/accounts/" +accountID+"/trades?"+ beforeID+"&instrument="+tradePair.toString('_')  + "rel=\"next\">");
//        Date: Wed, 22 Jun 2016 18:41:48 GMT
//        Access-Control-Allow-Origin: *
//        Access-Control-Allow-Methods: PUT, PATCH, POST, GET, OPTIONS, DELETE
//        Content-Type: application/json
        //Access-Control-Max-Age: 3600
        requestBuilder.header(
                "Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept-Datetime-Format");
        requestBuilder.header("Access-Control-Allow-Origin", "*");
        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        requestBuilder.header("Content-Encoding", "gzip");

logger.info(
        "OANDA_API_KEY: " + API_KEY + "\n" +
                "OANDA_ACCOUNT_ID: " + accountID + "\n" +
                "OANDA_TOKEN: " + token + "\n" +
                "OANDA_API_URL: " + "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instrument="+tradePair.toString('_')
);


    }



    public void setAccountID(String accountID) {
        Oanda.accountID = accountID;
    }

    public static String getAccountID() {
        return accountID;
    }
    public static void setAPI_KEY(String API_KEY) {
        Oanda.API_KEY = API_KEY;
    }
    public static String getAPI_KEY() {
        return API_KEY;
    }
//    Order Endpoints
//
//
//
//   POST	/v3/accounts/{accountID}/orders
//    Create an Order for an Account

    public boolean CreateOrder(@NotNull Order order) throws IOException, InterruptedException {

        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));

        String[] body = new String[]{

                "order{ units:" + order.getUnit() + ", instrument:" + order.getTradePair().toString('_') + ", side:" + order.getSide() +
                        ", type:" + order.getType() + ", timeInForce:" + order.getTimeInForce() +
                        ", price:" + order.getPrice() + ", timeInForce:" + order.getTimeInForce() + ", positionFill:" + POSITION_FILL.DEFAULT + " }"

        };
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders"));

       //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders"));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 201||response.statusCode() == 200) {


            orderMap.put(order, order);
            return true;
        } else {

            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return false;
        }
    }
//
//    GET	/v3/accounts/{accountID}/orders
//    Get a list of Orders for an Account

    public List<Order> GetOrders() throws IOException, InterruptedException {

           //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders"));

              HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }

    ConcurrentHashMap<Order,Order> orderMap = new ConcurrentHashMap<>();
//
//    GET	/v3/accounts/{accountID}/pendingOrders
//    List all pending Orders in an Account

    public List<Order> GetPendingOrders() throws IOException, InterruptedException {
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/pendingOrders"));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }

//
//    GET	/v3/accounts/{accountID}/orders/{orderSpecifier}
    //Get details for a single Order in an Account

    public Order GetOrder(String orderSpecifier) throws IOException, InterruptedException {
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders/" + orderSpecifier));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }
//
//
//    PUT	/v3/accounts/{accountID}/orders/{orderSpecifier}
//    Replace an Order in an Account by simultaneously cancelling it and creating a replacement Order

    public Order ReplaceOrder(String orderSpecifier,ORDER_TYPES type,long orderId, double price, double takeProfit, double stopLoss) throws IOException, InterruptedException {


        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders/" + orderSpecifier));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
//
//        {
//            "order": {
//            "timeInForce": "GTC",
//                    "price": "1.7000",
//                    "type": "TAKE_PROFIT",
//                    "tradeID": "6368"
//        }


        String body = "{\n" +
                "  \"order\": {\n" +
                "    \"timeInForce\": \"GTC\",\n" +
                "    \"price\": \""+price+"\",\n" +
                "    \"type\": \""+type+"\",\n" +
                "    \"tradeID\": \""+orderId+"\"\n" +
                "}\n " + "}";



        requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)));

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }

//
//
//    PUT	/v3/accounts/{accountID}/orders/{orderSpecifier}/cancel
//    Cancel a pending Order in an Account

    public Order CancelOrder(long orderID) throws IOException, InterruptedException {
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders/" + orderID + "/cancel"));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }
//
//    PUT	/v3/accounts/{accountID}/orders/{orderSpecifier}/clientExtensions
//    Update the Client Extensions for an Order in an Account. Do not set, modify, or delete clientExtensions if your account is associated with MT4.

    public Order UpdateClientExtensions(String orderSpecifier) throws IOException, InterruptedException {
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/orders/" + orderSpecifier + "/clientExtensions"));

        requestBuilder.header("Access-Control-Allow-Origin", "*");
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logger.info(response.toString());
        if (response.statusCode() == 200) {
            Object da = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
            });
            logger.info(da.toString());
            return OBJECT_MAPPER.convertValue(da, new TypeReference<>() {
            });
        }
        else {
            Alert alert =new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return null;
        }
    }
//

    public boolean CloseAll( )throws IOException, InterruptedException {

        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);
        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instrument=" + tradePair.toString('_') + "?rel=next"));

        requestBuilder.header("Access-Control-Allow-Origin", "*");

        requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
        //Content-Encoding: gzip
        //requestBuilder.header("Content-Encoding", "gzip");
        requestBuilder.timeout(Duration.ofMillis(10000));
        requestBuilder.uri(
                URI.create("https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instrument=" + tradePair.toString('_') + "?rel=next"));

   HttpResponse<String>response=client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

   if (response.statusCode() != 200) {
       logger.info("OANDA_API_KEY: " + API_KEY + "\n" +
                "OANDA_ACCOUNT_ID: " + accountID + "\n" +

                "OANDA_API_URL: " + "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/instrument="+tradePair.toString('_'));


       Alert alert = new Alert(Alert.AlertType.ERROR);
       alert.setTitle("Close All Error");
       alert.setHeaderText(null);
       alert.setContentText(response.body());
       alert.showAndWait();
       return false;
   }

        return true;
    }



    @Override
    public String getName() {
        return

                "OANDA";
    }

    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return new OandaCandleDataSupplier(200,tradePair) {
            @Override
            public CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
                return
                        CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
                return
                        CompletableFuture.completedFuture(Collections.emptyList());
            }
        };
    }

    public static double getMarginPercent() {
        return accounts.getMarginPercent();
    }

    public String getBalance() {
        return String.valueOf(accounts.getBalance());
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
                uriStr += "v3/accounts/" + accountID + "/trades?&instrument="+ tradePair.toString('_');


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
                        logger.info("Oanda got06 " + tradesResponse + " trades");
                        for (int j = 0; j < tradesResponse.size(); j++) {
                            JsonNode trade = tradesResponse.get(j);
                            Instant time = Instant.from(ISO_INSTANT.parse(trade.get("time").asText()));
                            if (time.compareTo(stopAt) <= 0) {
                                futureResult.complete(tradesBeforeStopTime);
                                break;
                            } else {
                                tradesBeforeStopTime.add(new Trade(tradePair,
                                        DefaultMoney.ofFiat(trade.get("price").asText(), String.valueOf(tradePair.getCounterCurrency())),
                                        DefaultMoney.ofFiat(trade.get("size").asText(), String.valueOf(tradePair.getBaseCurrency())),
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
        HttpRequest.Builder re = HttpRequest.newBuilder().uri(URI.create(
                        "https://api-fxtrade.oanda.com/v3/accounts/" +
                                        accountID + "instruments/" + tradePair.toString('_') +
                                        "/candles?price=BA&from=2016-10-17T15%3A00%3A00.000000000Z&granularity=" +
                                        granularity

                                ));

        re.header("Authorization", "Bearer " + apiKey);
        re.header("Content-Type", "application/json");
        re.header("Accept", "application/json");
        return HttpClient.newHttpClient().sendAsync(
                       re.build(), HttpResponse.BodyHandlers.ofString())

                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Log.info("Oanda Candles response 0 got : " , response);
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





    @NotNull
    public JSONObject getJSON() {

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

    public void createOrder(@NotNull TradePair tradePair, @NotNull ENUM_ORDER_TYPE orderType, String side, double size, double stoploss, double takeprofit) throws IOException, InterruptedException {


        JSONObject jsonObject = getJSON();

        String uriStr = "https://api-fxtrade.oanda.com/" +
                "v3/accounts/"+accountID +"/orders/"+ tradePair.toString('_') +
                "?side=" + side +
                "&type=market" +
                "&quantity=" + size +
                "&price=" + jsonObject.getJSONObject("data").getJSONObject("rates").getDouble("USD");





        if (orderType.equals(ENUM_ORDER_TYPE.LIMIT)) {
            uriStr = uriStr +
                    "&limitPrice=" + jsonObject.getJSONObject("data").getJSONObject("rates").getDouble("USD") +
                    "&stopPrice=" + stoploss +
                    "&takeProfitPrice=" + takeprofit;
        }else if (orderType.equals(ENUM_ORDER_TYPE.STOP_LOSS)) {
            uriStr = uriStr +
                    "&stopPrice=" + stoploss +
                    "&takeProfitPrice=" + takeprofit;
        }else if (orderType.equals(ENUM_ORDER_TYPE.TRAILING_STOP_LOSS)) {
            uriStr = uriStr +
                    "&trailingStopPrice=" + stoploss +
                    "&takeProfitPrice=" + takeprofit;
        }else if (orderType.equals(ENUM_ORDER_TYPE.MARKET)){
            uriStr = uriStr +
                    "&stopPrice=" + stoploss +
                    "&takeProfitPrice=" + takeprofit;
        }


        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);//    API key as a string

        HttpResponse<String> response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());


        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Order Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return;
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());


        System.out.println(uriStr);




    }
 public static final    ArrayList<Trade> trades=new ArrayList<>();

    public void createOrder(@NotNull TradePair tradePair, double price, @NotNull ENUM_ORDER_TYPE type, Side side, double quantity, double stopLoss, double takeProfit) throws IOException, InterruptedException {




        String uriStr = "https://api-fxtrade.oanda.com/" +
                "v3/accounts/"+accountID +"/orders/"+ tradePair.toString('_') +
                "?side=" + side +
                "&type=market" +
                "&quantity=" + quantity +
                "&price=" + price;


        if (type.equals(ENUM_ORDER_TYPE.LIMIT)) {
            uriStr = uriStr +
                    "&limitPrice=" + price+
                    "&stopPrice=" + stopLoss +
                    "&takeProfitPrice=" + takeProfit;
        }else if (type.equals(ENUM_ORDER_TYPE.STOP_LOSS)) {
            uriStr = uriStr +
                    "&stopPrice=" + stopLoss +
                    "&takeProfitPrice=" + takeProfit;
        }else if (type.equals(ENUM_ORDER_TYPE.TRAILING_STOP_LOSS)) {
            uriStr = uriStr +
                    "&trailingStopPrice=" + stopLoss +
                    "&takeProfitPrice=" + takeProfit;
        }else if (type.equals(ENUM_ORDER_TYPE.MARKET)){
            uriStr = uriStr +
                    "&stopPrice=" + stopLoss +
                    "&takeProfitPrice=" + takeProfit;
        }


        requestBuilder.uri(URI.create(uriStr));
        requestBuilder.header("Accept", "application/json");
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Authorization", "Bearer " + API_KEY);//    API key as a string

        HttpResponse<String> response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());


        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
            System.out.println(response.body());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Order Error");
            alert.setHeaderText(null);
            alert.setContentText(response.body());
            alert.showAndWait();
            return;
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());


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
            String uriStr;
            //uriStr = "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?beforeID=6397&instruments=" + tradePair.toString('_');
            // requestBuilder.header("Link", "<https://api-fxtrade.oanda.com/v3/accounts/" + accountID
             //       + "/trades?beforeID=6397&instrument="+ tradePair.toString('_')+"&rel=next");

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
            String uriStr1 = "https://api-fxtrade.oanda.com/v3/accounts/" + accounts.getAccountID() + "/trades?instruments=" + tradePair.toString('_');
            requestBuilder.uri(URI.create(uriStr1));
            requestBuilder.header("CB-BEFORE", startDateString);
            requestBuilder.header("CB-AFTER", endDateString);
            //requestBuilder.header("CB-AFTER", String.valueOf(afterCursor.get()));
            return HttpClient.newHttpClient().sendAsync(
                            requestBuilder.build(),
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(response -> {
                          JsonNode res;
                        try {
                            res = OBJECT_MAPPER.readTree(response);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }

                        Log.info("Oanda trade2 response got -->: " , res.toString());

                        JsonNode res1 = res;
                        if (!res.isEmpty()) {
                            try {
                                JsonNode trad = OBJECT_MAPPER.readTree(res.get("trades").toString());
                                logger.info("Got " + res.size() + " candles" + res);
                                List<CandleData> candleData;
                                Trade trade =        new Trade();
                                for (JsonNode tr : trad) {
                                    //   {"trades":[{"id":"142950","instrument":"EUR_USD","price":"1.07669","openTime":"2023-03-21T16:56:10.786314295Z","initialUnits":"-1700","initialMarginRequired":"36.6098","state":"OPEN","currentUnits":"-1700","realizedPL":"0.0000","financing":"0.1828","dividendAdjustment":"0.0000","clientExtensions":{"id":"140660466","tag":"0"},"unrealizedPL":"-21.6750","marginUsed":"37.0382"},{"id":"124829","instrument":"USD_CAD","price":"1.38016","openTime":"2023-03-15T14:46:04.088679752Z","initialUnits":"4000","initialMarginRequired":"80.0000","state":"OPEN","currentUnits":"4000","realizedPL":"0.0000","financing":"-0.7802","dividendAdjustment":"0.0000","clientExtensions":{"id":"140494560","tag":"0"},"unrealizedPL":"-45.2400","marginUsed":"80.0000"}],"lastTransactionID":"142955"}
                                    res = tr;
                                    trade.setTradePair(tradePair);
                                    trade.setAccountID(accountID);
                                    trade.setTradeID(res.get("id").asLong());
                                    trade.setInstrument(tradePair.toString('_'));
                                    trade.setPrice(res.get("price").asDouble());
                                    trade.setOpenTime(res.get("openTime").asInt());
                                    if (res.has("closeTime")) {
                                        trade.setCloseTime(res.get("closeTime").asInt());
                                    }
                                    trade.setVolume(res.get("currentUnits").asDouble());
                                    if (res.has("openTime")) {
                                        trade.setOpenTime(res.get("openTime").asInt());
                                    }

                                    trade.setInstrument(res.get("instrument").asText());
                                    trade.setFinancing(res.get("financing").asDouble());
                                    trade.setRealizedPL(res.get("realizedPL").asDouble());
                                    trade.setMarginUsed(res.get("marginUsed").asDouble());
                                    trade.setInitialUnits(res.get("initialUnits").asDouble());
                                    trade.setInitialMarginRequired(res.get("initialMarginRequired").asDouble());
                                    trade.setState(res.get("state").asText());
                                    trade.setCurrentUnits(res.get("currentUnits").asDouble());
                                    trade.setUnrealizedPL(res.get("unrealizedPL").asDouble());
                                    trade.setDividendAdjustment(res.get("dividendAdjustment").asDouble());
                                    trade.setClientExtensions(res.get("clientExtensions").get("id").asText());


                                    logger.info(trade.toString());

                                    trades.add(trade);


                                    logger.info("Got " + res.size() + " candles" + res);
                                    // Remove the current in-progress candle

                                    try {
                                        if (res1.has("openTime")) {

                                            if (Date.from(Instant.parse(res1.get("trades").get("openTime").asText())).getTime() + secondsPerCandle > endTime.get()) {

                                                ((ArrayNode) res1).remove(0);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                                trade.setLastTransactionID(res1.get("lastTransactionID").asLong());
                                endTime.set(startTime);





                                candleData = new ArrayList<>();
                                for (JsonNode candle : res1) {
                                    try {

                                 if (candle.has("openTime")) {
                                     int time = candle.get("openTime").asInt();
                                     candleData.add(new CandleData(0.1, 0, 0, 0,
//                                                candle.get("candles").get("bid").get("o").asDouble(),  // open price
//                                                candle.get("candles").get("bid").get("c").asDouble(),  // close price
//                                                candle.get("candles").get("bid").get("h").asDouble(),  // high price
//                                                candle.get("candles").get("bid").get("l").asDouble(),  // low price

                                             time,     // open time

                                             candle.get("volume").asDouble()  // volume
                                     ));
                                 }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                                candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
                                return candleData;
                            }
                            catch (Exception e) {
                                e.printStackTrace();

                            return Collections.emptyList();
                        }}
                        return null;
                    });
        }

        public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

        public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
    }

}