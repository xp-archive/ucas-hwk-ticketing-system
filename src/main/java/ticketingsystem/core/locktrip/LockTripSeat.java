package ticketingsystem.core.locktrip;

import ticketingsystem.core.Route;

/**
 * @author xp
 */
@SuppressWarnings("Duplicates")
public class LockTripSeat extends AbstractLockTripSeat {

    public LockTripSeat(Route route, int coachNo, int seatNo, int seatId, int stationNum) {
        super(route, coachNo, seatNo, seatId, stationNum);
    }

    @Override
    protected void lockTrip0(Trip trip) throws InterruptedException {
        trip.lock.lockInterruptibly();
    }

    @Override
    protected void unlockTrip0(Trip trip) {
        trip.lock.unlock();
    }
}
