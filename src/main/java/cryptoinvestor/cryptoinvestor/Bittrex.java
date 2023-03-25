package cryptoinvestor.cryptoinvestor;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Bittrex extends Exchange {
    TradePair tradePair;

    public Bittrex(String apikey) throws TelegramApiException, IOException {
        super(
                null
        );
    }


    //
//     "Bittrex",
//             "https://bittrex.com/api/v1.1/public/getmarketsummaries",
//             "https://bittrex.com/api/v1.1/public/getcurrencies",
//             "https://bittrex.com/api/v1.1/public/getorderbook",
//             "https://bittrex.com/api/v1.1/public/getticker",
//             "https://bittrex.com/api/v1.1/public/getticker24hr",
//             "https://bittrex.com/api/v1.1/public/gettradehistory",

    @Override
    public String getName() {
        return
                "Bittrex";
    }


    @Override
    public CandleDataSupplier getCandleDataSupplier(int secondsPerCandle, TradePair tradePair) {
        return null;
    }

    @Override
    public CompletableFuture<List<Trade>> fetchRecentTradesUntil(TradePair tradePair, Instant stopAt) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<InProgressCandleData>> fetchCandleDataForInProgressCandle(TradePair tradePair, Instant currentCandleStartedAt, long secondsIntoCurrentCandle, int secondsPerCandle) {
        return null;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {

    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    public void createOrder(double price, ENUM_ORDER_TYPE type, Side side, double quantity, double stopLoss, double takeProfit) {
    }

    public void CancelOrder(long orderID) {
        System.out.println(orderID);

    }

    public void createOrder(TradePair tradePair, Side sell, ENUM_ORDER_TYPE market, double quantity, int i, Instant timestamp, long orderID, double stopPrice, double takeProfitPrice) {
    }

    public void closeAll() {
    }

    public void createOrder(TradePair tradePair, Side buy, ENUM_ORDER_TYPE stopLoss, Double quantity, double price, Instant timestamp, long orderID, double stopPrice, double takeProfitPrice) {
    }
}
