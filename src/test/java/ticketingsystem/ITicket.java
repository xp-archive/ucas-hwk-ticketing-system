package ticketingsystem;

public class ITicket extends MyTicket {

    public ITicket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
        super();
        this.tid = tid;
        this.passenger = passenger;
        this.route = route;
        this.coach = coach;
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
    }

    public ITicket() {
        super();
    }
}
