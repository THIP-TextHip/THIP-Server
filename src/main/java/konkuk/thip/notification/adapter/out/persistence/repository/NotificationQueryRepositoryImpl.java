package konkuk.thip.notification.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.notification.adapter.out.jpa.QNotificationJpaEntity;
import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;
import konkuk.thip.notification.application.port.out.dto.QNotificationQueryDto;
import konkuk.thip.notification.domain.value.NotificationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NotificationQueryDto> findFeedNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize) {
        QNotificationJpaEntity notification = QNotificationJpaEntity.notificationJpaEntity;

        var where = notification.userJpaEntity.userId.eq(userId)
                .and(notification.notificationCategory.eq(NotificationCategory.FEED));

        where = applyCursor(lastNotificationId, where, notification);

        return getNotificationQueryDtos(pageSize, notification, where);
    }

    @Override
    public List<NotificationQueryDto> findRoomNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize) {
        QNotificationJpaEntity notification = QNotificationJpaEntity.notificationJpaEntity;

        var where = notification.userJpaEntity.userId.eq(userId)
                .and(notification.notificationCategory.eq(NotificationCategory.ROOM));
        where = applyCursor(lastNotificationId, where, notification);

        return getNotificationQueryDtos(pageSize, notification, where);
    }

    @Override
    public List<NotificationQueryDto> findFeedAndRoomNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize) {
        QNotificationJpaEntity notification = QNotificationJpaEntity.notificationJpaEntity;

        var where = notification.userJpaEntity.userId.eq(userId)
                .and(notification.notificationCategory.in(NotificationCategory.FEED, NotificationCategory.ROOM));
        where = applyCursor(lastNotificationId, where, notification);

        return getNotificationQueryDtos(pageSize, notification, where);
    }

    private static BooleanExpression applyCursor(Long lastNotificationId, BooleanExpression where, QNotificationJpaEntity notification) {
        if (lastNotificationId != null) {
            where = where.and(notification.notificationId.lt(lastNotificationId));
        }
        return where;
    }

    private List<NotificationQueryDto> getNotificationQueryDtos(int pageSize, QNotificationJpaEntity notification, BooleanExpression where) {
        return queryFactory.select(new QNotificationQueryDto(
                        notification.notificationId,
                        notification.title,
                        notification.content,
                        notification.isChecked,
                        notification.notificationCategory,
                        notification.createdAt
                ))
                .from(notification)
                .where(where)
                .orderBy(notification.notificationId.desc())    // PK 기준 내림차순 (= 최신순)
                .limit(pageSize + 1)
                .fetch();
    }
}
