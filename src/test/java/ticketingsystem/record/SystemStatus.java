package ticketingsystem.record;

public class SystemStatus {
    private long[][][][] status;

    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;

    public SystemStatus(int routeNum, int coachNum, int seatNum, int stationNum) {
        this.status = new long[routeNum+1][coachNum+1][seatNum+1][stationNum+1];
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
    }

    private boolean isSeatAvailable(int route, int coach, int seat, int departure, int arrival) {
        for (int i = departure; i != arrival; ++i) {
            if (status[route][coach][seat][i] != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean tryBuy(long tid, int route, int coach, int seat, int departure, int arrival) {
        if (isSeatAvailable(route, coach, seat, departure, arrival)) {
            for (int i = departure; i != arrival; ++i) {
                status[route][coach][seat][i] = tid;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean cannotBuy(int route, int departure, int arrival) {
        for (int c = 1; c <= coachNum; ++c) {
            for (int s = 1; s <= seatNum; ++s) {
                if (isSeatAvailable(route, c, s, departure, arrival)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int inquiry(int route, int departure, int arrival) {
        int sum = 0;
        for (int c = 1; c <= coachNum; ++c) {
            for (int s = 1; s <= seatNum; ++s) {
                if (isSeatAvailable(route, c, s, departure, arrival)) {
                    ++sum;
                }
            }
        }
        return sum;
    }

    private boolean tryRefund(long tid, int route, int coach, int seat, int departure, int arrival) {

        for (int i = departure; i != arrival; ++i) {
            if (status[route][coach][seat][i] != tid) {
                return false;
            }
        }
        for (int i = departure; i != arrival; ++i) {
            status[route][coach][seat][i] = 0;
        }
        return true;
    }

    public boolean transition(Record record) {
        int route = record.route;
        int departure = record.departure;
        int arrival = record.arrival;

        switch (record.operation) {
            case INQUIRY:
                int queryResult = (int)record.result;
                int shouldBe = inquiry(route, departure, arrival);
                if (shouldBe != queryResult) {
                    return false;
                }
                break;

            default:
                long tid = record.result;
                int coach = record.coach, seat = record.seat;
                switch (record.operation) {
                    case BUY:
                        if (tid > 0 && (!tryBuy(tid, route, coach, seat, departure, arrival)) ) {
                            return false;
                        }
                        if (tid <= 0 && (!cannotBuy(route, departure, arrival)) ) {
                            return false;
                        }
                        break;
                    case REFUND:
                        if (tid > 0) {
                            if (!tryRefund(
                                    tid, route, coach, seat, departure, arrival)) {
                                return false;
                            }

                        } else {
                            tid *= -1;
                            if (tryRefund(
                                    tid, route, coach, seat, departure, arrival)) {
                                return false;
                            }
                        }
                        break;

                    default:
                        System.out.println("Unexpected Status.");
                        return false;
                }
        }
        return true;
    }

    public void recoverFrom(Record record) {
        int route = record.route;
        int departure = record.departure;
        int arrival = record.arrival;
        long tid = record.result;
        int coach = record.coach, seat = record.seat;

        switch (record.operation) {
            case BUY:
                if (tid > 0) {
                    for (int i = departure; i != arrival; ++i) {
                        status[route][coach][seat][i] = 0;
                    }
                }
                break;
            case REFUND:
                if (tid > 0) {
                    for (int i = departure; i != arrival; ++i) {
                        status[route][coach][seat][i] = tid;
                    }
                }
                break;

            default:
                break;
        }
    }

    public void set(int route, int coach, int seat, int station, long value) {
        status[route][coach][seat][station] = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SystemStatus status = (SystemStatus) o;
        if (routeNum != status.routeNum
                || coachNum != status.coachNum
                || seatNum != status.seatNum
                || stationNum != status.stationNum) {
            return false;
        }

        for (int r = 1; r <= routeNum; ++r) {
            for (int c = 1; c <= coachNum; ++c) {
                for (int s = 1; s <= seatNum; ++s) {
                    for (int t = 1; t <= stationNum; ++t) {
                        long thisStatus = this.status[r][c][s][t];
                        long thatStatus = status.status[r][c][s][t];
                        if (thisStatus != thatStatus) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");

        boolean first = true;
        for (int station = 1; station < stationNum; ++station) {
            if (first) {
                first = false;
            } else {
                str.append("\n");
            }
            str.append("      {");
            boolean first2 = true;
            for (int route = 1; route <= routeNum; ++route) {
                if (first2) {
                    first2 = false;
                } else {
                    str.append(", ");
                }
                str.append("route");
                str.append(route);
                str.append(":{");
                boolean first3 = true;
                for (int coach = 1; coach <= coachNum; ++coach) {
                    if (first3) {
                        first3 = false;
                    } else {
                        str.append(", ");
                    }
                    str.append('{');
                    boolean first4 = true;
                    for (int seat = 1; seat <= seatNum; ++seat) {
                        if (first4) {
                            first4 = false;
                        } else {
                            str.append(",");
                        }
                        if (status[route][coach][seat][station] != 0) {
                            str.append('x');
                        } else {
                            str.append(' ');
                        }
                    }
                    str.append('}');
                }
                str.append('}');
            }
            str.append('}');
        }
        return str.toString();
    }
}
