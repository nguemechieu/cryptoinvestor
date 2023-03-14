package org.tradeexpert.tradeexpert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.System.out;

public class CryptoCurrencyDataProvider extends CurrencyDataProvider {

    public CryptoCurrencyDataProvider() {}

    @Override
    protected void registerCurrencies() throws ExecutionException, InterruptedException, TimeoutException, IOException {

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

                            for (JsonNode i :json) {
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

                                }
                                if (i.has("type")) {
                                    type = i.get("type").asText();
                                }


                                out.println("currency id" + id);

                                out.println("name " + name);
                                out.println("status " + status);
                                out.println("message " + message);
                                out.println("symbol " + symbol);
                                out.println("max_precision " + max_precision);
                                out.println("type " + type);

                                String finalName = name;
                                int finalMax_precision = max_precision;
                                String finalCrypto_address_link = crypto_address_link;

                            coinsToRegister.add(new CryptoCurrency(finalName, id, symbol, finalMax_precision, finalCrypto_address_link,
                                        CryptoCurrencyAlgorithms.getAlgorithm("SHA256"),
                                        "https://api.exchange.coinbase.com/currencies/" + id,"https://www.coinbase.com",new Date().getTime(), 2016, 3000000) {
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

                                //        String fullDisplayName, String shortDisplayName, String code, int fractionalDigits,
                                //      String symbol



                                out.println("currency id " + id);
                                out.println("Coin to register " + coinsToRegister);

                            }









        Currency.registerCurrencies(coinsToRegister);
    }
}
