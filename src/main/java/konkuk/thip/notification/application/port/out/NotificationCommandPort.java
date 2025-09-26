package konkuk.thip.notification.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.notification.domain.Notification;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_NOT_FOUND;

public interface NotificationCommandPort {

    Long save(Notification notification);

    Optional<Notification> findById(Long id);

    default Notification getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));
    }

    void update(Notification notification);
}
