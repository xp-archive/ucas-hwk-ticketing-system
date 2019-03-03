package ticketingsystem.core.locktrip;

/**
 * @author xp
 */
public abstract class DefaultTripConsumer implements TripConsumer {

    @Override
    public boolean apply(int i, Trip trip) throws InterruptedException {
        with(i, trip);
        return true;
    }

    protected abstract void with(int i, Trip trip);

    @Override
    public void rollback(int i, Trip trip) {
    }
}
