package konkuk.thip.user.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserRoomJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        QUserRoomJpaEntity userRoom = QUserRoomJpaEntity.userRoomJpaEntity;
        QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;

        return new HashSet<>(
                jpaQueryFactory
                        .select(userRoom.userJpaEntity.userId)
                        .distinct()
                        .from(userRoom)
                        .join(userRoom.roomJpaEntity, room)
                        .where(room.bookJpaEntity.bookId.eq(bookId))
                        .fetch()
        );
    }
}
