package tradeexpert.tradeexpert;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public abstract class Currency {
    static Logger logger = LoggerFactory.getLogger(Currency.class);
    CurrencyType currencyType;
    String fullDisplayName;

    protected String code;
    protected int fractionalDigits;
    protected String symbol;

    public static final CryptoCurrency NULL_CRYPTO_CURRENCY = new NullCryptoCurrency(CurrencyType.CRYPTO,"XXX","",
            "XXX", 0, "XXX");
    public static final FiatCurrency NULL_FIAT_CURRENCY = new NullFiatCurrency();
    String shortDisplayName;

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
        logger.debug("Created currency: " + this);
    }

    @Contract("null -> fail")
    public static @Nullable Currency of(Currency baseCurrency) {
        if (baseCurrency == null) {
            throw new IllegalArgumentException("baseCurrency must not be null");
        }

        for (Currency currency : CurrencyDataProvider.getInstance()) {
            if (currency.getCode().equals(baseCurrency.code)) {
                return currency;
            }
        }
        logger.debug("Currency not found: " + baseCurrency);

        return null;
    }


    public CurrencyType getCurrencyType() {
        return this.currencyType;
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
    public String toString() {
        return
                "currencyType=" + currencyType +
                ", fullDisplayName='" + fullDisplayName + '\'' +
                ", code='" + code + '\'' +
                ", fractionalDigits=" + fractionalDigits +
                ", symbol='" + symbol + '\'' +
                ", shortDisplayName='" + shortDisplayName + '\'' ;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(currencyType, code);
    }

    public abstract int compareTo(@NotNull java.util.Currency o);

    private static class NullCryptoCurrency extends CryptoCurrency {
        protected NullCryptoCurrency(CurrencyType crypto, String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol) {
            super(crypto,
                    fullDisplayName,
                    shortDisplayName,
                    code,
                    fractionalDigits,
                    symbol
            );
        }


        @Override
        public int compareTo(java.util.@NotNull Currency o) {
            return 0;

        }
    }


    private static class NullFiatCurrency extends FiatCurrency {
        protected NullFiatCurrency() {
            super();
        }


        @Override
        public int compareTo(java.util.@NotNull Currency o) {

            return 0;
        }
    }
}