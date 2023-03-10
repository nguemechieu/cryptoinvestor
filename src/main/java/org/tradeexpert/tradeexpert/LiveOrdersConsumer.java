package org.tradeexpert.tradeexpert;

public interface LiveOrdersConsumer {
    void consume(LiveOrder liveOrder);
    void close();

}
