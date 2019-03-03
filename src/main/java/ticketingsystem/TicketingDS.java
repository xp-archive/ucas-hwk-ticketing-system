package ticketingsystem;

import java.util.*;
import java.util.concurrent.atomic.*;

import ticketingsystem.core.Route;
import ticketingsystem.core.Seat;
import ticketingsystem.core.SeatTicketPair;

public class TicketingDS implements TicketingSystem {

    private AtomicLong maxTicketId = new AtomicLong(0);

    public final int routeNum;

    public final int coachNum;

    public final int seatNum;

    public final int stationNum;

    public final int threadNum;

    public final List<Route> routes;

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
//        this.maxTicketId2 = new ThreadLocal<long[]>() {
//            @Override
//            protected long[] initialValue() {
//                long base = ThreadId.get() * 100000000000L;
//                return new long[]{base};
//            }
//        };

        this.routes = new ArrayList<>(routeNum);
        for (int i = 0; i < routeNum; ++i) {
            Route route = new Route(i + 1, coachNum, seatNum, stationNum, maxTicketId);
            routes.add(route);
        }
    }

    @Override
    public int inquiry(int routeNo, int departureNo, int arrivalNo) {
        Route route = routes.get(routeNo - 1);
        int departureId = departureNo - 1;
        int arrivalId = arrivalNo - 1;
        return route.getFreeSeats(departureId, arrivalId);
    }

    @Override
    public MyTicket buyTicket(String passenger, int routeNo, int departureNo, int arrivalNo) {
        try {
            Route route = routes.get(routeNo - 1);
            int departureId = departureNo - 1;
            int arrivalId = arrivalNo - 1;
            SeatTicketPair pair = route.buy(departureId, arrivalId);
            if (pair == null) {
                return null;
            }
            MyTicket ticket = new MyTicket();
            ticket.tid = pair.ticketId;
            ticket.passenger = passenger;
            ticket.route = routeNo;
            ticket.coach = pair.seat.coachNo;
            ticket.seat = pair.seat.seatNo;
            ticket.departure = departureNo;
            ticket.arrival = arrivalNo;
            return ticket;
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        try {
            if (ticket == null) {
                return false;
            }
            Route route = routes.get(ticket.route - 1);
            int departureId = ticket.departure - 1;
            int arrivalId = ticket.arrival - 1;
            int seatId = (ticket.coach - 1) * seatNum + ticket.seat - 1;
            return route != null && route.refund(seatId, departureId, arrivalId, ticket.tid);
        } catch (InterruptedException e) {
            return false;
        }
    }

    // for validate

    public long rebuyTicket(int routeNo, int coachNo, int seatNo, int departureNo, int arrivalNo) {
        try {
            Route route = routes.get(routeNo - 1);
            int departureId = departureNo - 1;
            int arrivalId = arrivalNo - 1;
            int seatId = (coachNo - 1) * seatNum + seatNo - 1;
            Seat seat = route.getSeat(seatId);
            SeatTicketPair pair = route.occupySeat(seat, departureId, arrivalId);
            return pair == null ? 0 : pair.ticketId;
        } catch (InterruptedException e) {
            return 0;
        }
    }

    public void validate() {
        for (Route route : routes) {
            route.validate();
        }
    }
}
