package org.tradeexpert.tradeexpert;

import java.util.logging.Logger;

public abstract class CryptoCurrency extends Currency {
    String code;
    int fractionalDigits;
    String symbol;
    String fullDisplayName;

    Logger logger = Logger.getLogger(CryptoCurrency.class.getName());
    String maxCoinsIssued;

    protected CryptoCurrency() {
        super(CurrencyType.CRYPTO, "", "", "", 0, "");

        maxCoinsIssued = "";
    }

    protected CryptoCurrency(String fullDisplayName, String shortDisplayName, String code, int fractionalDigits,
                             String symbol) {
        super(CurrencyType.CRYPTO, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol);

        this.code = code;
        this.fractionalDigits = fractionalDigits;
        this.symbol = symbol;
        this.fullDisplayName = fullDisplayName;
        this.maxCoinsIssued = "";
        logger.info("CryptoCurrency created");


    }

    @Override
    public String toString() {
        return "CryptoCurrency{" +
                ", code='" + code + '\'' +
                ", fractionalDigits=" + fractionalDigits +
                ", symbol='" + symbol + '\'' +
                ", fullDisplayName='" + fullDisplayName + '\'' +
                '}';
    }
}
