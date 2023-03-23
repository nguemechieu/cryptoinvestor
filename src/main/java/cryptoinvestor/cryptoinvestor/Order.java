package cryptoinvestor.cryptoinvestor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class Order {
    public static String timestamp;
    public static TRADE_ORDER_TYPE order_type;
    public static double remaining;
    public static double fee;
    protected static double lotSize;
    protected static double price;
    protected static double stopLoss;
    protected static String symbol;
    protected static String type;
    private static int lastError;
    private static int ticket;
    protected Long id;
    protected double total;
    protected String currency;
    protected Date created;
    protected double takeProfit;
    protected Date updated;
    protected Date closed;
    protected String status;

    public Order(String timestamp, TRADE_ORDER_TYPE order_type, double remaining, double fee, double lotSize, double price) {
        Order.timestamp = timestamp;
        Order.order_type = order_type;
        Order.remaining = remaining;
        Order.fee = fee;
        Order.lotSize = lotSize;
        Order.price = price;


    }

    public static Object getOpenTime() {
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

    public static long getMagicNumber() {
        return timestamp.length();
    }

    public static int getTicket() {
        return ticket;
    }

    public static void setTicket(int ticket) {
        Order.ticket = ticket;
    }

    public static double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        Order.stopLoss = stopLoss;
    }

    public static double getOpenPrice() {
        return price;
    }

    public static String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        Order.symbol = symbol;
    }

    public static String getType() {
        return type;
    }

    public void setType(String type) {
        Order.type = type;
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
        Order.timestamp = timestamp;
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
        Order.order_type = order_type;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        Order.lotSize = lotSize;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        Order.price = price;
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
        Order.remaining = remaining;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        Order.fee = fee;
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
}
