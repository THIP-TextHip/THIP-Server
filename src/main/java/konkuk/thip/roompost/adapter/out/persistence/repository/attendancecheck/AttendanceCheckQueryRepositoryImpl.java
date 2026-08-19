package konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.roompost.adapter.out.jpa.QAttendanceCheckJpaEntity;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;
import konkuk.thip.roompost.application.port.out.dto.QAttendanceCheckQueryDto;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.user.adapter.out.persistence.expression.BlockFilterExpressions.notBlockedWith;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryRepositoryImpl implements AttendanceCheckQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<AttendanceCheckQueryDto> findAttendanceChecksByCreatedAtDesc(Long roomId, LocalDateTime lastCreatedAt, int size, Long viewerId) {
        QAttendanceCheckJpaEntity attendanceCheck = QAttendanceCheckJpaEntity.attendanceCheckJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

        BooleanExpression roomPredicate = attendanceCheck.roomJpaEntity.roomId.eq(roomId);
        BooleanExpression cursorPredicate = (lastCreatedAt == null) ? null : attendanceCheck.createdAt.lt(lastCreatedAt);
        BooleanExpression notBlockedPredicate = notBlockedWith(attendanceCheck.userJpaEntity.userId, viewerId);

        return jpaQueryFactory
                .select(new QAttendanceCheckQueryDto(
                        attendanceCheck.attendanceCheckId,
                        user.userId,
                        user.nickname,
                        user.alias,
                        attendanceCheck.todayComment,
                        attendanceCheck.createdAt
                ))
                .from(attendanceCheck)
                .join(attendanceCheck.userJpaEntity, user)
                .where(roomPredicate, cursorPredicate, notBlockedPredicate)
                .orderBy(
                        attendanceCheck.createdAt.desc()
                )
                .limit(size + 1)    // 다음 페이지 존재 여부를 확인하기 위해
                .fetch();
    }
}
