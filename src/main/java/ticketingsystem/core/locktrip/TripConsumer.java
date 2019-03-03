package ticketingsystem.core.locktrip;

/**
 * @author xp
 */
public interface TripConsumer {

    boolean apply(int i, Trip trip) throws InterruptedException;

    void rollback(int i, Trip trip);
}
