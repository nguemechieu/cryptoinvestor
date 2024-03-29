package cryptoinvestor.cryptoinvestor;

import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class ReverseRawTradeDataProcessor extends CandleDataSupplier {
    private final ReversedLinesFileReader fileReader;
    private int start;

    public ReverseRawTradeDataProcessor(Path rawTradeData, int secondsPerCandle, TradePair tradePair)
            throws IOException {
        super(200, secondsPerCandle, tradePair, new SimpleIntegerProperty(-1));
        fileReader = new ReversedLinesFileReader(rawTradeData, StandardCharsets.UTF_8);
    }

    @Override
    public Future<List<CandleData>> get() {
        final Map<Integer, TreeSet<Trade>> candleTrades = new HashMap<>(numCandles);

        String line;
        try {
            while ((line = fileReader.readLine()) != null) {
                String[] commaSplitLine = line.split(",");
                if (commaSplitLine.length != 3) {
                    throw new IllegalArgumentException("raw trade data malformed");
                }

                final int timestamp = Integer.parseInt(commaSplitLine[0]);

                if (endTime.get() == -1) {
                    start = timestamp;
                    endTime.set(start - (secondsPerCandle * numCandles));
                }

                if (timestamp < endTime.get()) {
                    break;
                }

                Trade trade = new Trade(timestamp, Double.parseDouble(commaSplitLine[1]),
                        Double.parseDouble(commaSplitLine[2]));

                int candleIndex = (numCandles - ((start - timestamp) / secondsPerCandle)) - 1;
                if (candleTrades.get(candleIndex) == null) {
                    // noinspection Convert2Diamond
                    candleTrades.put(candleIndex, new TreeSet<Trade>((t1, t2) ->
                            Integer.compare(t2.timestamp, t1.timestamp)));
                } else {
                    candleTrades.get(candleIndex).add(trade);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        final List<CandleData> candleData = new ArrayList<>(numCandles);
        double lastClose = -1;
        for (int i = 0; i < numCandles; i++) {
            int openTime = (start - secondsPerCandle) - (i * secondsPerCandle);
            double volume = 0;
            if (candleTrades.get(i) == null || candleTrades.get(i).isEmpty()) {
                // no trades occurred during this candle
                candleData.add(new CandleData(lastClose, lastClose, lastClose, lastClose, openTime, volume));
            } else {
                double open = 0;
                double high = -1;
                double low = Double.MAX_VALUE;
                double close = 0;

                double priceTotal = 0;
                double volumeWeightedPriceTotal = 0;
                int tradeIndex = 0;
                for (Trade trade : candleTrades.get(i)) {
                    if (tradeIndex == 0) {
                        open = trade.price;
                    }

                    if (trade.price > high) {
                        high = trade.price;
                    }

                    if (trade.price < low) {
                        low = trade.price;
                    }

                    if (tradeIndex == candleTrades.get(i).size() - 1) {
                        close = trade.price;
                    }

                    priceTotal += trade.price;
                    volumeWeightedPriceTotal += trade.price * trade.amount;
                    volume += trade.amount;
                    tradeIndex++;
                    lastClose = close;
                }

                double averagePrice = priceTotal / candleTrades.get(i).size();
                double volumeWeightedAveragePrice = volumeWeightedPriceTotal / volume;

                CandleData datum = new CandleData(open, close, Math.max(open, high), Math.min(open, low), openTime,
                        volume);
                candleData.add(datum);
            }
        }

        start = endTime.get();
        endTime.set(endTime.get() - (secondsPerCandle * numCandles));
        return CompletableFuture.completedFuture(candleData.stream().sorted(Comparator.comparingInt(
                CandleData::getOpenTime)).collect(Collectors.toList()));
    }

    @Override
    public List<CandleData> getCandleData() {
        return new ArrayList<CandleData>();
    }

    /**
     * Represents one line of the raw trade data. We use doubles because the results don't need to be
     * *exact* (i.e. small rounding errors are fine), and we want to favor speed.
     *
     * @param timestamp 1315922016,5.800000000000,1.000000000000
     */
    private record Trade(int timestamp, double price, double amount) {

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (object == null || object.getClass() != getClass()) {
                return false;
            }

            Trade other = (Trade) object;

            return Objects.equals(timestamp, other.timestamp) &&
                    Objects.equals(price, other.price) &&
                    Objects.equals(amount, other.amount);
        }


        @Override
        public String toString() {
            return String.format("Trade [timestamp = %d, price = %f, amount = %f]", timestamp, price, amount);
        }
    }
}
