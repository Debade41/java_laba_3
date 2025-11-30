import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Log {
    private static final Object LOCK = new Object();

    public static void msg(String who, String message) {
        synchronized (LOCK) {
            String time = LocalTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
            System.out.printf("[%s] [%s] %s%n", time, who, message);
        }
    }
}