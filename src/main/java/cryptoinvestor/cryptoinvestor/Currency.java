package cryptoinvestor.cryptoinvestor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Currency implements Comparable<Currency> {
    CurrencyType currencyType;
    String fullDisplayName;
    String shortDisplayName;
    protected String code;
    protected int fractionalDigits;
    protected String symbol;
    private static final Map<SymmetricPair<String, CurrencyType>, Currency> CURRENCIES = new ConcurrentHashMap<>();
    public static final CryptoCurrency NULL_CRYPTO_CURRENCY = new NullCryptoCurrency(
            CurrencyType.NULL,
            "",
            "",
            "",
            0,
            "",
            ""
    );
    public static final FiatCurrency NULL_FIAT_CURRENCY = new NullFiatCurrency(
            CurrencyType.NULL,
            "",
            "",
            "",
            0,
            "",
            ""
    ) {
        @Override
        public int compareTo(@NotNull Currency o) {
            return 0;
        }

        @Override
        public int compareTo(java.util.@NotNull Currency o) {
            return 0;
        }
    };
    private static final Logger logger = LoggerFactory.getLogger(Currency.class);



    private String image;


    /**
     * Private constructor used only for the {@code NULL_CURRENCY}.
     */
    protected Currency() {
        this.currencyType = CurrencyType.NULL;
        this.fullDisplayName = "xxx";
        this.shortDisplayName = "xxx";
        this.code = "XXX";
        this.fractionalDigits = 0;
        this.symbol = "xxx";
        this.image = "xxx";

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

    public Currency(CurrencyType currencyType, String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol, String image) {
        this(currencyType, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol);

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
        this.image = image;

    }

    protected static void registerCurrency(Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        CURRENCIES.put(SymmetricPair.of(currency.code, currency.currencyType), currency);
    }
    public static Currency of(String code) throws SQLException, ClassNotFoundException {
        Objects.requireNonNull(code, "code must not be null");
//        if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.FIAT))
//                && CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
//            logger.error("ambiguous currency code: " + code);
//            throw new IllegalArgumentException("ambiguous currency code: " + code + " (code" +
//                    " is used for multiple currency types); use ofCrypto(...) or ofFiat(...) instead");
//        } else {
//            if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.CRYPTO))) {
//                return CURRENCIES.get(SymmetricPair.of(code, CurrencyType.CRYPTO));
//            } else if (CURRENCIES.containsKey(SymmetricPair.of(code, CurrencyType.FIAT))) {
//                return CURRENCIES.getOrDefault(SymmetricPair.of(code, CurrencyType.FIAT), NULL_CRYPTO_CURRENCY);
//            } else {
//                logger.error("unknown currency code: " + code);
//                logger.error("known codes: " + CURRENCIES.keySet());
//                logger.info("Trying to fetch from database");
        Db1 db1 = new Db1();

        if (db1.getCurrency(code) != null) {
            return db1.getCurrency(code);
        } else {
            logger.error("could not fetch from database");
            throw new IllegalArgumentException("unknown currency code: " + code);
        }
        //  }
        //}

    }

    public void registerCurrencies(Collection<Currency> currencies) {
        Objects.requireNonNull(currencies, "currencies must not be null");
        currencies.forEach(Currency::registerCurrency);
    }

    /**
     * Get the fiat currency that has a currency code equal to the
     * given {@code}. Using {@literal "¤¤¤"} as the currency code
     * returns {@literal NULL_FIAT_CURRENCY}.
     *
     */
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

    public static String valueOf(String currency) {
        return lookupBySymbol(currency).getCode();
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

    /**
     * The finality of {@code equals(...)} ensures that the equality
     * contract for subclasses must be based on currency type and code alone.
     *
     */
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

    /**
     * The finality of {@code hashCode()} ensures that the equality
     * contract for subclasses must be based on currency
     * type and code alone.
     *
     */
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public abstract int compareTo(java.util.@NotNull Currency o);


    private static class NullCryptoCurrency extends CryptoCurrency {
        protected NullCryptoCurrency(CurrencyType currencyType, String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol, String image) {
            super(currencyType, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol, image);
        }
    }

    private static abstract class NullFiatCurrency extends FiatCurrency {
        protected NullFiatCurrency(CurrencyType currencyType, String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol, String image) {
            super(currencyType, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol, image);
        }
    }
}
