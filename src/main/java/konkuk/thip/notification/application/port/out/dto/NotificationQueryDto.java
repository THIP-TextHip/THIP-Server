package konkuk.thip.notification.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.notification.domain.value.NotificationCategory;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Builder
public record NotificationQueryDto(
        Long notificationId,
        String title,
        String content,
        boolean isChecked,
        NotificationCategory notificationCategory,
        LocalDateTime createdAt
) {
    @QueryProjection
    public NotificationQueryDto {
        Assert.notNull(notificationId, "NotificationId must not be null");
        Assert.notNull(title, "Title must not be null");
        Assert.notNull(content, "Content must not be null");
        Assert.notNull(isChecked, "isChecked must not be null");
        Assert.notNull(notificationCategory, "NotificationCategory must not be null");
        Assert.notNull(createdAt, "CreatedAt must not be null");
    }
}
