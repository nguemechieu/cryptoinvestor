package cryptoinvestor.cryptoinvestor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import static java.lang.System.out;
import static cryptoinvestor.cryptoinvestor.Currency.NULL_FIAT_CURRENCY;

public class CurrencyDataProvider {

private static final Logger logger = LoggerFactory.getLogger(CurrencyDataProvider.class);
    private static final ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> CURRENCIES = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<CryptoMarketData, CryptoMarketData> getMarketDataConcurrentHashMap() {

        return MARKET_DATA_CONCURRENT_HASH_MAP;
    }

    private static final ConcurrentHashMap<CryptoMarketData,CryptoMarketData> MARKET_DATA_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    private static Roi roi;

    @Contract(" -> new")
    public static @NotNull List<Currency> getInstance() {
        ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> currencies = CURRENCIES;

        if (currencies.isEmpty()) {
            try {
                registerCurrencies();
            } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<>(currencies.values());

    }

    protected static void registerCurrencies() throws IOException, InterruptedException, ParseException, URISyntaxException {

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

        if (response.statusCode() !=200) {
            logger.error("Error while getting currencies");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error while getting currencies\n"+ response.body());
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
            String last_updated = node.get("last_updated").asText();    times = "times";
            currency = "";
            percentage = "";
MARKET_DATA_CONCURRENT_HASH_MAP.put(new CryptoMarketData(id, symbol, name, image, current_price,
        market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h,
        price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h,
        circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date,
        currency, times, percentage, last_updated),new CryptoMarketData(id, symbol, name, image, current_price,
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
logger.info("CRYPTO MARKET DATA "+MARKET_DATA_CONCURRENT_HASH_MAP);



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
                circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date,
                roi));
    }






        for (CryptoMarketData data : marketData) {


            coinsToRegister.add(new Currency(CurrencyType.CRYPTO, data.name,data.id ,data.symbol, data.roi.percentage.length(), data.symbol,data.image) {
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
            MARKET_DATA_CONCURRENT_HASH_MAP.put(data,data);
        }


        for (Currency c : coinsToRegister) {
            CURRENCIES.put(new SymmetricPair<>(c.getSymbol(), c.getCurrencyType()), c);
            if (c.getCurrencyType() == CurrencyType.CRYPTO) {
                CURRENCIES.put(new SymmetricPair<>(c.getSymbol(), c.getCurrencyType()), c);
            }
            if (c.getCurrencyType() == CurrencyType.FIAT) {
                CURRENCIES.put(new SymmetricPair<>(c.getSymbol(), c.getCurrencyType()), c);
            } else if (c.getCurrencyType() == CurrencyType.NULL) {
                CURRENCIES.put(new SymmetricPair<>(c.getSymbol(), c.getCurrencyType()), c);
            }
        }
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

                    File relativeTo=new File("img/symbol.png");

                    img= null;
                }


                CURRENCIES.put(SymmetricPair.of(fiatCurrency.getCurrencyCode(), CurrencyType.FIAT), new FiatCurrency(fiatCurrency.getCurrencyCode(), fiatCurrency.getDisplayName(),
                        fiatCurrency.getCurrencyCode(), fractional, fiatCurrency.getSymbol(), Locale.of(fiatCurrency.getSymbol()), "", fractional,img) {
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
}
 

