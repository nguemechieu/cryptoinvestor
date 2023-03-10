package org.tradeexpert.tradeexpert;

import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class Trade implements Runnable{
    public static CandleData candle;
    private ConcurrentHashMap<Object,Object> trade=new ConcurrentHashMap<>();
    private String tradePair;
    private Money price;
    private Money amount;
    private Side transactionType;
    private long localTradeId;
    private Instant timestamp;

    List<Order> orderList=
            List.of(
                    new Order(
                            new Date(1530000000000L).toString(),TRADE_ORDER_TYPE.BUY,0,9,0.01,1.45

                    ),
                    new Order(
                            new Date().toString(),TRADE_ORDER_TYPE.SELL,0,9,0.01,1.45

                    ),
                    new Order(
                            new Date(1550000000000L).toString(),TRADE_ORDER_TYPE.BUY,0,9,0.01,1.45

                    ),
                    new Order(
                            new Date(1560000000000L).toString(),TRADE_ORDER_TYPE.SELL,0,9,0.01,1.45

                    ),
                    new Order(
                            new Date(1570000000000L).toString(),TRADE_ORDER_TYPE.BUY,0,9,2,1.45

                    )
                   );
    private Money fee;

    public Trade(String tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp, Money fee) {

        this.tradePair = tradePair;
        this.price = price;
        this.amount = amount;
        this.transactionType = transactionType;
        this.localTradeId = localTradeId;
        this.timestamp = timestamp;
        this.fee = fee;
    }

    public Trade(String tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp) {
        this(tradePair, price, amount, transactionType, localTradeId,
                timestamp, DefaultMoney.NULL_MONEY);
    }

    public Trade(String tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp) {
        this(tradePair, price, amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp),
                DefaultMoney.NULL_MONEY);
    }

    public Trade(String tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp, Money fee) {
        this(tradePair, price, amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp), fee);
    }

    public Trade(String tradeID, String instrument, String side, String quantity, String price, String time, String transactionID, String clientExtensions) {

        this.trade.put("tradeID", tradeID);
        this.trade.put("instrument", instrument);
        this.trade.put("side", side);
        this.trade.put("quantity", quantity);
        this.trade.put("price", price);
        this.trade.put("time", time);
        this.trade.put("transactionID", transactionID);
        this.trade.put("clientExtensions", clientExtensions);
    }


    public static int getConnexionInfo() {
        return 1;
    }

    public String getTradePair() {
        return tradePair;
    }

    public Money getPrice() {
        return price;
    }

    public Money getAmount() {
        return amount;
    }


    public Money getTotal() {
        // TODO implement multiply method in Money..but think of how to do it with
        // different currencies..maybe involve a TradePair? btc * usd/btc = usd, which
        // is technically what we are doing here
        return DefaultMoney.ofFiat(price.toBigDecimal().multiply(amount.toBigDecimal()), "USD");
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Side getTransactionType() {
        return transactionType;
    }

    @Override
    public String toString() {
        return String.format("Trade [tradePair = %s, price = %s, amount = %s, transactionType = %s, localId = %s, " +
                "timestamp = %s, fee = %s]", tradePair, price, amount, transactionType, localTradeId, timestamp, fee);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }

        Trade other = (Trade) object;

        return Objects.equals(tradePair, other.tradePair)
                && Objects.equals(price, other.price)
                && Objects.equals(amount, other.amount)
                && transactionType == other.transactionType
                && localTradeId == other.localTradeId
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradePair, price, amount, transactionType, localTradeId, timestamp);
    }




void OnInit(CandleData candle) {


}
    @Override
    public void run() {

        OnInit(candle);

        OnTick();
    }

    private void OnTick() {
    }
}
