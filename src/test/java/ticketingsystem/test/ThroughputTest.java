package ticketingsystem.test;

import ticketingsystem.Console;
import ticketingsystem.WrappedTDS;
import ticketingsystem.test.utils.Execution;
import ticketingsystem.test.utils.FormattedNumber;
import ticketingsystem.test.utils.OperationGenerator;
import ticketingsystem.test.utils.SoldTickets;

public class ThroughputTest {
    private static final int routeNum = Config.Throughput.routeNum;
    private static final int coachNum = Config.Throughput.coachNum;
    private static final int seatNum = Config.Throughput.seatNum;
    private static final int stationNum = Config.Throughput.stationNum;
    private static final OperationGenerator operationGenerator = new OperationGenerator(
            Config.Throughput.operationWeight.buy,
            Config.Throughput.operationWeight.inquiry,
            Config.Throughput.operationWeight.refund
    );

    public static void multiThreadNum() throws InterruptedException {
        final int operationNum = Config.Throughput.MultiThreadNum.operationNum;
        final int round = Config.Throughput.MultiThreadNum.round;
        final int[] threadNums = Config.Throughput.MultiThreadNum.threadNums;

        Console.log("Throughput With Different Thread Number");
        for (int threadNum: threadNums) {
            double sum = 0.0;
            for (int t = 0; t != round; ++t) {
                sum += calculateThroughput(threadNum, operationNum);
            }
            double throughput = sum / (double)round;
            Console.log("    - " + threadNum + " threads: " + FormattedNumber.get(throughput) + "k/s.");
        }
    }

    public static void multiOpNumber() throws InterruptedException {
        Console.log("Throughput With Different Operation Number");
        int threadNum = Config.Throughput.MultiOpNum.threadNum;
        int testRound = Config.Throughput.MultiOpNum.round;
        for (int opNum: Config.Throughput.MultiOpNum.operationNums) {
            double sum = 0.0;
            for (int t = 0; t != testRound; ++t) {
                sum += calculateThroughput(threadNum, opNum);
            }
            double throughput = sum / (double)testRound;
            Console.log("    - " + opNum + " operations: " + FormattedNumber.get(throughput) + "k/s.");
        }
    }

    public static double tdsThroughput(WrappedTDS tds, int operationNum, int round) throws InterruptedException {
        double sum = 0.0;
        for (int t = 0; t != round; ++t) {
            sum += tdsCalculateThroughput(tds, operationNum);
        }
        return sum / round;
    }

    private static double calculateThroughput(
            final int threadNum, final int operationNum) throws InterruptedException {
        final WrappedTDS tds = new WrappedTDS(routeNum, coachNum, seatNum, stationNum, threadNum);
        return tdsCalculateThroughput(tds, operationNum);
    }

    private static double tdsCalculateThroughput(
            final WrappedTDS tds, final int operationNum) throws InterruptedException {
        int threadNum = tds.getThreadNum();

        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i != threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                SoldTickets soldTickets = new SoldTickets();

                @Override
                public void run() {
                    for (int t = 0; t != operationNum; ++t) {
                        Execution.oneStep(tds, operationGenerator, soldTickets);
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i != threadNum; ++i) {
            threads[i].join();
        }

        long endTime = System.currentTimeMillis();

        double deltaTime = ((double)(endTime-startTime))/1000.0;
        return ((double)threadNum)*operationNum/deltaTime/1000.0;
    }

}
