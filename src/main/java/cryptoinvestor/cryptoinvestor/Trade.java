package cryptoinvestor.cryptoinvestor;

import com.google.gson.Gson;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Trade implements Runnable {


    Side side;
    Order order=new Order((long) (Math.random()*100000), new TradePair("BTC", "USD"),
            Instant.now(),
            ENUM_ORDER_TYPE.BUY,Side.BUY,0,0,price,0,0,0
            );

    ArrayList<Order> orders = new ArrayList<>();
    ConcurrentHashMap<String, Order> orderMap = new ConcurrentHashMap<>();
    ENUM_ORDER_TYPE order_type;
 double remaining;
     double size;
    double currentUnits;
    double initialMargin;
    double initialUnits;
    double initialMarginRequired;
    String state;
    long lastTransactionID;
    String clientExtensions;
    double unrealizedPL;
    double marginUsed;
    double financing;
    double dividendAdjustment;
    double realizedPL;
    double marginBalance;
    double marginAvailable;
    double marginBalanceAvailable;

    long  openTime;
    private  String instrument;
    private static Long id;
    long closeTime;
     double volume;
    private String accountID;

    //   {"trades":[{"id":"142950","instrument":"EUR_USD","price":"1.07669","openTime":"2023-03-21T16:56:10.786314295Z","initialUnits":"-1700","initialMarginRequired":"36.6098","state":"OPEN","currentUnits":"-1700","realizedPL":"0.0000","financing":"0.1828","dividendAdjustment":"0.0000","clientExtensions":{"id":"140660466","tag":"0"},"unrealizedPL":"-23.6130","marginUsed":"37.0770"},{"id":"124829","instrument":"USD_CAD","price":"1.38016","openTime":"2023-03-15T14:46:04.088679752Z","initialUnits":"4000","initialMarginRequired":"80.0000","state":"OPEN","currentUnits":"4000","realizedPL":"0.0000","financing":"-0.7802","dividendAdjustment":"0.0000","clientExtensions":{"id":"140494560","tag":"0"},"unrealizedPL":"-48.2803","marginUsed":"80.0000"}],"lastTransactionID":"142955"}

  public Trade(ENUM_ORDER_TYPE orderType, Long id, @NotNull TradePair instrument,  Side side, double price, Long openTime, int initialUnits, double initialMargin
               , String state, double currentUnits, double realizedPL, double financing, double dividendAdjustment,
               String clientExtensions, double unrealizedPL, double marginUsed) {
      order_type = orderType;
      Trade.id = id;
      this.instrument = instrument.getBaseCurrency() + "_" + instrument.getCounterCurrency();
      Trade.price = price;
      this.openTime = openTime;
      this.initialUnits = initialUnits;
      this.initialMargin = initialMargin;
      this.state = state;
      this.currentUnits = (int) currentUnits;
      this.realizedPL = realizedPL;
      this.financing = financing;
      this.dividendAdjustment = dividendAdjustment;
      this.clientExtensions = clientExtensions;
      this.unrealizedPL = unrealizedPL;
      this.marginUsed = marginUsed;
      this.timestamp = Instant.ofEpochSecond(openTime);
      this.side = side;//.equals(TRADE_ORDER_TYPE.BUY)? TRADE_ORDER_TYPE.BUY : TRADE_ORDER_TYPE.SELL ;


      this.order=new Order(
              id, instrument,timestamp,
              orderType,
              side,
              currentUnits,
              realizedPL,
              financing,
              dividendAdjustment,

              unrealizedPL,
              marginUsed
      );
     tradePair = instrument;
      trades.add(this);


  }

    static List<Trade> trades=new ArrayList<>();
    ConcurrentHashMap<Long, Trade> tradeMap = new ConcurrentHashMap<>();





    public static CandleData candle;
    static Logger logger = LoggerFactory.getLogger(Trade.class);
    static List<Order> orderList =new ArrayList<>();

    SimpleObjectProperty<Exchange> exchange = new SimpleObjectProperty<>(this, "exchange");
     static TradePair tradePair;
    private static double price;
     Money amount;
    private Side transactionType;
    private long localTradeId;
     Instant timestamp;
    private static Money fee;

    public Trade(TradePair tradePair, double price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException {

        Trade.tradePair = tradePair;
        Trade.price = price;

        this.amount = amount;
        this.transactionType = transactionType;
        this.localTradeId = localTradeId;
        this.timestamp = timestamp;

        Trade.fee = fee;
        logger.info("Trade created");
    }

    public Trade(TradePair tradePair, @NotNull Money price, Money amount, Side transactionType,
                 long localTradeId, Instant timestamp) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId,
                timestamp, DefaultMoney.NULL_MONEY);
    }

    public Trade(TradePair tradePair, @NotNull Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp),
                DefaultMoney.NULL_MONEY);

    }

    public Trade(TradePair tradePair, @NotNull Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp), fee);
        this.fee = fee;

        logger.info("Trade created");

    }

    public Trade() {

    }


    public static int getConnexionInfo() {
        return 1;
    }


    public static Trade fromMessage(String message) {
        logger.info(message);
        return new Gson().fromJson(message, Trade.class);
    }

    public static ArrayList<Order> getOrders() {
        return (ArrayList<Order>) orderList;
    }

    public static Trade @NotNull [] getTrades() {

        return trades.toArray(new Trade[0]);
    }


    public  double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public TradePair getTradePair() {
        return tradePair;
    }

    public double getPrice() {
        return price;
    }

    public Money getAmount() {

        return amount;
    }


    public Money getTotal() {

        // different currencies..maybe involve a TradePair? btc * usd/btc = usd, which
        // is technically what we are doing here
        return DefaultMoney.ofFiat(price, tradePair.getCounterCurrency().code);
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
        for (Order i : orderList) {
            if (i != null) {
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
        logger.info("OnTick");
        logger.info(tradePair.getCounterCurrency().code);
        logger.info(tradePair.getBaseCurrency().code);
        logger.info("Trade pair: " + tradePair.toString());
        logger.info("Price: " + price);
        logger.info("Amount: " + amount);
        logger.info("Transaction type: " + transactionType);
        logger.info("Local trade id: " + localTradeId);
        logger.info("Timestamp: " + timestamp);
        logger.info("Fee: " + fee);
        logger.info("Total: " + getTotal());
        logger.info("Orders total: " + OrdersTotal());
        logger.info("Orders: " + orderList.size());
        logger.info("Trades: " + trades.size());
        logger.info("OrderMap: " + orderMap.size());
        logger.info("TradeMap: " + tradeMap.size());
        logger.info("Exchange: " + exchange.get());
        logger.info("UnrealizedPL: " + unrealizedPL);
        logger.info("MarginUsed: " + marginUsed);
        logger.info("OrderMap: " + orderMap.size());

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Long getOpenTime() {

        if (state.equals("OPEN")) {
            return openTime;
        }else {
            return 0L;
        }
    }

    public void setOpenTime(int openTime) {
        this.openTime = openTime;
    }

    public double getVolume() {
        logger.info("Volume");
        logger.info(tradePair.getCounterCurrency().code);

        return initialUnits;
    }

    public Long getCloseTime() {
        logger.info("Close Time");
        logger.info(tradePair.getCounterCurrency().code);

        if (state .equals("CLOSED")) {
            return closeTime;
        }

        return 0L;


    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void setCloseTime(int closeTime) {
        this.closeTime = closeTime;
    }

    public void setTradeID(long id) {
        this.localTradeId = id;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setFinancing(double financing) {
        this.financing = financing;
    }

    public void setRealizedPL(double realizedPL) {
        this.realizedPL = realizedPL;
    }

    public void setMarginUsed(double marginUsed) {
        this.marginUsed = marginUsed;
    }

    public void setInitialUnits(double initialUnits) {
        this.initialUnits = initialUnits;
    }

    public void setInitialMarginRequired(double initialMarginRequired) {
        this.initialMarginRequired =  initialMarginRequired;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCurrentUnits(double currentUnits) {
        this.currentUnits = currentUnits;
    }

    public void setUnrealizedPL(double unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    public void setDividendAdjustment(double dividendAdjustment) {
        this.dividendAdjustment = dividendAdjustment;
    }

    public void setClientExtensions(String asText) {
        this.clientExtensions = asText;
    }

    public void setLastTransactionID(long lastTransactionID) {
        this.lastTransactionID = lastTransactionID;
    }

    public void setTradePair(TradePair tradePair) {
        Trade.tradePair = tradePair;
    }

    public void close() {
        state = "CLOSED";
        closeTime = Instant.now().getEpochSecond();
        logger.info("Trade closed");
        logger.info(tradePair.getCounterCurrency().code);
        logger.info(tradePair.getBaseCurrency().code);
    }
}
