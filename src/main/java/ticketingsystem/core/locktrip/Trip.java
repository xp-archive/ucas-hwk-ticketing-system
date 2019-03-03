package ticketingsystem.core.locktrip;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import ticketingsystem.core.Route;
import ticketingsystem.core.Seat;

/**
 * @author xp
 */
public class Trip {

    public final Route route;

    public final Seat seat;

    public final int startId;

    public final int endId;

    public Trip prev;

    public Trip next;

    public final Lock lock = new ReentrantLock();

    public volatile long ticketId = 0;

    public volatile boolean isHead = false;

    public volatile boolean isTail = false;

    public AtomicInteger seq = new AtomicInteger(0);

    public Trip(Route route, Seat seat, int startId, int endId) {
        this.route = route;
        this.seat = seat;
        this.startId = startId;
        this.endId = endId;
    }

    public void validate() {
        if (ticketId != 0) {
            throw new RuntimeException("ticketId=" + ticketId);
        }
        if (seq.get() != 0) {
            throw new RuntimeException("seq=" + seq.get());
        }
    }
}
