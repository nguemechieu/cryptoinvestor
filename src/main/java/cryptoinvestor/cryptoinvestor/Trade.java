package cryptoinvestor.cryptoinvestor;

import com.google.gson.Gson;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Trade extends RecursiveTreeObject<Trade> implements Runnable {


    Order order = new Order((long) (Math.random() * 100000), new TradePair("BTC", "USD"),
            new Date().toString(),
            ENUM_ORDER_TYPE.BUY, Side.BUY, 0, 0, price, 0, 0, 0
    );
    private boolean isBestMatch;
    private boolean isMaker;
    private boolean isBuyer;
    private long time;
    private String commissionAsset;
    private String commission;
    private double quoteQty;
    private double qty;
    private long orderListId;
    private long orderId;

    public Trade(String symbol, long id, long orderId, long orderListId, String price, String qty, String quoteQty, String commission, String commissionAsset, long time, boolean isBuyer, boolean isMaker, boolean isBestMatch) {
        this.instrument = symbol;
        Trade.id = id;
        this.orderId = orderId;
        this.orderListId = orderListId;
        Trade.price = Double.parseDouble(price);
        this.qty = Double.parseDouble(qty);
        this.quoteQty = Double.parseDouble(quoteQty);
        this.commission = commission;
        this.commissionAsset = commissionAsset;
        this.time = time;
        this.isBuyer = isBuyer;
        this.isMaker = isMaker;
        this.isBestMatch = isBestMatch;
    }

    public static void setTrades(List<Trade> trades) {
        Trade.trades = trades;
    }

    public static CandleData getCandle() {
        return candle;
    }

    public static void setCandle(CandleData candle) {
        Trade.candle = candle;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        Trade.logger = logger;
    }

    public static List<Order> getOrderList() {
        return orderList;
    }

    public static void setOrderList(List<Order> orderList) {
        Trade.orderList = orderList;
    }

    public static Money getFee() {
        return fee;
    }

    public static void setFee(Money fee) {
        Trade.fee = fee;
    }

    public boolean isBestMatch() {
        return isBestMatch;
    }

    public void setBestMatch(boolean bestMatch) {
        isBestMatch = bestMatch;
    }

    public boolean isMaker() {
        return isMaker;
    }

    public void setMaker(boolean maker) {
        isMaker = maker;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public void setBuyer(boolean buyer) {
        isBuyer = buyer;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCommissionAsset() {
        return commissionAsset;
    }

    public void setCommissionAsset(String commissionAsset) {
        this.commissionAsset = commissionAsset;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public double getQuoteQty() {
        return quoteQty;
    }

    public void setQuoteQty(double quoteQty) {
        this.quoteQty = quoteQty;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public long getOrderListId() {
        return orderListId;
    }

    public void setOrderListId(long orderListId) {
        this.orderListId = orderListId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public ConcurrentHashMap<String, Order> getOrderMap() {
        return orderMap;
    }

    public void setOrderMap(ConcurrentHashMap<String, Order> orderMap) {
        this.orderMap = orderMap;
    }

    public ENUM_ORDER_TYPE getOrder_type() {
        return order_type;
    }

    public void setOrder_type(ENUM_ORDER_TYPE order_type) {
        this.order_type = order_type;
    }

    public double getSize() {
        return size;
    }

    public double getCurrentUnits() {
        return currentUnits;
    }

    public double getInitialMargin() {
        return initialMargin;
    }

    public void setInitialMargin(double initialMargin) {
        this.initialMargin = initialMargin;
    }

    public double getInitialUnits() {
        return initialUnits;
    }

    public double getInitialMarginRequired() {
        return initialMarginRequired;
    }

    public String getState() {
        return state;
    }

    public long getLastTransactionID() {
        return lastTransactionID;
    }

    public String getClientExtensions() {
        return clientExtensions;
    }

    public double getUnrealizedPL() {
        return unrealizedPL;
    }

    public double getMarginUsed() {
        return marginUsed;
    }

    public double getFinancing() {
        return financing;
    }

    public double getDividendAdjustment() {
        return dividendAdjustment;
    }

    public double getRealizedPL() {
        return realizedPL;
    }

    public double getMarginBalance() {
        return marginBalance;
    }

    public void setMarginBalance(double marginBalance) {
        this.marginBalance = marginBalance;
    }

    public double getMarginAvailable() {
        return marginAvailable;
    }

    public void setMarginAvailable(double marginAvailable) {
        this.marginAvailable = marginAvailable;
    }

    public double getMarginBalanceAvailable() {
        return marginBalanceAvailable;
    }

    public void setMarginBalanceAvailable(double marginBalanceAvailable) {
        this.marginBalanceAvailable = marginBalanceAvailable;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public ConcurrentHashMap<Long, Trade> getTradeMap() {
        return tradeMap;
    }

    public void setTradeMap(ConcurrentHashMap<Long, Trade> tradeMap) {
        this.tradeMap = tradeMap;
    }

    public Exchange getExchange() {
        return exchange.get();
    }

    public void setExchange(Exchange exchange) {
        this.exchange.set(exchange);
    }

    public SimpleObjectProperty<Exchange> exchangeProperty() {
        return exchange;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setTransactionType(Side transactionType) {
        this.transactionType = transactionType;
    }

    public long getLocalTradeId() {
        return localTradeId;
    }

    Side side;

    public void setLocalTradeId(long localTradeId) {
        this.localTradeId = localTradeId;
    }

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
               String clientExtensions, double unrealizedPL, double marginUsed) throws IOException, ParseException, URISyntaxException, InterruptedException {
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
              id, instrument, timestamp.toString(),
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
                 long localTradeId, Instant timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException, ParseException, URISyntaxException {

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
                 long localTradeId, Instant timestamp) throws TelegramApiException, IOException, InterruptedException, ParseException, URISyntaxException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId,
                timestamp, DefaultMoney.NULL_MONEY);
    }

    public Trade(TradePair tradePair, @NotNull Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp) throws TelegramApiException, IOException, InterruptedException, ParseException, URISyntaxException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp),
                DefaultMoney.NULL_MONEY);

    }

    public Trade(TradePair tradePair, @NotNull Money price, Money amount, Side transactionType,
                 long localTradeId, long timestamp, Money fee) throws TelegramApiException, IOException, InterruptedException, ParseException, URISyntaxException {
        this(tradePair, price.toDouble(), amount, transactionType, localTradeId, Instant.ofEpochSecond(timestamp), fee);
        Trade.fee = fee;

        logger.info("Trade created");

    }

    public Trade() throws IOException, ParseException, URISyntaxException, InterruptedException {

    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
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

        return Objects.equals(tradePair, tradePair)
                && Objects.equals(price, price)
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
        Trade.id = id;
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
        Trade.price = price;
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
