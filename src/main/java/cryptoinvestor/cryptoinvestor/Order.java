package cryptoinvestor.cryptoinvestor;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

public class Order extends RecursiveTreeObject<Order> {

    String orderListId;
    private String time;
    private String clientOrderId;
    private String selfTradePreventionMode;
    private String updateTime;
    private String origQuoteOrderQty;
    private String isWorking;
    private String stopPrice;
    private String orderId;

    public Order(String price, String timeInForce, String symbol, String orderId, String orderListId, String clientOrderId, String origQty, String executedQty, String cummulativeQuoteQty, String status, String type, String side, String stopPrice, String icebergQty, String time, String updateTime, String isWorking, String origQuoteOrderQty, String selfTradePreventionMode) {

        this.price = price;
        this.timeInForce = timeInForce;
        this.symbol = symbol;
        this.orderId = orderId;
        this.orderListId = orderListId;
        this.clientOrderId = clientOrderId;
        this.origQty = origQty;
        this.executedQty = executedQty;
        this.cummulativeQuoteQty = cummulativeQuoteQty;
        this.status = status;
        this.type = ENUM_ORDER_TYPE.valueOf(type);
        this.side = Side.valueOf(side);
        this.stopPrice = stopPrice;
        this.icebergQty = icebergQty;
        this.time = time;
        this.updateTime = updateTime;
        this.isWorking = isWorking;
        this.origQuoteOrderQty = origQuoteOrderQty;
        this.selfTradePreventionMode = selfTradePreventionMode;
    }

    public Order(Long id, @NotNull TradePair tradePair, String timestamp, ENUM_ORDER_TYPE order_type, Side side, double remaining, double fee, double lotSize, double price

            , double stopLoss, double takeProfit
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.order_type = order_type;
        this.remaining = remaining;
        this.fee = fee;
        this.lotSize = lotSize;
        this.price = String.valueOf(price);
        this.stopLoss = stopLoss;
        this.symbol = tradePair.getCounterCurrency().symbol;
        this.type = order_type;

        this.currency = tradePair.getCounterCurrency().symbol;
        this.created = String.valueOf(new Date());
        this.takeProfit = takeProfit;
        this.updated = new Date();
        this.side = side;
        this.tradePair = tradePair;


    }

    public Order(String price, String timeInForce, String symbol, String orderId, String orderListId, String clientOrderId, String origQty, String executedQty, String cummulativeQuoteQty, String status, String type, String side, String stopPrice, String icebergQty, String updateTime, String isWorking, String origQuoteOrderQty, String selfTradePreventionMode) {
        this.price = price;
        this.timeInForce = timeInForce;
        this.symbol = symbol;
        this.orderId = orderId;
        this.orderListId = orderListId;
        this.clientOrderId = clientOrderId;
        this.origQty = origQty;
        this.executedQty = executedQty;
        this.cummulativeQuoteQty = cummulativeQuoteQty;
        this.status = status;
        this.type = ENUM_ORDER_TYPE.valueOf(type);
        this.side = Side.valueOf(side);
        this.stopPrice = stopPrice;
        this.icebergQty = icebergQty;
        this.updateTime = updateTime;
        this.isWorking = isWorking;
        this.origQuoteOrderQty = origQuoteOrderQty;
        this.selfTradePreventionMode = selfTradePreventionMode;
    }

