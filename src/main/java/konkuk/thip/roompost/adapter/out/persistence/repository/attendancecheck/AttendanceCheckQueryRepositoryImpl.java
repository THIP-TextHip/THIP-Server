package konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.roompost.adapter.out.jpa.QAttendanceCheckJpaEntity;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;
import konkuk.thip.roompost.application.port.out.dto.QAttendanceCheckQueryDto;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryRepositoryImpl implements AttendanceCheckQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<AttendanceCheckQueryDto> findAttendanceChecksByCreatedAtDesc(Long roomId, LocalDateTime lastCreatedAt, int size) {
        QAttendanceCheckJpaEntity attendanceCheck = QAttendanceCheckJpaEntity.attendanceCheckJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        BooleanExpression roomPredicate = attendanceCheck.roomJpaEntity.roomId.eq(roomId);
        BooleanExpression cursorPredicate = (lastCreatedAt == null) ? null : attendanceCheck.createdAt.lt(lastCreatedAt);

        return jpaQueryFactory
                .select(new QAttendanceCheckQueryDto(
                        attendanceCheck.attendanceCheckId,
                        user.userId,
                        user.nickname,
                        alias.imageUrl,
                        attendanceCheck.todayComment,
                        attendanceCheck.createdAt
                ))
                .from(attendanceCheck)
                .join(attendanceCheck.userJpaEntity, user)
                .join(user.aliasForUserJpaEntity, alias)
                .where(roomPredicate, cursorPredicate)
                .orderBy(
                        attendanceCheck.createdAt.desc()
                )
                .limit(size + 1)    // 다음 페이지 존재 여부를 확인하기 위해
                .fetch();
    }
}
