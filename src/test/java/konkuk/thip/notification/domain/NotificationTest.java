package konkuk.thip.notification.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.notification.domain.value.NotificationCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN;
import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_ALREADY_CHECKED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[단위] Notification 단위 테스트")
class NotificationTest {

    @Test
    @DisplayName("validateOwner(): 알림의 소유자가 아니면 InvalidStateException 을 던진다.")
    void validate_owner_other_user() throws Exception {
        //given
        Long ownerId = 1L;
        Long otherId = 2L;
        Notification notification = createNotification(ownerId);

        //when //then
        assertThatThrownBy(() -> notification.validateOwner(otherId))
                .isInstanceOf(InvalidStateException.class)
                .hasMessage(NOTIFICATION_ACCESS_FORBIDDEN.getMessage());

    }

    @Test
    @DisplayName("markToChecked(): 이미 읽음 처리된 알림에 대해 다시 읽음 처리하려고 하면, InvalidStateException 을 던진다.")
    void mark_to_checked_already_checked() throws Exception {
        //given
        Notification notification = createNotification(1L);
        notification.markToChecked();   // 이미 읽음 처리

        //when //then
        assertThatThrownBy(notification::markToChecked)
                .isInstanceOf(InvalidStateException.class)
                .hasMessage(NOTIFICATION_ALREADY_CHECKED.getMessage());
    }

    private Notification createNotification(Long targetUserId) {
        return Notification.builder()
                .title("title")
                .content("content")
                .isChecked(false)
                .notificationCategory(NotificationCategory.FEED)
                .targetUserId(targetUserId)
                .redirectSpec(null)
                .build();
    }
}
