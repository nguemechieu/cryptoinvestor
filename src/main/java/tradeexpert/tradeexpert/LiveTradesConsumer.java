package tradeexpert.tradeexpert;

import java.io.IOException;
import java.util.List;


public interface LiveTradesConsumer {
    void acceptTrades(List<Trade> trades);

    void onConnectionEstablished() throws IOException, InterruptedException;

    void onConnectionFailed();

    void onMessage(String message) throws IOException, InterruptedException;
}
