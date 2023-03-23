package cryptoinvestor.cryptoinvestor;

import javafx.scene.control.Alert;
import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import static java.lang.System.out;


public class TradePair extends Pair<Currency, Currency> {

    static final Logger logger = LoggerFactory.getLogger(TradePair.class);

    static {
        try {
            logger.debug("CurrencyDataProvider loaded");
            CurrencyDataProvider.registerCurrencies();
        } catch (IOException | InterruptedException | ParseException | URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    private final Currency baseCurrency;
    private final Currency counterCurrency;

    public TradePair(Currency baseCurrency, Currency counterCurrency) {
        super(baseCurrency, counterCurrency);
        this.baseCurrency = baseCurrency;
        this.counterCurrency = counterCurrency;
        logger.debug("TradePair created: {}", this);
    }

    public TradePair(String baseCurrency, String counterCurrency) {
        super(CurrencyDataProvider.of(baseCurrency), CurrencyDataProvider.of(counterCurrency));

        this.baseCurrency = CurrencyDataProvider.of(baseCurrency);
        this.counterCurrency = CurrencyDataProvider.of(counterCurrency);
        logger.debug("TradePair created: {}", this);
    }

    @Contract("_, _ -> new")
    public static @NotNull TradePair of(String baseCurrencyCode, String counterCurrencyCode) {
        return new TradePair(baseCurrencyCode, counterCurrencyCode);
    }

    @Contract("_, _ -> new")
    public static @NotNull TradePair of(@NotNull Currency baseCurrency, Currency counterCurrency) {

        if (baseCurrency.code.isEmpty() || counterCurrency.code.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "TradePair code not found : %s %s",
                            baseCurrency,
                            counterCurrency));
        }
        return new TradePair(baseCurrency, counterCurrency);
    }

    @Contract("_ -> new")
    public static @NotNull TradePair of(@NotNull Pair<Currency, Currency> currencyPair) {
        return new TradePair(currencyPair.getKey(), currencyPair.getValue());
    }

