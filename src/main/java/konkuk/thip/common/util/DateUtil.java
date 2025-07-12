package konkuk.thip.common.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    //마지막 활동 시간 포맷팅 -> ex. 1분 전, 1시간 전, 1일 전
    public String formatLastActivityTime(LocalDateTime createdAt) {
        long minutes = Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        return (hours / 24) + "일 전";
    }

    public static String formatAfterTime(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date.atStartOfDay();
        Duration d = Duration.between(now, dateTime);

        if (d.isNegative() || d.isZero()) {
            return "??";
        }

        long days = d.toDays();
        if (days > 0) {
            return days + "일 뒤";
        }

        long hours = d.toHours();
        if (hours > 0) {
            return hours + "시간 뒤";
        }

        long minutes = d.toMinutes();
        return minutes + "분 뒤";
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}

