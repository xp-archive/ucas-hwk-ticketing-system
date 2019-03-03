package ticketingsystem.record;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadID {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadID = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            return idGenerator.getAndIncrement();
        }
    };

    public static int get() {
        return threadID.get();
    }

}
