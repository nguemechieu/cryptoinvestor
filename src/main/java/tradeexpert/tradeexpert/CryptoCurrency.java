package tradeexpert.tradeexpert;

import org.jetbrains.annotations.NotNull;

public class CryptoCurrency extends  Currency
{


    protected CryptoCurrency(CurrencyType currencyType, String fullDisplayName, String shortDisplayName, String code, int fractionalDigits, String symbol) {
        super(CurrencyType.CRYPTO, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol);

        this .code=code;
        this.fractionalDigits=fractionalDigits;
        this.symbol=symbol;
        this.fullDisplayName=fullDisplayName;
        this.shortDisplayName=shortDisplayName;
        this.currencyType=currencyType;

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
    public int compareTo(java.util.@NotNull Currency o) {
        return 0;
    }
}
