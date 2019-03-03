package ticketingsystem.test;

import ticketingsystem.test.utils.OperationWeight;

public class Config {
    public static class Correctness {
        public static class SingleThread {
            public static final boolean enable = true;

            public static final int routeNum = 5;
            public static final int coachNum = 8;
            public static final int seatNum = 100;
            public static final int stationNum = 10;

            public static OperationWeight operationWeight = new OperationWeight(
                    30, 60, 10);
            public static final int operationNum = 5000;
        }

        public static class MultiThread {
            public static final boolean enable = true;

            public static final int routeNum = 1;
            public static final int coachNum = 5;
            public static final int seatNum = 5;
            public static final int stationNum = 5;
            public static final int threadNum = 3;

            public static OperationWeight operationWeight = new OperationWeight(
                    80, 0, 20);
            public static final int operationNum = 500;
        }
    }

    public static class Throughput {
        public static final int routeNum = 5;
        public static final int coachNum = 8;
        public static final int seatNum = 100;
        public static final int stationNum = 10;
        public static OperationWeight operationWeight = new OperationWeight(
                30, 60, 10);

        public static class MultiThreadNum {
            public static final boolean enable = true;
            public static final int[] threadNums = {4, 8, 16, 32, 64};
            public static final int operationNum = 10000;
            public static final int round = 5;
        }

        public static class MultiOpNum {
            public static final boolean enable = true;
            public static final int threadNum = 64;
            public static final int[] operationNums = {100, 1000, 5000, 10000, 50000, 100000};
            public static final int round = 5;
        }
    }

    public static class TDSCompare {
        public static final boolean enable = true;
        public static final int routeNum = 5;
        public static final int coachNum = 8;
        public static final int seatNum = 100;
        public static final int stationNum = 10;
        public static final int threadNum = 48;
        public static final int operationNum = 100000;
        public static final int round = 5;
        public static final String[] tdsTypes = {"SeatLockRoute", "LockOnWriteRoute", "AlmostSerialRoute"};
    }
}
