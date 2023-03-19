package cryptoinvestor.cryptoinvestor;

public interface LiveOrdersConsumer {
    void consume(LiveOrder liveOrder);
    void close();

}
