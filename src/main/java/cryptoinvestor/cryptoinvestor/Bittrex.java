package cryptoinvestor.cryptoinvestor;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Bittrex extends Exchange {
    TradePair tradePair;

    public Bittrex() throws TelegramApiException, IOException {
        super(
                "Bittrex",
                "https://bittrex.com/api/v1.1/public/getmarketsummaries",
                "https://bittrex.com/api/v1.1/public/getcurrencies",
                "https://bittrex.com/api/v1.1/public/getorderbook",
                "https://bittrex.com/api/v1.1/public/getticker",
                "https://bittrex.com/api/v1.1/public/getticker24hr",
                "https://bittrex.com/api/v1.1/public/gettradehistory"
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
    public Bittrex(TradePair tradePair, String ApiKey, String apiSecret, String phraseSecret1) throws TelegramApiException, IOException {
        super(
                "Bittrex",
                "https://bittrex.com/api/v1.1/public/getmarketsummaries",
                "https://bittrex.com/api/v1.1/public/getcurrencies",
                "https://bittrex.com/api/v1.1/public/getorderbook",
                "https://bittrex.com/api/v1.1/public/getticker",
                "https://bittrex.com/api/v1.1/public/getticker24hr",
                "https://bittrex.com/api/v1.1/public/gettradehistory"


        );
        this.apiKey = ApiKey;
        this.apiSecret = apiSecret;
        this.phraseSecret1 = phraseSecret1;
        this.tradePair = tradePair;
    }

    public Bittrex(String s, String s1, String s2) throws TelegramApiException, IOException {
        super(
                "Bittrex",
                "https://bittrex.com/api/v1.1/public/getmarketsummaries",
                "https://bittrex.com/api/v1.1/public/getcurrencies",
                "https://bittrex.com/api/v1.1/public/getorderbook",
                "https://bittrex.com/api/v1.1/public/getticker",
                "https://bittrex.com/api/v1.1/public/getticker24hr",
                "https://bittrex.com/api/v1.1/public/gettradehistory"
        );

    }

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
}
