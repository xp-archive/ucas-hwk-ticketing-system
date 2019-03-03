package ticketingsystem.test.utils;

import java.text.DecimalFormat;

public class FormattedNumber {
    private static DecimalFormat format = new DecimalFormat("0.000");

    private FormattedNumber() {}

    public static String get(double number) {
        return format.format(number);
    }
}
