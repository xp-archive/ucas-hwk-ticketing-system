package ticketingsystem.core;

/**
 * @author xp
 */
public abstract class Seat {

    public final Route route;

    public final int coachNo;

    public final int seatNo;

    public final int seatId;

    public final int stationNum;

    public final int tripNum;

    public Seat(Route route, int coachNo, int seatNo, int seatId, int stationNum) {
        this.route = route;
        this.coachNo = coachNo;
        this.seatNo = seatNo;
        this.seatId = seatId; // counting from first coach
        this.stationNum = stationNum;
        this.tripNum = stationNum - 1;
    }

    public abstract boolean isOccupied(int departureId, int arrivalId);

    public abstract long occupy(int departureId, int arrivalId) throws InterruptedException;

    public abstract boolean release(int departureId, int arrivalId, long ticketId) throws InterruptedException;

    public abstract void validate();
}
