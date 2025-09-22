package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.adapter.out.mapper.NotificationMapper;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.domain.Notification;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationCommandPersistenceAdapter implements NotificationCommandPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final NotificationMapper notificationMapper;

    @Override
    public void save(Notification notification) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(notification.getTargetUserId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND)
        );

        notificationJpaRepository.save(notificationMapper.toJpaEntity(notification, userJpaEntity));
    }
}
