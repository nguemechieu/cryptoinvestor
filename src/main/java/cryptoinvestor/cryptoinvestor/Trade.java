package cryptoinvestor.cryptoinvestor;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
public class Trade implements Runnable{

    public static CandleData candle;
   static Logger logger = LoggerFactory.getLogger(Trade.class);
    private TradePair tradePair;
    private Money price;
    private Money amount;
    private Side transactionType;
    private long localTradeId;
    private Instant timestamp;

    static List<Order> orderList=
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


    SimpleObjectProperty<Exchange> exchange = new SimpleObjectProperty<>(this, "exchange");

    public Trade(TradePair tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException {

        this.tradePair = tradePair;
        this.price = price;
        this.amount = amount;
        this.transactionType = transactionType;
        this.localTradeId = localTradeId;
        this.timestamp = timestamp;
        this.fee = fee;
        logger.info("Trade created");
    }

    public Trade(TradePair tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price, amount, transactionType, localTradeId,
                timestamp, DefaultMoney.NULL_MONEY);
    }

    public Trade(TradePair tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price, amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp),
                DefaultMoney.NULL_MONEY);
    }

    public Trade(TradePair tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price, amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp), fee);
    }

    public Trade(String tradeID, String instrument, String side, String quantity, String price, String time, String transactionID, String clientExtensions) throws TelegramApiException, IOException, InterruptedException {


        ConcurrentHashMap<String, String> trades = new ConcurrentHashMap<>();
        trades.put("tradeID", tradeID);
        trades.put("instrument", instrument);
        trades.put("side", side);
        trades.put("quantity", quantity);
        trades.put("price", price);
        trades.put("time", time);
        trades.put("transactionID", transactionID);
        trades.put("clientExtensions", clientExtensions);



    }

    public Trade() throws TelegramApiException, IOException, InterruptedException {
        this(new TradePair("BTC", "USD"),
                DefaultMoney.ofFiat(18, "USD"),
                DefaultMoney.ofFiat(18, "USD"),
                Side.BUY,
                1,
                Instant.now());

    }




    public static int getConnexionInfo() {
        return 1;
    }



    public static Trade fromMessage(String message) {
        logger.info(message);
        return new Gson().fromJson(message, Trade.class);
    }

    public static Order getOrders() {
        return orderList.get(0);
    }

    public TradePair getTradePair() {
        return tradePair;
    }

    public Money getPrice() {
        return price;
    }

    public Money getAmount() {
        return amount;
    }


    public Money getTotal() {

        // different currencies..maybe involve a TradePair? btc * usd/btc = usd, which
        // is technically what we are doing here
        return DefaultMoney.ofFiat(price.toBigDecimal().multiply(amount.toBigDecimal()), tradePair.getCounterCurrency().code);
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








    private int OrdersTotal() {
        int count = 0;
        for (Order i:  orderList) {
            if (i!= null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void run() {


       OnTick();
    }

    private void OnTick() {
    }
}
