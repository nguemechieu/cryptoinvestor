package cryptoinvestor.cryptoinvestor.Coinbase;

import cryptoinvestor.cryptoinvestor.LiveTradesConsumer;
import cryptoinvestor.cryptoinvestor.Trade;
import cryptoinvestor.cryptoinvestor.TradePair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
public class LiveTradeConsumer implements LiveTradesConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LiveTradeConsumer.class);
    public LiveTradeConsumer(@NotNull TradePair tradePair) {

        logger.info("LiveTradeConsumer " + tradePair);


    }

    @Override
    public void acceptTrades(@NotNull List<Trade> trades) {

        for (Trade trade : trades) {
            System.out.println(trade);


        }


    }

    @Override
    public void onConnectionEstablished()  {
        System.out.println("Connection established");

    }

    @Override
    public void onConnectionFailed() {
        System.out.println("Connection failed");


    }

    @Override
    public void onMessage(String message) throws IOException, InterruptedException {
        logger.info(message);

    }

    @Override
    public void accept(@NotNull Trade trade) {
        System.out.println(trade);
        logger.info(trade.toString());


    }
}
