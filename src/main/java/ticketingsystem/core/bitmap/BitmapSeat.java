package ticketingsystem.core.bitmap;

import java.util.concurrent.atomic.*;

import ticketingsystem.core.Route;
import ticketingsystem.core.Seat;

/**
 * @author xp
 */
public class BitmapSeat extends Seat {

    // occupied trips bitmap
    private AtomicLong occupied;

    private AtomicLongArray[] activeTicketIds;

//    private AtomicStampedReference<Boolean>[][] counteds;

    public BitmapSeat(Route route, int coachNo, int seatNo, int seatId, int stationNum) {
        super(route, coachNo, seatNo, seatId, stationNum);
        this.occupied = new AtomicLong(0L);
        this.activeTicketIds = new AtomicLongArray[stationNum - 1];
        for (int i = 0; i < stationNum - 1; ++i) {
            this.activeTicketIds[i] = new AtomicLongArray(stationNum);
            for (int j = i + 1; j < stationNum; ++j) {
                this.activeTicketIds[i].set(j, 0L);
            }
        }
        //noinspection unchecked
//        this.counteds = new AtomicStampedReference[tripNum][tripNum];
//        for (int i = 0; i < tripNum; ++i) {
//            for (int j = i; j < tripNum; ++j) {
//                counteds[i][j] = new AtomicStampedReference<>(Boolean.FALSE, 0);
//            }
//        }
    }

    private static boolean isOccupied(long occupied, int firstTripId, int lastTripId) {
        long target = BitUtils.get(firstTripId, lastTripId);
        return (occupied & target) != 0;
    }

    @Override
    public boolean isOccupied(int departureId, int arrivalId) {
        int firstTripId = departureId, lastTripId = arrivalId - 1;
        return isOccupied(occupied.get(), firstTripId, lastTripId);
    }

    @Override
    public long occupy(int departureId, int arrivalId) {
        int firstTripId = departureId, lastTripId = arrivalId - 1;
        if (!casOccupy(firstTripId, lastTripId)) {
            return 0;
        }
        long ticketId = route.maxTicketId.incrementAndGet();
        if (!activeTicketIds[departureId].compareAndSet(arrivalId, 0, ticketId)) {
            throw new RuntimeException("ERR: invalid ticketId");
        }
        return ticketId;
    }

    @Override
    public boolean release(int departureId, int arrivalId, long ticketId) {
        if (!activeTicketIds[departureId].compareAndSet(arrivalId, ticketId, 0)) {
            return false;
        }
        int firstTripId = departureId, lastTripId = arrivalId - 1;
        casRelease(firstTripId, lastTripId);
        return true;
    }

    private boolean casOccupy(int firstTripId, int lastTripId) {
        long target = BitUtils.get(firstTripId, lastTripId);
        while (true) {
            long prev = occupied.get();
            if ((prev & target) != 0) {
                return false;
            }
            long update = prev | target;
            if (occupied.compareAndSet(prev, update)) {
//                int[] stampHolder = new int[1];
//                for (int i = 0; i <= lastTripId; ++i) {
//                    for (int j = Math.max(firstTripId, i); j < tripNum; ++j) {
//                        if (!isOccupied(prev, i, j)) {
//                            AtomicStampedReference<Boolean> counted = counteds[i][j];
//                            while (true) {
//                                Boolean oldCounted = counted.get(stampHolder);
//                                int oldStamp = stampHolder[0];
//                                if (!counted.compareAndSet(oldCounted, Boolean.TRUE, oldStamp, oldStamp + 1)) {
//                                }
//                                if (!oldCounted) {
//                                    int free = route.freeSeats[i].getAndDecrement(j);
//                                    if (free == 0) {
//                                        throw new RuntimeException("ERR");
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                return true;
            }
            Thread.yield();
        }
    }

    private void casRelease(int firstTripId, int lastTripId) {
        long target = BitUtils.get(firstTripId, lastTripId);
        long reversed = ~target;
        while (true) {
            long prev = occupied.get();
            if ((prev & target) == 0) {
                throw new RuntimeException("ERR: invalid occupied");
            }
            long update = prev & reversed;
            if (occupied.compareAndSet(prev, update)) {
//                int[] stampHolder = new int[1];
//                for (int i = 0; i <= lastTripId; ++i) {
//                    for (int j = Math.max(firstTripId, i); j < tripNum; ++j) {
//                        if (!isOccupied(update, i, j)) {
//                            AtomicStampedReference<Boolean> counted = counteds[i][j];
//                            Boolean oldCounted = counted.get(stampHolder);
//                            int oldStamp = stampHolder[0];
//                            if (!counted.compareAndSet(oldCounted, Boolean.FALSE, oldStamp, oldStamp + 1)) {
//                                continue;
//                            }
//                            if (oldCounted) {
//                                int free = route.freeSeats[i].getAndIncrement(j);
//                                if (free == route.totalSeatNum) {
//                                    throw new RuntimeException("ERR");
//                                }
//                            }
//                        }
//                    }
//                }
                return;
            }
            Thread.yield();
        }
    }

    @Override
    public void validate() {
        for (int i = 0; i < stationNum - 1; ++i) {
            for (int j = i + 1; j < stationNum; ++j) {
                if (isOccupied(i, j)) {
                    throw new RuntimeException();
                }
            }
        }
    }
}
