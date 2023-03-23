package cryptoinvestor.cryptoinvestor;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Poloniex extends Exchange {
    String host = "https://poloniex.com";
    String url = "https://poloniex.com/public?command=returnTicker";

    public Poloniex(TradePair tradePair, String telegramApiKey, String POLONIEX_API_KEY, String coinbaseSecret) throws TelegramApiException, IOException, ParseException, InterruptedException {
        super(
                tradePair, telegramApiKey,
                POLONIEX_API_KEY,

                coinbaseSecret);
    }

    @Override
    public String getName() {
        return
                "Poloniex";
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
}
