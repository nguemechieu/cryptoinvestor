package org.tradeexpert.tradeexpert;

import javafx.beans.property.IntegerProperty;
import org.tradeexpert.tradeexpert.BinanceUs.Binance;
import org.tradeexpert.tradeexpert.Coinbase.Coinbase;
import org.tradeexpert.tradeexpert.oanda.Oanda;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static java.lang.System.out;

public abstract class CandleDataSupplier implements Supplier<Future<List<CandleData>>> {

    protected final int numCandles;
    protected final int secondsPerCandle;
    protected final String tradePair;
    protected final IntegerProperty endTime;
    public CandleDataSupplier(int numCandles, int secondsPerCandle, String tradePair, IntegerProperty endTime) {


        Objects.requireNonNull(tradePair);
        Objects.requireNonNull(endTime);
        if (numCandles <= 0) {
            throw new IllegalArgumentException("numCandles must be positive but was: " + numCandles);
        }
        if (secondsPerCandle <= 0) {
            throw new IllegalArgumentException("secondsPerCandle must be positive but was: " + secondsPerCandle);
        } else {
            out.println("CandleS " + this);
        }
        this.numCandles = numCandles;
        this.secondsPerCandle = secondsPerCandle;
        this.tradePair = tradePair;
        this.endTime = endTime;


    }

    public Set<Integer> getSupportedGranularities() {
        if (this.get() instanceof Coinbase) {

            return Set.of( 60,60 * 5, 60 * 15,3600,3600*6,3600*24);

        } else if (this.get()  instanceof Binance) {

            return Set.of(60, 60 * 5, 60 * 15, 60 * 30, 3600, 3600 * 2, 3600 * 3, 3600 * 4, 3600 * 6, 3600 * 24, 3600 * 24 * 7,
                    3600 * 24 * 7 * 4, 3600 * 24 * 365);

        }
        else if (this.get()  instanceof Oanda) {
            return Set.of(60, 60 * 5,
                    60 * 15, 60 * 30, 3600, 3600 * 2, 3600 * 3, 3600 * 4, 3600 * 6, 3600 * 24, 3600 * 24 * 7,
                    3600 * 24 * 7 * 4, 3600 * 24 * 365);
        }
        else  return Set.of(60, 60 * 5, 60 * 15,3600,
                    3600 * 2, 3600 * 3, 3600 * 4, 3600 * 6, 3600 * 24, 3600 * 24 * 7,
                    3600 * 24 * 7 * 4, 3600 * 24 * 365);



    }

    @Override
    public String toString() {
        return "CandleDataSupplier [" +
                "numCandles=" + numCandles +
                ", secondsPerCandle=" + secondsPerCandle +
                ", tradePair=" + tradePair +
                ", endTime=" + endTime +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CandleDataSupplier that = (CandleDataSupplier) o;

        out.println("CandleDataSupplier equals " + that);
        return numCandles == that.numCandles &&
                secondsPerCandle == that.secondsPerCandle &&
                Objects.equals(tradePair, that.tradePair) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numCandles, secondsPerCandle, tradePair, endTime);
    }

    public abstract List<CandleData> getCandleData();

    public abstract CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CompletableFuture<Optional<CandleData>>>>>>>>>>>> fetchCandleDataForInProgressCandle(
            String tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

    public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(String tradePair, Instant stopAt);
}