    public static <T extends Currency, V extends Currency> @NotNull TradePair parse(
            String tradePair, @NotNull String separator, Pair<Class<T>, Class<V>> pairType)
            throws CurrencyNotFoundException {
        Objects.requireNonNull(tradePair, "tradePair must not be null");
        Objects.requireNonNull(pairType, "pairType must not be null");
        Objects.requireNonNull(pairType.getKey(), "first member of pairType must not be null");

        String[] split;

        if (separator.equals("")) {
            // We don't know where to split so try the most logical thing (after 3 characters). We could
            // extend this by checking that the substring is indeed a real currency and if not try 4
            // characters.
            split = new String[]{tradePair.substring(0, 3), tradePair.substring(3)};

        } else {
            split = tradePair.split(separator);
        }

        if (pairType.getKey().equals(FiatCurrency.class) && pairType.getValue().equals(FiatCurrency.class)) {
            // tradePair must be (fiat, something)
            if (CurrencyDataProvider.of(split[0]) == CurrencyDataProvider.of(split[1])) {
                throw
                        new CurrencyNotFoundException(CurrencyType.valueOf(split[0]), split[1]);
            }

            if (pairType.getValue() == null) {
                // The counter currency is not specified, so try both (fiat first)
                if (CurrencyDataProvider.of(split[1]) != Currency.NULL_FIAT_CURRENCY) {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                } else if (CurrencyDataProvider.of(split[1]) != Currency.NULL_FIAT_CURRENCY) {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                } else {
                    //
                    throw new CurrencyNotFoundException(CurrencyType.valueOf(split[1]), split[0]);
                    //TradePair.of(Currency.NULL_FIAT_CURRENCY, Currency.NULL_CRYPTO_CURRENCY);
                }
            } else if (pairType.getValue().equals(FiatCurrency.class)) {
                if (CurrencyDataProvider.of(split[1]) == Currency.NULL_FIAT_CURRENCY) {
                    throw new CurrencyNotFoundException(CurrencyType.FIAT, split[1]);
                } else {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                }
            } else if (pairType.getValue().equals(CryptoCurrency.class)) {
                if (CurrencyDataProvider.of(split[1]) == Currency.NULL_CRYPTO_CURRENCY) {
                    throw new CurrencyNotFoundException(CurrencyType.CRYPTO, split[1]);
                } else {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                }
            } else {
                logger.error("bad value for second member of pairType - must be one of CryptoCurrency.class, " +
                        "FiatCurrency.class, or null but was: " + pairType.getValue());
                throw new IllegalArgumentException("bad value for second member of pairType - must be one of " +
                        "CryptoCurrency.class, FiatCurrency.class, or null but was: " + pairType.getValue());
            }
        } else {
            // tradePair must be (crypto, something)
            if (CurrencyDataProvider.of(split[0]) == Currency.NULL_CRYPTO_CURRENCY) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(
                        "The base currency of the trade pair must be a crypto currency, but was: " + split[0]);
                alert.showAndWait();
                //throw new CurrencyNotFoundException(CurrencyType.CRYPTO, split[0]);
                return new TradePair(Currency.NULL_CRYPTO_CURRENCY, CurrencyDataProvider.of(split[1]));
            } else if (pairType.getValue() == null) {

                Log.error("bad value for second member of pairType - must be one of CryptoCurrency.class,");

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(
                        "The counter currency of the trade pair must be a fiat or crypto currency, but was: " + split[1]);
                alert.showAndWait();
                return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));

            } else if (pairType.getValue().equals(FiatCurrency.class)) {
                if (CurrencyDataProvider.of(split[1]) == Currency.NULL_FIAT_CURRENCY) {
                    throw new CurrencyNotFoundException(CurrencyType.FIAT, split[1]);
                } else {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                }
            } else if (pairType.getValue().equals(CryptoCurrency.class)) {
                if (CurrencyDataProvider.of(split[1]) == Currency.NULL_CRYPTO_CURRENCY) {
                    throw new CurrencyNotFoundException(CurrencyType.CRYPTO, split[1]);
                } else {
                    return new TradePair(CurrencyDataProvider.of(split[0]), CurrencyDataProvider.of(split[1]));
                }
            } else {
                logger.error("bad value for second member of pairType - must be one of CryptoCurrency.class, " +
                        "FiatCurrency.class, or null but was: " + pairType.getValue());
                throw new IllegalArgumentException("bad value for second member of pairType - must be one of " +
                        "CryptoCurrency.class, FiatCurrency.class, or null but was: " + pairType.getValue());
            }

        }
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getCounterCurrency() {
        return counterCurrency;
    }

    public String toString(@NotNull Character separator) {

        if (separator.equals('_')) {
            out.println(baseCurrency.code);
            out.println("_");
            out.println(counterCurrency.code);
            out.println("baseCurrency image " + baseCurrency.getImage());
            out.println("counterCurrency image " + counterCurrency.getImage());
            return baseCurrency.code + "_" + counterCurrency.code;
        } else if (separator.equals('-')) {
            out.println(baseCurrency.code);
            out.println("-");
            out.println(counterCurrency.code);
            return baseCurrency.code + "-" + counterCurrency.code;
        } else if (separator.equals('/')) {
            out.println(baseCurrency.code);
            out.println("/");
            out.println(counterCurrency.code);
            out.println("baseCurrency image " + baseCurrency.getImage());
            out.println("counterCurrency image " + counterCurrency.getImage());
            return baseCurrency.code + counterCurrency.code;
        } else {
            out.println(baseCurrency.code);

            out.println(counterCurrency.code);

            if (baseCurrency.code.equals(counterCurrency.code)) {
                throw new IllegalArgumentException("baseCurrency and counterCurrency must be different");
            } else if (baseCurrency.code.isEmpty() || counterCurrency.code.isEmpty()) {
                throw new IllegalArgumentException(
                        "Currency code must be non-empty, but was: " + baseCurrency.code + " and" +
                                counterCurrency.code
                );
            }
            out.println("baseCurrency image " + baseCurrency.getImage());
            out.println("counterCurrency image " + counterCurrency.getImage());
            return counterCurrency.code + separator + baseCurrency.code;
        }
    }


    public CryptoMarketData getMarketData() throws IOException, ParseException, InterruptedException, URISyntaxException {
        for (CryptoMarketData marketData : CurrencyDataProvider.getMarketDataConcurrentHashMap().values()) {
            if (marketData.name.equals(baseCurrency.code)) {
                logger.debug("baseCurrency found" + baseCurrency.code);
                return marketData;
            } else if (marketData.id.equals(counterCurrency.code)) {
                logger.debug("counterCurrency found " + counterCurrency.code);
                return marketData;
            } else if (marketData.name.equals(counterCurrency.code) || marketData.name.equals(baseCurrency.fullDisplayName)) {
                logger.debug("counterCurrency found " + counterCurrency.code);
                return marketData;
            } else {
                logger.debug("baseCurrency not found " + baseCurrency.code);
                return marketData;
            }
        }

        return null;


    }

    public String getImage() throws IOException, ParseException, InterruptedException {
        for (CryptoMarketData marketData : CurrencyDataProvider.getMarketDataConcurrentHashMap().values()) {
            if (!marketData.symbol.equals(baseCurrency.symbol)) {
                return marketData.getImage();
            } else if (marketData.id.equals(counterCurrency.symbol)) {
                return marketData.getImage();
            } else if (marketData.name.equals(counterCurrency.symbol) || marketData.name.equals(baseCurrency.fullDisplayName)) {
                return marketData.getImage();
            } else {
                out.println(baseCurrency.code);
                out.println(counterCurrency.code);
                out.println("Image not found");
                return marketData.image;
            }
        }
        out.println(baseCurrency.code);
        out.println(counterCurrency.code);
        out.println("Image not found");
        return null;


    }


    public Date getStartTime() {
        return new Date();
    }
}
