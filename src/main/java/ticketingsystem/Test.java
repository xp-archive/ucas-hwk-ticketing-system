package ticketingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Test {

    private static class MyRunnable implements Runnable {

        private final TicketingDS tds;

        private final int routeNum;

        private final int stationNum;

        private final int testNum;

        private final boolean debug;

        private final AtomicInteger[] counters;

        private final AtomicLong[] timers;

        private final boolean validate;

        private final Collection<Ticket> allTickets;

        public MyRunnable(TicketingDS tds, int routeNum, int stationNum, int testNum, boolean debug, AtomicInteger[] counters, AtomicLong[] timers, boolean validate, Collection<Ticket> allTickets) {
            this.tds = tds;
            this.routeNum = routeNum;
            this.stationNum = stationNum;
            this.testNum = testNum;
            this.debug = debug;
            this.counters = counters;
            this.timers = timers;
            this.validate = validate;
            this.allTickets = allTickets;
        }

        @Override
        public void run() {
            try {
                List<Ticket> tickets = new LinkedList<>();

                Random random = new Random();
                long time1, time2;
                int sel, route, departure, arrival, left, index;
                Ticket ticket;
                long rebuyTicketId;
                for (int j = 0; j < testNum; ++j) {
                    time1 = System.currentTimeMillis();

                    sel = random.nextInt(100);
                    if (sel < 60) {
                        sel = 0;
                    } else if (sel < 90) {
                        sel = 1;
                    } else {
                        sel = 2;
                    }

                    if (sel == 0) {
                        // inquiry
                        route = random.nextInt(routeNum) + 1;
                        departure = random.nextInt(stationNum - 1) + 1;
                        arrival = departure + random.nextInt(stationNum - departure) + 1;

                        left = tds.inquiry(route, departure, arrival);
                        if (debug) {
                            System.out.println(String.format("route %d: %d -> %d, left %s", route, departure, arrival, left));
                        }
                    } else if (sel == 1) {
                        // buy

                        String passenger = "p" + j;
                        route = random.nextInt(routeNum) + 1;
                        departure = random.nextInt(stationNum - 1) + 1;
                        arrival = departure + random.nextInt(stationNum - departure) + 1;

                        ticket = tds.buyTicket(passenger, route, departure, arrival);
                        if (ticket == null) {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d, sold out!", route, departure, arrival));
                            }
                        } else {
                            if (debug) {
                                System.out.println(String.format("route %d: %d -> %d, bought!", route, departure, arrival));
                            }
                            tickets.add(ticket);
                            if (validate) {
                                rebuyTicketId = tds.rebuyTicket(ticket.route, ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
                                if (rebuyTicketId != 0) {
                                    throw new RuntimeException("rebuy unexpected success: " + ticket.toString() + " with " + rebuyTicketId);
                                }
                                allTickets.add(ticket);
                            }
                        }
                    } else {
                        // refund

                        if (!tickets.isEmpty()) {
                            while (true) {
                                index = random.nextInt(tickets.size());
                                ticket = tickets.remove(index);
                                if (ticket != null) {
                                    if (!tds.refundTicket(ticket)) {
                                        throw new RuntimeException("refund failed: " + ticket.toString());
                                    }
                                    if (validate) {
                                        if (tds.refundTicket(ticket)) {
                                            throw new RuntimeException("refund unexpected success: " + ticket.toString());
                                        }
                                    }
                                    if (debug) {
                                        System.out.println(String.format("route %d: %d -> %d, refund!", ticket.route, ticket.departure, ticket.arrival));
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    time2 = System.currentTimeMillis();
                    counters[sel].incrementAndGet();
                    timers[sel].addAndGet(time2 - time1);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private static float once(final String prefix, final int routeNum, final int coachNum, final int seatNum, final int stationNum, final int threadNum, final int testNum, final boolean debug, final boolean validate) throws InterruptedException {
        System.out.printf("%s: %d route, %d coach/route, %d seat/coach, %d station\n",
                prefix, routeNum, coachNum, seatNum, stationNum);

        long time1 = System.currentTimeMillis();

        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);

        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        final List<AtomicInteger[]> countersList = new ArrayList<>(threadNum);
        final List<AtomicLong[]> timersList = new ArrayList<>(threadNum);
        final Queue<Ticket> allTickets = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < threadNum; ++i) {
            AtomicInteger[] counters = new AtomicInteger[3];
            AtomicLong[] timers = new AtomicLong[3];
            for (int j = 0; j < 3; ++j) {
                counters[j] = new AtomicInteger(0);
                timers[j] = new AtomicLong(0);
            }
            countersList.add(counters);
            timersList.add(timers);
            executor.submit(new MyRunnable(tds, routeNum, stationNum, testNum, debug, counters, timers, validate, allTickets));
        }

        executor.shutdown();

        int minutes = 0;
        while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.printf("%s: waiting %dmins..\n", prefix, ++minutes);
        }

        long time2 = System.currentTimeMillis();

        int[] cs = new int[3];
        int[] call = new int[3];
        long[] ts = new long[3];
        long[] tall = new long[3];
        for (int j = 0; j < countersList.size(); j++) {
            AtomicInteger[] counters = countersList.get(j);
            AtomicLong[] timers = timersList.get(j);
            for (int k = 0; k < 3; ++k) {
                cs[k] = counters[k].get();
                ts[k] = timers[k].get();
                call[k] += cs[k];
                tall[k] += ts[k];
            }
            if (debug) {
                System.out.printf("%s: %d with: %d inquiry %dms, %d buy %dms, %d refund %dms\n", prefix, j,
                        cs[0], ts[0],
                        cs[1], ts[1],
                        cs[2], ts[2]
                );
            }
        }
        System.out.printf("%s: %d inquiry %dms, %d buy %dms, %d refund %dms\n", prefix,
                call[0], tall[0],
                call[1], tall[1],
                call[2], tall[2]
        );
        System.out.printf("%s: inquiry avg %fms, buy avg %fms, refund avg %fms\n", prefix,
                (float) tall[0] / call[0],
                (float) tall[1] / call[1],
                (float) tall[2] / call[2]
        );

        long timediff = time2 - time1;
        float throughput = (threadNum * (float) testNum) / timediff;
        System.out.printf("%s: %d threads, %d tests, used %dms, %fkop/s\n",
                prefix, threadNum, testNum, timediff, throughput);

        if (validate) {
            executor = Executors.newFixedThreadPool(threadNum);
            for (final Ticket ticket : allTickets) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        tds.refundTicket(ticket);
                    }
                });
            }
            executor.shutdown();
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.printf("%s: waiting %dmins..\n", prefix, ++minutes);
            }
            tds.validate();
            System.out.printf("%s: validated\n", prefix);
        }

        return throughput;
    }

    private static float once(final String prefix, final int routeNum, final int coachNum, final int seatNum, final int stationNum, final int threadNum, final int testNum, final boolean debug) throws InterruptedException {
        return once(prefix, routeNum, coachNum, seatNum, stationNum, threadNum, testNum, debug, false);
    }

    public static void main(String[] args) throws InterruptedException {
        int core = Runtime.getRuntime().availableProcessors();

        int defaultRouteNum = 5;
        int defaultCoachNum = 8;
        int defaultSeatNum = 100;
        int defaultStationNum = 10;

//        once("debug", 1, 1, 5, 6, 1, 100, true);

        once("1x10w", 1, 1, 5, 5, 1, 100000, false, true);
        System.out.println();

        int[] testCores = new int[]{4, 8, 16, 32, 64};
        int testNum = 500000;
        for (int k = 0; k < testCores.length; ++k) {
            if (core >= testCores[k]) {
                int usedCore = testCores[k];
                for (int i = 1; i <= 5; ++i) {
                    once(usedCore + "x100w-" + i, defaultRouteNum, defaultCoachNum, defaultSeatNum, defaultStationNum, usedCore, testNum, false);
                }
                System.out.println();
            }
        }

        System.out.println("test finished");
    }
}
