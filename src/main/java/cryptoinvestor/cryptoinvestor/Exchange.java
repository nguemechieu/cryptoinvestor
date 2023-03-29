package cryptoinvestor.cryptoinvestor;

import cryptoinvestor.cryptoinvestor.BinanceUs.BinanceUs;
import cryptoinvestor.cryptoinvestor.oanda.POSITION_FILL;
import javafx.scene.control.ListView;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class Exchange {
    protected  ExchangeWebSocketClient webSocketClient;

    protected Exchange(ExchangeWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }



    /**
     * @return this exchange's {@code ExchangeWebSocketClient} instance, which is responsible for grabbing
     * live-streaming data (such as trades, orders, etc).
     */
    public ExchangeWebSocketClient getWebsocketClient() {
        return webSocketClient;
    }


    public abstract Set<Integer> getSupportedGranularities() ;
    /**
     * Fetches the recent trades for the given trade pair from  {@code stopAt} till now (the current time).
     * <p>
     * This method only needs to be implemented to support live syncing.
     */
    public abstract CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt);

    public abstract String getName();

    /**
     * Returns the {@code CandleDataSupplier} implementation that will be used to provide pages of candle data for the
     * given {@code secondsPerCandle} and {@code tradePair}.
     */
    public abstract CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair);

    /**
     * Fetches completed candles (of smaller duration than the current {@code secondsPerCandle}) in the duration of
     * the current live-syncing candle.
     * <p>
     * TThis method only needs to be implemented to support live syncing.
     */
    public abstract CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle();


    public abstract CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(
            TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle);

    public abstract void onOpen(ServerHandshake handshake);

    public abstract void onMessage(String message);

    public abstract void onClose(int code, String reason, boolean remote);

    public abstract void onError(Exception ex);

    public abstract String getSymbol();

    public abstract String getPrice();

    public abstract String getVolume();

    public abstract String getOpen();

    public abstract String getHigh();

    public abstract String getLow();

    public abstract String getClose();

    public abstract String getTimestamp();

    public abstract String getTradeId();

    public abstract String getOrderId();

    public abstract String getTradeType();

    public abstract String getSide();

    public abstract String getExchange();

    public abstract String getCurrency();

    public abstract String getAmount();

    public abstract String getFee();

    public abstract String getAvailable();

    public abstract String getBalance();

    public abstract String getPending();

    public abstract String getTotal();

    public abstract String getDeposit();

    public abstract String getWithdraw();

    public abstract void deposit(Double value);

    public abstract void withdraw(Double value);

    public abstract @NotNull List<Currency> getAvailableSymbols() throws IOException, InterruptedException;

    public abstract void  createOrder(TradePair tradePair, POSITION_FILL defaultFill, double price, ENUM_ORDER_TYPE market, Side buy, double quantity, double stopPrice, double takeProfitPrice) throws IOException, InterruptedException;

    public abstract void closeAllOrders() throws IOException, InterruptedException;
    public abstract  List<TradePair> getTradePair() throws IOException, InterruptedException;

    public abstract void cancelOrder(long orderID) throws IOException, InterruptedException;
    public abstract void cancelAllOrders();
    public abstract void cancelAllOpenOrders();


    public abstract ListView<Order> getOrderView() throws IOException, InterruptedException;
    public abstract List<Objects> getOrderBook();

}