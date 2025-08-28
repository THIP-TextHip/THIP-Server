package konkuk.thip.book.adapter.out.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.book.adapter.out.jpa.QSavedBookJpaEntity;
import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.book.application.port.out.dto.QBookQueryDto;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomParticipantJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static konkuk.thip.common.entity.StatusType.ACTIVE;

@Repository
@RequiredArgsConstructor
public class BookQueryRepositoryImpl implements BookQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QSavedBookJpaEntity savedBook = QSavedBookJpaEntity.savedBookJpaEntity;

    @Override
    public List<BookQueryDto> findSavedBooksBySavedAt(Long userId, LocalDateTime savedAtCursor, int pageSize) {

        // 검색 조건(where) 조립
        // 유저가 저장한 책만: userId 조건
        // 존재하는 유저만: ACTIVE
        BooleanBuilder where = new BooleanBuilder();
        where.and(savedBook.userJpaEntity.userId.eq(userId));
        where.and(user.status.eq(ACTIVE));

        if (savedAtCursor != null) {
            where.and(savedBook.createdAt.lt(savedAtCursor)); // 커서 기준: 저장일 기준 최신순
        }

        return jpaQueryFactory
                .select(new QBookQueryDto(
                        book.bookId,
                        book.title,
                        book.authorName,
                        book.publisher,
                        book.imageUrl,
                        book.isbn,
                        savedBook.createdAt
                ))
                .from(savedBook)
                .join(savedBook.userJpaEntity, user)
                .join(savedBook.bookJpaEntity, book)
                .where(where)
                .orderBy(savedBook.createdAt.desc()) // 저장한 시간 최신순 (내림차순)
                .limit(pageSize + 1)
                .fetch();
    }

    @Override
    public List<BookQueryDto> findJoiningRoomsBooksByRoomPercentage(Long userId, Double roomPercentageCursor, Long bookIdCursor, int pageSize) {

        QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;
        QRoomParticipantJpaEntity participant = QRoomParticipantJpaEntity.roomParticipantJpaEntity;

        NumberExpression<Double> maxRoomPercentage = Expressions.numberTemplate(Double.class, "max({0})", room.roomPercentage);

        // 검색 조건(where) 조립
        // 유저가 참여한 방만: userId 조건
        // 존재하는 유저 방 관계만: ACTIVE, 존재하는 유저만: ACTIVE, 존재하는 방만: ACTIVE
        // 활동 기간 중인 방만: startDate ≤ today ≤ endDate
        BooleanBuilder where = new BooleanBuilder();
        where.and(participant.userJpaEntity.userId.eq(userId));
        where.and(participant.status.eq(ACTIVE));
        where.and(user.status.eq(ACTIVE));
        where.and(room.status.eq(StatusType.ACTIVE));
        where.and(room.startDate.loe(LocalDate.now()));
        where.and(room.endDate.goe(LocalDate.now()));

        BooleanBuilder having = new BooleanBuilder();
        if (roomPercentageCursor != null && bookIdCursor != null) {
            having.and(
                    maxRoomPercentage.lt(roomPercentageCursor)
                            .or(maxRoomPercentage.eq(roomPercentageCursor).and(book.bookId.gt(bookIdCursor)))
            );
        }

        return jpaQueryFactory
                    .select(new QBookQueryDto(
                            book.bookId,
                            book.title,
                            book.authorName,
                            book.publisher,
                            book.imageUrl,
                            book.isbn,
                            maxRoomPercentage
                    ))
                    .from(room)
                    .join(participant).on(participant.roomJpaEntity.eq(room))
                    .join(participant.userJpaEntity, user)
                    .join(room.bookJpaEntity, book)
                    .where(where)
                    .groupBy(book.bookId)
                    .having(having)  // 집계 함수 조건은 having 절에 넣기
                    .orderBy(maxRoomPercentage.desc(), book.bookId.asc()) // 방 진행도 높은 순 (내림차순), 같으면 방 아이디 작은 순 (오름차순)
                    .limit(pageSize + 1)
                    .fetch();
    }

}
