package ticketingsystem.core.bitmap;

/**
 * @author xp
 */
public class BitUtils {

    public static final int MAX_BIT = 31;

    private static final long BASE_BITS[];

    private static final long CACHED_BITS[][];

    static {
        BASE_BITS = new long[MAX_BIT];
        for (int i = 0; i < MAX_BIT; ++i) {
            BASE_BITS[i] = (long) 1 << i;
        }
        CACHED_BITS = new long[MAX_BIT][MAX_BIT];
        for (int i = 0; i < MAX_BIT; ++i) {
            for (int j = i; j < MAX_BIT; ++j) {
                CACHED_BITS[i][j] = getBits(i, j);
            }
        }
    }

    private static long getBits(int low, int high) {
        long ret = 0;
        for (int i = low; i <= high; ++i) {
            ret |= BASE_BITS[i];
        }
        return ret;
    }

    public static long get(int low, int high) {
        return CACHED_BITS[low][high];
    }

    public static byte[] extract(long bits) {
        byte[] ret = new byte[MAX_BIT];
        for (int i = 0; i < MAX_BIT; ++i) {
            if ((bits & BASE_BITS[i]) != 0) {
                ret[i] = 1;
            }
        }
        return ret;
    }
}
