package tradeexpert.tradeexpert;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import tradeexpert.tradeexpert.CandleData;
import tradeexpert.tradeexpert.Money;
import tradeexpert.tradeexpert.Order;
import tradeexpert.tradeexpert.TradePair;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
public class Trade implements Runnable{

    public static CandleData candle;
   static Logger logger = LoggerFactory.getLogger(Trade.class);
    private TradePair tradePair;
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
    private final ArrayList<String> symbols=new ArrayList<>();
     int NumOfSymbols;

    private static final List<Order> orderArrayList=new ArrayList<>();
    private TradeMode inpTradeMode;
    private boolean UseTime;
    private long MagicNumber;
    private boolean telegram;
    private Exchange exchange;
    private double ProfitValue;

    public Trade(TradePair tradePair, Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException {

        this.tradePair = tradePair;
        this.price = price;
        this.amount = amount;
        this.transactionType = transactionType;
        this.localTradeId = localTradeId;
        this.timestamp = timestamp;
        this.fee = fee;
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

        ConcurrentHashMap<Object, Object> tradeData = new ConcurrentHashMap<>();
        tradeData.put("tradeID", tradeID);
        tradeData.put("instrument", instrument);
        tradeData.put("side", side);
        tradeData.put("quantity", quantity);
        tradeData.put("price", price);
        tradeData.put("time", time);
        tradeData.put("transactionID", transactionID);
        tradeData.put("clientExtensions", clientExtensions);
    }

    public Trade() throws TelegramApiException, IOException, InterruptedException {

    }




    public static int getConnexionInfo() {
        return 1;
    }

    @Contract(pure = true)
    public static @NotNull ArrayList<Order> getOrders() {
        return new ArrayList<>(orderArrayList);
    }

    public static Trade fromMessage(String message) {
        logger.info(message);
        return new Gson().fromJson(message, Trade.class);
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
