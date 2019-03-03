package ticketingsystem.test;

import ticketingsystem.Console;
import ticketingsystem.WrappedTDS;
import ticketingsystem.test.utils.FormattedNumber;

public class TDSCompare {
    public static final boolean enable = Config.TDSCompare.enable;

    private static final int routeNum = Config.TDSCompare.routeNum;
    private static final int coachNum = Config.TDSCompare.coachNum;
    private static final int seatNum = Config.TDSCompare.seatNum;
    private static final int stationNum = Config.TDSCompare.stationNum;
    private static final int threadNum = Config.TDSCompare.threadNum;
    private static final int operationNum = Config.TDSCompare.operationNum;
    private static final int round = Config.TDSCompare.round;

    private static final int mRouteNum = Config.Correctness.MultiThread.routeNum;
    private static final int mCoachNum = Config.Correctness.MultiThread.coachNum;
    private static final int mSeatNum = Config.Correctness.MultiThread.seatNum;
    private static final int mStationNum = Config.Correctness.MultiThread.stationNum;
    private static final int mThreadNum = Config.Correctness.MultiThread.threadNum;

    public static void tdsCompare() throws InterruptedException {
        Console.log("Ticketing System Compare");
        for (String tdsTypeName: Config.TDSCompare.tdsTypes) {
            Console.log("    - " + tdsTypeName);
            final WrappedTDS tds = new WrappedTDS(routeNum, coachNum, seatNum, stationNum, threadNum);

            Console.log("       - correctness validation: ");

            boolean correct = true;

            Console.logWithoutNewLine("          - single-thread: ");
            if (CorrectnessTest.tdsSingleThread(tds)) {
                Console.log("passed");
            } else {
                correct = false;
                Console.log("failed");
            }

            if (correct) {
                Console.logWithoutNewLine("          - multi-thread: ");
                final WrappedTDS tdsMC = new WrappedTDS(mRouteNum, mCoachNum, mSeatNum, mStationNum, mThreadNum);
                if (CorrectnessTest.tdsMultiThread(tdsMC)) {
                    Console.log("passed");
                } else {
                    correct = false;
                    Console.log("failed");
                }
            }

            if (correct) {
                double throughput = ThroughputTest.tdsThroughput(tds, operationNum, round);
                Console.log("       - throughput: " + FormattedNumber.get(throughput) + "k/s");
            }
        }
    }
}
