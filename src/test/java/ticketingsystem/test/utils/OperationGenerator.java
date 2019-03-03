package ticketingsystem.test.utils;

import ticketingsystem.record.Operation;

import java.util.Random;

public class OperationGenerator {
    private static Random rand = new Random();

    private int buyWeight;
    private int inquiryWeight;
    private int sum;

    public OperationGenerator(int buyWeight, int inquiryWeight, int refundWeight) {
        this.buyWeight = buyWeight;
        this.inquiryWeight = inquiryWeight;
        this.sum = buyWeight + inquiryWeight + refundWeight;
    }

    public Operation next(SoldTickets soldTickets) {
        int op = rand.nextInt(sum);
        if (op < buyWeight) {
            return Operation.BUY;
        } else if (op < inquiryWeight + buyWeight) {
            return Operation.INQUIRY;
        } else {
            if (soldTickets.isEmpty()) {
                return (rand.nextBoolean() ? Operation.INQUIRY : Operation.BUY);
            } else {
                return Operation.REFUND;
            }
        }
    }
}
