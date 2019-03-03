package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * @author xp
 */
public class AtomicTest {

    public static void main(String[] args) throws InterruptedException {
        int nThreads = Runtime.getRuntime().availableProcessors();
        int nTests = 1000000;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        Set<Integer> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        AtomicInteger atomic = new AtomicInteger(0);
        for (int i = 0; i < nThreads; ++i) {
            executor.submit(() -> {
                for (int j = 0; j < nTests; ++j) {
                    set.add(atomic.getAndIncrement());
                }
            });
        }
        executor.shutdown();
        int mins = 0;
        while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            ++mins;
            System.out.println("wait " + mins + "mins");
        }
        if (set.size() < nThreads * nTests) {
            for (int i = nThreads * nTests - 1; i >= 0; --i) {
                if (!set.contains(i)) {
                    throw new RuntimeException(i + " not exists.");
                }
            }
        }
    }
}
