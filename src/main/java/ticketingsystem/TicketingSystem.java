package ticketingsystem;

class Ticket {
	long tid;
	String passenger;
	int route;
	int coach;
	int seat;
	int departure;
	int arrival;
}


public interface TicketingSystem {
	int inquiry(int route, int departure, int arrival);
	Ticket buyTicket(String passenger, int route, int departure, int arrival);
	boolean refundTicket(Ticket ticket);
}
