package konkuk.thip.common.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtilsss {

    public static String formatAfterTime(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date.atStartOfDay();
        Duration d = Duration.between(now, dateTime);

        if (d.isNegative() || d.isZero()) {
            return "??";
        }

        long days = d.toDays();
        if (days > 0) {
            return days + "일 뒤 ";
        }

        long hours = d.toHours();
        if (hours > 0) {
            return hours + "시간 뒤 ";
        }

        long minutes = d.toMinutes();
        return minutes + "분 뒤 ";
    }
}
