package ticketingsystem.test.utils;

import java.util.*;

import ticketingsystem.ITicket;
import ticketingsystem.MyTicket;
import ticketingsystem.WrappedTDS;
import ticketingsystem.record.Operation;
import ticketingsystem.record.Record;

public class Execution {

    private static Random rand = new Random();

    private Execution() {}

    public static void oneStep(WrappedTDS tds,
                               OperationGenerator operationGenerator,
                               SoldTickets soldTickets) {
        Operation operation = operationGenerator.next(soldTickets);
        if (operation == Operation.REFUND) {
            ITicket ticketToRefund = soldTickets.getOne();
            tds.refundTicket(ticketToRefund);
        } else {
            Param param = new Param(tds.getRouteNum(), tds.getStationNum());
            if (operation == Operation.BUY) {
                String passenger = getPassengerName();
                ITicket ticket = (ITicket) tds.buyTicket(
                        passenger, param.route, param.departure, param.arrival);
                if (ticket != null) {
                    soldTickets.add(ticket);
                }
            } else {
                tds.inquiry(param.route, param.departure, param.arrival);
            }
        }
    }

    public static Record oneStepRecorded(WrappedTDS tds,
                                         OperationGenerator operationGenerator,
                                         SoldTickets soldTickets) {
        Operation operation = operationGenerator.next(soldTickets);
        if (operation == Operation.REFUND) {
            ITicket ticketToRefund = soldTickets.getOne();
            return getRefundRecord(ticketToRefund, tds.refundTicket(ticketToRefund));
        } else {
            Param param = new Param(tds.getRouteNum(), tds.getStationNum());
            if (operation == Operation.BUY) {
                String passenger = getPassengerName();
                MyTicket ticket1 = tds.buyTicket(
                        passenger, param.route, param.departure, param.arrival);
                if (ticket1 != null) {
                    ITicket ticket = new ITicket(ticket1.getTid(), ticket1.getPassenger(),
                            ticket1.getRoute(), ticket1.getCoach(), ticket1.getSeat(),
                            ticket1.getDeparture(), ticket1.getArrival());
                    soldTickets.add(ticket);
                    return getSuccessBuyRecord(ticket);
                } else {
                    return getFailedBuyRecord(passenger, param.route, param.departure, param.arrival);
                }
            } else {
                int result = tds.inquiry(param.route, param.departure, param.arrival);
                return getInquiryRecord(param.route, param.departure, param.arrival, result);
            }
        }
    }

    private static Record getRefundRecord(ITicket ticket, boolean success) {
        if (ticket == null) {
            return null;
        }

        long tid = ticket.getTid();
        String passenger = ticket.getPassenger();
        int route = ticket.getRoute();
        int coach = ticket.getCoach();
        int seat = ticket.getSeat();
        int departure = ticket.getDeparture();
        int arrival = ticket.getArrival();

        if (success) {
            return Record.successRefund(tid, passenger, route, departure, arrival, coach, seat);
        } else {
            return Record.failedRefund(tid, passenger, route, departure, arrival, coach, seat);
        }
    }

    private static Record getSuccessBuyRecord(ITicket ticket) {
        long tid = ticket.getTid();
        String passenger = ticket.getPassenger();
        int route = ticket.getRoute();
        int coach = ticket.getCoach();
        int seat = ticket.getSeat();
        int departure = ticket.getDeparture();
        int arrival = ticket.getArrival();

        return Record.successBuy(tid, passenger, route, departure, arrival, coach, seat);
    }

    private static Record getFailedBuyRecord(String passenger, int route, int departure, int arrival) {
        return Record.failedBuy(passenger, route, departure, arrival);
    }

    private static Record getInquiryRecord(int route, int departure, int arrival, int result) {
        return Record.inqury(route, departure, arrival, result);
    }

    private static String getPassengerName() {
        long uid = rand.nextInt(100000);
        return "passenger" + uid;
    }
}
