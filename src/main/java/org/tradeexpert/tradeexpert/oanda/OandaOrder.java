package org.tradeexpert.tradeexpert.oanda;


import org.tradeexpert.tradeexpert.Order;
import org.tradeexpert.tradeexpert.TRADE_ORDER_TYPE;

public class OandaOrder extends Order {
    private String id;
    private String status;
    private String currency;
    private String amount;
    private String amountInCents;
    private String date;

    public OandaOrder() {
        super(
                "", TRADE_ORDER_TYPE.LIMIT_ORDER,1,0,0.01,0
        );

    }



    @Override
    public String toString() {
        return "OandaOrder{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", currency='" + currency + '\'' +
                ", amount='" + amount + '\'' +
                ", amountInCents='" + amountInCents + '\'' +
                ", date='" + date + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", id=" + id +
                ", order_type=" + order_type +
                ", lotSize=" + lotSize +
                ", price=" + price +
                ", total=" + total +
                ", remaining=" + remaining +
                ", fee=" + fee +
                ", currency='" + currency + '\'' +
                ", created=" + created +
                ", stopLoss=" + stopLoss +
                ", takeProfit=" + takeProfit +
                ", updated=" + updated +
                ", closed=" + closed +
                ", status='" + status + '\'' +
                ", symbol='" + symbol + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountInCents() {
        return amountInCents;
    }

    public void setAmountInCents(String amountInCents) {
        this.amountInCents = amountInCents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (id != null) {
            this.status = "open";
            this.currency = "USD";
            this.amount = "0.00";
            this.amountInCents = "0.00";
            this.date = new java.util.Date().toString();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOrderId(Object o) {
        this.id = o.toString();
    }

    public void setPrice(Object o) {

    }

    public void setSide(Object o) {

    }

    public void setUnits(Object o) {
    }

    public boolean getOrderId() {
        return true;
    }
}
