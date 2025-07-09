package konkuk.thip.common.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class DateUtil {

    //마지막 활동 시간 포맷팅 -> ex. 1분 전, 1시간 전, 1일 전
    public String formatLastActivityTime(LocalDateTime createdAt) {
        long minutes = Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;
        if (hours < 24) return hours + "시간 전";
        return (hours / 24) + "일 전";
    }
}

