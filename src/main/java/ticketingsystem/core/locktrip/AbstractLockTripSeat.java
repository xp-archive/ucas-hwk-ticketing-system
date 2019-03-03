package ticketingsystem.core.locktrip;

import java.util.*;

import ticketingsystem.core.Route;
import ticketingsystem.core.Seat;

/**
 * @author xp
 */
public abstract class AbstractLockTripSeat extends Seat {

    public final List<Trip> trips;

    public AbstractLockTripSeat(Route route, int coachNo, int seatNo, int seatId, int stationNum) {
        super(route, coachNo, seatNo, seatId, stationNum);
        this.trips = new ArrayList<>(stationNum);
        Trip lastTrip = null;
        for (int i = 0; i < stationNum - 1; ++i) {
            Trip trip = new Trip(route, this, i, i + 1);
            this.trips.add(trip);
            if (lastTrip != null) {
                lastTrip.next = trip;
                trip.prev = lastTrip;
            }
            lastTrip = trip;
        }
    }

    @Override
    public boolean isOccupied(int departureId, int arrivalId) {
        int prevSeq;
        if (departureId == 0) {
            prevSeq = 0;
        } else {
            Trip prev = trips.get(departureId - 1);
            prevSeq = prev.seq.get();
        }
        int lastSeq;
        Trip last = trips.get(arrivalId - 1);
        lastSeq = last.seq.get();
        return prevSeq < lastSeq;
    }

    @Override
    public long occupy(int departureId, int arrivalId) throws InterruptedException {
        Trip first = trips.get(departureId);
        Trip last = trips.get(arrivalId - 1);
        if (!lockTripsBeforeOccupying(first, last)) {
            return 0;
        }
        long ticketId = route.maxTicketId.incrementAndGet();
        unlockTripsAfterOccupied(first, last, ticketId);
        int len = arrivalId - departureId;
        Trip cur = last.next;
        while (cur != null) {
            cur.seq.addAndGet(len);
            cur = cur.next;
        }
//        for (int i = departureId; i < arrivalId; ++i) {
//            for (int j = i + 1; j < arrivalId; ++j) {
//                this.route.freeCountMap[i][j].decrementAndGet();
//            }
//        }
        return ticketId;
    }

    @Override
    public boolean release(int departureId, int arrivalId, final long ticketId) throws InterruptedException {
        Trip first = trips.get(departureId);
        Trip last = trips.get(arrivalId - 1);
        if (first == null || last == null) {
            return false;
        }
        if (!lockTripsBeforeReleasing(first, last, ticketId)) {
            return false;
        }
        unlockTripsAfterReleased(first, last);
        int len = arrivalId - departureId;
        Trip cur = last.next;
        while (cur != null) {
            cur.seq.addAndGet(-len);
            cur = cur.next;
        }
//        for (int i = departureId; i < arrivalId; ++i) {
//            for (int j = i + 1; j < arrivalId; ++j) {
//                this.route.freeCountMap[i][j].incrementAndGet();
//            }
//        }
        return true;
    }

    private boolean walkTrips(Trip from, Trip to, TripConsumer consumer) throws InterruptedException {
        int i = 1;
        Trip cur = from;
        while (true) {
            if (!consumer.apply(i, cur)) {
                while (true) {
                    consumer.rollback(i, cur);
                    if (cur == from) {
                        break;
                    }
                    --i;
                    cur = cur.prev;
                }
                return false;
            }
            if (cur == to) {
                break;
            }
            ++i;
            cur = cur.next;
        }
        return true;
    }

    private boolean fastFail(final Trip from, final Trip to, final long ticketId) throws InterruptedException {
        return walkTrips(from, to, new TripConsumer() {
            @Override
            public boolean apply(int i, Trip trip) {
                return ticketId == trip.ticketId;
            }

            @Override
            public void rollback(int i, Trip trip) {
            }
        });
    }

    private boolean lockTripsBeforeOccupying(final Trip from, final Trip to) throws InterruptedException {
        return fastFail(from, to, 0) && walkTrips(from, to, new TripConsumer() {
            @Override
            public boolean apply(int i, Trip trip) throws InterruptedException {
                lockTrip0(trip);
                return 0 == trip.ticketId;
            }

            @Override
            public void rollback(int i, Trip trip) {
                unlockTrip0(trip);
            }
        });
    }

    private boolean lockTripsBeforeReleasing(final Trip from, final Trip to, final long ticketId) throws InterruptedException {
        return fastFail(from, to, ticketId) && walkTrips(from, to, new TripConsumer() {
            @Override
            public boolean apply(int i, Trip trip) throws InterruptedException {
                lockTrip0(trip);
                if (trip.isHead && trip != from) {
                    return false;
                }
                if (trip.isTail && trip != to) {
                    return false;
                }
                return ticketId == trip.ticketId;
            }

            @Override
            public void rollback(int i, Trip trip) {
                unlockTrip0(trip);
            }
        });
    }

    private void unlockTripsAfterOccupied(final Trip from, final Trip to, final long ticketId) throws InterruptedException {
        walkTrips(from, to, new DefaultTripConsumer() {
            @Override
            public void with(int i, Trip trip) {
                trip.ticketId = ticketId;
                trip.isHead = trip == from;
                trip.isTail = trip == to;
                unlockTrip0(trip);
//                trip.seq.addAndGet(i);
            }
        });
    }

    private void unlockTripsAfterReleased(final Trip from, final Trip to) throws InterruptedException {
        walkTrips(from, to, new DefaultTripConsumer() {
            @Override
            public void with(int i, Trip trip) {
                trip.ticketId = 0;
                unlockTrip0(trip);
//                trip.seq.addAndGet(-i);
            }
        });
    }

    protected abstract void lockTrip0(Trip trip) throws InterruptedException;

    protected abstract void unlockTrip0(Trip trip);

    public void validate() {
        for (Trip trip : trips) {
            trip.validate();
        }
    }
}
