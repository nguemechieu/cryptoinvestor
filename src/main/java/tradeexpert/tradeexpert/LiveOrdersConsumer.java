package tradeexpert.tradeexpert;

public interface LiveOrdersConsumer {
    void consume(LiveOrder liveOrder);
    void close();

}
