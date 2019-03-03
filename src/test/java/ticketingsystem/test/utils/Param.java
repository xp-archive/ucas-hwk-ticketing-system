package ticketingsystem.test.utils;

import java.util.Random;

public class Param {
    private static Random rand = new Random();

    public int route;
    public int departure;
    public int arrival;

    public Param(int routeNum, int stationNum) {
        this.route = rand.nextInt(routeNum) + 1;
        this.departure = rand.nextInt(stationNum - 1) + 1;
        this.arrival = departure + rand.nextInt(stationNum - departure) + 1;
    }
}
