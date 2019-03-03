package ticketingsystem.record;

import java.util.concurrent.atomic.*;

public class Record {
    public int recordID;
    public static AtomicInteger idGenerator = new AtomicInteger(0);

    public Operation operation;
    public String passenger;
    public int route;
    public int departure;
    public int arrival;
    public int coach;
    public int seat;
    public long result;

    private Record() {
        this.recordID = idGenerator.getAndIncrement();
    }

    public static void resetRecordID() {
        idGenerator.set(0);
    }

    public static Record successBuy(long tid, String passenger, int route, int departure, int arrival, int coach, int seat) {
        Record record = new Record();
        record.operation = Operation.BUY;
        record.passenger = passenger;
        record.route = route;
        record.departure = departure;
        record.arrival = arrival;
        record.coach = coach;
        record.seat = seat;
        record.result = tid;
        return record;
    }

    public static Record failedBuy(
            String passenger, int route, int departure, int arrival) {
        return successBuy(-1, passenger, route, departure, arrival, -1, -1);
    }

    public static Record inqury(
            int route, int departure, int arrival, long result) {
        Record record = new Record();
        record.operation = Operation.INQUIRY;
        record.route = route;
        record.departure = departure;
        record.arrival = arrival;
        record.result = result;
        record.passenger = null;
        record.coach = -1;
        record.seat = -1;
        return record;
    }

    public static Record successRefund(
            long tid, String passenger, int route, int departure, int arrival, int coach, int seat) {
        Record record = new Record();
        record.operation = Operation.REFUND;
        record.result = tid;
        record.passenger = passenger;
        record.route = route;
        record.departure = departure;
        record.arrival = arrival;
        record.coach = coach;
        record.seat = seat;
        return record;
    }

    public static Record failedRefund(
            long tid, String passenger, int route, int departure, int arrival, int coach, int seat) {
        return successRefund(-1*tid, passenger, route, departure, arrival, coach, seat);
    }

    @Override
    public String toString() {
        switch (operation) {
            case BUY:
                if (result > 0) {
                    return ("TicketBought tid:" + result + " passenger:" + passenger + " route:" + route + " coach:" + coach + " seat:" + seat + " departure" + departure + " arrival" + arrival);
                } else {
                    return ("TicketSoldOut" + " route:" + route+ " departure:" + departure+ " arrival:" + arrival);
                }
            case INQUIRY:
                return ("RemainTicket" + " route:" + route+ " departure:" + departure+ " arrival:" + arrival + " result:" + result);
            case REFUND:
                if (result < 0) {
                    return "ErrOfRefund";
                } else {
                    return ("TicketRefund tid:" + result + " passenger:" + passenger + " route:" + route + " coach:" + coach  + " departure:" + departure + " arrival:" + arrival + " seat:" + seat);
                }

            default:
                return "Error";
        }
    }
}
