package cryptoinvestor.cryptoinvestor.Coinbase;

import cryptoinvestor.cryptoinvestor.LiveTradesConsumer;
import cryptoinvestor.cryptoinvestor.Trade;
import cryptoinvestor.cryptoinvestor.TradePair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class LiveTradeConsumer implements LiveTradesConsumer {
    public LiveTradeConsumer(@NotNull TradePair tradePair) {

        System.out.println(tradePair);

    }

    @Override
    public void acceptTrades(List<Trade> trades) {



    }

    @Override
    public void onConnectionEstablished()  {

    }

    @Override
    public void onConnectionFailed()  {
        System.out.println("Connection failed");


    }

    @Override
    public void onMessage(String message) throws IOException, InterruptedException {

    }
}