    public Order(@NotNull JSONArray orders) {
        this.orders = orders;
        for (int i = 0; i < orders.length(); i++) {
            try {
                JSONObject order = orders.getJSONObject(i);
                this.orders.put(order);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getSelfTradePreventionMode() {
        return selfTradePreventionMode;
    }

    public void setSelfTradePreventionMode(String selfTradePreventionMode) {
        this.selfTradePreventionMode = selfTradePreventionMode;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getOrigQuoteOrderQty() {
        return origQuoteOrderQty;
    }

    public void setOrigQuoteOrderQty(String origQuoteOrderQty) {
        this.origQuoteOrderQty = origQuoteOrderQty;
    }

    public String getIsWorking() {
        return isWorking;
    }

    public void setIsWorking(String isWorking) {
        this.isWorking = isWorking;
    }

    public String getOrderListId() {
        return orderListId;
    }

    public void setOrderListId(String orderListId) {
        this.orderListId = orderListId;
    }

    public void setStopPrice(String stopPrice) {
        this.stopPrice = stopPrice;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    protected String created;
    String clientTradeID1;
    String triggerCondition, createTime,//": "2023-03-30T20:00:00.583871679Z",
            price,//": "1.08063",
            clientTradeID,//": "140930333",

    state,//": "PENDING",

    timeInForce,//": "GTC",
            tradeID;//": "143118"
    private String executedQty;
    private String origQty;
    private String cummulativeQuoteQty;
    private String icebergQty;
    private TradePair tradePair;
    private String timestamp;
    private ENUM_ORDER_TYPE order_type;
    private double remaining;
    private double fee;
    private double lotSize;
    private double stopLoss;
    private String symbol;
    private ENUM_ORDER_TYPE type;
    private JSONArray orders;
    public Order(String clientTradeID, String triggerCondition, String createTime, String price, String clientTradeID1, String state, String timeInForce, String tradeID) {
        this.clientTradeID = clientTradeID;
        this.triggerCondition = triggerCondition;
        this.createTime = createTime;
        this.price = price;
        this.clientTradeID1 = clientTradeID1;
        this.state = state;
        this.timeInForce = timeInForce;
        this.tradeID = tradeID;
    }

    public String getClientTradeID1() {
        return clientTradeID1;
    }

    public void setClientTradeID1(String clientTradeID1) {
        this.clientTradeID1 = clientTradeID1;
    }

    @Override
    public String toString() {
        return "Order " +
                "clientTradeID1='" + clientTradeID1 + '\'' + ", id=" + id +
                ", triggerCondition='" + triggerCondition + '\'' +
                ", createTime='" + createTime + '\'' +
                ", price='" + price + '\'' + ", status='" + status + '\'' +
                ", clientTradeID='" + clientTradeID + '\'' +
                ", state='" + state + '\'' +
                ", timeInForce='" + timeInForce + '\'' +
                ", tradeID='" + tradeID + '\'' +
                ", order_type=" + order_type +
                ", takeProfit=" + takeProfit +
                ", stopLoss=" + stopLoss +
                ", lotSize=" + lotSize +
                ", type=" + type +
                ", orderID=" + orderID +
                ", ticket=" + ticket +
                ", created='" + created + '\'' + ", updated=" + updated +
                ", closed=" + closed +

                ", side=" + side +
                ", filled='" + filled + '\'' +
                ", unit='" + unit + '\'' +
                ", orders=" + orders;
    }

    public String getExecutedQty() {
        return executedQty;
    }

    public void setExecutedQty(String executedQty) {
        this.executedQty = executedQty;
    }

    private static int lastError;
    private int orderID;

    int ticket=orderID;
    protected Long id;
    protected double total;
    protected String currency;

    public String getOrigQty() {
        return origQty;
    }
    protected double takeProfit;
    protected Date updated;
    protected Date closed;
    protected String status;
    private Side side;
    private String filled;
    private String unit;

    public void setOrigQty(String origQty) {
        this.origQty = origQty;
    }


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

    public String getCummulativeQuoteQty() {
        return cummulativeQuoteQty;
    }

    public void setCummulativeQuoteQty(String cummulativeQuoteQty) {
        this.cummulativeQuoteQty = cummulativeQuoteQty;
    }

    public JSONArray getOrders() {
        return orders;
    }

    public ENUM_ORDER_TYPE getOrder_type() {
        return order_type;
    }

    public void setOrder_type(ENUM_ORDER_TYPE order_type) {
        this.order_type = order_type;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        this.lotSize = lotSize;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setOrders(JSONArray orders) {
        this.orders = orders;
    }

    public String getOpenTime() {
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
        return Double.parseDouble(price);
    }

    public  String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public ENUM_ORDER_TYPE getType() {
        return type;
    }

    public void setType(ENUM_ORDER_TYPE type) {
        this.type = type;
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

    public double getPrice() {
        return Double.parseDouble(price);
    }

    public void setPrice(double price) {
        this.price = String.valueOf(price);
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

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCreated() {
        return created;
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

    public void setCreated(String created) {
        this.created = created;
    }

    public String getOrderId() {
        return String.valueOf(orderID);
    }

    public String getFilled() {
        return filled;
    }

    public String getUnit() {
        return unit;
    }

    public String getTimeInForce() {
        return "GTC";
    }

    public String getTime() {
        return timestamp;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getClientTradeID() {
        return clientTradeID;
    }

    public void setClientTradeID(String clientTradeID) {
        this.clientTradeID = clientTradeID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }

    public String getStopPrice() {
        return String.valueOf(stopLoss);
    }

    public void setStopPrice(double stopLoss) {
        this.stopLoss = stopLoss;
    }


    public String getIcebergQty() {
        return icebergQty;
    }

    public void setIcebergQty(String icebergQty) {
        this.icebergQty = icebergQty;
    }
}
