package tradeexpert.tradeexpert;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;


public interface LiveTradesConsumer {
    void acceptTrades(List<Trade> trades);

    void onConnectionEstablished() throws IOException, InterruptedException, ParseException;

    void onConnectionFailed();

    void onMessage(String message) throws IOException, InterruptedException;
}
