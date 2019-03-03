package ticketingsystem;

public class Console {
    public static void log(String str) {
        System.out.println(str);
        System.out.flush();
    }

    public static void logWithoutNewLine(String str) {
        System.out.print(str);
        System.out.flush();
    }

    public static void seperateLine() {
        System.out.println("\n=================================================\n");
        System.out.flush();
    }
}
