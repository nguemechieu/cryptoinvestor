package cryptoinvestor.cryptoinvestor;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Binance extends Exchange {

    public Binance(@NotNull TradePair tradePair, String ur, String token, @NotNull String passphrase) throws TelegramApiException, IOException {
        super(tradePair, ur, token, passphrase);
    }

    public Binance(String bittrex, String token, String s, String s1, String s2, String s3, String s4) throws TelegramApiException, IOException {
        super(bittrex, token, s, s1, s2, s3, s4);
    }

    public Binance(String coinbaseApiKey, String coinbaseSecret, String coinbaseSecret1) throws TelegramApiException, IOException {
        super(coinbaseApiKey, coinbaseSecret, coinbaseSecret1);
    }

    @Override
    public String getName() {
        return null;
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
