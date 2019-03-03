package ticketingsystem.core;

import java.util.*;
import java.util.concurrent.atomic.*;

import ticketingsystem.core.bitmap.BitmapSeat;

/**
 * @author xp
 */
public class Route {

    public final int routeNo;

    public final int coachNum;

    public final int seatNum;

    public final int totalSeatNum;

    public final int stationNum;

    public final int tripNum;

    private final List<Seat> seats;

    public final AtomicIntegerArray[] freeSeats;

    public final AtomicLong maxTicketId;

    public Route(int routeNo, int coachNum, int seatNum, int stationNum, AtomicLong maxTicketId) {
        this.routeNo = routeNo;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.totalSeatNum = coachNum * seatNum;
        this.stationNum = stationNum;
        this.tripNum = stationNum - 1;

        this.seats = new ArrayList<>(totalSeatNum);
        int pos = 0;
        for (int i = 0; i < coachNum; ++i) {
            for (int j = 0; j < seatNum; ++j) {
                Seat seat = new BitmapSeat(this, i + 1, j + 1, pos, stationNum);
                seats.add(seat);
                pos++;
            }
        }

        this.freeSeats = new AtomicIntegerArray[tripNum];
        for (int i = 0; i < tripNum; ++i) {
            AtomicIntegerArray atomic = new AtomicIntegerArray(tripNum);
            this.freeSeats[i] = atomic;
            for (int j = i; j < tripNum; ++j) {
                atomic.set(j, totalSeatNum);
            }
        }

        this.maxTicketId = maxTicketId;
    }

    public Seat getSeat(int i) {
        return seats.get(i);
    }

    public int getFreeSeats(int departureId, int arrivalId) {
        int firstTripId = departureId, lastTripId = arrivalId - 1;
//        return freeSeats[firstTripId].get(lastTripId);
        int free = 0;
        for (int i = 0; i < totalSeatNum; ++i) {
            Seat seat = getSeat(i);
            if (!seat.isOccupied(departureId, arrivalId)) {
                free++;
            }
        }
        return free;
    }

    public SeatTicketPair buy(int departureId, int arrivalId) throws InterruptedException {
        for (int i = 0; i < totalSeatNum; ++i) {
            Seat seat = getSeat(i);
            SeatTicketPair pair = occupySeat(seat, departureId, arrivalId);
            if (pair != null) {
                return pair;
            }
        }
        return null;
    }

    public SeatTicketPair occupySeat(Seat seat, int departureId, int arrivalId) throws InterruptedException {
        long ticketId = seat.occupy(departureId, arrivalId);
        if (ticketId == 0) {
            return null;
        }
        SeatTicketPair pair = new SeatTicketPair();
        pair.seat = seat;
        pair.ticketId = ticketId;
        return pair;
    }

    public boolean refund(int seatId, int departureId, int arrivalId, long ticketId) throws InterruptedException {
        Seat seat = getSeat(seatId);
        return seat.release(departureId, arrivalId, ticketId);
    }

    public void validate() {
        for (Seat seat : seats) {
            seat.validate();
        }
        for (int i = 0; i < stationNum - 1; ++i) {
            for (int j = i + 1; j < stationNum; ++j) {
                int freeSeats = getFreeSeats(i, j);
                if (freeSeats != totalSeatNum) {
                    throw new RuntimeException("free=" + freeSeats);
                }
            }
        }
    }
}
