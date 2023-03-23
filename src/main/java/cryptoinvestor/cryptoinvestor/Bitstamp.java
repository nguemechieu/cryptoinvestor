package cryptoinvestor.cryptoinvestor;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Bitstamp extends Exchange {
    public Bitstamp(TradePair tradePair, String bitstamp_key, String bitstamp_secret, String coinbaseSecret) throws TelegramApiException, IOException {

        super(tradePair, bitstamp_key, bitstamp_secret, coinbaseSecret);
        Exchange.tradePair = tradePair;
    }

    @Override
    public String getName() {
        return
                "Bitstamp";
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

    public void createOrder(double price,ENUM_ORDER_TYPE type, Side side, double quantity, double stopLoss, double takeProfit) {
        try {
            String url = "https://api.bitstamp.net/v2/order/new";
            String payload = String.format("{\"pair\": \"%s\", \"type\": \"%s\", \"side\": \"%s\", \"price\": %f, \"quantity\": %f, \"stop_loss\": %f, \"take_profit\": %f}",
                    tradePair.toString('_'),
                    type.toString(),
                    side.toString(),
                    price,
                    quantity,
                    stopLoss,
                    takeProfit);
            sendRequest(url, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelOrder(long orderId) {}

    public void cancelAllOrders() {}


    private void sendRequest(String url, String payload) {

    }
}
