package ticketingsystem;

import java.util.*;

/**
 * @author xp
 */
public class UnitTest {

    public static void main(String[] args) {
        final TicketingDS tds = new TicketingDS(2, 2, 3, 7, 1);
        List<Ticket> tickets = new LinkedList<>();
        Ticket ticket, ticket1;

        assert 7 == tds.inquiry(1, 1, 7);
        assert 7 == tds.inquiry(2, 1, 7);

        assert 7 == tds.inquiry(1, 2, 4);
        assert 7 == tds.inquiry(1, 4, 6);

        ticket = tds.buyTicket(null, 1, 2, 5);
        assert null != ticket;
        tickets.add(ticket);

        assert 7 == tds.inquiry(1, 1, 2);
        assert 6 == tds.inquiry(1, 1, 4);
        assert 6 == tds.inquiry(1, 2, 3);
        assert 6 == tds.inquiry(1, 2, 5);
        assert 6 == tds.inquiry(1, 3, 4);
        assert 6 == tds.inquiry(1, 4, 6);
        assert 7 == tds.inquiry(1, 5, 6);
        assert 7 == tds.inquiry(1, 6, 7);

        for (int i = 7; i >= 0; --i) {
            ticket = tds.buyTicket(null, 2, 3, 5);
            assert null != ticket;
            tickets.add(ticket);
        }
        assert 0 == tds.inquiry(2, 1, 6);
        assert 7 == tds.inquiry(2, 2, 3);
        assert 7 == tds.inquiry(2, 6, 7);

        ticket = tds.buyTicket(null, 2, 1, 6);
        assert null == ticket;

        ticket = tickets.get(0);
        assert tds.refundTicket(ticket);
        assert !tds.refundTicket(ticket);

        tickets.remove(ticket);
        ticket = tickets.iterator().next();

        ticket1 = new Ticket();
        ticket1.tid = ticket.tid;
        ticket1.route = ticket.route;
        ticket1.coach = ticket.coach;
        ticket1.seat = ticket.seat;
        ticket1.departure = ticket.departure;
        ticket1.arrival = ticket.arrival - 1;
        assert !tds.refundTicket(ticket1);

        assert tds.refundTicket(ticket);
        tickets.remove(ticket);
        ticket1 = tds.buyTicket(null, ticket.route, ticket.departure, ticket.arrival);
        assert null != ticket1;
        assert ticket1.tid != ticket.tid;

        for (Ticket ticket2 : tickets) {
            assert tds.refundTicket(ticket2);
        }

        assert 7 == tds.inquiry(2, 1, 7);

        System.out.println("fine");
    }
}
