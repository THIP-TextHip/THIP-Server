package konkuk.thip.user.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomParticipantJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.application.port.out.dto.QUserQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        QRoomParticipantJpaEntity userRoom = QRoomParticipantJpaEntity.roomParticipantJpaEntity;
        QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;

        return new HashSet<>(
                queryFactory
                        .select(userRoom.userJpaEntity.userId)
                        .distinct()
                        .from(userRoom)
                        .join(userRoom.roomJpaEntity, room)
                        .where(room.bookJpaEntity.bookId.eq(bookId))
                        .fetch()
        );
    }

    @Override
    public List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size) {
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        String pattern = "%" + keyword + "%";

        NumberExpression<Integer> priority = new CaseBuilder()
                .when(user.nickname.eq(keyword)).then(3)
                .when(user.nickname.like(keyword + "%")).then(2)
                .when(user.nickname.like(pattern)).then(1)
                .otherwise(0);

        return queryFactory
                .select(new QUserQueryDto(
                        user.userId,
                        user.nickname,
                        alias.imageUrl,
                        alias.value,
                        alias.color,
                        user.followerCount,
                        user.createdAt
                ))
                .from(user)
                .leftJoin(user.aliasForUserJpaEntity, alias)
                .where(user.nickname.like(pattern)
                        .and(user.userId.ne(userId))
                        .and(user.status.eq(StatusType.ACTIVE)))
                .orderBy(priority.desc(), user.nickname.asc())
                .limit(size)
                .fetch();
    }


}
