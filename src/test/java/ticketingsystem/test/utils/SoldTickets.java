package ticketingsystem.test.utils;

import ticketingsystem.ITicket;

import java.util.ArrayList;
import java.util.Random;

public class SoldTickets {
    private static Random rand = new Random();

    private ArrayList<ITicket> tickets;

    public SoldTickets() {
        this.tickets = new ArrayList<>();
    }

    public void add(ITicket ticket) {
        tickets.add(ticket);
    }

    public boolean isEmpty() {
        return tickets.isEmpty();
    }

    public ITicket getOne() {
        int i = rand.nextInt(tickets.size());
        ITicket ticket = tickets.remove(i);
        if (ticket != null) {
            return ticket;
        } else {
            return null;
        }
    }
}
