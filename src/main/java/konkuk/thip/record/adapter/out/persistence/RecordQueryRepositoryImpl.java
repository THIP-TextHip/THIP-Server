package konkuk.thip.record.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.record.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordQueryRepositoryImpl implements RecordQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<RecordJpaEntity> findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId) {
        QRecordJpaEntity record = QRecordJpaEntity.recordJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

        return jpaQueryFactory
                .select(record)
                .from(record)
                .leftJoin(record.userJpaEntity, user).fetchJoin()
                .where(
                        record.roomJpaEntity.roomId.eq(roomId),
                        filterByType(type, record, userId),
                        (startEndNull(pageStart, pageEnd) ? record.isOverview.isTrue() : record.page.between(pageStart, pageEnd))
                )
                .fetch();
    }

    private boolean startEndNull(Integer start, Integer end) {
        return start == null || end == null;
    }

    private BooleanExpression filterByType(String type, QRecordJpaEntity post, Long userId) {
        if ("mine".equalsIgnoreCase(type)) {
            return post.userJpaEntity.userId.eq(userId);
        }
        return null;
    }
}