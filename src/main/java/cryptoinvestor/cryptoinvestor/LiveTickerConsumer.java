package cryptoinvestor.cryptoinvestor;

public interface LiveTickerConsumer {
    void onTicker(LiveTicker liveTicker);

    void onError(Throwable throwable);

    void onComplete();

    void cancel();

    boolean isCancelled();

    boolean isDone();

    boolean isSuccess();

    boolean isFailed();

    boolean isRunning();

    boolean isStarted();

    boolean isStopped();
}
