package org.tradeexpert.tradeexpert;

import java.util.Date;

 public class Order {
    public String timestamp;
    public TRADE_ORDER_TYPE order_type;
    public double remaining;
    public double fee;


     protected Long id;
    protected double lotSize;
    protected double price;
    protected double total;
    protected String currency;
    protected Date created;
    protected double stopLoss;
    protected double takeProfit;
    protected Date updated;
    protected Date closed;
    protected String status;
    protected String symbol;
    protected String type;


     public Order(String timestamp, TRADE_ORDER_TYPE order_type, double remaining, double fee, double lotSize, double price) {

         this.order_type = order_type;

         this.id = id;

         this.created = created;

         this.updated = updated;
         this.closed = closed;

         this.timestamp = timestamp;
         this.remaining = remaining;
         this.fee = fee;
         this.lotSize = lotSize;
         this.price = price;
         this.total = total;
         this.currency = currency;
         this.stopLoss = stopLoss;
         this.takeProfit = takeProfit;
         this.status = status;
         this.symbol = symbol;
         this.type = type;
     }

     public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public double getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    @Override
    public String toString() {
        return
                id +
                        ", order_type=" + order_type +
                        ", lotSize=" + lotSize +
                        ", symbol='" + symbol + currency + '\'' + ", price=" + price +
                        ", stopLoss=" + stopLoss +
                        ", takeProfit=" + takeProfit +
                        ", status='" + status + '\'' +
                        ", fee=" + fee +

                        ", created=" + created +
                        ", updated=" + updated +

                        ", type='" + type + '\'';
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
