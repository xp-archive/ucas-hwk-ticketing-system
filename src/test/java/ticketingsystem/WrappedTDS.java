package ticketingsystem;

import ticketingsystem.core.Route;
import ticketingsystem.core.locktrip.AbstractLockTripSeat;
import ticketingsystem.record.SystemStatus;

/**
 * @author xp
 */
public class WrappedTDS extends TicketingDS {

    public WrappedTDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        super(routeNum, coachNum, seatNum, stationNum, threadNum);
    }

    public int getRouteNum() {
        return routeNum;
    }

    public int getCoachNum() {
        return coachNum;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public int getStationNum() {
        return stationNum;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus(routeNum, coachNum, seatNum, stationNum);
        for (int i = 1; i <= routeNum; ++i) {
            Route route = routes.get(i - 1);
            for (int j = 1; j <= coachNum; ++j) {
                for (int k = 1; k <= seatNum; ++k) {
                    AbstractLockTripSeat seat = (AbstractLockTripSeat) route.getSeat((j - 1) * seatNum + k - 1);
                    for (int l = 1; l < stationNum; ++l) {
                        long ticketId = seat.trips.get(l - 1).ticketId;
                        status.set(i, j, k, l, ticketId);
                    }
                }
            }
        }
        return status;
    }
}
