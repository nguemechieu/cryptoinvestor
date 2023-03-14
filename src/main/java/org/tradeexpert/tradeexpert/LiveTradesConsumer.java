package org.tradeexpert.tradeexpert;

import java.io.IOException;
import java.util.List;


public interface LiveTradesConsumer {
    void acceptTrades(List<Trade> trades);

    void onConnectionEstablished();

    void onConnectionFailed();

    void onMessage(String message) throws IOException, InterruptedException;
}
