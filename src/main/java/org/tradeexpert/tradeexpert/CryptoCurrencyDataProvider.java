package org.tradeexpert.tradeexpert;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;


public class CryptoCurrencyDataProvider extends CurrencyDataProvider {
    public CryptoCurrencyDataProvider() {
        super();
        registerCurrencies();

    }
    List<Currency> coinsToRegister = new ArrayList<>();

    @Override
    protected void registerCurrencies() {


        HttpRequest.Builder request = HttpRequest.newBuilder();
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        request.uri(URI.create("https://api.exchange.coinbase.com/currencies"));

        HttpClient.newHttpClient().sendAsync(
                        request.build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    String id = "";
                    String name = "";
                    String status = "";
                    String symbol = "";
                    String message = "";
                    int max_precision = 0;
                    String type = "";

                    JSONObject json = new JSONObject(response);
                    for (int i = 0; i < json.length(); i++) {
                        String crypto_address_link = null;
                        if (json.has("id")) {
                            id = json.getString("id");
                        }

                        if (json.has("name")) {
                            name = json.getString("name");
                        }
                        if (json.has("status")) {
                            status = json.getString("status");
                        }
                        if (json.has("message")) {
                            message = json.getString("message");

                        }
                        if (json.has("symbol")) {
                            symbol = json.getString("symbol");
                        }
                        if (
                                json.has("max_precision")
                        ) {
                            max_precision = json.getInt("max_precision");
                        }

                        if (json.has("details")) {
                            crypto_address_link = json.getJSONObject("details").getString("crypto_address_link");

                        }
                        if (json.has("type")) {
                             type = json.getString("type");
                        }


                        out.println("currency id" + id);

                        out.println("name " + name);
                        out.println("status " + status);
                        out.println("message " + message);
                        out.println("symbol " + symbol);
                        out.println("max_precision " + max_precision);
                        out.println("type " + type);

                        coinsToRegister.add(i, new CryptoCurrency(name, id, symbol, max_precision, crypto_address_link) {
                            @Override
                            public int compareTo(@NotNull java.util.Currency o) {
                                return 0;
                            }
                        });

                        //        String fullDisplayName, String shortDisplayName, String code, int fractionalDigits,
                        //      String symbol

                        Currency.registerCurrencies(coinsToRegister);
                        JsonToCsv convert = new JsonToCsv();
                        try {
                            convert.convertJsonToCsv("currency.json",coinsToRegister);
                        } catch (IOException | ParseException e) {
                            throw new RuntimeException(e);
                        }
                        out.println("currency id " + id);
                        out.println("Coin to register " + coinsToRegister);

                    }


                    out.println("Cannot register currencies");

                            return coinsToRegister;
                        }
                );



    }
}

