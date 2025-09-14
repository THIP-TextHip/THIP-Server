package konkuk.thip.common.util;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    //마지막 활동 시간 포맷팅 -> ex. 1분 전, 1시간 전, 1일 전
    public static String formatBeforeTime(LocalDateTime createdAt) {
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

    public static String RecruitingRoomFormatAfterTime(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date.atStartOfDay();
        Duration d = Duration.between(now, dateTime);

        if (d.isNegative() || d.isZero()) {
            return "??";
        }

        long days = d.toDays();
        if (days > 0) {
            return days + "일 남음";
        }

        long hours = d.toHours();
        if (hours >= 1) {
            return hours + "시간 남음";
        }

        return "마감 임박";
    }

    public static String RecruitingRoomFormatAfterTimeSimple(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTime = date.atStartOfDay();
        Duration d = Duration.between(now, dateTime);

        if (d.isNegative() || d.isZero()) {
            return "??";
        }

        long days = d.toDays();
        if (days > 0) {
            return days + "일";
        }

        long hours = d.toHours();
        if (hours >= 1) {
            return hours + "시간";
        }

        long minutes = d.toMinutes();
        return minutes + "분";
    }


    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    //문자열을 LocalDateTime으로 변환
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new InvalidStateException(ErrorCode.API_INVALID_TYPE, new IllegalArgumentException(
                    dateTimeStr + "는 LocalDateTime으로 변환할 수 없는 문자열입니다. 예외 메시지: " +  e));
        }
    }
}

