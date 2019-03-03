package ticketingsystem;

/**
 * @author xp
 */
public class MyTicket extends Ticket {

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getPassenger() {
        return passenger;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public int getRoute() {
        return route;
    }

    public void setRoute(int route) {
        this.route = route;
    }

    public int getCoach() {
        return coach;
    }

    public void setCoach(int coach) {
        this.coach = coach;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getDeparture() {
        return departure;
    }

    public void setDeparture(int departure) {
        this.departure = departure;
    }

    public int getArrival() {
        return arrival;
    }

    public void setArrival(int arrival) {
        this.arrival = arrival;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "tid=" + tid +
                ", route=" + route +
                ", coach=" + coach +
                ", seat=" + seat +
                ", departure=" + departure +
                ", arrival=" + arrival +
                "}";
    }
}
