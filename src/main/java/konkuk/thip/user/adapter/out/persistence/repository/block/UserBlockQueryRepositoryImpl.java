package konkuk.thip.user.adapter.out.persistence.repository.block;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.user.adapter.out.jpa.QUserBlockJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;
import konkuk.thip.user.application.port.out.dto.QBlockedUserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserBlockQueryRepositoryImpl implements UserBlockQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<UserBlockJpaEntity> findByUserAndBlockedUser(Long userId, Long blockedUserId) {
        QUserBlockJpaEntity block = QUserBlockJpaEntity.userBlockJpaEntity;

        UserBlockJpaEntity userBlockJpaEntity = jpaQueryFactory
                .selectFrom(block)
                .where(block.userJpaEntity.userId.eq(userId)
                        .and(block.blockedUserJpaEntity.userId.eq(blockedUserId)))
                .fetchOne();

        return Optional.ofNullable(userBlockJpaEntity);
    }

    @Override
    public List<BlockedUserQueryDto> findBlockedUserDtosByUserId(Long userId, LocalDateTime cursorCreatedAt, Long cursorBlockId, int size) {
        QUserBlockJpaEntity block = QUserBlockJpaEntity.userBlockJpaEntity;
        QUserJpaEntity blockedUser = QUserJpaEntity.userJpaEntity;

        BooleanBuilder condition = new BooleanBuilder()
                .and(block.userJpaEntity.userId.eq(userId));

        // 같은 시각에 차단된 항목이 페이지 경계에서 누락되지 않도록 (createdAt, blockId) 복합 커서를 사용한다
        if (cursorCreatedAt != null && cursorBlockId != null) {
            condition.and(block.createdAt.lt(cursorCreatedAt)
                    .or(block.createdAt.eq(cursorCreatedAt)
                            .and(block.blockId.lt(cursorBlockId))));
        }

        return jpaQueryFactory
                .select(new QBlockedUserQueryDto(
                        blockedUser.userId,
                        blockedUser.nickname,
                        blockedUser.alias,
                        block.blockId,
                        block.createdAt
                ))
                .from(block)
                .join(block.blockedUserJpaEntity, blockedUser)
                .where(condition)
                .orderBy(block.createdAt.desc(), block.blockId.desc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public Set<Long> findBlockedUserIdsBothWays(Long userId) {
        QUserBlockJpaEntity block = QUserBlockJpaEntity.userBlockJpaEntity;

        List<Long> blockedByMe = jpaQueryFactory
                .select(block.blockedUserJpaEntity.userId)
                .from(block)
                .where(block.userJpaEntity.userId.eq(userId))
                .fetch();

        List<Long> blockedMe = jpaQueryFactory
                .select(block.userJpaEntity.userId)
                .from(block)
                .where(block.blockedUserJpaEntity.userId.eq(userId))
                .fetch();

        Set<Long> blockedUserIds = new HashSet<>(blockedByMe);
        blockedUserIds.addAll(blockedMe);
        return blockedUserIds;
    }
}
