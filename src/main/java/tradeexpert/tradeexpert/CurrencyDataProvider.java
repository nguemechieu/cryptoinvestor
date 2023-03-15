package tradeexpert.tradeexpert;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.lang.System.out;
import static tradeexpert.tradeexpert.Currency.NULL_FIAT_CURRENCY;

public class CurrencyDataProvider {

    private static final ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> CURRENCIES = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(CurrencyDataProvider.class.getSimpleName());

    @Contract(" -> new")
    public static @NotNull List<Currency> getInstance() {
        ConcurrentHashMap<SymmetricPair<String, CurrencyType>, Currency> currencies = CURRENCIES;

        if (currencies.isEmpty()) {
            try {
                registerCurrencies();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return new ArrayList<>(currencies.values());

    }

    protected static void registerCurrencies() throws IOException, InterruptedException, ParseException {

        List<Currency> coinsToRegister = new ArrayList<>();

        HttpRequest.Builder request = HttpRequest.newBuilder();
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        request.uri(URI.create("https://api.exchange.coinbase.com/currencies")).GET().build();
        request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0)");
        request.uri(URI.create("https://api.exchange.coinbase.com/currencies"));


        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());

        String id = "";
        String name = "";
        String status = "";
        String symbol = "";
        String message = "";
        int max_precision = 0;
        String type = "";
        out.println(response.body());
        ObjectMapper mapper = new ObjectMapper();

        JsonNode json = mapper.readTree(response.body());

        for (JsonNode i : json) {
            String crypto_address_link = null;

            if (i.has("name")) {
                name = i.get("name").asText();
            }
            if (i.has("status")) {
                status = i.get("status").asText();
            }
            if (i.has("message")) {
                message = i.get("message").asText();

            }
            if (i.has("id")) {
                symbol = i.get("id").asText();
            }
            if (
                    i.has("max_precision")
            ) {
                max_precision = i.get("max_precision").asInt();
            }

            if (i.has("details")) {
                crypto_address_link = i.get("details").get("crypto_address_link").asText();
                type = i.get("details").get("type").asText();
                out.println("crypto_address_link " + crypto_address_link);
                out.println("type " + type);
                out.println("name " + name);

            }


            out.println("currency id" + id);

            out.println("name " + name);
            out.println("status " + status);
            out.println("message " + message);
            out.println("symbol " + symbol);
            out.println("max_precision " + max_precision);
            out.println("type " + type);
            CurrencyType type0 = CurrencyType.NULL;
            String finalName = name;
            int finalMax_precision = max_precision;
            String finalCrypto_address_link = crypto_address_link;
            if (type.equals("crypto")) {
                out.println("crypto_address_link " + crypto_address_link);
                out.println("finalCrypto_address_link " + finalCrypto_address_link);
                out.println("finalName " + finalName);
                out.println("finalMax_precision " + finalMax_precision);
                out.println("finalSymbol " + symbol);
                type0 = CurrencyType.CRYPTO;
            }

            coinsToRegister.add(new Currency(type0, name, symbol, id, max_precision, symbol) {
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
                CURRENCIES.put(SymmetricPair.of(fiatCurrency.getCurrencyCode(), CurrencyType.FIAT), new FiatCurrency(fiatCurrency.getCurrencyCode(), fiatCurrency.getDisplayName(),
                        fiatCurrency.getCurrencyCode(), fractional, fiatCurrency.getSymbol(), Locale.of(fiatCurrency.getSymbol()), "", fractional) {
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
}
 

