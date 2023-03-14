package org.tradeexpert.tradeexpert;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents some currency. Could be a fiat currency issued by a country or a crypto-currency.
 *
 * @author Michael Ennen
 */
public class Currency {
    private final CurrencyType currencyType;
    private final String fullDisplayName;
    private final String shortDisplayName;
    protected final String code;
    protected final int fractionalDigits;
    protected final String symbol;
    private static final Map<SymmetricPair<String, CurrencyType>, Currency> CURRENCIES = new ConcurrentHashMap<>();
    public static final CryptoCurrency NULL_CRYPTO_CURRENCY = new NullCryptoCurrency();
    public static final FiatCurrency NULL_FIAT_CURRENCY = new NullFiatCurrency();
    private static final Logger logger = LoggerFactory.getLogger(Currency.class);

    static {
        CryptoCurrencyDataProvider cryptoCurrencyDataProvider = new CryptoCurrencyDataProvider();
        try {
            cryptoCurrencyDataProvider.registerCurrencies();
        } catch (ExecutionException | InterruptedException | TimeoutException | IOException e) {
            throw new RuntimeException(e);
        }
        FiatCurrencyDataProvider fiatCurrencyDataProvider = new FiatCurrencyDataProvider();
        fiatCurrencyDataProvider.registerCurrencies();

    }

    /**
     * Private constructor used only for the {@code NULL_CURRENCY}.
     */
    protected Currency() {
        this.currencyType = CurrencyType.NULL;
        this.fullDisplayName = "";
        this.shortDisplayName = "";
        this.code = "XXX";
        this.fractionalDigits = 0;
        this.symbol = "";
    }

    /**
     * Protected constructor, called only by CurrencyDataProvider's.
     */
    protected Currency(CurrencyType currencyType, String fullDisplayName, String shortDisplayName, String code,
                       int fractionalDigits, String symbol) {
        Objects.requireNonNull(currencyType, "currencyType must not be null");
        Objects.requireNonNull(fullDisplayName, "fullDisplayName must not be null");
        Objects.requireNonNull(shortDisplayName, "shortDisplayName must not be null");
        Objects.requireNonNull(code, "code must not be null");

        if (fractionalDigits < 0) {
            throw new IllegalArgumentException("fractional digits must be non-negative, was: " + fractionalDigits);
        }
        Objects.requireNonNull(symbol, "symbol must not be null");

        this.currencyType = currencyType;
        this.fullDisplayName = fullDisplayName;
        this.shortDisplayName = shortDisplayName;
        this.code = code;
        this.fractionalDigits = fractionalDigits;
        this.symbol = symbol;
    }

    protected static void registerCurrency(Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");

        CURRENCIES.put(SymmetricPair.of(currency.code, currency.currencyType), currency);
    }

    protected static void registerCurrencies(Collection<Currency> currencies) {
        Objects.requireNonNull(currencies, "currencies must not be null");
        currencies.forEach(Currency::registerCurrency);
    }

    public static Currency of(String code) {
        Objects.requireNonNull(code, "code must not be null");
//        if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.FIAT))
//                && CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
//            logger.error("ambiguous currency code: " + code);
//
//            throw new IllegalArgumentException("ambiguous currency code: " + code + " (code" +
//                    " is used for multiple currency types); use ofCrypto(...) or ofFiat(...) instead");
//        } else {
            if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
                return CURRENCIES.get(SymmetricPair.of(code, CurrencyType.CRYPTO));
            } else {
                return CURRENCIES.getOrDefault(SymmetricPair.of(code, CurrencyType.FIAT), NULL_CRYPTO_CURRENCY);
            }
        }

    public static FiatCurrency ofFiat(@NotNull String code) {
        if (code.equals("¤¤¤")) {
            return NULL_FIAT_CURRENCY;
        }

        FiatCurrency result = (FiatCurrency) CURRENCIES.get(SymmetricPair.of(code, CurrencyType.FIAT));
        return result == null ? NULL_FIAT_CURRENCY : result;
    }
    public static CryptoCurrency ofCrypto(@NotNull String code) {
        if (code.equals("¤¤¤")) {
            return NULL_CRYPTO_CURRENCY;
        }

        CryptoCurrency result = (CryptoCurrency) CURRENCIES.get(SymmetricPair.of(code, CurrencyType.CRYPTO));
        return result == null ? NULL_CRYPTO_CURRENCY : result;
    }

    public static List<FiatCurrency> getFiatCurrencies() {
        return CURRENCIES.values().stream()
                .filter(currency -> currency.getCurrencyType() == CurrencyType.FIAT)
                .map(currency -> (FiatCurrency) currency).toList();
    }

    public static Currency lookupBySymbol(String symbol) {
        // FIXME: why fiat?
        return CURRENCIES.values().stream().filter(currency -> currency.getSymbol().equals(symbol))
                .findAny().orElse(NULL_FIAT_CURRENCY);
    }

    public static FiatCurrency lookupFiatByCode(String code) {
        return (FiatCurrency) CURRENCIES.values().stream()
                .filter(currency -> currency.currencyType == CurrencyType.FIAT && currency.code.equals(code))
                .findAny().orElse(NULL_FIAT_CURRENCY);
    }

    public static FiatCurrency lookupLocalFiatCurrency() {
        return (FiatCurrency) CURRENCIES.values().stream()
                .filter(currency -> currency.currencyType == CurrencyType.FIAT)
                .findAny().orElse(NULL_FIAT_CURRENCY);
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public String getFullDisplayName() {
        return this.fullDisplayName;
    }

    public String getShortDisplayName() {
        return this.shortDisplayName;
    }

    public String getCode() {
        return this.code;
    }

    public int getFractionalDigits() {
        return this.fractionalDigits;
    }

    public String getSymbol() {
        return this.symbol;
    }


    @Override
    public final boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Currency other)) {
            return false;
        }

        if (object == this) {
            return true;
        }

        return currencyType == other.currencyType && code.equals(other.code);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(currencyType, code);
    }

    @Override
    public String toString() {
        if (this == NULL_CRYPTO_CURRENCY) {
            return "the null cryptocurrency";
        } else if (this == NULL_FIAT_CURRENCY) {
            return "the null fiat currency";
        }
        return String.format("%s (%s)", fullDisplayName, code);
    }

    private static class NullCryptoCurrency extends CryptoCurrency {
        protected NullCryptoCurrency(String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol, Algorithm algorithm, String homeUrl, String walletUrl, long genesisTimeInEpochSeconds, int difficultyRetarget, int maxCoinsIssued) {
            super(fullDisplayName, shortDisplayName, code, fractionalDigits, symbol, algorithm, homeUrl, walletUrl, genesisTimeInEpochSeconds, difficultyRetarget, maxCoinsIssued);
        }

        public NullCryptoCurrency() {
            super();
        }

        @Override
        public int compareTo(java.util.@NotNull Currency o) {
            return 0;

        }
    }

    private static class NullFiatCurrency extends FiatCurrency {
        @Override
        public int compareTo(java.util.@NotNull Currency o) {
            return 0;
        }
    }
}
