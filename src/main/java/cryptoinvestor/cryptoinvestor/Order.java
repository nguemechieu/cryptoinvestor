package cryptoinvestor.cryptoinvestor;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

public class Order extends RecursiveTreeObject<Order> {
     ArrayList<Order> orders=new ArrayList<>();
    private  TradePair tradePair;
    String timestamp;
    TRADE_ORDER_TYPE order_type;
    double remaining;
    double fee;
   double lotSize;
    double price;
   double stopLoss;
 String symbol;
   TRADE_ORDER_TYPE type;

    private static int lastError;
    private int orderID;

    int ticket=orderID;
    protected Long id;
    protected double total;
    protected String currency;
    protected Date created;
    protected double takeProfit;
    protected Date updated;
    protected Date closed;
    protected String status;
    private Side side;
    private String filled;

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setFilled(String filled) {
        this.filled = filled;
    }

    public Order(@NotNull TradePair tradePair, String timestamp, TRADE_ORDER_TYPE order_type, Side side, double remaining, double fee, double lotSize, double price

    , double stopLoss, double takeProfit
    ) {
        this.timestamp = timestamp;
        this.order_type = order_type;
        this.remaining = remaining;
        this.fee = fee;
        this.lotSize = lotSize;
        this.price = price;
        this.stopLoss = stopLoss;
        this.symbol = tradePair.getCounterCurrency().symbol;
        this.type = order_type;

        this.currency = tradePair.getCounterCurrency().symbol;
        this.created = new Date();
        this.takeProfit = takeProfit;
        this.updated = new Date();
        this.side = side;
        this.tradePair = tradePair;

 orders.add(this);


    }

    public Object getOpenTime() {
        return timestamp;
    }

    public static int getLastError() {
        return lastError;
    }

    public static void setLastError(int lastError) {
        Order.lastError = lastError;
    }

    @Contract(pure = true)
    public static @NotNull String getErrorDescription(int err) {
        return "Error " + err;
    }

    public  long getMagicNumber() {
        return timestamp.length();
    }

    public  int getTicket() {
        return ticket;
    }

    public  void setTicket(int ticket) {
        this.ticket = ticket;
    }

    public  double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public  double getOpenPrice() {
        return price;
    }

    public  String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public  TRADE_ORDER_TYPE getType() {
        return type;
    }

    public void setType(TRADE_ORDER_TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", total=" + total +
                ", currency='" + currency + '\'' +
                ", created=" + created +
                ", takeProfit=" + takeProfit +
                ", updated=" + updated +
                ", closed=" + closed +
                ", status='" + status + '\'' +
                '}';
    }

    public double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TRADE_ORDER_TYPE getOrder_type() {
        return order_type;
    }

    public void setOrder_type(TRADE_ORDER_TYPE order_type) {
        this.order_type = order_type;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        this.lotSize = lotSize;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void showOrderDetails() {
        System.out.println("id: " + id);
        System.out.println("order_type: " + order_type);
        System.out.println("lotSize: " + lotSize);
        System.out.println("price: " + price);
        System.out.println("total: " + total);
        System.out.println("remaining: " + remaining);
        System.out.println("fee: " + fee);
        System.out.println("currency: " + currency);
        System.out.println("created: " + created);
        System.out.println("updated: " + updated);
        System.out.println("closed: " + closed);
        System.out.println("status: " + status);
        System.out.println("symbol: " + symbol);
        System.out.println("type: " + type);
    }

    public Side getSide() {
        return side;
    }

    public TradePair getTradePair() {
        return tradePair;
    }

    public void setTradePair(TradePair tradePair) {
        this.tradePair = tradePair;
    }

    public String getTime() {
        return timestamp;
    }

    public String getOrderId() {
        return String.valueOf(orderID);
    }

    public String getFilled() {
        return filled;
    }
}
