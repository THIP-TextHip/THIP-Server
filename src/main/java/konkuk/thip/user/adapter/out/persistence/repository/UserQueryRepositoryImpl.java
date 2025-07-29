package konkuk.thip.user.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

        NumberExpression<Double> relevance = Expressions.numberTemplate( // 정확도 계산
                Double.class,
                "MATCH({0}) AGAINST ({1} IN NATURAL LANGUAGE MODE)",
                user.nickname, keyword
        );

        return queryFactory
                .select(
                        new QUserQueryDto(
                                user.userId,
                                user.nickname,
                                user.aliasForUserJpaEntity.imageUrl,
                                user.aliasForUserJpaEntity.value,
                                user.followerCount,
                                user.createdAt
                        )
                )
                .from(user)
                .leftJoin(user.aliasForUserJpaEntity, alias)
                .where(relevance.gt(0),
                        user.userId.ne(userId) // 자기 자신 제외
                )
                .orderBy(relevance.desc())
                .limit(size)
                .fetch();
    }
}
