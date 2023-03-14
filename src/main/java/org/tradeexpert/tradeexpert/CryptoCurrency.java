package org.tradeexpert.tradeexpert;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

import java.time.Instant;
import java.util.Objects;

public abstract class CryptoCurrency extends Currency {
    private  Algorithm algorithm;
    private  URI homeUrl;
    private  URI walletUrl;
    /**
     * Time that the genesis block was created.
     */
    private  Instant genesisTime;

    public CryptoCurrency() {


    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public URI getHomeUrl() {
        return homeUrl;
    }

    public URI getWalletUrl() {
        return walletUrl;
    }

    public Instant getGenesisTime() {
        return genesisTime;
    }

    public int getDifficultyRetarget() {
        return difficultyRetarget;
    }

    public int getMaxCoinsIssued() {
        return maxCoinsIssued;
    }

    /**
     * After how many blocks is difficulty recalculated.
     */
    private int difficultyRetarget;
    private  int  maxCoinsIssued;



    protected CryptoCurrency(String fullDisplayName, String shortDisplayName, String code, int fractionalDigits,
                             String symbol, Algorithm algorithm, String homeUrl, String walletUrl,
                             long genesisTimeInEpochSeconds, int difficultyRetarget, int maxCoinsIssued) {
        super(CurrencyType.CRYPTO, fullDisplayName, shortDisplayName, code, fractionalDigits, symbol);

        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(homeUrl, "homeUrl must not be null");
        Objects.requireNonNull(walletUrl, "walletUrl must not be null");

        this.algorithm = algorithm;
        this.homeUrl = URI.create(homeUrl);
        this.walletUrl = URI.create(walletUrl);
        this.genesisTime = Instant.ofEpochSecond(genesisTimeInEpochSeconds);
        this.difficultyRetarget = difficultyRetarget;
        this.maxCoinsIssued = maxCoinsIssued;
    }

    public abstract int compareTo(@NotNull java.util.Currency o);
}
