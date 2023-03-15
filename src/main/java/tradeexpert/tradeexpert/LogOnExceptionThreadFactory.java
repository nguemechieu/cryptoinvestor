package tradeexpert.tradeexpert;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class LogOnExceptionThreadFactory implements ThreadFactory {
    private final AtomicInteger threadIndex = new AtomicInteger(1);
    private final String threadNamePrefix;
    private final int threadPriority;

    /**
     * Creates a LogOnExceptionThreadFactory that has an uncaught
     * exception handler that logs uncaught exceptions.
     *
     * @param threadNamePrefix the prefix of the thread name that will
     *                         show up in logs, etc.
     */
    public LogOnExceptionThreadFactory(String threadNamePrefix) {
        this(threadNamePrefix, Thread.NORM_PRIORITY);
    }

    public LogOnExceptionThreadFactory(String threadNamePrefix, int threadPriority) {
        this.threadNamePrefix = threadNamePrefix;
        this.threadPriority = threadPriority;
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        String threadName = threadNamePrefix + "-Thread-" + threadIndex.getAndIncrement();

        Thread thread = new Thread(runnable, threadName);
        if (threadPriority != Thread.NORM_PRIORITY) {
            thread.setPriority(threadPriority);
        }

        thread.setUncaughtExceptionHandler((t, e) -> Log.error(t.getName() + (e.getMessage() + e)));

        return thread;
    }
}
