package cryptoinvestor.cryptoinvestor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
import cryptoinvestor.cryptoinvestor.Coinbase.Coinbase;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cryptoinvestor.cryptoinvestor.Currency.NULL_FIAT_CURRENCY;
import static java.lang.System.out;

public class CurrencyDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyDataProvider.class);
    private static final ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> CURRENCIES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<CryptoMarketData, CryptoMarketData> MARKET_DATA_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();
    private static Roi roi;

    static Set<TradePair> tradePairs=
            new HashSet<>() {{
                add(new TradePair("BTC", "USD"));
                add(new TradePair("ETH", "USD"));
                add(new TradePair("LTC", "USD"));
                add(new TradePair("BCH", "USD"));
                add(new TradePair("XRP", "USD"));
                add(new TradePair("EOS", "USD"));
                add(new TradePair("NEO", "USD"));
                add(new TradePair("TRX", "USD"));
                add(new TradePair("XLM", "USD"));
                add(new TradePair("DASH", "USD"));
                add(new TradePair("ZEC", "USD"));
                add(new TradePair("ETC", "USD"));
                add(new TradePair("XMR", "USD"));
                add(new TradePair("XEM", "USD"));



            }};


    public static Set<TradePair> getTradePairs() {
        for (Currency currency : CURRENCIES.values()) {

            if (currency.currencyType.equals(CurrencyType.CRYPTO)) {
                TradePair tradePair1 = new TradePair(currency.code, "USD");
                tradePairs.add(tradePair1);

                logger.info("tradePair1: " + tradePair1);
            }
        }
        return tradePairs;

    }
    public static ConcurrentHashMap<CryptoMarketData, CryptoMarketData> getMarketDataConcurrentHashMap() {

        return MARKET_DATA_CONCURRENT_HASH_MAP;
    }


    @Contract(" -> new")
    public static @NotNull List<Currency> getInstance(Exchange exchange) {
        ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> currencies = CURRENCIES;

        if (currencies.isEmpty()) {
            try {
                registerCurrencies(exchange);
            } catch (IOException | InterruptedException | ParseException | URISyntaxException |
                     NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<>(currencies.values());

    }

    protected static void registerCurrencies(Exchange exchange) throws IOException, InterruptedException, ParseException, URISyntaxException, NoSuchAlgorithmException {
        List<Currency> coinsToRegister = new ArrayList<>();

        HttpRequest.Builder request = HttpRequest.newBuilder();
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        request.uri(URI.create("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false")).GET().build();
        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0)");
        request.uri(URI.create("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false"));


        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
//        "id": "ethereum",
//                "symbol": "eth",
//                "name": "Ethereum",
//                "image": "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
//                "current_price": 1712.53,
//                "market_cap": 206313355645,
//                "market_cap_rank": 2,
//                "fully_diluted_valuation": 206311043920,
//                "total_volume": 20894593775,
//                "high_24h": 1778.85,
//                "low_24h": 1662.79,
//                "price_change_24h": 33.16,
//                "price_change_percentage_24h": 1.97466,
//                "market_cap_change_24h": 3905596129,
//                "market_cap_change_percentage_24h": 1.92957,
//                "circulating_supply": 120457776.583888,
//                "total_supply": 120456426.863651,
//                "max_supply": null,
//                "ath": 4878.26,
//                "ath_change_percentage": -64.92421,
//                "ath_date": "2021-11-10T14:24:19.604Z",
//                "atl": 0.432979,
//                "atl_change_percentage": 395089.81203,
//                "atl_date": "2015-10-20T00:00:00.000Z",
//                "roi": {
//            "times": 90.99975323831556,
//                    "currency": "btc",
//                    "percentage": 9099.975323831555
//        },
//        "last_updated": "2023-03-15T04:42:18.949Z"

        ArrayList<CryptoMarketData> marketData = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        if (response.statusCode() != 200) {
            logger.error("Error while getting currencies");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error while getting currencies\n" + response.body());
            alert.showAndWait();

            return;
        }
        JsonNode jsonNode = mapper.readTree(response.body());
        for (JsonNode node : jsonNode) {
            String id = node.get("id").asText();
            String symbol = node.get("symbol").asText();
            String name = node.get("name").asText();
            String image = node.get("image").asText();
            String current_price = node.get("current_price").asText();
            String market_cap = node.get("market_cap").asText();
            String market_cap_rank = node.get("market_cap_rank").asText();
            String fully_diluted_valuation = node.get("fully_diluted_valuation").asText();
            String total_volume = node.get("total_volume").asText();
            String high_24h = node.get("high_24h").asText();
            String low_24h = node.get("low_24h").asText();
            String price_change_24h = node.get("price_change_24h").asText();
            String price_change_percentage_24h = node.get("price_change_percentage_24h").asText();
            String market_cap_change_24h = node.get("market_cap_change_24h").asText();
            String market_cap_change_percentage_24h = node.get("market_cap_change_percentage_24h").asText();
            String circulating_supply = node.get("circulating_supply").asText();
            String total_supply = node.get("total_supply").asText();
            String max_supply = node.get("max_supply").asText();
            String ath = node.get("ath").asText();
            String ath_change_percentage = node.get("ath_change_percentage").asText();
            String ath_date = node.get("ath_date").asText();
            String atl = node.get("atl").asText();
            String atl_change_percentage = node.get("atl_change_percentage").asText();
            String atl_date = node.get("atl_date").asText();
            String currency;
            String times;
            String percentage;
            String last_updated = node.get("last_updated").asText();
            times = "times";
            currency = "";
            percentage = "";
            MARKET_DATA_CONCURRENT_HASH_MAP.put(new CryptoMarketData(id, symbol, name, image, current_price,
                    market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h,
                    price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h,
                    circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date,
                    currency, times, percentage, last_updated), new CryptoMarketData(id, symbol, name, image, current_price,
                    market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h,
                    price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h,
                    circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date,
                    currency, times, percentage, last_updated));
            roi = new Roi(times, currency, percentage);
            logger.info("Registered %s %s %s");
            logger.info("id: %s, symbol:");
            logger.info(String.format(Locale.ENGLISH, "%s", id));
            logger.info(String.format(Locale.ENGLISH, "%s", symbol));
            logger.info("name: %s, image: %s, current_price: %s");
            logger.info("market_cap: %s, market_cap_rank: %s, fully_diluted_valuation: %s, total_volume: %s");
            logger.info("high_24h: %s, low_24h: %s, price_change_24h: %s, price_change_percentage_24h: %s, market_cap_change_24h: %s, market_cap_change_percentage_24h: %s, circulating_supply: %s, total_supply: %s, max_supply: %s, ath:");
            logger.info(String.format(Locale.ENGLISH, "%s", high_24h));
            logger.info(String.format(Locale.ENGLISH, "%s", low_24h));
            logger.info(String.format(Locale.ENGLISH, "%s", price_change_24h));
            logger.info(String.format(Locale.ENGLISH, "%s", price_change_percentage_24h));
            logger.info(String.format(Locale.ENGLISH, "%s", market_cap_change_24h));
            logger.info("CRYPTO MARKET DATA " + MARKET_DATA_CONCURRENT_HASH_MAP);


//             if (node.elements()!=null&& node.has("roi") ) {
//
//                 JsonNode node2 =node.get("roi");
//
//                 for (JsonNode node3 : node2) {


//                     if (!Objects.equals(node3.elements().next().asText(), "null")) {
//                         times = node3.get("times").asText("times");
//                         currency = node3.get("currency").asText();
//                         percentage = node3.get("percentage").asText();
//                     } else {


            marketData.add(new CryptoMarketData(id.toUpperCase(), symbol.toUpperCase(), name.toUpperCase(), image, current_price, market_cap,
                    market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h,
                    price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h,
                    circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date, roi));
        }


        for (String data : exchange.getTradePair()) {
            coinsToRegister.add(new Currency(CurrencyType.CRYPTO, data.split("/")[0], data.split("/")[0], data.split("/")[0], 8, data.split("/")[0], "") {
                @Override
                public int compareTo(@NotNull Currency o) {
                    return 0;
                }

                @Override
                public int compareTo(@NotNull java.util.Currency o) {
                    if (!o.getCurrencyCode().equals(this.getCode())) {
                        java.util.Currency.getAvailableCurrencies().add(java.util.Currency.getInstance(o.getCurrencyCode()));
                        java.util.Currency currency = java.util.Currency.getInstance(o.getCurrencyCode());
                        if (currency == null) {
                            return -1;
                        }
                        return this.getCode().compareTo(o.getCurrencyCode());
                    }
                    return 0;
                }
            });
            coinsToRegister.add(new Currency(CurrencyType.CRYPTO, data.split("/")[1], data.split("/")[0], data.split("/")[0], 10, data.split("/")[0], "") {
                @Override
                public int compareTo(@NotNull Currency o) {
                    return 0;
                }

                @Override
                public int compareTo(@NotNull java.util.Currency o) {
                    if (!o.getCurrencyCode().equals(this.getCode())) {
                        java.util.Currency.getAvailableCurrencies().add(java.util.Currency.getInstance(o.getCurrencyCode()));
                        java.util.Currency currency = java.util.Currency.getInstance(o.getCurrencyCode());
                        if (currency == null) {
                            return -1;
                        }
                        return this.getCode().compareTo(o.getCurrencyCode());
                    }
                    return 0;
                }
            });


        }


        for (CryptoMarketData data : marketData) {
            coinsToRegister.add(new Currency(CurrencyType.CRYPTO, data.name, data.id, data.symbol, 8, data.symbol, data.image) {
                @Override
                public int compareTo(@NotNull Currency o) {
                    return 0;
                }

                @Override
                public int compareTo(@NotNull java.util.Currency o) {
                    if (!o.getCurrencyCode().equals(this.getCode())) {
                        java.util.Currency.getAvailableCurrencies().add(java.util.Currency.getInstance(o.getCurrencyCode()));
                        java.util.Currency currency = java.util.Currency.getInstance(o.getCurrencyCode());
                        if (currency == null) {
                            return -1;
                        }
                        return this.getCode().compareTo(o.getCurrencyCode());
                    }
                    return 0;
                }
            });
            coinsToRegister.iterator().next().setImage(data.image);
            MARKET_DATA_CONCURRENT_HASH_MAP.put(data, data);
        }


        // Coinbase coinbase = new Coinbase("rt","rty");


        out.println("Available Currencies " + java.util.Currency.getAvailableCurrencies());


        //Register all world  known fiat currencies
        for (java.util.Currency fiatCurrency : java.util.Currency.getAvailableCurrencies()) {

            if (!fiatCurrency.getSymbol().equals("XXX")) {
                int fractional;
                if (fiatCurrency.getDefaultFractionDigits() != -1) {
                    fractional = fiatCurrency.getDefaultFractionDigits();
                } else {
                    fractional = 2;
                }
                String img = "";
                if (Objects.equals(fiatCurrency.getCurrencyCode(), "USD")) {

                    // File relativeTo = new File("/img/symbol.png");

                    File relativeTo = new File("img/symbol.png");

                    img = null;
                }


                CURRENCIES.put(SymmetricPair.of(fiatCurrency.getCurrencyCode(), CurrencyType.FIAT), new FiatCurrency(fiatCurrency.getCurrencyCode(), fiatCurrency.getDisplayName(),
                        fiatCurrency.getCurrencyCode(), fractional, fiatCurrency.getSymbol(), Locale.of(fiatCurrency.getSymbol()), "", fractional, img) {
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

        //Register all crypto currencies
        for (Currency c : coinsToRegister) {
            CURRENCIES.put(SymmetricPair.of(c.code, c.getCurrencyType()), c);
        }

        out.println("Currencies " + CURRENCIES);


        // JsonToCsv jsonToCsv = new JsonToCsv();
//
//        ArrayList<Currency> list = null;
//        for (Currency o : CURRENCIES.values().stream().toList()) {
//            list = new ArrayList<>();
//            list.add(o);
//        }
//        //assert list != null;
//       // jsonToCsv.convertJsonToCsv("currencies.json", "{"+ list +"}");

    }

    public static FiatCurrency ofFiat(@NotNull String code) {
        if (code.equals("¤¤¤")) {
            return NULL_FIAT_CURRENCY;
        }

        FiatCurrency result = (FiatCurrency) CURRENCIES.get(SymmetricPair.of(code, CurrencyType.FIAT));
        return result == null ? NULL_FIAT_CURRENCY : result;
    }

    public static Currency of(String code) {
        Objects.requireNonNull(code, "code must not be null");
        if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.FIAT))
                && CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
            logger.info("ambiguous currency code: " + code);

            throw new IllegalArgumentException("ambiguous currency code: " + code + " (code" +
                    " is used for multiple currency types); use ofCrypto(...) or ofFiat(...) instead");
        } else {
            if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
                return CURRENCIES.get(SymmetricPair.of(code, CurrencyType.CRYPTO));
            } else {
                return CURRENCIES.getOrDefault(SymmetricPair.of(code, CurrencyType.FIAT), null);
            }
        }


    }

    public static Roi getRoi() {
        return roi;
    }

    public static void setRoi(Roi roi) {
        CurrencyDataProvider.roi = roi;
    }


    public enum OANDA_ACCESS_TOKEN {
        ACCESS_TOKEN("adc94d655c0c81e0afdd3da09292f82d-dacf61d05de1d1867d27f725069b9aa2"),
        ACCESS_TOKEN_SECRET("db192b201fce04e1f6cf8e7b963998f9-6f32501f70abf16b7183421607c91e01"), ACCOUNT_ID(
                "001-001-2783446-006"
        );

        OANDA_ACCESS_TOKEN(String value) {
        }
//    }
//
//    public static class Oanda extends Exchange {
//
//        private static final Logger logger = LoggerFactory.getLogger(Oanda.class);
//        //"wss://api-fxtrade.oanda.com/v3/accounts/";
//        private static final Accounts accounts = new Accounts();
//        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
//                .registerModule(new JavaTimeModule())
//                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        static HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
//        private static String accountID;
//        public double dividendAdjustment;
//        public double unrealizedPL;
//        public double resettablePL;
//        public int units;
//        public double financing;
//        public double guaranteedExecutionFees;
//        public double pl;
//        protected String API_KEY;
//        String passphrase;
//        String timestamp;
//
//        public Oanda(TradePair tradePair, String api_key, String accountID, String telegramToken) throws IOException, ParseException, InterruptedException, TelegramApiException {
//            super(tradePair, "ws://api-fxtrade.oanda.com", telegramToken, "");
//            this.API_KEY = api_key;
//            Oanda.accountID = accountID;
//            accounts.setAccountID(accountID);
//
//
//            requestBuilder.header("Content-Type", "application/json");
//            requestBuilder.header("Accept", "application/json");
//            requestBuilder.header("Authorization", "Bearer " + API_KEY);
//
//
//            //        HTTP/1.1 200 OK
//            //        Access-Control-Allow-Headers: Authorization, Content-Type, Accept-Datetime-Format
//            //        Content-Encoding: gzip
//            //        Transfer-Encoding: chunked
//            //        Server: openresty/1.7.0.1
//            //        Connection: keep-alive
//            //        Link: <https://api-fxtrade.oanda.com/v3/accounts/<ACCOUNT>/trades?beforeID=6397&instrument=USD_CAD>; rel="next"
//            //        Date: Wed, 22 Jun 2016 18:41:48 GMT
//            //        Access-Control-Allow-Origin: *
//            //        Access-Control-Allow-Methods: PUT, PATCH, POST, GET, OPTIONS, DELETE
//            //        Content-Type: application/json
//
//            //  requestBuilder.timeout(Duration.ofMillis(10000));
//            // requestBuilder.header("Accept-Datetime-Format", "Wed, 22 Jun 2016 18:41:48 GMT");
//            //   requestBuilder.header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept-Datetime-Format");
//
//
//            //requestBuilder.header("Access-Control-Allow-Origin", "*");
//            //  requestBuilder.header("Access-Control-Allow-Methods", "PUT, PATCH, POST, GET, OPTIONS, DELETE");
//            //Content-Encoding: gzip
//            // requestBuilder.header("Content-Encoding", "gzip");
//
//
//            logger.info(
//                    "OANDA_API_KEY: " + API_KEY + "\n" +
//                            "OANDA_ACCOUNT_ID: " + accountID + "\n" +
//                            "OANDA_TOKEN: " + telegramToken + "\n" +
//                            "OANDA_API_URL: " + "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?beforeID=142955&instrument=USD_CAD"
//            );
//
//
//        }
//
//        public static double getMarginPercent() {
//            return accounts.getMarginPercent();
//        }
//
//        public static double getBalance() {
//            return accounts.getBalance();
//        }
//
//        public static double getOpen() {
//            return accounts.getOpen();
//        }
//
//        public static double getHigh() {
//            return
//                    Math.max(accounts.getHigh(), accounts.getOpen());
//        }
//
//        public static double getLow() {
//            return
//                    Math.min(accounts.getLow(), accounts.getOpen());
//        }
//
//        public static double getFreeMargin() {
//            return
//                    Math.max(accounts.getFreeMargin(), accounts.getOpen());
//        }
//
//        public static double getProfit() {
//            return accounts.getProfit();
//        }
//
//        public double getDividendAdjustment() {
//            return dividendAdjustment;
//        }
//
//        public void setDividendAdjustment(double dividendAdjustment) {
//            this.dividendAdjustment = dividendAdjustment;
//        }
//
//        public double getUnrealizedPL() {
//            return unrealizedPL;
//        }
//
//        public void setUnrealizedPL(double unrealizedPL) {
//            this.unrealizedPL = unrealizedPL;
//        }
//
//        public double getResettablePL() {
//            return resettablePL;
//        }
//
//        public void setResettablePL(double resettablePL) {
//            this.resettablePL = resettablePL;
//        }
//
//        @Override
//        public String toString() {
//            return "MY-SHORT{" +
//                    "dividendAdjustment=" + dividendAdjustment +
//                    ", unrealizedPL=" + unrealizedPL +
//                    ", resettablePL=" + resettablePL +
//                    ", units=" + units +
//                    ", financing=" + financing +
//                    ", guaranteedExecutionFees=" + guaranteedExecutionFees +
//                    ", pl=" + pl +
//                    '}';
//        }
//
//        public int getUnits() {
//            return units;
//        }
//
//        public void setUnits(int units) {
//            this.units = units;
//        }
//
//        public double getFinancing() {
//            return financing;
//        }
//
//        public void setFinancing(double financing) {
//            this.financing = financing;
//        }
//
//        public double getGuaranteedExecutionFees() {
//            return guaranteedExecutionFees;
//        }
//
//        public void setGuaranteedExecutionFees(double guaranteedExecutionFees) {
//            this.guaranteedExecutionFees = guaranteedExecutionFees;
//        }
//
//        public double getPl() {
//            return pl;
//        }
//
//        public void setPl(double pl) {
//            this.pl = pl;
//        }
//
//        @Override
//        public String getName() {
//            return
//                    "OANDA";
//        }
//
//        @Override
//        public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
//            return
//                    new OandaCandleDataSupplier(secondsPerCandle, tradePair) {
//                        @Override
//                        public CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
//                            return null;
//                        }
//
//                        @Override
//                        public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
//                            return null;
//                        }
//                    };
//        }
//
//
//        /**
//         * Fetches the recent trades for the given trade pair from  {@code stopAt} till now (the current time).
//         * <p>
//         * This method only needs to be implemented to support live syncing.
//         */
//        @Override
//        public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
//            Objects.requireNonNull(tradePair);
//            Objects.requireNonNull(stopAt);
//
//            if (stopAt.isAfter(Instant.now())) {
//                return CompletableFuture.completedFuture(Collections.emptyList());
//            }
//
//            CompletableFuture<List<Trade>> futureResult = new CompletableFuture<>();
//            CompletableFuture.runAsync(() -> {
//                IntegerProperty afterCursor = new SimpleIntegerProperty(0);
//                List<Trade> tradesBeforeStopTime = new ArrayList<>();
//
//                // For Public Endpoints, our rate limit is 3 requests per second, up to 6 requests per second in
//                // burst.
//                // We will know if we get rate limited if we get a 429 response code.
//                for (int i = 0; !futureResult.isDone(); i++) {
//                    String uriStr = "https://api-fxtrade.oanda.com/";
//                    uriStr += "v3/accounts/" + accountID + "/trades/instrument=" + tradePair.toString('_');
//
//
//                    if (i != 0) {
//                        uriStr += "?after=" + afterCursor.get();
//                    }
//                    requestBuilder.uri(URI.create(uriStr));
//
//
//                    try {
//                        HttpResponse<String> response = HttpClient.newHttpClient().send(requestBuilder.build()
//                                ,
//                                HttpResponse.BodyHandlers.ofString());
//
//                        Log.info("response headers: ", response.headers().toString());
//                        if (response.headers().firstValue("CB-AFTER").isEmpty()) {
//                            futureResult.completeExceptionally(new RuntimeException(
//                                    "cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda trades response did not contain header \"CB-AFTER\": " + response));
//                            return;
//                        }
//
//                        afterCursor.setValue(Integer.valueOf((response.headers().firstValue("CB-AFTER").get())));
//
//                        JsonNode tradesResponse = OBJECT_MAPPER.readTree(response.body());
//
//                        if (!tradesResponse.isArray()) {
//                            futureResult.completeExceptionally(new RuntimeException(
//                                    "cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda trades response was not an array!"));
//                        }
//                        if (tradesResponse.isEmpty()) {
//                            futureResult.completeExceptionally(new IllegalArgumentException("tradesResponse was empty"));
//                        } else {
//                            logger.info("cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda got " + tradesResponse + " trades");
//                            for (int j = 0; j < tradesResponse.size(); j++) {
//                                JsonNode trade = tradesResponse.get(j);
//                                Instant time = Instant.from(ISO_INSTANT.parse(trade.get("time").asText()));
//                                if (time.compareTo(stopAt) <= 0) {
//                                    futureResult.complete(tradesBeforeStopTime);
//                                    break;
//                                } else {
//                                    tradesBeforeStopTime.add(new Trade(tradePair,
//                                            DefaultMoney.ofFiat(trade.get("price").asText(), String.valueOf(tradePair.getCounterCurrency())),
//                                            DefaultMoney.ofFiat(trade.get("size").asText(), String.valueOf(tradePair.getBaseCurrency())),
//                                            Side.getSide(trade.get("side").asText()), trade.get("trade_id").asLong(), time));
//                                }
//                            }
//                        }
//                    } catch (IOException | InterruptedException ex) {
//                        Log.error("ex: " + ex);
//                        futureResult.completeExceptionally(ex);
//                    } catch (TelegramApiException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//
//            return futureResult;
//        }
//
//        /**
//         * This method only needs to be implemented to support live syncing.
//         */
//        @Override
//        public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
//                TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
//            String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(
//                    currentCandleStartedAt, ZoneOffset.UTC));
//            long idealGranularity = Math.max(10, secondsIntoCurrentCandle / 200);
//            // Get the closest supported granularity to the ideal granularity.
//            int actualGranularity = getCandleDataSupplier(secondsPerCandle, tradePair).getSupportedGranularities().stream()
//                    .min(Comparator.comparingInt(i -> (int) Math.abs(i - idealGranularity)))
//                    .orElseThrow(() -> new NoSuchElementException("Supported granularities was empty!"));
//            // TODO: If actualGranularity = secondsPerCandle there are no sub-candles to fetch and we must get all the
//            //  data for the current live syncing candle from the raw trades method.
//
//
//            String x;
//            String str;
//            if (secondsPerCandle < 3600) {
//                x = String.valueOf(secondsPerCandle / 60);
//                str = "M";
//            } else if (secondsPerCandle < 86400) {
//                x = String.valueOf((secondsPerCandle / 3600));
//                str = "H";
//            } else if (secondsPerCandle < 604800) {
//                x = "";//String.valueOf(secondsPerCandle / 86400);
//                str = "D";
//            } else if (secondsPerCandle < 2592000) {
//                x = String.valueOf((secondsPerCandle / 604800));
//                str = "W";
//            } else {
//                x = String.valueOf((secondsPerCandle * 7 / 2592000 / 7));
//                str = "M";
//            }
//
//            if (x.isEmpty() || x.equals("0")) {
//                throw new NoSuchElementException("secondsPerCandle is too small!");
//            }
//
//            String granularity = str + x;
//
//
//            //   //   {"trades":[{"id":"142950","instrument":"EUR_USD","price":"1.07669","openTime":"2023-03-21T16:56:10.786314295Z","initialUnits":"-1700","initialMarginRequired":"36.6098","state":"OPEN","currentUnits":"-1700","realizedPL":"0.0000","financing":"0.1828","dividendAdjustment":"0.0000","clientExtensions":{"id":"140660466","tag":"0"},"unrealizedPL":"-19.4480","marginUsed":"36.9940"}],"lastTransactionID":"142955"}
//
//
//            return HttpClient.newHttpClient().sendAsync(
//                            HttpRequest.newBuilder()
//                                    .uri(URI.create(
//                                            "https://api-fxtrade.oanda.com/v3/instruments/" + tradePair.toString('_') + "/candles?price=BA&from=2016-10-17T15%3A00%3A00.000000000Z&granularity=" + granularity
//                                                    + ""))
//                                    .GET().build(),
//                            HttpResponse.BodyHandlers.ofString())
//                    .thenApply(HttpResponse::body)
//                    .thenApply(response -> {
//                        Log.info("cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda Candles response got : ", response);
//                        JsonNode res;
//                        try {
//                            res = OBJECT_MAPPER.readTree(response);
//                        } catch (JsonProcessingException ex) {
//                            throw new RuntimeException(ex);
//                        }
//
//                        if (res.isEmpty()) {
//                            return Optional.empty();
//                        }
//
//                        JsonNode currCandle;
//                        Iterator<JsonNode> candleItr = res.iterator();
//                        int currentTill = -1;
//                        double openPrice = -1;
//                        double highSoFar = -1;
//                        double lowSoFar = Double.MAX_VALUE;
//                        double volumeSoFar = 0;
//                        double lastTradePrice = -1;
//                        boolean foundFirst = false;
//                        while (candleItr.hasNext()) {
//                            currCandle = candleItr.next();
//                            if (currCandle.get(0).asInt() < currentCandleStartedAt.getEpochSecond() ||
//                                    currCandle.get(0).asInt() >= currentCandleStartedAt.getEpochSecond() +
//                                            secondsPerCandle) {
//                                // skip this sub-candle if it is not in the parent candle's duration (this is just a
//                                //  Coinbase is  not respecting start/end times
//                                continue;
//
//                            } else {
//                                if (!foundFirst) {
//                                    // FIXME: Why are we only using the first sub-candle here?
//                                    currentTill = currCandle.get(0).asInt();
//                                    lastTradePrice = currCandle.get(4).asDouble();
//                                    foundFirst = true;
//                                }
//                            }
//
//                            openPrice = currCandle.get(3).asDouble();
//
//                            if (currCandle.get(2).asDouble() > highSoFar) {
//                                highSoFar = currCandle.get(2).asDouble();
//                            }
//
//                            if (currCandle.get(1).asDouble() < lowSoFar) {
//                                lowSoFar = currCandle.get(1).asDouble();
//                            }
//
//                            volumeSoFar += currCandle.get(5).asDouble();
//                        }
//
//                        int openTime = (int) (currentCandleStartedAt.toEpochMilli() / 1000L);
//
//                        return Optional.of(new InProgressCandleData(openTime, openPrice, highSoFar, lowSoFar,
//                                currentTill, lastTradePrice, volumeSoFar));
//                    });
//        }
//
//
//        private @NotNull JSONObject getJSON(@NotNull TradePair tradePair) {
//
//            JSONObject jsonObject = new JSONObject();
//            try {
//                URL url = new URL("https://api-fxtrade.oanda.com/v3/accounts/instruments/" + tradePair.toString('_') + "/candles?price=BA&from=2016-");
//                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//                conn.setRequestMethod("GET");
//                conn.setRequestProperty("Accept", "application/json");
//                conn.setRequestProperty("charset", "utf-8");
//                conn.setRequestProperty("Accept-Charset", "utf-8");
//                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10)");
//                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);//    API key as a string
//                timestamp = new Date().toString();
//                conn.setRequestProperty("CB-ACCESS-SIGN", timestamp + "GET" + url);
//                //"base64-encoded signature (see Signing a Message)");
//                conn.setRequestProperty("CB-ACCESS-TIMESTAMP", new Date().toString());//    Timestamp for your request
//
//
//                conn.connect();
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//
//                }
//                in.close();
//
//                out.println(response);
//                //Put data into json file
//                jsonObject = new JSONObject(response.toString());
//                out.println(jsonObject.toString(4));
//
//                String rates;
//                if (jsonObject.has("data")) {
//                    JSONObject dat = new JSONObject(jsonObject.getJSONObject("data").toString(4));
//                    if (dat.has("rates")) {
//                        rates = dat.getJSONObject("rates").toString(4);
//                        out.println(rates);
//                    }
//
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            out.println(jsonObject.toString(4));
//            return jsonObject;
//        }
//
//        @Override
//        public void onOpen(ServerHandshake handshake) {
//
//
//            System.out.println("Connected");
//
//        }
//
//        @Override
//        public void onMessage(String message) {
//            System.out.println(message);
//        }
//
//        @Override
//        public void onClose(int code, String reason, boolean remote) {
//            System.out.println(
//                    "Connection closed: " + code + " " + reason + " " + remote
//            );
//
//
//        }
//
//        @Override
//        public void onError(Exception ex) {
//            System.out.println("Error");
//
//        }
//
//        public void createMarketOrder(@NotNull TradePair tradePair, String side, double size) {
//
//
//            JSONObject jsonObject = getJSON(tradePair);
//
//            String uriStr = "https://api-fxtrade.oanda.com/" +
//                    "products/" + tradePair.toString('_') + "/orders" +
//                    "?side=" + side +
//                    "&type=market" +
//                    "&quantity=" + size +
//                    "&price=" + jsonObject.getJSONObject("data").getJSONObject("rates").getDouble("USD");
//
//            System.out.println(uriStr);
//
//
//        }
//
//
//        public static abstract class OandaCandleDataSupplier extends CandleDataSupplier {
//            private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
//                    .registerModule(new JavaTimeModule())
//                    .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            private static final int EARLIEST_DATA = 1422144000; // roughly the first trade
//            private int time;
//
//            OandaCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
//                super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
//            }
//
//
//            @Override
//            public List<CandleData> getCandleData() {
//                return new ArrayList<>();
//            }
//
//            @Override
//            public Future<List<CandleData>> get() {
//                //  uriStr="https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?beforeID=6397&instruments="+ tradePair.toString('_');
//                // requestBuilder.header("Link", "<https://api-fxtrade.oanda.com/v3/accounts/" + accountID
//                //       + "/trades?beforeID=6397&instrument="+ tradePair.toString('_')+"&rel=next");
//
//                if (endTime.get() == -1) {
//                    endTime.set((int) (Instant.now().toEpochMilli() / 1000L));
//                }
//
//                String endDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
//                        .format(LocalDateTime.ofEpochSecond(endTime.get(), 0, ZoneOffset.UTC));
//
//                int startTime = Math.max(endTime.get() - (numCandles * secondsPerCandle), EARLIEST_DATA);
//                String startDateString = DateTimeFormatter.ISO_LOCAL_DATE_TIME
//                        .format(LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC));
//
//
//                if (startTime == EARLIEST_DATA) {
//                    // signal more data is false
//                    return CompletableFuture.completedFuture(Collections.emptyList());
//                }
//                String uriStr1 = "https://api-fxtrade.oanda.com/v3/accounts/" + accountID + "/trades?instrument=" + tradePair.toString(
//                        '_');//"&beforeID=" + startDateString + "&afterID=" + endDateString ;
//
//
//                //  uriStr1= "https://api-fxtrade.oanda.com/v3/accounts/"+accountID+"/trades?instrument="+tradePair.toString('_');
//
//
//                String x;
//                String str;
//                if (secondsPerCandle < 3600) {
//                    x = String.valueOf(secondsPerCandle / 60);
//                    str = "M";
//                } else if (secondsPerCandle < 86400) {
//                    x = String.valueOf((secondsPerCandle / 3600));
//                    str = "H";
//                } else if (secondsPerCandle < 604800) {
//                    x = "";//String.valueOf(secondsPerCandle / 86400);
//                    str = "D";
//                } else if (secondsPerCandle < 2592000) {
//                    x = String.valueOf((secondsPerCandle / 604800));
//                    str = "W";
//                } else {
//                    x = String.valueOf((secondsPerCandle * 7 / 2592000 / 7));
//                    str = "M";
//                }
//
//                if (x.isEmpty() || x.equals("0")) {
//                    throw new NoSuchElementException("secondsPerCandle is too small!");
//                }
//
//                String granularity = str + x;
//                uriStr1 = "https://api-fxtrade.oanda.com/v3/instruments/USD_CAD/candles?price=BA&from=2016-10-17T15%3A00%3A00.000000000Z&granularity=" + granularity;
//                requestBuilder.uri(URI.create(uriStr1));
//                //  requestBuilder.header("CB-BEFORE", startDateString);
//                //   requestBuilder.header("CB-AFTER", endDateString);
//                //requestBuilder.header("CB-AFTER", String.valueOf(afterCursor.get()));
//
//
//                return HttpClient.newHttpClient().sendAsync(
//                                requestBuilder.build(),
//                                HttpResponse.BodyHandlers.ofString())
//                        .thenApply(HttpResponse::body)
//                        .thenApply(response -> {
//                            Log.info("cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda trade2 response got= -->: ", response);
//                            JsonNode res;
//                            try {
//                                res = OBJECT_MAPPER.readTree(response);
//
//
//                                if (res.has("message")) {
//
//
//                                    logger.info("cryptoinvestor.cryptoinvestor.CurrencyDataProvider.Oanda trade2 response got -->: "
//
//                                            + res.get("message").asText());
//
//
//                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                                    alert.setHeaderText(null);
//                                    alert.setContentText(res.get("message").asText());
//
//                                    alert.showAndWait();
//
//
//                                    if (!res.isEmpty()) {
//                                        logger.info("Got " + res.size() + " candles" + res);
//                                        // Remove the current in-progress candle
//
//
//                                        time = (int) res.get("candles").get(1).get("time").asLong();
//
//
//                                        if (
//                                                time
//                                                        + secondsPerCandle > endTime.get()) {
//                                            ((ArrayNode) res).remove(0);
//                                        }
//                                        endTime.set(startTime);
//                                        //  {"instrument":"USD_CAD","granularity":"H1","candles":[{"complete":true,
//                                        //  "volume":2466,"time":"2016-10-17T15:00:00.000000000Z","bid":{"o":"1.31488","h":"1.31501","l":"1.31314","c":"1.31359"},"ask":{"o":"1.31505","h":"1.31519","l":"1.31333","c":"1.31376"}},{"complete":true,"volume":1123,"time":"2016-10-17T16:00:00.000000000Z","bid":{"o":"1.31358","h":"1.31407","l":"1.31280","c":"1.31304"},"ask":{"o":"1.31376","h":"1.31422","l":"1.31295","c":"1.31321"}},{"complete":true,"volume":1339,"time":"2016-10-17T17:00:00.000000000Z","bid":{"o":"1.31308","h":"1.31351","l":"1.31219","c":"1.31304"},"ask":{"o":"1.31325","h":"1.31369","l":"1.31240","c":"1.31321"}},{"complete":true,"volume":1163,"time":"2016-10-17T18:00:00.000000000Z","bid":{"o":"1.31306","h":"1.31388","l":"1.31241","c":"1.31244"},"ask":{"o":"1.31323","h":"1.31405","l":"1.31259","c":"1.31262"}},{"complete":true,"volume":784,"time":"2016-10-17T19:00:00.000000000Z","bid":{"o":"1.31248","h":"1.31321","l":"1.31202","c":"1.31276"},"ask":{"o":"1.31265","h":"1.31339","l":"1.31219","c":"1.31293"}},{"complete":true,"volume":532,"time":"2016-10-17T20:00:00.000000000Z","bid":{"o":"1.31278","h":"1.31320","l":"1.31248","c":"1.31269"},"ask":{"o":"1.31295","h":"1.31338","l":"1.31269","c":"1.31294"}},{"complete":true,"volume":385,"time":"2016-10-17T21:00:00.000000000Z","bid":{"o":"1.31226","h":"1.31326","l":"1.31226","c":"1.31269"},"ask":{"o":"1.31304","h":"1.31420","l":"1.31286","c":"1.31308"}},{"complete":true,"volume":812,"time":"2016-10-17T22:00:00.000000000Z","bid":{"o":"1.31269","h":"1.31308","l":"1.31204","c":"1.31234"},"ask":{"o":"1.31309","h":"1.31344","l":"1.31231","c":"1.31261"}},{"complete":true,"volume":341,"time":"2016-10-17T23:00:00.000000000Z","bid":{"o":"1.31234","h":"1.31287","l":"1.31140","c":"1.31149"},"ask":{"o":"1.31260","h":"1.31314","l":"1.31161","c":"1.31172"}},{"complete":true,"volume":717,"time":"2016-10-18T00:00:00.000000000Z","bid":{"o":"1.31149","h":"1.31163","l":"1.30848","c":"1.30938"},"ask":{"o":"1.31171","h":"1.31190","l":"1.30881","c":"1.30958"}},{"complete":true,"volume":828,"time":"2016-10-18T01:00:00.000000000Z","bid":{"o":"1.30936","h":"1.30979","l":"1.30824","c":"1.30925"},"ask":{"o":"1.30959","h":"1.31000","l":"1.30845","c":"1.30942"}},{"complete":true,"volume":404,"time":"2016-10-18T02:00:00.000000000Z","bid":{"o":"1.30925","h":"1.30950","l":"1.30885","c":"1.30926"},"ask":{"o":"1.30941","h":"1.30970","l":"1.30909","c":"1.30947"}},{"complete":true,"volume":460,"time":"2016-10-18T03:00:00.000000000Z","bid":{"o":"1.30930","h":"1.31006","l":"1.30898","c":"1.30918"},"ask":{"o":"1.30948","h":"1.31032","l":"1.30916","c":"1.30940"}},{"complete":true,"volume":319,"time":"2016-10-18T04:00:00.000000000Z","bid":{"o":"1.30915","h":"1.30938","l":"1.30873","c":"1.30933"},"ask":{"o":"1.30937","h":"1.30958","l":"1.30891","c":"1.30950"}},{"complete":true,"volume":618,"time":"2016-10-18T05:00:00.000000000Z","bid":{"o":"1.30934","h":"1.30985","l":"1.30898","c":"1.30961"},"ask":{"o":"1.30954","h":"1.31006","l":"1.30920","c":"1.30981"}},{"complete":true,"volume":2057,"time":"2016-10-18T06:00:00.000000000Z","bid":{"o":"1.30964","h":"1.30972","l":"1.30744","c":"1.30794"},"ask":{"o":"1.30986","h":"1.30999","l":"1.30768","c":"1.30815"}},{"complete":true,"volume":2055,"time":"2016-10-18T07:00:00.000000000Z","bid":{"o":"1.30794","h":"1.30849","l":"1.30687","c":"1.30745"},"ask":{"o":"1.30816","h":"1.30871","l":"1.30708","c":"1.30766"}},{"complete":true,"volume":2120,"time":"2016-10-18T08:00:00.000000000Z","bid":{"o":"1.30740","h":"1.30741","l":"1.30616","c":"1.30692"},"ask":{"o":"1.30761","h":"1.30762","l":"1.30636","c":"1.30712"}},{"complete":true,"volume":1918,"time":"2016-10-18T09:00:00.000000000Z","bid":{"o":"1.30694","h":"1.30737","l":"1.30633","c":"1.30676"},"ask":{"o":"1.30713","h":"1.30759","l":"1.30652","c":"1.30697"}},{"complete":true,"volume":1998,"time":"2016-10-18T10:00:00.000000000Z","bid":{"o":"1.30680","h":"1.30711","l":"1.30537","c":"1.30627"},"ask":{"o":"1.30702","h":"1.30733","l":"1.30558","c":"1.30644"}},{"complete":true,"volume":1508,"time":"2016-10-18T11:00:00.000000000Z","bid":{"o":"1.30627","h":"1.30766","l":"1.30589","c":"1.30761"},"ask":{"o":"1.30648","h":"1.30786","l":"1.30611","c":"1.30784"}},{"complete":true,"volume":3525,"time":"2016-10-18T12:00:00.000000000Z","bid":{"o":"1.30765","h":"1.30926","l":"1.30544","c":"1.30915"},"ask":{"o":"1.30788","h":"1.30944","l":"1.30566","c":"1.30931"}},{"complete":true,"volume":2811,"time":"2016-10-18T13:00:00.000000000Z","bid":{"o":"1.30909","h":"1.31037","l":"1.30733","c":"1.31005"},"ask":{"o":"1.30928","h":"1.31055","l":"1.30751","c":"1.31025"}},{"complete":true,"volume":3714,"time":"2016-10-18T14:00:00.000000000Z","bid":{"o":"1.31003","h":"1.31376","l":"1.30946","c":"1.31356"},"ask":{"o":"1.31024","h":"1.31398","l":"1.30966","c":"1.31375"}},{"complete":true,"volume":2806,"time":"2016-10-18T15:00:00.000000000Z","bid":{"o":"1.31351","h":"1.31390","l":"1.31033","c":"1.31047"},"ask":{"o":"1.31371","h":"1.31409","l":"1.31053","c":"1.31064"}},{"complete":true,"volume":846,"time":"2016-10-18T16:00:00.000000000Z","bid":{"o":"1.31046","h":"1.31120","l":"1.31018","c":"1.31072"},"ask":{"o":"1.31064","h":"1.31141","l":"1.31036","c":"1.31090"}},{"complete":true,"volume":814,"time":"2016-10-18T17:00:00.000000000Z","bid":{"o":"1.31076","h":"1.31236","l":"1.31063","c":"1.31216"},"ask":{"o":"1.31094","h":"1.31255","l":"1.31081","c":"1.31234"}},{"complete":true,"volume":825,"time":"2016-10-18T18:00:00.000000000Z","bid":{"o":"1.31213","h":"1.31234","l":"1.31124","c":"1.31140"},"ask":{"o":"1.31233","h":"1.31254","l":"1.31142","c":"1.31161"}},{"complete":true,"volume":935,"time":"2016-10-18T19:00:00.000000000Z","bid":{"o":"1.31144","h":"1.31250","l":"1.31144","c":"1.31206"},"ask":{"o":"1.31163","h":"1.31269","l":"1.31163","c":"1.31225"}},{"complete":true,"volume":1295,"time":"2016-10-18T20:00:00.000000000Z","bid":{"o":"1.31203","h":"1.31275","l":"1.31036","c":"1.31068"},"ask":{"o":"1.31227","h":"1.31295","l":"1.31069","c":"1.31112"}},{"complete":true,"volume":489,"time":"2016-10-18T21:00:00.000000000Z","bid":{"o":"1.31068","h":"1.31112","l":"1.31025","c":"1.31073"},"ask":{"o":"1.31156","h":"1.31180","l":"1.31065","c":"1.31123"}},{"complete":true,"volume":653,"time":"2016-10-18T22:00:00.000000000Z","bid":{"o":"1.31073","h":"1.31121","l":"1.31063","c":"1.31077"},"ask":{"o":"1.31121","h":"1.31153","l":"1.31098","c":"1.31111"}},{"complete":true,"volume":406,"time":"2016-10-18T23:00:00.000000000Z","bid":{"o":"1.31085","h":"1.31105","l":"1.31042","c":"1.31076"},"ask":{"o":"1.31114","h":"1.31133","l":"1.31068","c":"1.31094"}},{"complete":true,"volume":797,"time":"2016-10-19T00:00:00.000000000Z","bid":{"o":"1.31079","h":"1.31089","l":"1.30877","c":"1.30904"},"ask":{"o":"1.31100","h":"1.31112","l":"1.30898","c":"1.30927"}},{"complete":true,"volume":884,"time":"2016-10-19T01:00:00.000000000Z","bid":{"o":"1.30906","h":"1.30954","l":"1.30824","c":"1.30851"},"ask":{"o":"1.30931","h":"1.30976","l":"1.30845","c":"1.30879"}},{"complete":true,"volume":861,"time":"2016-10-19T02:00:00.000000000Z","bid":{"o":"1.30851","h":"1.31056","l":"1.30851","c":"1.31056"},"ask":{"o":"1.30885","h":"1.31079","l":"1.30878","c":"1.31079"}},{"complete":true,"volume":448,"time":"2016-10-19T03:00:00.000000000Z","bid":{"o":"1.31057","h":"1.31057","l":"1.30988","c":"1.31009"},"ask":{"o":"1.31079","h":"1.31080","l":"1.31013","c":"1.31025"}},{"complete":true,"volume":523,"time":"2016-10-19T04:00:00.000000000Z","bid":{"o":"1.31006","h":"1.31122","l":"1.30986","c":"1.31114"},"ask":{"o":"1.31026","h":"1.31149","l":"1.31009","c":"1.31140"}},{"complete":true,"volume":942,"time":"2016-10-19T05:00:00.000000000Z","bid":{"o":"1.31108","h":"1.31128","l":"1.31015","c":"1.31023"},"ask":{"o":"1.31137","h":"1.31154","l":"1.31040","c":"1.31051"}},{"complete":true,"volume":1960,"time":"2016-10-19T06:00:00.000000000Z","bid":{"o":"1.31022","h":"1.31231","l":"1.30984","c":"1.31161"},"ask":{"o":"1.31051","h":"1.31254","l":"1.31010","c":"1.31184"}},{"complete":true,"volume":2477,"time":"2016-10-19T07:00:00.000000000Z","bid":{"o":"1.31160","h":"1.31274","l":"1.31105","c":"1.31106"},"ask":{"o":"1.31181","h":"1.31298","l":"1.31124","c":"1.31128"}},{"complete":true,"volume":1874,"time":"2016-10-19T08:00:00.000000000Z","bid":{"o":"1.31101","h":"1.31137","l":"1.30866","c":"1.30909"},"ask":{"o":"1.31124","h":"1.31159","l":"1.30888","c":"1.30929"}},{"complete":true,"volume":1680,"time":"2016-10-19T09:00:00.000000000Z","bid":{"o":"1.30908","h":"1.30927","l":"1.30769","c":"1.30841"},"ask":{"o":"1.30929","h":"1.30948","l":"1.30793","c":"1.30864"}},{"complete":true,"volume":1519,"time":"2016-10-19T10:00:00.000000000Z","bid":{"o":"1.30837","h":"1.30907","l":"1.30737","c":"1.30796"},"ask":{"o":"1.30860","h":"1.30931","l":"1.30757","c":"1.30816"}},{"complete":true,"volume":1533,"time":"2016-10-19T11:00:00.000000000Z","bid":{"o":"1.30795","h":"1.30895","l":"1.30764","c":"1.30838"},"ask":{"o":"1.30816","h":"1.30918","l":"1.30786","c":"1.30859"}},{"complete":true,"volume":1871,"time":"2016-10-19T12:00:00.000000000Z","bid":{"o":"1.30843","h":"1.30855","l":"1.30743","c":"1.30770"},"ask":{"o":"1.30864","h":"1.30880","l":"1.30759","c":"1.30787"}},{"complete":true,"volume":2594,"time":"2016-10-19T13:00:00.000000000Z","bid":{"o":"1.30772","h":"1.31104","l":"1.30768","c":"1.31087"},"ask":{"o":"1.30788","h":"1.31167","l":"1.30787","c":"1.31150"}},{"complete":true,"volume":8483,"time":"2016-10-19T14:00:00.000000000Z","bid":{"o":"1.31097","h":"1.31128","l":"1.30106","c":"1.30132"},"ask":{"o":"1.31163","h":"1.31209","l":"1.30125","c":"1.30151"}},{"complete":true,"volume":8473,"time":"2016-10-19T15:00:00.000000000Z","bid":{"o":"1.30130","h":"1.31138","l":"1.30052","c":"1.31048"},"ask":{"o":"1.30149","h":"1.31169","l":"1.30071","c":"1.31066"}},{"complete":true,"volume":1856,"time":"2016-10-19T16:00:00.000000000Z","bid":{"o":"1.31047","h":"1.31209","l":"1.30981","c":"1.31167"},"ask":{"o":"1.31068","h":"1.31227","l":"1.31001","c":"1.31187"}},{"complete":true,"volume":1545,"time":"2016-10-19T17:00:00.000000000Z","bid":{"o":"1.31170","h":"1.31407","l":"1.31170","c":"1.31261"},"ask":{"o":"1.31189","h":"1.31428","l":"1.31189","c":"1.31280"}},{"complete":true,"volume":1577,"time":"2016-10-19T18:00:00.000000000Z","bid":{"o":"1.31260","h":"1.31293","l":"1.31192","c":"1.31276"},"ask":{"o":"1.31280","h":"1.31315","l":"1.31211","c":"1.31297"}},{"complete":true,"volume":753,"time":"2016-10-19T19:00:00.000000000Z","bid":{"o":"1.31276","h":"1.31322","l":"1.31186","c":"1.31267"},"ask":{"o":"1.31296","h":"1.31340","l":"1.31209","c":"1.31286"}},{"complete":true,"volume":989,"time":"2016-10-19T20:00:00.000000000Z","bid":{"o":"1.31263","h":"1.31273","l":"1.31113","c":"1.31119"},"ask":{"o":"1.31281","h":"1.31294","l":"1.31143","c":"1.31155"}},{"complete":true,"volume":622,"time":"2016-10-19T21:00:00.000000000Z","bid":{"o":"1.31118","h":"1.31218","l":"1.31113","c":"1.31217"},"ask":{"o":"1.31184","h":"1.31267","l":"1.31175","c":"1.31258"}},{"complete":true,"volume":1081,"time":"2016-10-19T22:00:00.000000000Z","bid":{"o":"1.31208","h":"1.31280","l":"1.31174","c":"1.31206"},"ask":{"o":"1.31256","h":"1.31310","l":"1.31209","c":"1.31240"}},{"complete":true,"volume":431,"time":"2016-10-19T23:00:00.000000000Z","bid":{"o":"1.31208","h":"1.31329","l":"1.31188","c":"1.31319"},"ask":{"o":"1.31240","h":"1.31352","l":"1.31219","c":"1.31344"}},{"complete":true,"volume":2044,"time":"2016-10-20T00:00:00.000000000Z","bid":{"o":"1.31312","h":"1.31458","l":"1.31266","c":"1.31363"},"ask":{"o":"1.31340","h":"1.31481","l":"1.31289","c":"1.31385"}},{"complete":true,"volume":1168,"time":"2016-10-20T01:00:00.000000000Z","bid":{"o":"1.31363","h":"1.31431","l":"1.31330","c":"1.31399"},"ask":{"o":"1.31387","h":"1.31454","l":"1.31351","c":"1.31422"}},{"complete":true,"volume":776,"time":"2016-10-20T02:00:00.000000000Z","bid":{"o":"1.31399","h":"1.31454","l":"1.31323","c":"1.31350"},"ask":{"o":"1.31420","h":"1.31478","l":"1.31343","c":"1.31371"}},{"complete":true,"volume":362,"time":"2016-10-20T03:00:00.000000000Z","bid":{"o":"1.31349","h":"1.31378","l":"1.31341","c":"1.31372"},"ask":{"o":"1.31369","h":"1.31397","l":"1.31362","c":"1.31395"}},{"complete":true,"volume":582,"time":"2016-10-20T04:00:00.000000000Z","bid":{"o":"1.31374","h":"1.31496","l":"1.31367","c":"1.31472"},"ask":{"o":"1.31395","h":"1.31522","l":"1.31388","c":"1.31496"}},{"complete":true,"volume":1018,"time":"2016-10-20T05:00:00.000000000Z","bid":{"o":"1.31473","h":"1.31644","l":"1.31473","c":"1.31587"},"ask":{"o":"1.31495","h":"1.31670","l":"1.31495","c":"1.31612"}},{"complete":true,"volume":1972,"time":"2016-10-20T06:00:00.000000000Z","bid":{"o":"1.31590","h":"1.31695","l":"1.31503","c":"1.31618"},"ask":{"o":"1.31613","h":"1.31724","l":"1.31529","c":"1.31638"}},{"complete":true,"volume":2225,"time":"2016-10-20T07:00:00.000000000Z","bid":{"o":"1.31617","h":"1.31720","l":"1.31562","c":"1.31580"},"ask":{"o":"1.31639","h":"1.31743","l":"1.31585","c":"1.31600"}},{"complete":true,"volume":1731,"time":"2016-10-20T08:00:00.000000000Z","bid":{"o":"1.31582","h":"1.31755","l":"1.31582","c":"1.31687"},"ask":{"o":"1.31604","h":"1.31777","l":"1.31604","c":"1.31709"}},{"complete":true,"volume":1353,"time":"2016-10-20T09:00:00.000000000Z","bid":{"o":"1.31689","h":"1.31734","l":"1.31618","c":"1.31619"},"ask":{"o":"1.31710","h":"1.31755","l":"1.31639","c":"1.31640"}},{"complete":true,"volume":1987,"time":"2016-10-20T10:00:00.000000000Z","bid":{"o":"1.31621","h":"1.31642","l":"1.31414","c":"1.31487"},"ask":{"o":"1.31643","h":"1.31664","l":"1.31440","c":"1.31508"}},{"complete":true,"volume":1676,"time":"2016-10-20T11:00:00.000000000Z","bid":{"o":"1.31492","h":"1.31667","l":"1.31416","c":"1.31667"},"ask":{"o":"1.31513","h":"1.31686","l":"1.31438","c":"1.31686"}},{"complete":true,"volume":7180,"time":"2016-10-20T12:00:00.000000000Z","bid":{"o":"1.31667","h":"1.32117","l":"1.31627","c":"1.31863"},"ask":{"o":"1.31687","h":"1.32151","l":"1.31650","c":"1.31882"}},{"complete":true,"volume":4901,"time":"2016-10-20T13:00:00.000000000Z","bid":{"o":"1.31860","h":"1.31994","l":"1.31669","c":"1.31939"},"ask":{"o":"1.31884","h":"1.32014","l":"1.31691","c":"1.31967"}},{"complete":true,"volume":3501,"time":"2016-10-20T14:00:00.000000000Z","bid":{"o":"1.31942","h":"1.32158","l":"1.31925","c":"1.32117"},"ask":{"o":"1.31969","h":"1.32177","l":"1.31942","c":"1.32136"}},{"complete":true,"volume":1865,"time":"2016-10-20T15:00:00.000000000Z","bid":{"o":"1.32118","h":"1.32152","l":"1.31907","c":"1.31966"},"ask":{"o":"1.32136","h":"1.32171","l":"1.31925","c":"1.31985"}},{"complete":true,"volume":1241,"time":"2016-10-20T16:00:00.000000000Z","bid":{"o":"1.31961","h":"1.32024","l":"1.31911","c":"1.31978"},"ask":{"o":"1.31981","h":"1.32044","l":"1.31930","c":"1.31994"}},{"complete":true,"volume":1020,"time":"2016-10-20T17:00:00.000000000Z","bid":{"o":"1.31980","h":"1.32102","l":"1.31911","c":"1.32102"},"ask":{"o":"1.31998","h":"1.32118","l":"1.31931","c":"1.32117"}},{"complete":true,"volume":1407,"time":"2016-10-20T18:00:00.000000000Z","bid":{"o":"1.32101","h":"1.32258","l":"1.32046","c":"1.32222"},"ask":{"o":"1.32119","h":"1.32282","l":"1.32067","c":"1.32242"}},{"complete":true,"volume":840,"time":"2016-10-20T19:00:00.000000000Z","bid":{"o":"1.32218","h":"1.32270","l":"1.32176","c":"1.32209"},"ask":{"o":"1.32237","h":"1.32291","l":"1.32195","c":"1.32228"}},{"complete":true,"volume":628,"time":"2016-10-20T20:00:00.000000000Z","bid":{"o":"1.32212","h":"1.32315","l":"1.32208","c":"1.32280"},"ask":{"o":"1.32234","h":"1.32339","l":"1.32230","c":"1.32316"}},{"complete":true,"volume":316,"time":"2016-10-20T21:00:00.000000000Z","bid":{"o":"1.32238","h":"1.32314","l":"1.32219","c":"1.32282"},"ask":{"o":"1.32320","h":"1.32356","l":"1.32297","c":"1.32337"}},{"complete":true,"volume":663,"time":"2016-10-20T22:00:00.000000000Z","bid":{"o":"1.32267","h":"1.32325","l":"1.32239","c":"1.32253"},"ask":{"o":"1.32344","h":"1.32365","l":"1.32269","c":"1.32285"}},{"complete":true,"volume":367,"time":"2016-10-20T23:00:00.000000000Z","bid":{"o":"1.32251","h":"1.32283","l":"1.32241","c":"1.32268"},"ask":{"o":"1.32282","h":"1.32307","l":"1.32272","c":"1.32296"}},{"complete":true,"volume":687,"time":"2016-10-21T00:00:00.000000000Z","bid":{"o":"1.32269","h":"1.32407","l":"1.32269","c":"1.32380"},"ask":{"o":"1.32300","h":"1.32431","l":"1.32298","c":"1.32401"}},{"complete":true,"volume":812,"time":"2016-10-21T01:00:00.000000000Z","bid":{"o":"1.32378","h":"1.32442","l":"1.32347","c":"1.32438"},"ask":{"o":"1.32404","h":"1.32463","l":"1.32371","c":"1.32458"}},{"complete":true,"volume":709,"time":"2016-10-21T02:00:00.000000000Z","bid":{"o":"1.32431","h":"1.32477","l":"1.32397","c":"1.32453"},"ask":{"o":"1.32456","h":"1.32502","l":"1.32420","c":"1.32476"}},{"complete":true,"volume":332,"time":"2016-10-21T03:00:00.000000000Z","bid":{"o":"1.32450","h":"1.32497","l":"1.32426","c":"1.32435"},"ask":{"o":"1.32473","h":"1.32517","l":"1.32447","c":"1.32454"}},{"complete":true,"volume":427,"time":"2016-10-21T04:00:00.000000000Z","bid":{"o":"1.32429","h":"1.32477","l":"1.32423","c":"1.32460"},"ask":{"o":"1.32451","h":"1.32501","l":"1.32446","c":"1.32483"}},{"complete":true,"volume":731,"time":"2016-10-21T05:00:00.000000000Z","bid":{"o":"1.32453","h":"1.32477","l":"1.32401","c":"1.32408"},"ask":{"o":"1.32481","h":"1.32501","l":"1.32423","c":"1.32432"}},{"complete":true,"volume":1955,"time":"2016-10-21T06:00:00.000000000Z","bid":{"o":"1.32409","h":"1.32430","l":"1.32248","c":"1.32390"},"ask":{"o":"1.32432","h":"1.32454","l":"1.32279","c":"1.32411"}},{"complete":true,"volume":1917,"time":"2016-10-21T07:00:00.000000000Z","bid":{"o":"1.32388","h":"1.32528","l":"1.32338","c":"1.32520"},"ask":{"o":"1.32413","h":"1.32549","l":"1.32360","c":"1.32541"}},{"complete":true,"volume":1971,"time":"2016-10-21T08:00:00.000000000Z","bid":{"o":"1.32519","h":"1.32568","l":"1.32397","c":"1.32542"},"ask":{"o":"1.32542","h":"1.32589","l":"1.32418","c":"1.32562"}},{"complete":true,"volume":1633,"time":"2016-10-21T09:00:00.000000000Z","bid":{"o":"1.32536","h":"1.32558","l":"1.32306","c":"1.32353"},"ask":{"o":"1.32559","h":"1.32580","l":"1.32325","c":"1.32375"}},{"complete":true,"volume":1189,"time":"2016-10-21T10:00:00.000000000Z","bid":{"o":"1.32357","h":"1.32456","l":"1.32320","c":"1.32373"},"ask":{"o":"1.32380","h":"1.32477","l":"1.32342","c":"1.32394"}},{"complete":true,"volume":1108,"time":"2016-10-21T11:00:00.000000000Z","bid":{"o":"1.32372","h":"1.32420","l":"1.32270","c":"1.32337"},"ask":{"o":"1.32394","h":"1.32441","l":"1.32294","c":"1.32359"}},{"complete":true,"volume":5839,"time":"2016-10-21T12:00:00.000000000Z","bid":{"o":"1.32342","h":"1.33384","l":"1.32264","c":"1.33357"},"ask":{"o":"1.32363","h":"1.33411","l":"1.32289","c":"1.33375"}},{"complete":true,"volume":4130,"time":"2016-10-21T13:00:00.000000000Z","bid":{"o":"1.33354","h":"1.33498","l":"1.33198","c":"1.33334"},"ask":{"o":"1.33372","h":"1.33515","l":"1.33217","c":"1.33358"}},{"complete":true,"volume":4234,"time":"2016-10-21T14:00:00.000000000Z","bid":{"o":"1.33335","h":"1.33446","l":"1.33031","c":"1.33342"},"ask":{"o":"1.33357","h":"1.33466","l":"1.33057","c":"1.33361"}},{"complete":true,"volume":2539,"time":"2016-10-21T15:00:00.000000000Z","bid":{"o":"1.33340","h":"1.33368","l":"1.33143","c":"1.33247"},"ask":{"o":"1.33357","h":"1.33386","l":"1.33164","c":"1.33265"}},{"complete":true,"volume":1317,"time":"2016-10-21T16:00:00.000000000Z","bid":{"o":"1.33245","h":"1.33362","l":"1.33189","c":"1.33235"},"ask":{"o":"1.33261","h":"1.33381","l":"1.33209","c":"1.33252"}},{"complete":true,"volume":1411,"time":"2016-10-21T17:00:00.000000000Z","bid":{"o":"1.33236","h":"1.33540","l":"1.33234","c":"1.33423"},"ask":{"o":"1.33251","h":"1.33560","l":"1.33251","c":"1.33441"}},{"complete":true,"volume":1155,"time":"2016-10-21T18:00:00.000000000Z","bid":{"o":"1.33427","h":"1.33492","l":"1.33314","c":"1.33391"},"ask":{"o":"1.33444","h":"1.33509","l":"1.33331","c":"1.33409"}},{"complete":true,"volume":1166,"time":"2016-10-21T19:00:00.000000000Z","bid":{"o":"1.33385","h":"1.33402","l":"1.33237","c":"1.33255"},"ask":{"o":"1.33407","h":"1.33423","l":"1.33256","c":"1.33273"}},{"complete":true,"volume":799,"time":"2016-10-21T20:00:00.000000000Z","bid":{"o":"1.33250","h":"1.33376","l":"1.33234","c":"1.33321"},"ask":{"o":"1.33270","h":"1.33434","l":"1.33253","c":"1.33421"}},{"complete":true,"volume":373,"time":"2016-10-23T21:00:00.000000000Z","bid":{"o":"1.33241","h":"1.33442","l":"1.33241","c":"1.33413"},"ask":{"o":"1.33325","h":"1.33513","l":"1.33317","c":"1.33479"}},{"complete":true,"volume":1047,"time":"2016-10-23T22:00:00.000000000Z","bid":{"o":"1.33417","h":"1.33467","l":"1.33339","c":"1.33434"},"ask":{"o":"1.33468","h":"1.33496","l":"1.33373","c":"1.33457"}},{"complete":true,"volume":721,"time":"2016-10-23T23:00:00.000000000Z","bid":{"o":"1.33435","h":"1.33538","l":"1.33401","c":"1.33508"},"ask":{"o":"1.33459","h":"1.33569","l":"1.33432","c":"1.33533"}},{"complete":true,"volume":619,"time":"2016-10-24T00:00:00.000000000Z","bid":{"o":"1.33509","h":"1.33526","l":"1.33415","c":"1.33478"},"ask":{"o":"1.33533","h":"1.33548","l":"1.33437","c":"1.33498"}},{"complete":true,"volume":960,"time":"2016-10-24T01:00:00.000000000Z","bid":{"o":"1.33476","h":"1.33583","l":"1.33473","c":"1.33569"},"ask":{"o":"1.33499","h":"1.33604","l":"1.33494","c":"1.33590"}},{"complete":true,"volume":795,"time":"2016-10-24T02:00:00.000000000Z","bid":{"o":"1.33567","h":"1.33574","l":"1.33534","c":"1.33557"},"ask":{"o":"1.33588","h":"1.33594","l":"1.33556","c":"1.33577"}},{"complete":true,"volume":228,"time":"2016-10-24T03:00:00.000000000Z","bid":{"o":"1.33558","h":"1.33565","l":"1.33488","c":"1.33512"},"ask":{"o":"1.33586","h":"1.33586","l":"1.33510","c":"1.33530"}},{"complete":true,"volume":283,"time":"2016-10-24T04:00:00.000000000Z","bid":{"o":"1.33509","h":"1.33535","l":"1.33498","c":"1.33517"},"ask":{"o":"1.33528","h":"1.33556","l":"1.33517","c":"1.33534"}},{"complete":true,"volume":587,"time":"2016-10-24T05:00:00.000000000Z","bid":{"o":"1.33519","h":"1.33542","l":"1.33441","c":"1.33468"},"ask":{"o":"1.33536","h":"1.33562","l":"1.33463","c":"1.33488"}},{"complete":true,"volume":1316,"time":"2016-10-24T06:00:00.000000000Z","bid":{"o":"1.33470","h":"1.33476","l":"1.33244","c":"1.33302"},"ask":{"o":"1.33489","h":"1.33493","l":"1.33261","c":"1.33328"}},{"complete":true,"volume":2070,"time":"2016-10-24T07:00:00.000000000Z","bid":{"o":"1.33305","h":"1.33348","l":"1.33251","c":"1.33299"},"ask":{"o":"1.33330","h":"1.33368","l":"1.33270","c":"1.33317"}},{"complete":true,"volume":1155,"time":"2016-10-24T08:00:00.000000000Z","bid":{"o":"1.33302","h":"1.33476","l":"1.33265","c":"1.33387"},"ask":{"o":"1.33322","h":"1.33495","l":"1.33281","c":"1.33407"}},{"complete":true,"volume":779,"time":"2016-10-24T09:00:00.000000000Z","bid":{"o":"1.33384","h":"1.33427","l":"1.33322","c":"1.33366"},"ask":{"o":"1.33403","h":"1.33444","l":"1.33340","c":"1.33381"}},{"complete":true,"volume":1073,"time":"2016-10-24T10:00:00.000000000Z","bid":{"o":"1.33369","h":"1.33427","l":"1.33269","c":"1.33351"},"ask":{"o":"1.33385","h":"1.33446","l":"1.33287","c":"1.33367"}},{"complete":true,"volume":1401,"time":"2016-10-24T11:00:00.000000000Z","bid":{"o":"1.33350","h":"1.33489","l":"1.33344","c":"1.33477"},"ask":{"o":"1.33369","h":"1.33506","l":"1.33361","c":"1.33493"}},{"complete":true,"volume":2434,"time":"2016-10-24T12:00:00.000000000Z","bid":{"o":"1.33476","h":"1.33576","l":"1.33415","c":"1.33435"},"ask":{"o":"1.33495","h":"1.33595","l":"1.33433","c":"1.33453"}},{"complete":true,"volume":2999,"time":"2016-10-24T13:00:00.000000000Z","bid":{"o":"1.33436","h":"1.33506","l":"1.33316","c":"1.33393"},"ask":{"o":"1.33453","h":"1.33524","l":"1.33335","c":"1.33412"}},{"complete":true,"volume":3099,"time":"2016-10-24T14:00:00.000000000Z","bid":{"o":"1.33390","h":"1.33579","l":"1.33355","c":"1.33539"},"ask":{"o":"1.33409","h":"1.33599","l":"1.33374","c":"1.33558"}},{"complete":true,"volume":2610,"time":"2016-10-24T15:00:00.000000000Z","bid":{"o":"1.33534","h":"1.33893","l":"1.33502","c":"1.33886"},"ask":{"o":"1.33554","h":"1.33911","l":"1.33520","c":"1.33904"}},{"complete":true,"volume":1673,"time":"2016-10-24T16:00:00.000000000Z","bid":{"o":"1.33884","h":"1.33954","l":"1.33819","c":"1.33875"},"ask":{"o":"1.33900","h":"1.33972","l":"1.33838","c":"1.33895"}},{"complete":true,"volume":1117,"time":"2016-10-24T17:00:00.000000000Z","bid":{"o":"1.33872","h":"1.33967","l":"1.33839","c":"1.33884"},"ask":{"o":"1.33890","h":"1.33988","l":"1.33863","c":"1.33900"}},{"complete":true,"volume":1027,"time":"2016-10-24T18:00:00.000000000Z","bid":{"o":"1.33883","h":"1.33885","l":"1.33725","c":"1.33742"},"ask":{"o":"1.33901","h":"1.33903","l":"1.33743","c":"1.33761"}},{"complete":true,"volume":1065,"time":"2016-10-24T19:00:00.000000000Z","bid":{"o":"1.33745","h":"1.33920","l":"1.33739","c":"1.33850"},"ask":{"o":"1.33764","h":"1.33938","l":"1.33756","c":"1.33868"}},{"complete":true,"volume":4560,"time":"2016-10-24T20:00:00.000000000Z","bid":{"o":"1.33846","h":"1.33858","l":"1.32842","c":"1.32851"},"ask":{"o":"1.33864","h":"1.33875","l":"1.32875","c":"1.32882"}},{"complete":true,"volume":1965,"time":"2016-10-24T21:00:00.000000000Z","bid":{"o":"1.32813","h":"1.33538","l":"1.32759","c":"1.33485"},"ask":{"o":"1.32888","h":"1.33593","l":"1.32802","c":"1.33544"}},{"complete":true,"volume":2638,"time":"2016-10-24T22:00:00.000000000Z","bid":{"o":"1.33487","h":"1.33619","l":"1.33384","c":"1.33454"},"ask":{"o":"1.33544","h":"1.33649","l":"1.33446","c":"1.33482"}},{"complete":true,"volume":837,"time":"2016-10-24T23:00:00.000000000Z","bid":{"o":"1.33454","h":"1.33531","l":"1.33399","c":"1.33485"},"ask":{"o":"1.33483","h":"1.33557","l":"1.33420","c":"1.33509"}},{"complete":true,"volume":1453,"time":"2016-10-25T00:00:00.000000000Z","bid":{"o":"1.33480","h":"1.33536","l":"1.33414","c":"1.33496"},"ask":{"o":"1.33505","h":"1.33563","l":"1.33442","c":"1.33524"}},{"complete":true,"volume":1053,"time":"2016-10-25T01:00:00.000000000Z","bid":{"o":"1.33493","h":"1.33512","l":"1.33392","c":"1.33408"},"ask":{"o":"1.33521","h":"1.33536","l":"1.33416","c":"1.33431"}},{"complete":true,"volume":1218,"time":"2016-10-25T02:00:00.000000000Z","bid":{"o":"1.33409","h":"1.33432","l":"1.33326","c":"1.33373"},"ask":{"o":"1.33431","h":"1.33454","l":"1.33351","c":"1.33396"}},{"complete":true,"volume":583,"time":"2016-10-25T03:00:00.000000000Z","bid":{"o":"1.33377","h":"1.33427","l":"1.33355","c":"1.33357"},"ask":{"o":"1.33402","h":"1.33446","l":"1.33375","c":"1.33378"}},{"complete":true,"volume":632,"time":"2016-10-25T04:00:00.000000000Z","bid":{"o":"1.33357","h":"1.33405","l":"1.33289","c":"1.33334"},"ask":{"o":"1.33379","h":"1.33425","l":"1.33315","c":"1.33354"}},{"complete":true,"volume":804,"time":"2016-10-25T05:00:00.000000000Z","bid":{"o":"1.33329","h":"1.33337","l":"1.33265","c":"1.33286"},"ask":{"o":"1.33350","h":"1.33357","l":"1.33287","c":"1.33303"}},{"complete":true,"volume":1176,"time":"2016-10-25T06:00:00.000000000Z","bid":{"o":"1.33289","h":"1.33441","l":"1.33280","c":"1.33427"},"ask":{"o":"1.33307","h":"1.33459","l":"1.33299","c":"1.33445"}},{"complete":true,"volume":1686,"time":"2016-10-25T07:00:00.000000000Z","bid":{"o":"1.33430","h":"1.33487","l":"1.33314","c":"1.33468"},"ask":{"o":"1.33447","h":"1.33503","l":"1.33333","c":"1.33485"}},{"complete":true,"volume":1755,"time":"2016-10-25T08:00:00.000000000Z","bid":{"o":"1.33465","h":"1.33497","l":"1.33184","c":"1.33224"},"ask":{"o":"1.33484","h":"1.33518","l":"1.33201","c":"1.33242"}},{"complete":true,"volume":1230,"time":"2016-10-25T09:00:00.000000000Z","bid":{"o":"1.33220","h":"1.33322","l":"1.33187","c":"1.33217"},"ask":{"o":"1.33239","h":"1.33340","l":"1.33203","c":"1.33236"}},{"complete":true,"volume":1667,"time":"2016-10-25T10:00:00.000000000Z","bid":{"o":"1.33218","h":"1.33258","l":"1.33111","c":"1.33200"},"ask":{"o":"1.33235","h":"1.33276","l":"1.33130","c":"1.33219"}},{"complete":true,"volume":1139,"time":"2016-10-25T11:00:00.000000000Z","bid":{"o":"1.33202","h":"1.33205","l":"1.33063","c":"1.33080"},"ask":{"o":"1.33219","h":"1.33223","l":"1.33080","c":"1.33098"}},{"complete":true,"volume":2666,"time":"2016-10-25T12:00:00.000000000Z","bid":{"o":"1.33082","h":"1.33535","l":"1.33082","c":"1.33452"},"ask":{"o":"1.33101","h":"1.33554","l":"1.33099","c":"1.33474"}},{"complete":true,"volume":3728,"time":"2016-10-25T13:00:00.000000000Z","bid":{"o":"1.33450","h":"1.33583","l":"1.33271","c":"1.33334"},"ask":{"o":"1.33471","h":"1.33602","l":"1.33291","c":"1.33358"}},{"complete":true,"volume":5105,"time":"2016-10-25T14:00:00.000000000Z","bid":{"o":"1.33338","h":"1.33706","l":"1.33216","c":"1.33518"},"ask":{"o":"1.33365","h":"1.33725","l":"1.33236","c":"1.33535"}},{"complete":true,"volume":4570,"time":"2016-10-25T15:00:00.000000000Z","bid":{"o":"1.33516","h":"1.33579","l":"1.33163","c":"1.33339"},"ask":{"o":"1.33533","h":"1.33600","l":"1.33183","c":"1.33360"}},{"complete":true,"volume":2480,"time":"2016-10-25T16:00:00.000000000Z","bid":{"o":"1.33336","h":"1.33494","l":"1.33237","c":"1.33307"},"ask":{"o":"1.33355","h":"1.33511","l":"1.33254","c":"1.33326"}},{"complete":true,"volume":1592,"time":"2016-10-25T17:00:00.000000000Z","bid":{"o":"1.33308","h":"1.33414","l":"1.33222","c":"1.33360"},"ask":{"o":"1.33326","h":"1.33430","l":"1.33242","c":"1.33377"}},{"complete":true,"volume":1396,"time":"2016-10-25T18:00:00.000000000Z","bid":{"o":"1.33358","h":"1.33478","l":"1.33251","c":"1.33457"},"ask":{"o":"1.33376","h":"1.33503","l":"1.33270","c":"1.33474"}},{"complete":true,"volume":1058,"time":"2016-10-25T19:00:00.000000000Z","bid":{"o":"1.33461","h":"1.33541","l":"1.33411","c":"1.33505"},"ask":{"o":"1.33477","h":"1.33559","l":"1.33427","c":"1.33524"}},{"complete":true,"volume":1186,"time":"2016-10-25T20:00:00.000000000Z","bid":{"o":"1.33500","h":"1.33573","l":"1.33417","c":"1.33485"},"ask":{"o":"1.33519","h":"1.33595","l":"1.33436","c":"1.33519"}},{"complete":true,"volume":333,"time":"2016-10-25T21:00:00.000000000Z","bid":{"o":"1.33462","h":"1.33570","l":"1.33462","c":"1.33549"},"ask":{"o":"1.33537","h":"1.33596","l":"1.33537","c":"1.33582"}},{"complete":true,"volume":973,"time":"2016-10-25T22:00:00.000000000Z","bid":{"o":"1.33539","h":"1.33539","l":"1.33483","c":"1.33508"},"ask":{"o":"1.33578","h":"1.33578","l":"1.33510","c":"1.33534"}},{"complete":true,"volume":528,"time":"2016-10-25T23:00:00.000000000Z","bid":{"o":"1.33506","h":"1.33544","l":"1.33491","c":"1.33516"},"ask":{"o":"1.33532","h":"1.33568","l":"1.33519","c":"1.33539"}},{"complete":true,"volume":2348,"time":"2016-10-26T00:00:00.000000000Z","bid":{"o":"1.33515","h":"1.33586","l":"1.33311","c":"1.33509"},"ask":{"o":"1.33541","h":"1.33609","l":"1.33335","c":"1.33530"}},{"complete":true,"volume":886,"time":"2016-10-26T01:00:00.000000000Z","bid":{"o":"1.33508","h":"1.33570","l":"1.33477","c":"1.33521"},"ask":{"o":"1.33530","h":"1.33592","l":"1.33496","c":"1.33537"}},{"complete":true,"volume":587,"time":"2016-10-26T02:00:00.000000000Z","bid":{"o":"1.33519","h":"1.33519","l":"1.33453","c":"1.33486"},"ask":{"o":"1.33536","h":"1.33536","l":"1.33473","c":"1.33505"}},{"complete":true,"volume":531,"time":"2016-10-26T03:00:00.000000000Z","bid":{"o":"1.33483","h":"1.33533","l":"1.33471","c":"1.33525"},"ask":{"o":"1.33506","h":"1.33555","l":"1.33491","c":"1.33543"}},{"complete":true,"volume":531,"time":"2016-10-26T04:00:00.000000000Z","bid":{"o":"1.33525","h":"1.33547","l":"1.33497","c":"1.33547"},"ask":{"o":"1.33544","h":"1.33569","l":"1.33516","c":"1.33569"}},{"complete":true,"volume":331,"time":"2016-10-26T05:00:00.000000000Z","bid":{"o":"1.33548","h":"1.33581","l":"1.33507","c":"1.33507"},"ask":{"o":"1.33567","h":"1.33599","l":"1.33527","c":"1.33532"}},{"complete":true,"volume":2010,"time":"2016-10-26T06:00:00.000000000Z","bid":{"o":"1.33507","h":"1.33579","l":"1.33331","c":"1.33384"},"ask":{"o":"1.33531","h":"1.33600","l":"1.33350","c":"1.33404"}},{"complete":true,"volume":2307,"time":"2016-10-26T07:00:00.000000000Z","bid":{"o":"1.33389","h":"1.33573","l":"1.33354","c":"1.33405"},"ask":{"o":"1.33407","h":"1.33590","l":"1.33372","c":"1.33424"}},{"complete":true,"volume":2370,"time":"2016-10-26T08:00:00.000000000Z","bid":{"o":"1.33402","h":"1.33572","l":"1.33355","c":"1.33451"},"ask":{"o":"1.33419","h":"1.33592","l":"1.33372","c":"1.33467"}},{"complete":true,"volume":2496,"time":"2016-10-26T09:00:00.000000000Z","bid":{"o":"1.33455","h":"1.33659","l":"1.33438","c":"1.33581"},"ask":{"o":"1.33471","h":"1.33678","l":"1.33458","c":"1.33599"}},{"complete":true,"volume":1497,"time":"2016-10-26T10:00:00.000000000Z","bid":{"o":"1.33583","h":"1.33656","l":"1.33530","c":"1.33613"},"ask":{"o":"1.33603","h":"1.33672","l":"1.33547","c":"1.33633"}},{"complete":true,"volume":1832,"time":"2016-10-26T11:00:00.000000000Z","bid":{"o":"1.33617","h":"1.33798","l":"1.33580","c":"1.33623"},"ask":{"o":"1.33635","h":"1.33818","l":"1.33596","c":"1.33640"}},{"complete":true,"volume":2787,"time":"2016-10-26T12:00:00.000000000Z","bid":{"o":"1.33626","h":"1.33751","l":"1.33538","c":"1.33684"},"ask":{"o":"1.33644","h":"1.33769","l":"1.33556","c":"1.33703"}},{"complete":true,"volume":4938,"time":"2016-10-26T13:00:00.000000000Z","bid":{"o":"1.33687","h":"1.33737","l":"1.33496","c":"1.33716"},"ask":{"o":"1.33705","h":"1.33758","l":"1.33513","c":"1.33739"}},{"complete":true,"volume":6795,"time":"2016-10-26T14:00:00.000000000Z","bid":{"o":"1.33710","h":"1.33779","l":"1.33130","c":"1.33307"},"ask":{"o":"1.33736","h":"1.33795","l":"1.33156","c":"1.33325"}},{"complete":true,"volume":3530,"time":"2016-10-26T15:00:00.000000000Z","bid":{"o":"1.33304","h":"1.33694","l":"1.33301","c":"1.33603"},"ask":{"o":"1.33321","h":"1.33712","l":"1.33317","c":"1.33621"}},{"complete":true,"volume":1702,"time":"2016-10-26T16:00:00.000000000Z","bid":{"o":"1.33600","h":"1.33654","l":"1.33523","c":"1.33626"},"ask":{"o":"1.33617","h":"1.33673","l":"1.33540","c":"1.33642"}},{"complete":true,"volume":2040,"time":"2016-10-26T17:00:00.000000000Z","bid":{"o":"1.33623","h":"1.33747","l":"1.33596","c":"1.33620"},"ask":{"o":"1.33640","h":"1.33768","l":"1.33614","c":"1.33637"}},{"complete":true,"volume":1487,"time":"2016-10-26T18:00:00.000000000Z","bid":{"o":"1.33619","h":"1.33712","l":"1.33579","c":"1.33709"},"ask":{"o":"1.33637","h":"1.33733","l":"1.33595","c":"1.33728"}},{"complete":true,"volume":1344,"time":"2016-10-26T19:00:00.000000000Z","bid":{"o":"1.33710","h":"1.33840","l":"1.33695","c":"1.33814"},"ask":{"o":"1.33731","h":"1.33857","l":"1.33712","c":"1.33833"}},{"complete":true,"volume":524,"time":"2016-10-26T20:00:00.000000000Z","bid":{"o":"1.33811","h":"1.33811","l":"1.33685","c":"1.33781"},"ask":{"o":"1.33830","h":"1.33835","l":"1.33708","c":"1.33815"}},{"complete":true,"volume":215,"time":"2016-10-26T21:00:00.000000000Z","bid":{"o":"1.33724","h":"1.33778","l":"1.33680","c":"1.33756"},"ask":{"o":"1.33824","h":"1.33836","l":"1.33744","c":"1.33795"}},{"complete":true,"volume":616,"time":"2016-10-26T22:00:00.000000000Z","bid":{"o":"1.33744","h":"1.33833","l":"1.33734","c":"1.33783"},"ask":{"o":"1.33791","h":"1.33863","l":"1.33779","c":"1.33809"}},{"complete":true,"volume":455,"time":"2016-10-26T23:00:00.000000000Z","bid":{"o":"1.33785","h":"1.33827","l":"1.33727","c":"1.33732"},"ask":{"o":"1.33812","h":"1.33849","l":"1.33744","c":"1.33751"}},{"complete":true,"volume":1187,"time":"2016-10-27T00:00:00.000000000Z","bid":{"o":"1.33734","h":"1.33883","l":"1.33710","c":"1.33871"},"ask":{"o":"1.33754","h":"1.33904","l":"1.33732","c":"1.33892"}},{"complete":true,"volume":869,"time":"2016-10-27T01:00:00.000000000Z","bid":{"o":"1.33873","h":"1.33942","l":"1.33834","c":"1.33918"},"ask":{"o":"1.33893","h":"1.33962","l":"1.33860","c":"1.33938"}},{"complete":true,"volume":696,"time":"2016-10-27T02:00:00.000000000Z","bid":{"o":"1.33921","h":"1.33927","l":"1.33826","c":"1.33921"},"ask":{"o":"1.33943","h":"1.33946","l":"1.33848","c":"1.33936"}},{"complete":true,"volume":552,"time":"2016-10-27T03:00:00.000000000Z","bid":{"o":"1.33923","h":"1.33931","l":"1.33871","c":"1.33890"},"ask":{"o":"1.33943","h":"1.33950","l":"1.33890","c":"1.33910"}},{"complete":true,"volume":396,"time":"2016-10-27T04:00:00.000000000Z","bid":{"o":"1.33892","h":"1.33898","l":"1.33857","c":"1.33881"},"ask":{"o":"1.33912","h":"1.33919","l":"1.33877","c":"1.33902"}},{"complete":true,"volume":613,"time":"2016-10-27T05:00:00.000000000Z","bid":{"o":"1.33879","h":"1.33901","l":"1.33808","c":"1.33836"},"ask":{"o":"1.33903","h":"1.33922","l":"1.33832","c":"1.33857"}},{"complete":true,"volume":2247,"time":"2016-10-27T06:00:00.000000000Z","bid":{"o":"1.33828","h":"1.33899","l":"1.33757","c":"1.33850"},"ask":{"o":"1.33856","h":"1.33920","l":"1.33781","c":"1.33874"}},{"complete":true,"volume":2563,"time":"2016-10-27T07:00:00.000000000Z","bid":{"o":"1.33845","h":"1.33936","l":"1.33736","c":"1.33737"},"ask":{"o":"1.33871","h":"1.33958","l":"1.33757","c":"1.33758"}},{"complete":true,"volume":4126,"time":"2016-10-27T08:00:00.000000000Z","bid":{"o":"1.33735","h":"1.33876","l":"1.33598","c":"1.33837"},"ask":{"o":"1.33759","h":"1.33899","l":"1.33623","c":"1.33859"}},{"complete":true,"volume":2485,"time":"2016-10-27T09:00:00.000000000Z","bid":{"o":"1.33840","h":"1.33843","l":"1.33659","c":"1.33788"},"ask":{"o":"1.33865","h":"1.33867","l":"1.33681","c":"1.33809"}},{"complete":true,"volume":3224,"time":"2016-10-27T10:00:00.000000000Z","bid":{"o":"1.33784","h":"1.33785","l":"1.33514","c":"1.33591"},"ask":{"o":"1.33804","h":"1.33806","l":"1.33540","c":"1.33612"}},{"complete":true,"volume":2322,"time":"2016-10-27T11:00:00.000000000Z","bid":{"o":"1.33591","h":"1.33819","l":"1.33544","c":"1.33775"},"ask":{"o":"1.33611","h":"1.33843","l":"1.33568","c":"1.33796"}},{"complete":true,"volume":3236,"time":"2016-10-27T12:00:00.000000000Z","bid":{"o":"1.33780","h":"1.33794","l":"1.33602","c":"1.33622"},"ask":{"o":"1.33799","h":"1.33816","l":"1.33626","c":"1.33641"}},{"complete":true,"volume":3512,"time":"2016-10-27T13:00:00.000000000Z","bid":{"o":"1.33621","h":"1.33828","l":"1.33523","c":"1.33677"},"ask":{"o":"1.33641","h":"1.33848","l":"1.33541","c":"1.33697"}},{"complete":true,"volume":4572,"time":"2016-10-27T14:00:00.000000000Z","bid":{"o":"1.33672","h":"1.33906","l":"1.33652","c":"1.33851"},"ask":{"o":"1.33692","h":"1.33924","l":"1.33679","c":"1.33870"}},{"complete":true,"volume":2681,"time":"2016-10-27T15:00:00.000000000Z","bid":{"o":"1.33846","h":"1.33922","l":"1.33778","c":"1.33849"},"ask":{"o":"1.33865","h":"1.33948","l":"1.33796","c":"1.33867"}},{"complete":true,"volume":2537,"time":"2016-10-27T16:00:00.000000000Z","bid":{"o":"1.33847","h":"1.34061","l":"1.33841","c":"1.33882"},"ask":{"o":"1.33864","h":"1.34090","l":"1.33858","c":"1.33898"}},{"complete":true,"volume":1928,"time":"2016-10-27T17:00:00.000000000Z","bid":{"o":"1.33877","h":"1.33915","l":"1.33785","c":"1.33867"},"ask":{"o":"1.33897","h":"1.33932","l":"1.33804","c":"1.33884"}},{"complete":true,"volume":1602,"time":"2016-10-27T18:00:00.000000000Z","bid":{"o":"1.33867","h":"1.34029","l":"1.33808","c":"1.34003"},"ask":{"o":"1.33883","h":"1.34054","l":"1.33825","c":"1.34019"}},{"complete":true,"volume":1332,"time":"2016-10-27T19:00:00.000000000Z","bid":{"o":"1.33996","h":"1.34000","l":"1.33836","c":"1.33855"},"ask":{"o":"1.34018","h":"1.34019","l":"1.33857","c":"1.33875"}},{"complete":true,"volume":1192,"time":"2016-10-27T20:00:00.000000000Z","bid":{"o":"1.33859","h":"1.33891","l":"1.33821","c":"1.33844"},"ask":{"o":"1.33877","h":"1.33910","l":"1.33848","c":"1.33874"}},{"complete":true,"volume":350,"time":"2016-10-27T21:00:00.000000000Z","bid":{"o":"1.33823","h":"1.33899","l":"1.33780","c":"1.33870"},"ask":{"o":"1.33878","h":"1.33940","l":"1.33839","c":"1.33917"}},{"complete":true,"volume":1050,"time":"2016-10-27T22:00:00.000000000Z","bid":{"o":"1.33870","h":"1.33957","l":"1.33854","c":"1.33936"},"ask":{"o":"1.33915","h":"1.33992","l":"1.33887","c":"1.33965"}},{"complete":true,"volume":560,"time":"2016-10-27T23:00:00.000000000Z","bid":{"o":"1.33938","h":"1.33952","l":"1.33853","c":"1.33855"},"ask":{"o":"1.33967","h":"1.33980","l":"1.33879","c":"1.33885"}},{"complete":true,"volume":712,"time":"2016-10-28T00:00:00.000000000Z","bid":{"o":"1.33851","h":"1.33903","l":"1.33842","c":"1.33859"},"ask":{"o":"1.33879","h":"1.33927","l":"1.33866","c":"1.33884"}},{"complete":true,"volume":809,"time":"2016-10-28T01:00:00.000000000Z","bid":{"o":"1.33860","h":"1.33879","l":"1.33794","c":"1.33816"},"ask":{"o":"1.33884","h":"1.33897","l":"1.33817","c":"1.33839"}},{"complete":true,"volume":589,"time":"2016-10-28T02:00:00.000000000Z","bid":{"o":"1.33815","h":"1.33825","l":"1.33735","c":"1.33816"},"ask":{"o":"1.33839","h":"1.33847","l":"1.33754","c":"1.33839"}},{"complete":true,"volume":457,"time":"2016-10-28T03:00:00.000000000Z","bid":{"o":"1.33815","h":"1.33818","l":"1.33750","c":"1.33766"},"ask":{"o":"1.33839","h":"1.33843","l":"1.33772","c":"1.33788"}},{"complete":true,"volume":370,"time":"2016-10-28T04:00:00.000000000Z","bid":{"o":"1.33762","h":"1.33797","l":"1.33750","c":"1.33784"},"ask":{"o":"1.33783","h":"1.33819","l":"1.33770","c":"1.33806"}},{"complete":true,"volume":763,"time":"2016-10-28T05:00:00.000000000Z","bid":{"o":"1.33788","h":"1.33857","l":"1.33782","c":"1.33803"},"ask":{"o":"1.33811","h":"1.33885","l":"1.33804","c":"1.33829"}},{"complete":true,"volume":2029,"time":"2016-10-28T06:00:00.000000000Z","bid":{"o":"1.33806","h":"1.33945","l":"1.33801","c":"1.33891"},"ask":{"o":"1.33832","h":"1.33966","l":"1.33825","c":"1.33915"}},{"complete":true,"volume":3044,"time":"2016-10-28T07:00:00.000000000Z","bid":{"o":"1.33890","h":"1.33977","l":"1.33866","c":"1.33887"},"ask":{"o":"1.33916","h":"1.33997","l":"1.33888","c":"1.33910"}},{"complete":true,"volume":2570,"time":"2016-10-28T08:00:00.000000000Z","bid":{"o":"1.33892","h":"1.33979","l":"1.33747","c":"1.33816"},"ask":{"o":"1.33914","h":"1.33999","l":"1.33771","c":"1.33838"}},{"complete":true,"volume":2619,"time":"2016-10-28T09:00:00.000000000Z","bid":{"o":"1.33816","h":"1.33924","l":"1.33774","c":"1.33905"},"ask":{"o":"1.33840","h":"1.33945","l":"1.33796","c":"1.33927"}},{"complete":true,"volume":1931,"time":"2016-10-28T10:00:00.000000000Z","bid":{"o":"1.33907","h":"1.33917","l":"1.33768","c":"1.33776"},"ask":{"o":"1.33925","h":"1.33939","l":"1.33788","c":"1.33795"}},{"complete":true,"volume":1822,"time":"2016-10-28T11:00:00.000000000Z","bid":{"o":"1.33774","h":"1.33904","l":"1.33752","c":"1.33842"},"ask":{"o":"1.33796","h":"1.33926","l":"1.33772","c":"1.33863"}},{"complete":true,"volume":5384,"time":"2016-10-28T12:00:00.000000000Z","bid":{"o":"1.33842","h":"1.34182","l":"1.33766","c":"1.33856"},"ask":{"o":"1.33865","h":"1.34236","l":"1.33840","c":"1.33876"}},{"complete":true,"volume":4243,"time":"2016-10-28T13:00:00.000000000Z","bid":{"o":"1.33856","h":"1.34038","l":"1.33769","c":"1.33837"},"ask":{"o":"1.33877","h":"1.34056","l":"1.33787","c":"1.33866"}},{"complete":true,"volume":5382,"time":"2016-10-28T14:00:00.000000000Z","bid":{"o":"1.33833","h":"1.33904","l":"1.33518","c":"1.33684"},"ask":{"o":"1.33865","h":"1.33925","l":"1.33539","c":"1.33702"}},{"complete":true,"volume":3724,"time":"2016-10-28T15:00:00.000000000Z","bid":{"o":"1.33688","h":"1.33924","l":"1.33535","c":"1.33843"},"ask":{"o":"1.33706","h":"1.33939","l":"1.33554","c":"1.33861"}},{"complete":true,"volume":1323,"time":"2016-10-28T16:00:00.000000000Z","bid":{"o":"1.33837","h":"1.33848","l":"1.33773","c":"1.33820"},"ask":{"o":"1.33857","h":"1.33867","l":"1.33789","c":"1.33837"}},{"complete":true,"volume":8406,"time":"2016-10-28T17:00:00.000000000Z","bid":{"o":"1.33819","h":"1.34177","l":"1.33777","c":"1.34141"},"ask":{"o":"1.33839","h":"1.34199","l":"1.33794","c":"1.34161"}},{"complete":true,"volume":5482,"time":"2016-10-28T18:00:00.000000000Z","bid":{"o":"1.34136","h":"1.34324","l":"1.33859","c":"1.33880"},"ask":{"o":"1.34156","h":"1.34352","l":"1.33896","c":"1.33901"}},{"complete":true,"volume":3964,"time":"2016-10-28T19:00:00.000000000Z","bid":{"o":"1.33881","h":"1.33973","l":"1.33757","c":"1.33844"},"ask":{"o":"1.33905","h":"1.34001","l":"1.33792","c":"1.33866"}},{"complete":true,"volume":1451,"time":"2016-10-28T20:00:00.000000000Z","bid":{"o":"1.33838","h":"1.33957","l":"1.33804","c":"1.33926"},"ask":{"o":"1.33864","h":"1.34027","l":"1.33825","c":"1.34019"}},{"complete":true,"volume":412,"time":"2016-10-30T21:00:00.000000000Z","bid":{"o":"1.34236","h":"1.34237","l":"1.34005","c":"1.34074"},"ask":{"o":"1.34305","h":"1.34328","l":"1.34105","c":"1.34134"}},{"complete":true,"volume":1709,"time":"2016-10-30T22:00:00.000000000Z","bid":{"o":"1.34079","h":"1.34134","l":"1.34038","c":"1.34129"},"ask":{"o":"1.34137","h":"1.34168","l":"1.34081","c":"1.34161"}},{"complete":true,"volume":865,"time":"2016-10-30T23:00:00.000000000Z","bid":{"o":"1.34129","h":"1.34134","l":"1.33993","c":"1.34045"},"ask":{"o":"1.34163","h":"1.34163","l":"1.34019","c":"1.34068"}},{"complete":true,"volume":1187,"time":"2016-10-31T00:00:00.000000000Z","bid":{"o":"1.34049","h":"1.34226","l":"1.34021","c":"1.34156"},"ask":{"o":"1.34073","h":"1.34250","l":"1.34045","c":"1.34177"}},{"complete":true,"volume":1110,"time":"2016-10-31T01:00:00.000000000Z","bid":{"o":"1.34151","h":"1.34208","l":"1.34056","c":"1.34119"},"ask":{"o":"1.34172","h":"1.34234","l":"1.34080","c":"1.34146"}},{"complete":true,"volume":915,"time":"2016-10-31T02:00:00.000000000Z","bid":{"o":"1.34124","h":"1.34191","l":"1.34110","c":"1.34129"},"ask":{"o":"1.34150","h":"1.34214","l":"1.34132","c":"1.34149"}},{"complete":true,"volume":626,"time":"2016-10-31T03:00:00.000000000Z","bid":{"o":"1.34128","h":"1.34132","l":"1.34039","c":"1.34064"},"ask":{"o":"1.34149","h":"1.34152","l":"1.34058","c":"1.34085"}},{"complete":true,"volume":425,"time":"2016-10-31T04:00:00.000000000Z","bid":{"o":"1.34066","h":"1.34066","l":"1.34003","c":"1.34022"},"ask":{"o":"1.34083","h":"1.34085","l":"1.34020","c":"1.34042"}},{"complete":true,"volume":849,"time":"2016-10-31T05:00:00.000000000Z","bid":{"o":"1.34021","h":"1.34042","l":"1.33955","c":"1.33983"},"ask":{"o":"1.34042","h":"1.34064","l":"1.33979","c":"1.34008"}},{"complete":true,"volume":863,"time":"2016-10-31T06:00:00.000000000Z","bid":{"o":"1.33981","h":"1.34064","l":"1.33981","c":"1.34058"},"ask":{"o":"1.34009","h":"1.34087","l":"1.34009","c":"1.34083"}},{"complete":true,"volume":2275,"time":"2016-10-31T07:00:00.000000000Z","bid":{"o":"1.34052","h":"1.34182","l":"1.33919","c":"1.33967"},"ask":{"o":"1.34079","h":"1.34204","l":"1.33940","c":"1.33988"}},{"complete":true,"volume":2560,"time":"2016-10-31T08:00:00.000000000Z","bid":{"o":"1.33967","h":"1.33979","l":"1.33743","c":"1.33855"},"ask":{"o":"1.33989","h":"1.34003","l":"1.33767","c":"1.33877"}},{"complete":true,"volume":1675,"time":"2016-10-31T09:00:00.000000000Z","bid":{"o":"1.33850","h":"1.33931","l":"1.33783","c":"1.33835"},"ask":{"o":"1.33872","h":"1.33954","l":"1.33806","c":"1.33860"}},{"complete":true,"volume":1901,"time":"2016-10-31T10:00:00.000000000Z","bid":{"o":"1.33833","h":"1.33922","l":"1.33776","c":"1.33894"},"ask":{"o":"1.33861","h":"1.33943","l":"1.33798","c":"1.33914"}},{"complete":true,"volume":1840,"time":"2016-10-31T11:00:00.000000000Z","bid":{"o":"1.33893","h":"1.33964","l":"1.33753","c":"1.33768"},"ask":{"o":"1.33915","h":"1.33982","l":"1.33775","c":"1.33789"}},{"complete":true,"volume":2336,"time":"2016-10-31T12:00:00.000000000Z","bid":{"o":"1.33773","h":"1.33943","l":"1.33746","c":"1.33901"},"ask":{"o":"1.33794","h":"1.33960","l":"1.33767","c":"1.33919"}},{"complete":true,"volume":3058,"time":"2016-10-31T13:00:00.000000000Z","bid":{"o":"1.33905","h":"1.34073","l":"1.33900","c":"1.34066"},"ask":{"o":"1.33923","h":"1.34092","l":"1.33919","c":"1.34085"}},{"complete":true,"volume":3317,"time":"2016-10-31T14:00:00.000000000Z","bid":{"o":"1.34067","h":"1.34212","l":"1.33948","c":"1.34087"},"ask":{"o":"1.34084","h":"1.34233","l":"1.33972","c":"1.34108"}},{"complete":true,"volume":3606,"time":"2016-10-31T15:00:00.000000000Z","bid":{"o":"1.34085","h":"1.34231","l":"1.33953","c":"1.34016"},"ask":{"o":"1.34105","h":"1.34253","l":"1.33974","c":"1.34036"}},{"complete":true,"volume":3300,"time":"2016-10-31T16:00:00.000000000Z","bid":{"o":"1.34013","h":"1.34074","l":"1.33865","c":"1.34031"},"ask":{"o":"1.34037","h":"1.34090","l":"1.33883","c":"1.34050"}},{"complete":true,"volume":1787,"time":"2016-10-31T17:00:00.000000000Z","bid":{"o":"1.34032","h":"1.34127","l":"1.33976","c":"1.34043"},"ask":{"o":"1.34050","h":"1.34149","l":"1.33995","c":"1.34062"}},{"complete":true,"volume":1719,"time":"2016-10-31T18:00:00.000000000Z","bid":{"o":"1.34045","h":"1.34219","l":"1.33990","c":"1.34202"},"ask":{"o":"1.34064","h":"1.34239","l":"1.34011","c":"1.34220"}},{"complete":true,"volume":1558,"time":"2016-10-31T19:00:00.000000000Z","bid":{"o":"1.34204","h":"1.34210","l":"1.34098","c":"1.34101"},"ask":{"o":"1.34223","h":"1.34231","l":"1.34118","c":"1.34120"}},{"complete":true,"volume":1368,"time":"2016-10-31T20:00:00.000000000Z","bid":{"o":"1.34101","h":"1.34220","l":"1.34057","c":"1.34064"},"ask":{"o":"1.34121","h":"1.34241","l":"1.34087","c":"1.34112"}},{"complete":true,"volume":230,"time":"2016-10-31T21:00:00.000000000Z","bid":{"o":"1.34060","h":"1.34170","l":"1.34058","c":"1.34108"},"ask":{"o":"1.34146","h":"1.34213","l":"1.34121","c":"1.34155"}},{"complete":true,"volume":1191,"time":"2016-10-31T22:00:00.000000000Z","bid":{"o":"1.34114","h":"1.34163","l":"1.34105","c":"1.34110"},"ask":{"o":"1.34166","h":"1.34216","l":"1.34136","c":"1.34140"}},{"complete":true,"volume":769,"time":"2016-10-31T23:00:00.000000000Z","bid":{"o":"1.34113","h":"1.34201","l":"1.34107","c":"1.34188"},"ask":{"o":"1.34141","h":"1.34229","l":"1.34136","c":"1.34216"}},{"complete":true,"volume":980,"time":"2016-11-01T00:00:00.000000000Z","bid":{"o":"1.34189","h":"1.34244","l":"1.34180","c":"1.34192"},"ask":{"o":"1.34221","h":"1.34275","l":"1.34207","c":"1.34228"}},{"complete":true,"volume":1699,"time":"2016-11-01T01:00:00.000000000Z","bid":{"o":"1.34178","h":"1.34210","l":"1.34082","c":"1.34083"},"ask":{"o":"1.34227","h":"1.34244","l":"1.34104","c":"1.34105"}},{"complete":true,"volume":801,"time":"2016-11-01T02:00:00.000000000Z","bid":{"o":"1.34086","h":"1.34087","l":"1.34041","c":"1.34062"},"ask":{"o":"1.34107","h":"1.34112","l":"1.34064","c":"1.34081"}},{"complete":true,"volume":1658,"time":"2016-11-01T03:00:00.000000000Z","bid":{"o":"1.34055","h":"1.34150","l":"1.34020","c":"1.34139"},"ask":{"o":"1.34079","h":"1.34173","l":"1.34045","c":"1.34162"}},{"complete":true,"volume":1124,"time":"2016-11-01T04:00:00.000000000Z","bid":{"o":"1.34138","h":"1.34165","l":"1.34093","c":"1.34161"},"ask":{"o":"1.34162","h":"1.34187","l":"1.34117","c":"1.34182"}},{"complete":true,"volume":1216,"time":"2016-11-01T05:00:00.000000000Z","bid":{"o":"1.34162","h":"1.34175","l":"1.34059","c":"1.34097"},"ask":{"o":"1.34182","h":"1.34197","l":"1.34082","c":"1.34124"}},{"complete":true,"volume":1051,"time":"2016-11-01T06:00:00.000000000Z","bid":{"o":"1.34100","h":"1.34145","l":"1.34027","c":"1.34074"},"ask":{"o":"1.34126","h":"1.34169","l":"1.34053","c":"1.34099"}},{"complete":true,"volume":1857,"time":"2016-11-01T07:00:00.000000000Z","bid":{"o":"1.34072","h":"1.34082","l":"1.33910","c":"1.33918"},"ask":{"o":"1.34097","h":"1.34108","l":"1.33933","c":"1.33939"}},{"complete":true,"volume":2782,"time":"2016-11-01T08:00:00.000000000Z","bid":{"o":"1.33917","h":"1.33980","l":"1.33752","c":"1.33931"},"ask":{"o":"1.33939","h":"1.34000","l":"1.33777","c":"1.33954"}},{"complete":true,"volume":2344,"time":"2016-11-01T09:00:00.000000000Z","bid":{"o":"1.33929","h":"1.34055","l":"1.33893","c":"1.33979"},"ask":{"o":"1.33951","h":"1.34079","l":"1.33916","c":"1.34001"}},{"complete":true,"volume":1960,"time":"2016-11-01T10:00:00.000000000Z","bid":{"o":"1.33975","h":"1.34020","l":"1.33827","c":"1.33841"},"ask":{"o":"1.33997","h":"1.34046","l":"1.33849","c":"1.33863"}},{"complete":true,"volume":2306,"time":"2016-11-01T11:00:00.000000000Z","bid":{"o":"1.33841","h":"1.33932","l":"1.33764","c":"1.33845"},"ask":{"o":"1.33864","h":"1.33955","l":"1.33786","c":"1.33864"}},{"complete":true,"volume":3082,"time":"2016-11-01T12:00:00.000000000Z","bid":{"o":"1.33840","h":"1.33887","l":"1.33611","c":"1.33652"},"ask":{"o":"1.33860","h":"1.33911","l":"1.33661","c":"1.33674"}},{"complete":true,"volume":3389,"time":"2016-11-01T13:00:00.000000000Z","bid":{"o":"1.33656","h":"1.33882","l":"1.33526","c":"1.33847"},"ask":{"o":"1.33678","h":"1.33899","l":"1.33546","c":"1.33879"}},{"complete":true,"volume":4367,"time":"2016-11-01T14:00:00.000000000Z","bid":{"o":"1.33847","h":"1.34036","l":"1.33772","c":"1.33887"},"ask":{"o":"1.33877","h":"1.34053","l":"1.33795","c":"1.33904"}},{"complete":true,"volume":4332,"time":"2016-11-01T15:00:00.000000000Z","bid":{"o":"1.33887","h":"1.34134","l":"1.33686","c":"1.33756"},"ask":{"o":"1.33905","h":"1.34172","l":"1.33709","c":"1.33775"}},{"complete":true,"volume":3930,"time":"2016-11-01T16:00:00.000000000Z","bid":{"o":"1.33753","h":"1.34036","l":"1.33722","c":"1.33806"},"ask":{"o":"1.33771","h":"1.34057","l":"1.33745","c":"1.33828"}},{"complete":true,"volume":2616,"time":"2016-11-01T17:00:00.000000000Z","bid":{"o":"1.33804","h":"1.33971","l":"1.33713","c":"1.33808"},"ask":{"o":"1.33827","h":"1.33993","l":"1.33733","c":"1.33826"}},{"complete":true,"volume":3002,"time":"2016-11-01T18:00:00.000000000Z","bid":{"o":"1.33807","h":"1.33921","l":"1.33740","c":"1.33852"},"ask":{"o":"1.33828","h":"1.33942","l":"1.33761","c":"1.33872"}},{"complete":true,"volume":1807,"time":"2016-11-01T19:00:00.000000000Z","bid":{"o":"1.33850","h":"1.33953","l":"1.33841","c":"1.33918"},"ask":{"o":"1.33872","h":"1.33973","l":"1.33863","c":"1.33940"}},{"complete":true,"volume":1248,"time":"2016-11-01T20:00:00.000000000Z","bid":{"o":"1.33920","h":"1.33971","l":"1.33838","c":"1.33870"},"ask":{"o":"1.33939","h":"1.33999","l":"1.33859","c":"1.33914"}},{"complete":true,"volume":825,"time":"2016-11-01T21:00:00.000000000Z","bid":{"o":"1.33839","h":"1.33959","l":"1.33829","c":"1.33855"},"ask":{"o":"1.33918","h":"1.34007","l":"1.33876","c":"1.33896"}},{"complete":true,"volume":1354,"time":"2016-11-01T22:00:00.000000000Z","bid":{"o":"1.33854","h":"1.33872","l":"1.33823","c":"1.33848"},"ask":{"o":"1.33898","h":"1.33912","l":"1.33864","c":"1.33885"}},{"complete":true,"volume":515,"time":"2016-11-01T23:00:00.000000000Z","bid":{"o":"1.33853","h":"1.33926","l":"1.33845","c":"1.33923"},"ask":{"o":"1.33894","h":"1.33952","l":"1.33879","c":"1.33950"}},{"complete":true,"volume":1017,"time":"2016-11-02T00:00:00.000000000Z","bid":{"o":"1.33923","h":"1.33948","l":"1.33851","c":"1.33878"},"ask":{"o":"1.33951","h":"1.33973","l":"1.33877","c":"1.33899"}},{"complete":true,"volume":1251,"time":"2016-11-02T01:00:00.000000000Z","bid":{"o":"1.33874","h":"1.34007","l":"1.33849","c":"1.33944"},"ask":{"o":"1.33898","h":"1.34030","l":"1.33875","c":"1.33972"}},{"complete":true,"volume":1007,"time":"2016-11-02T02:00:00.000000000Z","bid":{"o":"1.33947","h":"1.34002","l":"1.33933","c":"1.33976"},"ask":{"o":"1.33975","h":"1.34026","l":"1.33960","c":"1.33999"}},{"complete":true,"volume":557,"time":"2016-11-02T03:00:00.000000000Z","bid":{"o":"1.33972","h":"1.33994","l":"1.33929","c":"1.33949"},"ask":{"o":"1.33995","h":"1.34019","l":"1.33955","c":"1.33972"}},{"complete":true,"volume":554,"time":"2016-11-02T04:00:00.000000000Z","bid":{"o":"1.33946","h":"1.33948","l":"1.33901","c":"1.33942"},"ask":{"o":"1.33971","h":"1.33975","l":"1.33924","c":"1.33967"}},{"complete":true,"volume":871,"time":"2016-11-02T05:00:00.000000000Z","bid":{"o":"1.33942","h":"1.33965","l":"1.33886","c":"1.33940"},"ask":{"o":"1.33962","h":"1.33993","l":"1.33909","c":"1.33960"}},{"complete":true,"volume":994,"time":"2016-11-02T06:00:00.000000000Z","bid":{"o":"1.33935","h":"1.33981","l":"1.33903","c":"1.33974"},"ask":{"o":"1.33965","h":"1.34003","l":"1.33928","c":"1.33999"}},{"complete":true,"volume":2076,"time":"2016-11-02T07:00:00.000000000Z","bid":{"o":"1.33978","h":"1.34044","l":"1.33706","c":"1.33726"},"ask":{"o":"1.34004","h":"1.34071","l":"1.33728","c":"1.33750"}},{"complete":true,"volume":2919,"time":"2016-11-02T08:00:00.000000000Z","bid":{"o":"1.33730","h":"1.33824","l":"1.33691","c":"1.33785"},"ask":{"o":"1.33754","h":"1.33846","l":"1.33714","c":"1.33809"}},{"complete":true,"volume":2115,"time":"2016-11-02T09:00:00.000000000Z","bid":{"o":"1.33789","h":"1.33867","l":"1.33733","c":"1.33814"},"ask":{"o":"1.33814","h":"1.33890","l":"1.33759","c":"1.33834"}},{"complete":true,"volume":2211,"time":"2016-11-02T10:00:00.000000000Z","bid":{"o":"1.33807","h":"1.33951","l":"1.33800","c":"1.33846"},"ask":{"o":"1.33832","h":"1.33975","l":"1.33823","c":"1.33872"}},{"complete":true,"volume":2661,"time":"2016-11-02T11:00:00.000000000Z","bid":{"o":"1.33847","h":"1.33909","l":"1.33598","c":"1.33606"},"ask":{"o":"1.33874","h":"1.33931","l":"1.33621","c":"1.33629"}},{"complete":true,"volume":3485,"time":"2016-11-02T12:00:00.000000000Z","bid":{"o":"1.33606","h":"1.33749","l":"1.33575","c":"1.33719"},"ask":{"o":"1.33628","h":"1.33771","l":"1.33605","c":"1.33739"}},{"complete":true,"volume":3849,"time":"2016-11-02T13:00:00.000000000Z","bid":{"o":"1.33723","h":"1.33856","l":"1.33603","c":"1.33627"},"ask":{"o":"1.33744","h":"1.33878","l":"1.33623","c":"1.33648"}},{"complete":true,"volume":6739,"time":"2016-11-02T14:00:00.000000000Z","bid":{"o":"1.33623","h":"1.34224","l":"1.33613","c":"1.34057"},"ask":{"o":"1.33643","h":"1.34245","l":"1.33634","c":"1.34076"}},{"complete":true,"volume":4261,"time":"2016-11-02T15:00:00.000000000Z","bid":{"o":"1.34055","h":"1.34058","l":"1.33714","c":"1.33867"},"ask":{"o":"1.34074","h":"1.34077","l":"1.33733","c":"1.33886"}},{"complete":true,"volume":2570,"time":"2016-11-02T16:00:00.000000000Z","bid":{"o":"1.33871","h":"1.34000","l":"1.33840","c":"1.33946"},"ask":{"o":"1.33889","h":"1.34018","l":"1.33860","c":"1.33965"}},{"complete":true,"volume":1915,"time":"2016-11-02T17:00:00.000000000Z","bid":{"o":"1.33949","h":"1.33961","l":"1.33706","c":"1.33757"},"ask":{"o":"1.33967","h":"1.33978","l":"1.33730","c":"1.33857"}},{"complete":true,"volume":6126,"time":"2016-11-02T18:00:00.000000000Z","bid":{"o":"1.33763","h":"1.33985","l":"1.33696","c":"1.33970"},"ask":{"o":"1.33863","h":"1.34008","l":"1.33730","c":"1.33988"}},{"complete":true,"volume":1437,"time":"2016-11-02T19:00:00.000000000Z","bid":{"o":"1.33969","h":"1.33976","l":"1.33800","c":"1.33946"},"ask":{"o":"1.33989","h":"1.33997","l":"1.33818","c":"1.33962"}},{"complete":true,"volume":1076,"time":"2016-11-02T20:00:00.000000000Z","bid":{"o":"1.33945","h":"1.33996","l":"1.33866","c":"1.33911"},"ask":{"o":"1.33963","h":"1.34015","l":"1.33897","c":"1.33942"}},{"complete":true,"volume":830,"time":"2016-11-02T21:00:00.000000000Z","bid":{"o":"1.33909","h":"1.33925","l":"1.33838","c":"1.33849"},"ask":{"o":"1.33945","h":"1.34001","l":"1.33877","c":"1.33892"}},{"complete":true,"volume":1260,"time":"2016-11-02T22:00:00.000000000Z","bid":{"o":"1.33842","h":"1.33910","l":"1.33842","c":"1.33902"},"ask":{"o":"1.33891","h":"1.33942","l":"1.33888","c":"1.33928"}},{"complete":true,"volume":524,"time":"2016-11-02T23:00:00.000000000Z","bid":{"o":"1.33903","h":"1.33950","l":"1.33868","c":"1.33947"},"ask":{"o":"1.33932","h":"1.33975","l":"1.33897","c":"1.33969"}},{"complete":true,"volume":741,"time":"2016-11-03T00:00:00.000000000Z","bid":{"o":"1.33947","h":"1.34000","l":"1.33869","c":"1.33891"},"ask":{"o":"1.33974","h":"1.34024","l":"1.33891","c":"1.33912"}},{"complete":true,"volume":984,"time":"2016-11-03T01:00:00.000000000Z","bid":{"o":"1.33890","h":"1.33913","l":"1.33832","c":"1.33839"},"ask":{"o":"1.33910","h":"1.33934","l":"1.33852","c":"1.33859"}},{"complete":true,"volume":1215,"time":"2016-11-03T02:00:00.000000000Z","bid":{"o":"1.33839","h":"1.33856","l":"1.33739","c":"1.33841"},"ask":{"o":"1.33857","h":"1.33880","l":"1.33760","c":"1.33864"}},{"complete":true,"volume":2209,"time":"2016-11-03T03:00:00.000000000Z","bid":{"o":"1.33839","h":"1.33952","l":"1.33736","c":"1.33759"},"ask":{"o":"1.33862","h":"1.33980","l":"1.33758","c":"1.33783"}},{"complete":true,"volume":1051,"time":"2016-11-03T04:00:00.000000000Z","bid":{"o":"1.33755","h":"1.33802","l":"1.33707","c":"1.33728"},"ask":{"o":"1.33779","h":"1.33826","l":"1.33732","c":"1.33753"}},{"complete":true,"volume":761,"time":"2016-11-03T05:00:00.000000000Z","bid":{"o":"1.33729","h":"1.33773","l":"1.33683","c":"1.33690"},"ask":{"o":"1.33753","h":"1.33798","l":"1.33710","c":"1.33718"}},{"complete":true,"volume":933,"time":"2016-11-03T06:00:00.000000000Z","bid":{"o":"1.33690","h":"1.33691","l":"1.33606","c":"1.33663"},"ask":{"o":"1.33716","h":"1.33718","l":"1.33631","c":"1.33690"}},{"complete":true,"volume":2192,"time":"2016-11-03T07:00:00.000000000Z","bid":{"o":"1.33662","h":"1.33750","l":"1.33607","c":"1.33645"},"ask":{"o":"1.33690","h":"1.33774","l":"1.33636","c":"1.33670"}},{"complete":true,"volume":2760,"time":"2016-11-03T08:00:00.000000000Z","bid":{"o":"1.33644","h":"1.33844","l":"1.33644","c":"1.33768"},"ask":{"o":"1.33668","h":"1.33867","l":"1.33668","c":"1.33792"}},{"complete":true,"volume":1580,"time":"2016-11-03T09:00:00.000000000Z","bid":{"o":"1.33769","h":"1.33859","l":"1.33706","c":"1.33750"},"ask":{"o":"1.33792","h":"1.33884","l":"1.33728","c":"1.33774"}},{"complete":true,"volume":3972,"time":"2016-11-03T10:00:00.000000000Z","bid":{"o":"1.33749","h":"1.33837","l":"1.33688","c":"1.33805"},"ask":{"o":"1.33776","h":"1.33860","l":"1.33716","c":"1.33828"}},{"complete":true,"volume":3000,"time":"2016-11-03T11:00:00.000000000Z","bid":{"o":"1.33809","h":"1.33908","l":"1.33722","c":"1.33864"},"ask":{"o":"1.33833","h":"1.33931","l":"1.33750","c":"1.33887"}},{"complete":true,"volume":3106,"time":"2016-11-03T12:00:00.000000000Z","bid":{"o":"1.33863","h":"1.33927","l":"1.33778","c":"1.33796"},"ask":{"o":"1.33894","h":"1.33946","l":"1.33796","c":"1.33814"}},{"complete":true,"volume":2594,"time":"2016-11-03T13:00:00.000000000Z","bid":{"o":"1.33800","h":"1.33980","l":"1.33777","c":"1.33887"},"ask":{"o":"1.33818","h":"1.33998","l":"1.33793","c":"1.33914"}},{"complete":true,"volume":2876,"time":"2016-11-03T14:00:00.000000000Z","bid":{"o":"1.33888","h":"1.33912","l":"1.33718","c":"1.33856"},"ask":{"o":"1.33919","h":"1.33933","l":"1.33735","c":"1.33872"}},{"complete":true,"volume":2694,"time":"2016-11-03T15:00:00.000000000Z","bid":{"o":"1.33859","h":"1.34017","l":"1.33807","c":"1.33835"},"ask":{"o":"1.33875","h":"1.34035","l":"1.33823","c":"1.33857"}},{"complete":true,"volume":2138,"time":"2016-11-03T16:00:00.000000000Z","bid":{"o":"1.33831","h":"1.33989","l":"1.33827","c":"1.33966"},"ask":{"o":"1.33852","h":"1.34010","l":"1.33847","c":"1.33980"}},{"complete":true,"volume":1325,"time":"2016-11-03T17:00:00.000000000Z","bid":{"o":"1.33960","h":"1.34002","l":"1.33885","c":"1.33943"},"ask":{"o":"1.33977","h":"1.34028","l":"1.33903","c":"1.33960"}},{"complete":true,"volume":1314,"time":"2016-11-03T18:00:00.000000000Z","bid":{"o":"1.33947","h":"1.33947","l":"1.33805","c":"1.33823"},"ask":{"o":"1.33964","h":"1.33964","l":"1.33825","c":"1.33842"}},{"complete":true,"volume":1008,"time":"2016-11-03T19:00:00.000000000Z","bid":{"o":"1.33821","h":"1.33849","l":"1.33753","c":"1.33831"},"ask":{"o":"1.33841","h":"1.33868","l":"1.33774","c":"1.33846"}},{"complete":true,"volume":1049,"time":"2016-11-03T20:00:00.000000000Z","bid":{"o":"1.33832","h":"1.33955","l":"1.33817","c":"1.33952"},"ask":{"o":"1.33851","h":"1.33988","l":"1.33835","c":"1.33988"}},{"complete":true,"volume":221,"time":"2016-11-03T21:00:00.000000000Z","bid":{"o":"1.33931","h":"1.33962","l":"1.33886","c":"1.33960"},"ask":{"o":"1.33978","h":"1.34016","l":"1.33941","c":"1.33994"}},{"complete":true,"volume":525,"time":"2016-11-03T22:00:00.000000000Z","bid":{"o":"1.33958","h":"1.33969","l":"1.33894","c":"1.33940"},"ask":{"o":"1.33993","h":"1.34006","l":"1.33929","c":"1.33972"}},{"complete":true,"volume":361,"time":"2016-11-03T23:00:00.000000000Z","bid":{"o":"1.33943","h":"1.33952","l":"1.33907","c":"1.33916"},"ask":{"o":"1.33974","h":"1.33979","l":"1.33933","c":"1.33941"}},{"complete":true,"volume":916,"time":"2016-11-04T00:00:00.000000000Z","bid":{"o":"1.33919","h":"1.33955","l":"1.33882","c":"1.33922"},"ask":{"o":"1.33943","h":"1.33981","l":"1.33906","c":"1.33947"}},{"complete":true,"volume":884,"time":"2016-11-04T01:00:00.000000000Z","bid":{"o":"1.33924","h":"1.33936","l":"1.33852","c":"1.33903"},"ask":{"o":"1.33947","h":"1.33957","l":"1.33878","c":"1.33925"}},{"complete":true,"volume":698,"time":"2016-11-04T02:00:00.000000000Z","bid":{"o":"1.33903","h":"1.33985","l":"1.33889","c":"1.33970"},"ask":{"o":"1.33927","h":"1.34006","l":"1.33911","c":"1.33992"}},{"complete":true,"volume":538,"time":"2016-11-04T03:00:00.000000000Z","bid":{"o":"1.33965","h":"1.33985","l":"1.33940","c":"1.33973"},"ask":{"o":"1.33987","h":"1.34004","l":"1.33964","c":"1.33997"}},{"complete":true,"volume":422,"time":"2016-11-04T04:00:00.000000000Z","bid":{"o":"1.33971","h":"1.33984","l":"1.33952","c":"1.33967"},"ask":{"o":"1.33995","h":"1.34006","l":"1.33974","c":"1.33993"}},{"complete":true,"volume":806,"time":"2016-11-04T05:00:00.000000000Z","bid":{"o":"1.33963","h":"1.34076","l":"1.33954","c":"1.34070"},"ask":{"o":"1.33987","h":"1.34099","l":"1.33978","c":"1.34091"}},{"complete":true,"volume":671,"time":"2016-11-04T06:00:00.000000000Z","bid":{"o":"1.34068","h":"1.34068","l":"1.33985","c":"1.34051"},"ask":{"o":"1.34089","h":"1.34095","l":"1.34009","c":"1.34076"}},{"complete":true,"volume":1728,"time":"2016-11-04T07:00:00.000000000Z","bid":{"o":"1.34053","h":"1.34165","l":"1.33988","c":"1.34094"},"ask":{"o":"1.34075","h":"1.34188","l":"1.34014","c":"1.34119"}},{"complete":true,"volume":2121,"time":"2016-11-04T08:00:00.000000000Z","bid":{"o":"1.34093","h":"1.34128","l":"1.33999","c":"1.34007"},"ask":{"o":"1.34119","h":"1.34151","l":"1.34022","c":"1.34027"}},{"complete":true,"volume":2082,"time":"2016-11-04T09:00:00.000000000Z","bid":{"o":"1.34006","h":"1.34112","l":"1.33972","c":"1.34038"},"ask":{"o":"1.34028","h":"1.34133","l":"1.33994","c":"1.34065"}},{"complete":true,"volume":1844,"time":"2016-11-04T10:00:00.000000000Z","bid":{"o":"1.34036","h":"1.34141","l":"1.34003","c":"1.34093"},"ask":{"o":"1.34062","h":"1.34164","l":"1.34028","c":"1.34118"}},{"complete":true,"volume":1808,"time":"2016-11-04T11:00:00.000000000Z","bid":{"o":"1.34093","h":"1.34169","l":"1.34058","c":"1.34118"},"ask":{"o":"1.34117","h":"1.34191","l":"1.34081","c":"1.34141"}},{"complete":true,"volume":9328,"time":"2016-11-04T12:00:00.000000000Z","bid":{"o":"1.34116","h":"1.34493","l":"1.33830","c":"1.34230"},"ask":{"o":"1.34140","h":"1.34525","l":"1.33908","c":"1.34251"}},{"complete":true,"volume":3684,"time":"2016-11-04T13:00:00.000000000Z","bid":{"o":"1.34231","h":"1.34387","l":"1.34198","c":"1.34333"},"ask":{"o":"1.34250","h":"1.34409","l":"1.34220","c":"1.34375"}},{"complete":true,"volume":7871,"time":"2016-11-04T14:00:00.000000000Z","bid":{"o":"1.34340","h":"1.34643","l":"1.33996","c":"1.34118"},"ask":{"o":"1.34375","h":"1.34674","l":"1.34023","c":"1.34138"}},{"complete":true,"volume":4669,"time":"2016-11-04T15:00:00.000000000Z","bid":{"o":"1.34119","h":"1.34221","l":"1.33893","c":"1.34139"},"ask":{"o":"1.34141","h":"1.34243","l":"1.33917","c":"1.34158"}},{"complete":true,"volume":2743,"time":"2016-11-04T16:00:00.000000000Z","bid":{"o":"1.34137","h":"1.34148","l":"1.33958","c":"1.34035"},"ask":{"o":"1.34154","h":"1.34167","l":"1.33976","c":"1.34051"}},{"complete":true,"volume":1550,"time":"2016-11-04T17:00:00.000000000Z","bid":{"o":"1.34033","h":"1.34190","l":"1.34021","c":"1.34136"},"ask":{"o":"1.34052","h":"1.34208","l":"1.34041","c":"1.34156"}},{"complete":true,"volume":1681,"time":"2016-11-04T18:00:00.000000000Z","bid":{"o":"1.34138","h":"1.34178","l":"1.34008","c":"1.34078"},"ask":{"o":"1.34155","h":"1.34201","l":"1.34029","c":"1.34095"}},{"complete":true,"volume":1450,"time":"2016-11-04T19:00:00.000000000Z","bid":{"o":"1.34082","h":"1.34111","l":"1.33990","c":"1.34031"},"ask":{"o":"1.34098","h":"1.34129","l":"1.34010","c":"1.34051"}},{"complete":true,"volume":1430,"time":"2016-11-04T20:00:00.000000000Z","bid":{"o":"1.34034","h":"1.34043","l":"1.33912","c":"1.33988"},"ask":{"o":"1.34056","h":"1.34076","l":"1.33937","c":"1.34067"}},{"complete":true,"volume":1150,"time":"2016-11-06T22:00:00.000000000Z","bid":{"o":"1.33381","h":"1.33825","l":"1.33380","c":"1.33621"},"ask":{"o":"1.33481","h":"1.33914","l":"1.33480","c":"1.33695"}},{"complete":true,"volume":2671,"time":"2016-11-06T23:00:00.000000000Z","bid":{"o":"1.33615","h":"1.33722","l":"1.33419","c":"1.33589"},"ask":{"o":"1.33693","h":"1.33752","l":"1.33452","c":"1.33620"}},{"complete":true,"volume":2082,"time":"2016-11-07T00:00:00.000000000Z","bid":{"o":"1.33582","h":"1.33898","l":"1.33576","c":"1.33863"},"ask":{"o":"1.33616","h":"1.33928","l":"1.33605","c":"1.33885"}},{"complete":true,"volume":1239,"time":"2016-11-07T01:00:00.000000000Z","bid":{"o":"1.33860","h":"1.33886","l":"1.33785","c":"1.33801"},"ask":{"o":"1.33887","h":"1.33908","l":"1.33810","c":"1.33825"}},{"complete":true,"volume":894,"time":"2016-11-07T02:00:00.000000000Z","bid":{"o":"1.33803","h":"1.33874","l":"1.33719","c":"1.33860"},"ask":{"o":"1.33827","h":"1.33898","l":"1.33751","c":"1.33882"}},{"complete":true,"volume":623,"time":"2016-11-07T03:00:00.000000000Z","bid":{"o":"1.33861","h":"1.33871","l":"1.33791","c":"1.33818"},"ask":{"o":"1.33885","h":"1.33896","l":"1.33816","c":"1.33838"}},{"complete":true,"volume":460,"time":"2016-11-07T04:00:00.000000000Z","bid":{"o":"1.33818","h":"1.33844","l":"1.33784","c":"1.33842"},"ask":{"o":"1.33836","h":"1.33863","l":"1.33807","c":"1.33862"}},{"complete":true,"volume":835,"time":"2016-11-07T05:00:00.000000000Z","bid":{"o":"1.33843","h":"1.33939","l":"1.33825","c":"1.33843"},"ask":{"o":"1.33862","h":"1.33959","l":"1.33847","c":"1.33873"}},{"complete":true,"volume":947,"time":"2016-11-07T06:00:00.000000000Z","bid":{"o":"1.33846","h":"1.33880","l":"1.33805","c":"1.33858"},"ask":{"o":"1.33874","h":"1.33905","l":"1.33836","c":"1.33892"}},{"complete":true,"volume":2579,"time":"2016-11-07T07:00:00.000000000Z","bid":{"o":"1.33856","h":"1.33955","l":"1.33790","c":"1.33885"},"ask":{"o":"1.33888","h":"1.33982","l":"1.33822","c":"1.33908"}},{"complete":true,"volume":2710,"time":"2016-11-07T08:00:00.000000000Z","bid":{"o":"1.33889","h":"1.33969","l":"1.33788","c":"1.33966"},"ask":{"o":"1.33912","h":"1.33991","l":"1.33808","c":"1.33990"}},{"complete":true,"volume":2067,"time":"2016-11-07T09:00:00.000000000Z","bid":{"o":"1.33965","h":"1.34048","l":"1.33860","c":"1.33980"},"ask":{"o":"1.33990","h":"1.34070","l":"1.33882","c":"1.34002"}},{"complete":true,"volume":1878,"time":"2016-11-07T10:00:00.000000000Z","bid":{"o":"1.33976","h":"1.34163","l":"1.33964","c":"1.34037"},"ask":{"o":"1.33997","h":"1.34185","l":"1.33984","c":"1.34060"}},{"complete":true,"volume":1603,"time":"2016-11-07T11:00:00.000000000Z","bid":{"o":"1.34033","h":"1.34041","l":"1.33890","c":"1.33933"},"ask":{"o":"1.34056","h":"1.34063","l":"1.33913","c":"1.33957"}},{"complete":true,"volume":1259,"time":"2016-11-07T12:00:00.000000000Z","bid":{"o":"1.33934","h":"1.33942","l":"1.33833","c":"1.33880"},"ask":{"o":"1.33957","h":"1.33965","l":"1.33855","c":"1.33902"}},{"complete":true,"volume":1491,"time":"2016-11-07T13:00:00.000000000Z","bid":{"o":"1.33877","h":"1.33929","l":"1.33832","c":"1.33870"},"ask":{"o":"1.33900","h":"1.33951","l":"1.33850","c":"1.33889"}},{"complete":true,"volume":2659,"time":"2016-11-07T14:00:00.000000000Z","bid":{"o":"1.33874","h":"1.33910","l":"1.33682","c":"1.33768"},"ask":{"o":"1.33893","h":"1.33929","l":"1.33700","c":"1.33786"}},{"complete":true,"volume":3106,"time":"2016-11-07T15:00:00.000000000Z","bid":{"o":"1.33769","h":"1.34029","l":"1.33738","c":"1.33988"},"ask":{"o":"1.33786","h":"1.34045","l":"1.33758","c":"1.34007"}},{"complete":true,"volume":2422,"time":"2016-11-07T16:00:00.000000000Z","bid":{"o":"1.33986","h":"1.34129","l":"1.33807","c":"1.33810"},"ask":{"o":"1.34003","h":"1.34147","l":"1.33827","c":"1.33829"}},{"complete":true,"volume":865,"time":"2016-11-07T17:00:00.000000000Z","bid":{"o":"1.33807","h":"1.33886","l":"1.33801","c":"1.33821"},"ask":{"o":"1.33824","h":"1.33903","l":"1.33820","c":"1.33837"}},{"complete":true,"volume":929,"time":"2016-11-07T18:00:00.000000000Z","bid":{"o":"1.33825","h":"1.33864","l":"1.33696","c":"1.33708"},"ask":{"o":"1.33841","h":"1.33882","l":"1.33712","c":"1.33726"}},{"complete":true,"volume":1148,"time":"2016-11-07T19:00:00.000000000Z","bid":{"o":"1.33706","h":"1.33734","l":"1.33601","c":"1.33665"},"ask":{"o":"1.33722","h":"1.33751","l":"1.33625","c":"1.33681"}},{"complete":true,"volume":1031,"time":"2016-11-07T20:00:00.000000000Z","bid":{"o":"1.33667","h":"1.33766","l":"1.33595","c":"1.33704"},"ask":{"o":"1.33686","h":"1.33788","l":"1.33613","c":"1.33724"}},{"complete":true,"volume":927,"time":"2016-11-07T21:00:00.000000000Z","bid":{"o":"1.33700","h":"1.33729","l":"1.33564","c":"1.33619"},"ask":{"o":"1.33722","h":"1.33747","l":"1.33594","c":"1.33651"}},{"complete":true,"volume":734,"time":"2016-11-07T22:00:00.000000000Z","bid":{"o":"1.33602","h":"1.33694","l":"1.33602","c":"1.33685"},"ask":{"o":"1.33695","h":"1.33749","l":"1.33677","c":"1.33738"}},{"complete":true,"volume":1001,"time":"2016-11-07T23:00:00.000000000Z","bid":{"o":"1.33694","h":"1.33746","l":"1.33672","c":"1.33716"},"ask":{"o":"1.33748","h":"1.33780","l":"1.33719","c":"1.33738"}},{"complete":true,"volume":856,"time":"2016-11-08T00:00:00.000000000Z","bid":{"o":"1.33713","h":"1.33846","l":"1.33692","c":"1.33846"},"ask":{"o":"1.33741","h":"1.33870","l":"1.33715","c":"1.33870"}},{"complete":true,"volume":1210,"time":"2016-11-08T01:00:00.000000000Z","bid":{"o":"1.33850","h":"1.33901","l":"1.33823","c":"1.33857"},"ask":{"o":"1.33875","h":"1.33925","l":"1.33849","c":"1.33882"}},{"complete":true,"volume":1279,"time":"2016-11-08T02:00:00.000000000Z","bid":{"o":"1.33856","h":"1.33899","l":"1.33843","c":"1.33873"},"ask":{"o":"1.33882","h":"1.33920","l":"1.33866","c":"1.33896"}},{"complete":true,"volume":405,"time":"2016-11-08T03:00:00.000000000Z","bid":{"o":"1.33872","h":"1.33891","l":"1.33831","c":"1.33833"},"ask":{"o":"1.33896","h":"1.33912","l":"1.33850","c":"1.33855"}},{"complete":true,"volume":346,"time":"2016-11-08T04:00:00.000000000Z","bid":{"o":"1.33828","h":"1.33838","l":"1.33802","c":"1.33819"},"ask":{"o":"1.33850","h":"1.33858","l":"1.33825","c":"1.33841"}},{"complete":true,"volume":370,"time":"2016-11-08T05:00:00.000000000Z","bid":{"o":"1.33817","h":"1.33820","l":"1.33745","c":"1.33750"},"ask":{"o":"1.33842","h":"1.33845","l":"1.33767","c":"1.33773"}},{"complete":true,"volume":308,"time":"2016-11-08T06:00:00.000000000Z","bid":{"o":"1.33754","h":"1.33805","l":"1.33745","c":"1.33756"},"ask":{"o":"1.33777","h":"1.33832","l":"1.33767","c":"1.33792"}},{"complete":true,"volume":1340,"time":"2016-11-08T07:00:00.000000000Z","bid":{"o":"1.33762","h":"1.33771","l":"1.33599","c":"1.33614"},"ask":{"o":"1.33787","h":"1.33795","l":"1.33622","c":"1.33638"}},{"complete":true,"volume":1827,"time":"2016-11-08T08:00:00.000000000Z","bid":{"o":"1.33612","h":"1.33673","l":"1.33518","c":"1.33529"},"ask":{"o":"1.33635","h":"1.33695","l":"1.33543","c":"1.33552"}},{"complete":true,"volume":1263,"time":"2016-11-08T09:00:00.000000000Z","bid":{"o":"1.33533","h":"1.33597","l":"1.33501","c":"1.33565"},"ask":{"o":"1.33557","h":"1.33620","l":"1.33524","c":"1.33588"}},{"complete":true,"volume":1303,"time":"2016-11-08T10:00:00.000000000Z","bid":{"o":"1.33566","h":"1.33673","l":"1.33546","c":"1.33647"},"ask":{"o":"1.33586","h":"1.33695","l":"1.33567","c":"1.33668"}},{"complete":true,"volume":1127,"time":"2016-11-08T11:00:00.000000000Z","bid":{"o":"1.33641","h":"1.33753","l":"1.33635","c":"1.33672"},"ask":{"o":"1.33664","h":"1.33777","l":"1.33657","c":"1.33695"}},{"complete":true,"volume":1106,"time":"2016-11-08T12:00:00.000000000Z","bid":{"o":"1.33674","h":"1.33709","l":"1.33642","c":"1.33671"},"ask":{"o":"1.33692","h":"1.33730","l":"1.33664","c":"1.33693"}},{"complete":true,"volume":1637,"time":"2016-11-08T13:00:00.000000000Z","bid":{"o":"1.33676","h":"1.33713","l":"1.33540","c":"1.33556"},"ask":{"o":"1.33697","h":"1.33734","l":"1.33559","c":"1.33577"}},{"complete":true,"volume":2760,"time":"2016-11-08T14:00:00.000000000Z","bid":{"o":"1.33560","h":"1.33817","l":"1.33560","c":"1.33717"},"ask":{"o":"1.33582","h":"1.33838","l":"1.33579","c":"1.33737"}},{"complete":true,"volume":4370,"time":"2016-11-08T15:00:00.000000000Z","bid":{"o":"1.33718","h":"1.33746","l":"1.33334","c":"1.33378"},"ask":{"o":"1.33737","h":"1.33766","l":"1.33358","c":"1.33396"}},{"complete":true,"volume":3278,"time":"2016-11-08T16:00:00.000000000Z","bid":{"o":"1.33377","h":"1.33445","l":"1.33241","c":"1.33348"},"ask":{"o":"1.33396","h":"1.33467","l":"1.33263","c":"1.33368"}},{"complete":true,"volume":2034,"time":"2016-11-08T17:00:00.000000000Z","bid":{"o":"1.33345","h":"1.33421","l":"1.33291","c":"1.33309"},"ask":{"o":"1.33366","h":"1.33452","l":"1.33312","c":"1.33326"}},{"complete":true,"volume":1685,"time":"2016-11-08T18:00:00.000000000Z","bid":{"o":"1.33309","h":"1.33383","l":"1.33216","c":"1.33250"},"ask":{"o":"1.33328","h":"1.33404","l":"1.33238","c":"1.33270"}},{"complete":true,"volume":1988,"time":"2016-11-08T19:00:00.000000000Z","bid":{"o":"1.33254","h":"1.33302","l":"1.33140","c":"1.33177"},"ask":{"o":"1.33275","h":"1.33327","l":"1.33168","c":"1.33198"}},{"complete":true,"volume":1714,"time":"2016-11-08T20:00:00.000000000Z","bid":{"o":"1.33177","h":"1.33188","l":"1.32971","c":"1.33044"},"ask":{"o":"1.33196","h":"1.33208","l":"1.32993","c":"1.33064"}},{"complete":true,"volume":1863,"time":"2016-11-08T21:00:00.000000000Z","bid":{"o":"1.33041","h":"1.33054","l":"1.32847","c":"1.32866"},"ask":{"o":"1.33063","h":"1.33080","l":"1.32870","c":"1.32908"}},{"complete":true,"volume":807,"time":"2016-11-08T22:00:00.000000000Z","bid":{"o":"1.32847","h":"1.32940","l":"1.32828","c":"1.32900"},"ask":{"o":"1.32947","h":"1.33028","l":"1.32872","c":"1.32936"}},{"complete":true,"volume":1429,"time":"2016-11-08T23:00:00.000000000Z","bid":{"o":"1.32898","h":"1.32986","l":"1.32875","c":"1.32957"},"ask":{"o":"1.32940","h":"1.33027","l":"1.32923","c":"1.32988"}},{"complete":true,"volume":6997,"time":"2016-11-09T00:00:00.000000000Z","bid":{"o":"1.32956","h":"1.33267","l":"1.32664","c":"1.32708"},"ask":{"o":"1.32990","h":"1.33315","l":"1.32705","c":"1.32746"}},{"complete":true,"volume":6896,"time":"2016-11-09T01:00:00.000000000Z","bid":{"o":"1.32696","h":"1.33277","l":"1.32622","c":"1.33277"},"ask":{"o":"1.32742","h":"1.33320","l":"1.32664","c":"1.33320"}},{"complete":true,"volume":14717,"time":"2016-11-09T02:00:00.000000000Z","bid":{"o":"1.33267","h":"1.34014","l":"1.33162","c":"1.33679"},"ask":{"o":"1.33315","h":"1.34061","l":"1.33226","c":"1.33721"}},{"complete":true,"volume":12952,"time":"2016-11-09T03:00:00.000000000Z","bid":{"o":"1.33678","h":"1.34991","l":"1.33678","c":"1.34852"},"ask":{"o":"1.33724","h":"1.35035","l":"1.33720","c":"1.34888"}},{"complete":true,"volume":9238,"time":"2016-11-09T04:00:00.000000000Z","bid":{"o":"1.34852","h":"1.35224","l":"1.34346","c":"1.35162"},"ask":{"o":"1.34890","h":"1.35270","l":"1.34390","c":"1.35196"}},{"complete":true,"volume":6154,"time":"2016-11-09T05:00:00.000000000Z","bid":{"o":"1.35160","h":"1.35234","l":"1.34643","c":"1.34673"},"ask":{"o":"1.35195","h":"1.35280","l":"1.34694","c":"1.34714"}},{"complete":true,"volume":7225,"time":"2016-11-09T06:00:00.000000000Z","bid":{"o":"1.34673","h":"1.34681","l":"1.34210","c":"1.34344"},"ask":{"o":"1.34713","h":"1.34732","l":"1.34249","c":"1.34391"}},{"complete":true,"volume":12554,"time":"2016-11-09T07:00:00.000000000Z","bid":{"o":"1.34342","h":"1.34342","l":"1.33429","c":"1.33673"},"ask":{"o":"1.34388","h":"1.34388","l":"1.33473","c":"1.33717"}},{"complete":true,"volume":10921,"time":"2016-11-09T08:00:00.000000000Z","bid":{"o":"1.33680","h":"1.34275","l":"1.33631","c":"1.33989"},"ask":{"o":"1.33724","h":"1.34319","l":"1.33671","c":"1.34024"}},{"complete":true,"volume":8088,"time":"2016-11-09T09:00:00.000000000Z","bid":{"o":"1.33990","h":"1.34422","l":"1.33869","c":"1.34288"},"ask":{"o":"1.34024","h":"1.34459","l":"1.33900","c":"1.34322"}},{"complete":true,"volume":5972,"time":"2016-11-09T10:00:00.000000000Z","bid":{"o":"1.34290","h":"1.34352","l":"1.33965","c":"1.34049"},"ask":{"o":"1.34323","h":"1.34389","l":"1.33995","c":"1.34079"}},{"complete":true,"volume":6586,"time":"2016-11-09T11:00:00.000000000Z","bid":{"o":"1.34047","h":"1.34471","l":"1.33987","c":"1.34454"},"ask":{"o":"1.34079","h":"1.34498","l":"1.34022","c":"1.34481"}},{"complete":true,"volume":5974,"time":"2016-11-09T12:00:00.000000000Z","bid":{"o":"1.34448","h":"1.34511","l":"1.34123","c":"1.34305"},"ask":{"o":"1.34476","h":"1.34539","l":"1.34150","c":"1.34332"}},{"complete":true,"volume":6662,"time":"2016-11-09T13:00:00.000000000Z","bid":{"o":"1.34307","h":"1.34663","l":"1.34090","c":"1.34536"},"ask":{"o":"1.34335","h":"1.34699","l":"1.34120","c":"1.34559"}},{"complete":true,"volume":6888,"time":"2016-11-09T14:00:00.000000000Z","bid":{"o":"1.34534","h":"1.34745","l":"1.34352","c":"1.34685"},"ask":{"o":"1.34557","h":"1.34771","l":"1.34374","c":"1.34714"}},{"complete":true,"volume":6247,"time":"2016-11-09T15:00:00.000000000Z","bid":{"o":"1.34685","h":"1.34760","l":"1.34363","c":"1.34435"},"ask":{"o":"1.34713","h":"1.34785","l":"1.34384","c":"1.34454"}},{"complete":true,"volume":3930,"time":"2016-11-09T16:00:00.000000000Z","bid":{"o":"1.34434","h":"1.34473","l":"1.34059","c":"1.34066"},"ask":{"o":"1.34455","h":"1.34494","l":"1.34079","c":"1.34085"}},{"complete":true,"volume":2560,"time":"2016-11-09T17:00:00.000000000Z","bid":{"o":"1.34063","h":"1.34248","l":"1.34045","c":"1.34178"},"ask":{"o":"1.34081","h":"1.34266","l":"1.34066","c":"1.34199"}},{"complete":true,"volume":2785,"time":"2016-11-09T18:00:00.000000000Z","bid":{"o":"1.34179","h":"1.34363","l":"1.34143","c":"1.34227"},"ask":{"o":"1.34198","h":"1.34383","l":"1.34166","c":"1.34249"}},{"complete":true,"volume":2114,"time":"2016-11-09T19:00:00.000000000Z","bid":{"o":"1.34223","h":"1.34284","l":"1.34037","c":"1.34037"},"ask":{"o":"1.34244","h":"1.34305","l":"1.34063","c":"1.34063"}},{"complete":true,"volume":5907,"time":"2016-11-09T20:00:00.000000000Z","bid":{"o":"1.34031","h":"1.34146","l":"1.33639","c":"1.33778"},"ask":{"o":"1.34058","h":"1.34169","l":"1.33678","c":"1.33803"}},{"complete":true,"volume":3602,"time":"2016-11-09T21:00:00.000000000Z","bid":{"o":"1.33777","h":"1.34222","l":"1.33766","c":"1.34213"},"ask":{"o":"1.33803","h":"1.34270","l":"1.33797","c":"1.34258"}},{"complete":true,"volume":616,"time":"2016-11-09T22:00:00.000000000Z","bid":{"o":"1.34195","h":"1.34206","l":"1.33992","c":"1.34106"},"ask":{"o":"1.34265","h":"1.34267","l":"1.34065","c":"1.34158"}},{"complete":true,"volume":1161,"time":"2016-11-09T23:00:00.000000000Z","bid":{"o":"1.34112","h":"1.34205","l":"1.34096","c":"1.34096"},"ask":{"o":"1.34173","h":"1.34234","l":"1.34125","c":"1.34125"}},{"complete":true,"volume":2048,"time":"2016-11-10T00:00:00.000000000Z","bid":{"o":"1.34099","h":"1.34184","l":"1.34078","c":"1.34179"},"ask":{"o":"1.34134","h":"1.34209","l":"1.34100","c":"1.34199"}},{"complete":true,"volume":2156,"time":"2016-11-10T01:00:00.000000000Z","bid":{"o":"1.34174","h":"1.34223","l":"1.34043","c":"1.34089"},"ask":{"o":"1.34194","h":"1.34247","l":"1.34066","c":"1.34113"}},{"complete":true,"volume":1351,"time":"2016-11-10T02:00:00.000000000Z","bid":{"o":"1.34090","h":"1.34122","l":"1.34041","c":"1.34100"},"ask":{"o":"1.34113","h":"1.34145","l":"1.34063","c":"1.34125"}},{"complete":true,"volume":1041,"time":"2016-11-10T03:00:00.000000000Z","bid":{"o":"1.34098","h":"1.34146","l":"1.34062","c":"1.34127"},"ask":{"o":"1.34122","h":"1.34167","l":"1.34086","c":"1.34153"}},{"complete":true,"volume":1101,"time":"2016-11-10T04:00:00.000000000Z","bid":{"o":"1.34123","h":"1.34241","l":"1.34106","c":"1.34232"},"ask":{"o":"1.34148","h":"1.34260","l":"1.34130","c":"1.34253"}},{"complete":true,"volume":1378,"time":"2016-11-10T05:00:00.000000000Z","bid":{"o":"1.34227","h":"1.34229","l":"1.34042","c":"1.34079"},"ask":{"o":"1.34250","h":"1.34254","l":"1.34068","c":"1.34107"}},{"complete":true,"volume":1241,"time":"2016-11-10T06:00:00.000000000Z","bid":{"o":"1.34080","h":"1.34152","l":"1.34050","c":"1.34081"},"ask":{"o":"1.34107","h":"1.34179","l":"1.34079","c":"1.34110"}},{"complete":true,"volume":2801,"time":"2016-11-10T07:00:00.000000000Z","bid":{"o":"1.34081","h":"1.34113","l":"1.33886","c":"1.33888"},"ask":{"o":"1.34109","h":"1.34139","l":"1.33910","c":"1.33911"}},{"complete":true,"volume":5059,"time":"2016-11-10T08:00:00.000000000Z","bid":{"o":"1.33881","h":"1.34078","l":"1.33857","c":"1.34058"},"ask":{"o":"1.33908","h":"1.34101","l":"1.33882","c":"1.34082"}},{"complete":true,"volume":3778,"time":"2016-11-10T09:00:00.000000000Z","bid":{"o":"1.34059","h":"1.34370","l":"1.34052","c":"1.34317"},"ask":{"o":"1.34084","h":"1.34395","l":"1.34074","c":"1.34340"}},{"complete":true,"volume":3894,"time":"2016-11-10T10:00:00.000000000Z","bid":{"o":"1.34312","h":"1.34352","l":"1.34202","c":"1.34226"},"ask":{"o":"1.34335","h":"1.34374","l":"1.34223","c":"1.34249"}},{"complete":true,"volume":4698,"time":"2016-11-10T11:00:00.000000000Z","bid":{"o":"1.34227","h":"1.34371","l":"1.34195","c":"1.34313"},"ask":{"o":"1.34249","h":"1.34395","l":"1.34220","c":"1.34336"}},{"complete":true,"volume":6986,"time":"2016-11-10T12:00:00.000000000Z","bid":{"o":"1.34310","h":"1.34962","l":"1.34295","c":"1.34709"},"ask":{"o":"1.34335","h":"1.34989","l":"1.34318","c":"1.34733"}},{"complete":true,"volume":9085,"time":"2016-11-10T13:00:00.000000000Z","bid":{"o":"1.34713","h":"1.34932","l":"1.34631","c":"1.34679"},"ask":{"o":"1.34737","h":"1.34957","l":"1.34655","c":"1.34702"}},{"complete":true,"volume":6480,"time":"2016-11-10T14:00:00.000000000Z","bid":{"o":"1.34682","h":"1.35068","l":"1.34622","c":"1.35040"},"ask":{"o":"1.34707","h":"1.35089","l":"1.34640","c":"1.35059"}},{"complete":true,"volume":11415,"time":"2016-11-10T15:00:00.000000000Z","bid":{"o":"1.35041","h":"1.35082","l":"1.34632","c":"1.34951"},"ask":{"o":"1.35062","h":"1.35104","l":"1.34652","c":"1.34973"}},{"complete":true,"volume":7522,"time":"2016-11-10T16:00:00.000000000Z","bid":{"o":"1.34951","h":"1.34952","l":"1.34666","c":"1.34738"},"ask":{"o":"1.34975","h":"1.34975","l":"1.34686","c":"1.34756"}},{"complete":true,"volume":3827,"time":"2016-11-10T17:00:00.000000000Z","bid":{"o":"1.34736","h":"1.34742","l":"1.34394","c":"1.34475"},"ask":{"o":"1.34754","h":"1.34763","l":"1.34415","c":"1.34498"}},{"complete":true,"volume":3538,"time":"2016-11-10T18:00:00.000000000Z","bid":{"o":"1.34473","h":"1.34619","l":"1.34314","c":"1.34329"},"ask":{"o":"1.34496","h":"1.34639","l":"1.34340","c":"1.34353"}},{"complete":true,"volume":4614,"time":"2016-11-10T19:00:00.000000000Z","bid":{"o":"1.34330","h":"1.34682","l":"1.34290","c":"1.34669"},"ask":{"o":"1.34355","h":"1.34706","l":"1.34311","c":"1.34689"}},{"complete":true,"volume":3682,"time":"2016-11-10T20:00:00.000000000Z","bid":{"o":"1.34672","h":"1.34890","l":"1.34623","c":"1.34812"},"ask":{"o":"1.34695","h":"1.34914","l":"1.34647","c":"1.34832"}},{"complete":true,"volume":1543,"time":"2016-11-10T21:00:00.000000000Z","bid":{"o":"1.34806","h":"1.34826","l":"1.34671","c":"1.34679"},"ask":{"o":"1.34830","h":"1.34850","l":"1.34708","c":"1.34717"}},{"complete":true,"volume":593,"time":"2016-11-10T22:00:00.000000000Z","bid":{"o":"1.34672","h":"1.34733","l":"1.34603","c":"1.34676"},"ask":{"o":"1.34749","h":"1.34825","l":"1.34652","c":"1.34730"}},{"complete":true,"volume":1454,"time":"2016-11-10T23:00:00.000000000Z","bid":{"o":"1.34675","h":"1.34723","l":"1.34614","c":"1.34708"},"ask":{"o":"1.34732","h":"1.34788","l":"1.34649","c":"1.34743"}},{"complete":true,"volume":2184,"time":"2016-11-11T00:00:00.000000000Z","bid":{"o":"1.34715","h":"1.34979","l":"1.34715","c":"1.34936"},"ask":{"o":"1.34748","h":"1.35003","l":"1.34743","c":"1.34963"}},{"complete":true,"volume":2654,"time":"2016-11-11T01:00:00.000000000Z","bid":{"o":"1.34938","h":"1.34983","l":"1.34871","c":"1.34955"},"ask":{"o":"1.34964","h":"1.35007","l":"1.34899","c":"1.34979"}},{"complete":true,"volume":3279,"time":"2016-11-11T02:00:00.000000000Z","bid":{"o":"1.34960","h":"1.35120","l":"1.34759","c":"1.34883"},"ask":{"o":"1.34984","h":"1.35148","l":"1.34787","c":"1.34909"}},{"complete":true,"volume":2489,"time":"2016-11-11T03:00:00.000000000Z","bid":{"o":"1.34884","h":"1.34891","l":"1.34726","c":"1.34780"},"ask":{"o":"1.34907","h":"1.34919","l":"1.34757","c":"1.34808"}},{"complete":true,"volume":2458,"time":"2016-11-11T04:00:00.000000000Z","bid":{"o":"1.34779","h":"1.34782","l":"1.34547","c":"1.34608"},"ask":{"o":"1.34809","h":"1.34812","l":"1.34581","c":"1.34635"}},{"complete":true,"volume":1707,"time":"2016-11-11T05:00:00.000000000Z","bid":{"o":"1.34610","h":"1.34683","l":"1.34563","c":"1.34575"},"ask":{"o":"1.34637","h":"1.34712","l":"1.34594","c":"1.34609"}},{"complete":true,"volume":2717,"time":"2016-11-11T06:00:00.000000000Z","bid":{"o":"1.34574","h":"1.34662","l":"1.34551","c":"1.34578"},"ask":{"o":"1.34605","h":"1.34692","l":"1.34579","c":"1.34619"}},{"complete":true,"volume":4806,"time":"2016-11-11T07:00:00.000000000Z","bid":{"o":"1.34578","h":"1.35023","l":"1.34578","c":"1.34956"},"ask":{"o":"1.34617","h":"1.35049","l":"1.34617","c":"1.34982"}},{"complete":true,"volume":7921,"time":"2016-11-11T08:00:00.000000000Z","bid":{"o":"1.34956","h":"1.35095","l":"1.34858","c":"1.35055"},"ask":{"o":"1.34984","h":"1.35119","l":"1.34883","c":"1.35082"}},{"complete":true,"volume":7458,"time":"2016-11-11T09:00:00.000000000Z","bid":{"o":"1.35054","h":"1.35088","l":"1.34826","c":"1.34890"},"ask":{"o":"1.35080","h":"1.35113","l":"1.34853","c":"1.34916"}},{"complete":true,"volume":8373,"time":"2016-11-11T10:00:00.000000000Z","bid":{"o":"1.34890","h":"1.35156","l":"1.34879","c":"1.35093"},"ask":{"o":"1.34914","h":"1.35194","l":"1.34906","c":"1.35116"}},{"complete":true,"volume":6512,"time":"2016-11-11T11:00:00.000000000Z","bid":{"o":"1.35088","h":"1.35406","l":"1.35036","c":"1.35347"},"ask":{"o":"1.35112","h":"1.35433","l":"1.35061","c":"1.35371"}},{"complete":true,"volume":6392,"time":"2016-11-11T12:00:00.000000000Z","bid":{"o":"1.35348","h":"1.35365","l":"1.34922","c":"1.34961"},"ask":{"o":"1.35371","h":"1.35390","l":"1.34946","c":"1.34983"}},{"complete":true,"volume":9249,"time":"2016-11-11T13:00:00.000000000Z","bid":{"o":"1.34960","h":"1.35160","l":"1.34875","c":"1.35096"},"ask":{"o":"1.34984","h":"1.35182","l":"1.34899","c":"1.35118"}},{"complete":true,"volume":5885,"time":"2016-11-11T14:00:00.000000000Z","bid":{"o":"1.35098","h":"1.35373","l":"1.35044","c":"1.35311"},"ask":{"o":"1.35116","h":"1.35392","l":"1.35065","c":"1.35350"}},{"complete":true,"volume":10317,"time":"2016-11-11T15:00:00.000000000Z","bid":{"o":"1.35312","h":"1.35462","l":"1.35164","c":"1.35231"},"ask":{"o":"1.35350","h":"1.35485","l":"1.35185","c":"1.35252"}},{"complete":true,"volume":8972,"time":"2016-11-11T16:00:00.000000000Z","bid":{"o":"1.35229","h":"1.35445","l":"1.35110","c":"1.35115"},"ask":{"o":"1.35249","h":"1.35467","l":"1.35134","c":"1.35136"}},{"complete":true,"volume":5311,"time":"2016-11-11T17:00:00.000000000Z","bid":{"o":"1.35111","h":"1.35188","l":"1.34910","c":"1.34998"},"ask":{"o":"1.35132","h":"1.35211","l":"1.34934","c":"1.35021"}},{"complete":true,"volume":5178,"time":"2016-11-11T18:00:00.000000000Z","bid":{"o":"1.34998","h":"1.35277","l":"1.34994","c":"1.35213"},"ask":{"o":"1.35019","h":"1.35304","l":"1.35015","c":"1.35233"}},{"complete":true,"volume":4471,"time":"2016-11-11T19:00:00.000000000Z","bid":{"o":"1.35216","h":"1.35470","l":"1.35216","c":"1.35455"},"ask":{"o":"1.35239","h":"1.35498","l":"1.35239","c":"1.35475"}},{"complete":true,"volume":1877,"time":"2016-11-11T20:00:00.000000000Z","bid":{"o":"1.35455","h":"1.35455","l":"1.35382","c":"1.35445"},"ask":{"o":"1.35476","h":"1.35476","l":"1.35403","c":"1.35465"}},{"complete":true,"volume":1249,"time":"2016-11-11T21:00:00.000000000Z","bid":{"o":"1.35448","h":"1.35465","l":"1.35214","c":"1.35347"},"ask":{"o":"1.35471","h":"1.35490","l":"1.35264","c":"1.35447"}},{"complete":true,"volume":840,"time":"2016-11-13T22:00:00.000000000Z","bid":{"o":"1.34999","h":"1.35208","l":"1.34983","c":"1.35114"},"ask":{"o":"1.35045","h":"1.35308","l":"1.35045","c":"1.35194"}},{"complete":true,"volume":2593,"time":"2016-11-13T23:00:00.000000000Z","bid":{"o":"1.35127","h":"1.35262","l":"1.35127","c":"1.35250"},"ask":{"o":"1.35204","h":"1.35299","l":"1.35184","c":"1.35284"}},{"complete":true,"volume":3219,"time":"2016-11-14T00:00:00.000000000Z","bid":{"o":"1.35256","h":"1.35521","l":"1.35244","c":"1.35483"},"ask":{"o":"1.35290","h":"1.35542","l":"1.35276","c":"1.35508"}},{"complete":true,"volume":3807,"time":"2016-11-14T01:00:00.000000000Z","bid":{"o":"1.35482","h":"1.35624","l":"1.35474","c":"1.35584"},"ask":{"o":"1.35508","h":"1.35647","l":"1.35499","c":"1.35614"}},{"complete":true,"volume":2729,"time":"2016-11-14T02:00:00.000000000Z","bid":{"o":"1.35584","h":"1.35619","l":"1.35509","c":"1.35519"},"ask":{"o":"1.35615","h":"1.35643","l":"1.35534","c":"1.35546"}},{"complete":true,"volume":1949,"time":"2016-11-14T03:00:00.000000000Z","bid":{"o":"1.35524","h":"1.35566","l":"1.35464","c":"1.35496"},"ask":{"o":"1.35546","h":"1.35594","l":"1.35487","c":"1.35519"}},{"complete":true,"volume":1329,"time":"2016-11-14T04:00:00.000000000Z","bid":{"o":"1.35493","h":"1.35567","l":"1.35452","c":"1.35555"},"ask":{"o":"1.35517","h":"1.35589","l":"1.35480","c":"1.35580"}},{"complete":true,"volume":1812,"time":"2016-11-14T05:00:00.000000000Z","bid":{"o":"1.35553","h":"1.35615","l":"1.35536","c":"1.35580"},"ask":{"o":"1.35573","h":"1.35639","l":"1.35559","c":"1.35608"}},{"complete":true,"volume":1951,"time":"2016-11-14T06:00:00.000000000Z","bid":{"o":"1.35586","h":"1.35634","l":"1.35526","c":"1.35630"},"ask":{"o":"1.35611","h":"1.35659","l":"1.35552","c":"1.35653"}},{"complete":true,"volume":4516,"time":"2016-11-14T07:00:00.000000000Z","bid":{"o":"1.35630","h":"1.35662","l":"1.35511","c":"1.35607"},"ask":{"o":"1.35655","h":"1.35688","l":"1.35540","c":"1.35631"}},{"complete":true,"volume":4713,"time":"2016-11-14T08:00:00.000000000Z","bid":{"o":"1.35605","h":"1.35829","l":"1.35565","c":"1.35793"},"ask":{"o":"1.35632","h":"1.35853","l":"1.35589","c":"1.35816"}},{"complete":true,"volume":3328,"time":"2016-11-14T09:00:00.000000000Z","bid":{"o":"1.35793","h":"1.35837","l":"1.35681","c":"1.35720"},"ask":{"o":"1.35815","h":"1.35860","l":"1.35702","c":"1.35744"}},{"complete":true,"volume":2895,"time":"2016-11-14T10:00:00.000000000Z","bid":{"o":"1.35722","h":"1.35876","l":"1.35627","c":"1.35757"},"ask":{"o":"1.35744","h":"1.35901","l":"1.35650","c":"1.35780"}},{"complete":true,"volume":4931,"time":"2016-11-14T11:00:00.000000000Z","bid":{"o":"1.35755","h":"1.35844","l":"1.35569","c":"1.35625"},"ask":{"o":"1.35777","h":"1.35867","l":"1.35592","c":"1.35649"}},{"complete":true,"volume":4513,"time":"2016-11-14T12:00:00.000000000Z","bid":{"o":"1.35622","h":"1.35631","l":"1.35392","c":"1.35414"},"ask":{"o":"1.35643","h":"1.35656","l":"1.35416","c":"1.35436"}},{"complete":true,"volume":6179,"time":"2016-11-14T13:00:00.000000000Z","bid":{"o":"1.35418","h":"1.35585","l":"1.35194","c":"1.35487"},"ask":{"o":"1.35441","h":"1.35606","l":"1.35216","c":"1.35504"}},{"complete":true,"volume":7242,"time":"2016-11-14T14:00:00.000000000Z","bid":{"o":"1.35483","h":"1.35650","l":"1.35383","c":"1.35629"},"ask":{"o":"1.35501","h":"1.35671","l":"1.35401","c":"1.35653"}},{"complete":true,"volume":6016,"time":"2016-11-14T15:00:00.000000000Z","bid":{"o":"1.35630","h":"1.35815","l":"1.35569","c":"1.35771"},"ask":{"o":"1.35652","h":"1.35836","l":"1.35591","c":"1.35790"}},{"complete":true,"volume":4222,"time":"2016-11-14T16:00:00.000000000Z","bid":{"o":"1.35766","h":"1.35870","l":"1.35641","c":"1.35806"},"ask":{"o":"1.35786","h":"1.35892","l":"1.35661","c":"1.35826"}},{"complete":true,"volume":2084,"time":"2016-11-14T17:00:00.000000000Z","bid":{"o":"1.35802","h":"1.35857","l":"1.35631","c":"1.35700"},"ask":{"o":"1.35821","h":"1.35878","l":"1.35652","c":"1.35722"}},{"complete":true,"volume":3519,"time":"2016-11-14T18:00:00.000000000Z","bid":{"o":"1.35701","h":"1.35756","l":"1.35519","c":"1.35605"},"ask":{"o":"1.35721","h":"1.35778","l":"1.35541","c":"1.35624"}},{"complete":true,"volume":2423,"time":"2016-11-14T19:00:00.000000000Z","bid":{"o":"1.35604","h":"1.35682","l":"1.35534","c":"1.35597"},"ask":{"o":"1.35624","h":"1.35704","l":"1.35555","c":"1.35617"}},{"complete":true,"volume":1823,"time":"2016-11-14T20:00:00.000000000Z","bid":{"o":"1.35599","h":"1.35661","l":"1.35474","c":"1.35502"},"ask":{"o":"1.35619","h":"1.35682","l":"1.35496","c":"1.35524"}},{"complete":true,"volume":1260,"time":"2016-11-14T21:00:00.000000000Z","bid":{"o":"1.35502","h":"1.35606","l":"1.35476","c":"1.35598"},"ask":{"o":"1.35525","h":"1.35639","l":"1.35498","c":"1.35630"}},{"complete":true,"volume":871,"time":"2016-11-14T22:00:00.000000000Z","bid":{"o":"1.35523","h":"1.35550","l":"1.35433","c":"1.35454"},"ask":{"o":"1.35623","h":"1.35650","l":"1.35486","c":"1.35486"}},{"complete":true,"volume":1216,"time":"2016-11-14T23:00:00.000000000Z","bid":{"o":"1.35455","h":"1.35471","l":"1.35355","c":"1.35373"},"ask":{"o":"1.35490","h":"1.35514","l":"1.35383","c":"1.35398"}},{"complete":true,"volume":1842,"time":"2016-11-15T00:00:00.000000000Z","bid":{"o":"1.35370","h":"1.35387","l":"1.35285","c":"1.35336"},"ask":{"o":"1.35396","h":"1.35417","l":"1.35311","c":"1.35365"}},{"complete":true,"volume":1535,"time":"2016-11-15T01:00:00.000000000Z","bid":{"o":"1.35333","h":"1.35366","l":"1.35280","c":"1.35359"},"ask":{"o":"1.35363","h":"1.35392","l":"1.35302","c":"1.35387"}},{"complete":true,"volume":2305,"time":"2016-11-15T02:00:00.000000000Z","bid":{"o":"1.35357","h":"1.35520","l":"1.35328","c":"1.35431"},"ask":{"o":"1.35384","h":"1.35547","l":"1.35355","c":"1.35455"}},{"complete":true,"volume":1795,"time":"2016-11-15T03:00:00.000000000Z","bid":{"o":"1.35432","h":"1.35452","l":"1.35313","c":"1.35364"},"ask":{"o":"1.35455","h":"1.35478","l":"1.35342","c":"1.35392"}},{"complete":true,"volume":1504,"time":"2016-11-15T04:00:00.000000000Z","bid":{"o":"1.35365","h":"1.35420","l":"1.35308","c":"1.35405"},"ask":{"o":"1.35392","h":"1.35446","l":"1.35335","c":"1.35430"}},{"complete":true,"volume":1435,"time":"2016-11-15T05:00:00.000000000Z","bid":{"o":"1.35405","h":"1.35479","l":"1.35321","c":"1.35465"},"ask":{"o":"1.35432","h":"1.35504","l":"1.35347","c":"1.35492"}},{"complete":true,"volume":1813,"time":"2016-11-15T06:00:00.000000000Z","bid":{"o":"1.35465","h":"1.35466","l":"1.35308","c":"1.35315"},"ask":{"o":"1.35493","h":"1.35493","l":"1.35337","c":"1.35355"}},{"complete":true,"volume":4164,"time":"2016-11-15T07:00:00.000000000Z","bid":{"o":"1.35315","h":"1.35364","l":"1.35094","c":"1.35124"},"ask":{"o":"1.35354","h":"1.35389","l":"1.35119","c":"1.35147"}},{"complete":true,"volume":4225,"time":"2016-11-15T08:00:00.000000000Z","bid":{"o":"1.35125","h":"1.35203","l":"1.34878","c":"1.35086"},"ask":{"o":"1.35147","h":"1.35227","l":"1.34905","c":"1.35108"}},{"complete":true,"volume":3641,"time":"2016-11-15T09:00:00.000000000Z","bid":{"o":"1.35086","h":"1.35347","l":"1.35074","c":"1.35257"},"ask":{"o":"1.35110","h":"1.35370","l":"1.35097","c":"1.35280"}},{"complete":true,"volume":2390,"time":"2016-11-15T10:00:00.000000000Z","bid":{"o":"1.35252","h":"1.35328","l":"1.35199","c":"1.35297"},"ask":{"o":"1.35277","h":"1.35351","l":"1.35221","c":"1.35318"}},{"complete":true,"volume":2913,"time":"2016-11-15T11:00:00.000000000Z","bid":{"o":"1.35295","h":"1.35412","l":"1.35066","c":"1.35146"},"ask":{"o":"1.35319","h":"1.35435","l":"1.35091","c":"1.35165"}}]}
//
//                                        List<CandleData> candleData = new ArrayList<>();
//                                        for (JsonNode candle : res) {
//
//
//                                            out.println("test candle--> " + candle);
//
//
//                                            time = 0;
//
//                                            out.println("time " + time);
//
//
//                                            logger.info(
//                                                    candle.get("o").asDouble() + " " + candle.get("c").asDouble() + " " + candle.get("high").asDouble() + " " + candle.get("low").asDouble() + " " + candle.get("volume").asDouble()
//                                            );
//
//
//                                            if (candle.has("candles")) {
//
//
//                                                JsonNode candles = candle.get("candles");
//                                                for (JsonNode cand : candles) {
//
//                                                    String tim;
//                                                    if (cand.has("time")) {
//                                                        tim = cand.get("time").asText();
//                                                        logger.info(tim);
//                                                        //   time += Long.parseLong(tim);
//
//
//                                                        time +=
//                                                                Long.parseLong(cand.get("time").asText());
//
//                                                    }
//
//
//                                                    if (cand.has("bid")) {
//                                                        JsonNode bid = cand.get("bid");
//                                                        out.println("test bid--> " + bid);
//                                                        out.println("time " + time);
//                                                        for (JsonNode b : bid) {
//                                                            out.println("test bid--> " + b);
//                                                            out.println("time " + time);
//                                                            candleData.add(new CandleData
//                                                                    (
//                                                                            b.get("o").asDouble(),  // open price
//                                                                            b.get("c").asDouble(),  // close price
//                                                                            b.get("h").asDouble(),  // high price
//                                                                            b.get("l").asDouble(),  // low price
//                                                                            time,  // date
//                                                                            0   // volume
//
//                                                                    ));
//                                                            out.println("time " + time + " " + b);
//                                                        }
//
//
//                                                    }
//
//                                                }
//                                            }
//
//                                        }
//                                        candleData.sort(Comparator.comparingInt(CandleData::getOpenTime));
//
//
//                                        return candleData;
//                                    } else {
//                                        return Collections.emptyList();
//                                    }
//
//
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                return Collections.emptyList();
//                            }
//
//
//                            return null;
//                        });
//
//
//            }
//
//            public abstract CompletableFuture<Optional<?>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);
//
//            public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);
//        }
//
//    }
//
//    public static class OandaException extends Throwable {
//        @Serial
//        private static final long serialVersionUID = 1L;
//
//        public OandaException() {
//            super();
//        }
//
//        public OandaException(String message) {
//            super(message);
//        }
//
//
//    }
//
//    public record OandaTransaction() {
//        private static String transactionId = "12";
//
//        public static String getTransactionId() {
//            return transactionId;
//        }
//
//        public void setTransactionId(String toString) {
//            transactionId = toString;
//        }
//    }
//
//    public static class OandaOrder extends Order {
//        private String id;
//        private String status;
//        private String currency;
//        private String amount;
//        private String amountInCents;
//        private String date;
//
//        public OandaOrder() {
//            super(
//                    "", TRADE_ORDER_TYPE.LIMIT_ORDER, 1, 0, 0.01, 0
//            );
//
//        }
//
//
//        @Override
//        public String toString() {
//            return "OandaOrder{" +
//                    "id='" + id + '\'' +
//                    ", status='" + status + '\'' +
//                    ", currency='" + currency + '\'' +
//                    ", amount='" + amount + '\'' +
//                    ", amountInCents='" + amountInCents + '\'' +
//                    ", date='" + date + '\'' +
//                    ", timestamp='" + timestamp + '\'' +
//                    ", id=" + id +
//                    ", order_type=" + order_type +
//                    ", lotSize=" + lotSize +
//                    ", price=" + price +
//                    ", total=" + total +
//                    ", remaining=" + remaining +
//                    ", fee=" + fee +
//                    ", currency='" + currency + '\'' +
//                    ", created=" + created +
//                    ", stopLoss=" + stopLoss +
//                    ", takeProfit=" + takeProfit +
//                    ", updated=" + updated +
//                    ", closed=" + closed +
//                    ", status='" + status + '\'' +
//                    ", symbol='" + symbol + '\'' +
//                    ", type='" + type + '\'' +
//                    '}';
//        }
//
//        public String getCurrency() {
//            return currency;
//        }
//
//        public void setCurrency(String currency) {
//            this.currency = currency;
//        }
//
//        public String getAmount() {
//            return amount;
//        }
//
//        public void setAmount(String amount) {
//            this.amount = amount;
//        }
//
//        public String getAmountInCents() {
//            return amountInCents;
//        }
//
//        public void setAmountInCents(String amountInCents) {
//            this.amountInCents = amountInCents;
//        }
//
//        public String getDate() {
//            return date;
//        }
//
//        public void setDate(String date) {
//            this.date = date;
//        }
//
//        public String getId() {
//            return id;
//        }
//
//        public void setId(String id) {
//            this.id = id;
//            if (id != null) {
//                this.status = "open";
//                this.currency = "USD";
//                this.amount = "0.00";
//                this.amountInCents = "0.00";
//                this.date = new Date().toString();
//            }
//        }
//
//        public String getStatus() {
//            return status;
//        }
//
//        public void setStatus(String status) {
//            this.status = status;
//        }
//
//        public void setPrice(Object o) {
//
//        }
//
//        public void setSide(Object o) {
//
//        }
//
//        public void setUnits(Object o) {
//        }
//
//        public boolean getOrderId() {
//            return true;
//        }
//
//        public void setOrderId(Object o) {
//            this.id = o.toString();
//        }
//    }
    }
}
