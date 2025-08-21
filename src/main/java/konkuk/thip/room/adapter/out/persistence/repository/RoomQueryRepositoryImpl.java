package konkuk.thip.room.adapter.out.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomParticipantJpaEntity;
import konkuk.thip.room.application.port.out.dto.QRoomQueryDto;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomQueryRepositoryImpl implements RoomQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QRoomParticipantJpaEntity participant = QRoomParticipantJpaEntity.roomParticipantJpaEntity;

    /** 모집중 + ACTIVE 공통 where */
    private BooleanBuilder recruitingActiveWhere(LocalDate today) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(room.startDate.after(today))
                .and(room.status.eq(StatusType.ACTIVE));
        return where;
    }

    /** 카테고리 조건 추가 */
    private void applyCategory(BooleanBuilder where, Category category) {
        if (category != null) {
            where.and(room.category.eq(category));
        }
    }

    /** 키워드(방 이름 OR 책 제목) 조건 추가 */
    private void applyKeyword(BooleanBuilder where, String keyword) {
        // TODO : 현재는 like %keyword% 로 쿼리가 나감. 개선할 수 있는지 고민할 것
        if (keyword != null && !keyword.isBlank()) {
            where.and(room.title.containsIgnoreCase(keyword)
                    .or(book.title.containsIgnoreCase(keyword)));
        }
    }

    /** 커서 조건: startDate ASC */
    private void applyCursorStartDateAsc(BooleanBuilder where, DateExpression<LocalDate> cursorExpr, LocalDate lastStartDate, Long roomId) {
        if (lastStartDate != null && roomId != null) {
            where.and(cursorExpr.gt(lastStartDate)
                    .or(cursorExpr.eq(lastStartDate).and(room.roomId.gt(roomId))));
        }
    }

    /** 커서 조건: memberCount DESC */
    private void applyCursorMemberCountDesc(BooleanBuilder where, Integer lastMemberCount, Long roomId) {
        if (lastMemberCount != null && roomId != null) {
            where.and(room.memberCount.lt(lastMemberCount)
                    .or(room.memberCount.eq(lastMemberCount).and(room.roomId.gt(roomId))));
        }
    }

    /** 공통 SELECT 프로젝션 */
    private QRoomQueryDto projectionForRecruitingRoomSearch() {
        return new QRoomQueryDto(
                room.roomId,
                book.imageUrl,
                room.title,
                room.recruitCount,
                room.memberCount,
                room.startDate,
                room.isPublic
        );
    }

    /**
     * 모집중인 방 검색 관련 메서드
     */
    @Override
    public List<RoomQueryDto> findRecruitingRoomsOrderByStartDateAsc(String keyword, LocalDate lastStartDate, Long roomId, int pageSize) {
        final LocalDate today = LocalDate.now();
        DateExpression<LocalDate> cursorExpr = room.startDate; // 커서 비교는 startDate

        BooleanBuilder where = recruitingActiveWhere(today);
        applyKeyword(where, keyword);
        applyCursorStartDateAsc(where, cursorExpr, lastStartDate, roomId);
        
        return queryFactory
                .select(projectionForRecruitingRoomSearch())
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(cursorExpr.asc(), room.roomId.asc())
                .limit(pageSize + 1)
                .fetch();
    }

    @Override
    public List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByStartDateAsc(String keyword, Category category, LocalDate lastStartDate, Long roomId, int pageSize) {
        final LocalDate today = LocalDate.now();
        DateExpression<LocalDate> cursorExpr = room.startDate;

        BooleanBuilder where = recruitingActiveWhere(today);
        applyCategory(where, category);
        applyKeyword(where, keyword);
        applyCursorStartDateAsc(where, cursorExpr, lastStartDate, roomId);

        return queryFactory
                .select(projectionForRecruitingRoomSearch())
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(cursorExpr.asc(), room.roomId.asc())
                .limit(pageSize + 1)
                .fetch();
    }

    @Override
    public List<RoomQueryDto> findRecruitingRoomsOrderByMemberCountDesc(String keyword, Integer lastMemberCount, Long roomId, int pageSize) {
        final LocalDate today = LocalDate.now();

        BooleanBuilder where = recruitingActiveWhere(today);
        applyKeyword(where, keyword);
        applyCursorMemberCountDesc(where, lastMemberCount, roomId);

        return queryFactory
                .select(projectionForRecruitingRoomSearch())
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(room.memberCount.desc(), room.roomId.asc())
                .limit(pageSize + 1)
                .fetch();
    }

    @Override
    public List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByMemberCountDesc(String keyword, Category category, Integer lastMemberCount, Long roomId, int pageSize) {
        final LocalDate today = LocalDate.now();

        BooleanBuilder where = recruitingActiveWhere(today);
        applyCategory(where, category);
        applyKeyword(where, keyword);
        applyCursorMemberCountDesc(where, lastMemberCount, roomId);

        return queryFactory
                .select(projectionForRecruitingRoomSearch())
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(room.memberCount.desc(), room.roomId.asc())
                .limit(pageSize + 1)
                .fetch();
    }
//  -----------------------------------------------------------------------------------------------------------------------

    @Override
    public List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Long roomId, Category category, int count) {
        List<Tuple> tuples = queryFactory
                .select(room.roomId, room.title, room.memberCount, room.recruitCount, room.startDate, book.imageUrl)
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(
                        room.category.eq(category)
                                .and(room.startDate.after(LocalDate.now()))     // 모집 마감 시각 > 현재 시각
                                .and(room.roomId.ne(roomId))// 현재 방 제외
                                .and(room.isPublic.isTrue()) // 공개방 만
                )
                .orderBy(room.startDate.asc())
                .limit(count)
                .fetch();

        return tuples.stream()
                .map(t -> RoomRecruitingDetailViewResponse.RecommendRoom.builder()
                        .roomId(t.get(room.roomId))
                        .bookImageUrl(t.get(book.imageUrl))
                        .roomName(t.get(room.title))
                        .memberCount(t.get(room.memberCount))
                        .recruitCount(t.get(room.recruitCount))
                        .recruitEndDate(DateUtil.formatAfterTime(t.get(room.startDate)))
                        .build())
                .toList();
    }

    @Override
    public Page<RoomGetHomeJoinedListResponse.JoinedRoomInfo> searchHomeJoinedRooms(Long userId, LocalDate date, Pageable pageable) {

        QRoomParticipantJpaEntity userRoomSub = new QRoomParticipantJpaEntity("userRoomSub");

        // 1. 검색 조건(where) 조립
        // 유저가 참여한 방만: userId 조건
        // 활동 기간 중인 방만: startDate ≤ today ≤ endDate
        BooleanBuilder where = new BooleanBuilder();
        where.and(participant.userJpaEntity.userId.eq(userId));
        where.and(participant.status.eq(StatusType.ACTIVE));
        where.and(room.startDate.loe(date));
        where.and(room.endDate.goe(date));

        // 2. 페이징된 목록 조회
        List<Tuple> tuples = queryFactory
                .select(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.memberCount,
                        room.recruitCount,
                        room.startDate,
                        book.title,
                        participant.userPercentage
                )
                .from(participant)
                .join(participant.roomJpaEntity, room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(
                        participant.userPercentage.desc(), // 진행률 높은 순(내림차순)
                        room.startDate.asc() // 진행률 같으면 활동 시작일 빠른 순 (오름차순)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // TODO : 추후에 오프셋 페이징이 아니라, 키셋 페이징 기법 도입 검토

        // 3. Tuple → DTO 매핑
        List<RoomGetHomeJoinedListResponse.JoinedRoomInfo> content = tuples.stream()
                .map(t -> RoomGetHomeJoinedListResponse.JoinedRoomInfo.builder()
                        .roomId(t.get(room.roomId))
                        .bookImageUrl(t.get(book.imageUrl))
                        .roomTitle(t.get(room.title))
                        .memberCount(t.get(room.memberCount))
                        .userPercentage(t.get(participant.userPercentage).intValue())
                        .build()
                )
                .toList();

        // 4. 전체 개수 조회 (페이징 정보 계산용)
        Long totalCount = queryFactory
                .select(participant.count())
                .from(participant)
                .join(participant.roomJpaEntity, room)
                .where(where)
                .fetchOne();
        long total = (totalCount != null) ? totalCount : 0L;

        // 5. PageImpl 생성하여 반환
        return new PageImpl<>(content, pageable, total);
    }

    // 1) 모집중인 방
    @Override
    public List<RoomQueryDto> findRecruitingRoomsUserParticipated(
            Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(participant.status.eq(StatusType.ACTIVE))
                .and(room.startDate.after(today))
                .and(room.status.eq(StatusType.ACTIVE));      // 유저가 참여한 방 && 모집중인 방
        DateExpression<LocalDate> cursorExpr = room.startDate;      // 커서 비교는 startDate(= 모집 마감일 - 1일)
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.asc(), room.roomId.asc()
        };

        return fetchMyRooms(base, cursorExpr, orders, true, dateCursor, roomIdCursor, pageSize);
    }

    // 2) 진행중인 방
    @Override
    public List<RoomQueryDto> findPlayingRoomsUserParticipated(
            Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(participant.status.eq(StatusType.ACTIVE))
                .and(room.startDate.loe(today))
                .and(room.endDate.goe(today))
                .and(room.status.eq(StatusType.ACTIVE));      // 유저가 참여한 방 && 현재 진행중인 방
        DateExpression<LocalDate> cursorExpr = room.endDate;        // 커서 비교는 endDate(= 진행 마감일)
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.asc(), room.roomId.asc()
        };

        return fetchMyRooms(base, cursorExpr, orders, true, dateCursor, roomIdCursor, pageSize);
    }

    // 3) 진행＋모집 통합
    @Override
    public List<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(
            Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression playing   = room.startDate.loe(today).and(room.endDate.goe(today));
        BooleanExpression recruiting = room.startDate.after(today);
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(participant.status.eq(StatusType.ACTIVE))
                .and(playing.or(recruiting))
                .and(room.status.eq(StatusType.ACTIVE));     // 유저가 참여한 방 && 현재 진행중인 방 + 모집중인 방

        // 진행중: cursor=endDate, 모집중: cursor=startDate
        DateExpression<LocalDate> cursorExpr = new CaseBuilder()
                .when(playing).then(room.endDate)
                .otherwise(room.startDate);

        // 진행중 먼저(0), 모집중 다음(1) -> 조회 우선순위 반영
        NumberExpression<Integer> priority = new CaseBuilder()
                .when(playing).then(0)
                .otherwise(1);

        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                priority.asc(),
                cursorExpr.asc(),
                room.roomId.asc()
        };

        return fetchMyRooms(base, cursorExpr, orders, true, dateCursor, roomIdCursor, pageSize);
    }

    // 4) 만료된 방
    @Override
    public List<RoomQueryDto> findExpiredRoomsUserParticipated(
            Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(participant.status.eq(StatusType.ACTIVE))
                .and(room.endDate.before(today))
                .and(room.status.eq(StatusType.ACTIVE));       // 유저가 참여한 방 && 만료된 방

        DateExpression<LocalDate> cursorExpr = room.endDate;
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.desc(), room.roomId.desc()       // 만료된 방은 가장 최근에 만료된 방부터 반환
        };

        return fetchMyRooms(base, cursorExpr, orders, false, dateCursor, roomIdCursor, pageSize);
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByStartDateAsc(Category category, int limit, Long userId) {
        return queryFactory
                .select(new QRoomQueryDto(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.recruitCount,
                        room.memberCount,
                        room.startDate
                ))
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(findDeadlinePopularRoomCondition(category, userId))
                .orderBy(room.startDate.asc(), room.memberCount.desc(), room.roomId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByMemberCount(Category category, int limit, Long userId) {
        return queryFactory
                .select(new QRoomQueryDto(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.recruitCount,
                        room.memberCount,
                        room.startDate
                ))
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(findDeadlinePopularRoomCondition(category, userId))
                .orderBy(room.memberCount.desc(), room.startDate.asc(), room.roomId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<RoomQueryDto> findRoomsByIsbnOrderByStartDateAsc(String isbn, LocalDate dateCursor, Long roomIdCursor, int pageSize) {
        DateExpression<LocalDate> cursorExpr = room.startDate; // 커서 비교는 startDate(= 모집 마감일 - 1일)
        BooleanExpression baseCondition = room.bookJpaEntity.isbn.eq(isbn)
                .and(room.startDate.after(LocalDate.now())) // 모집 마감 시각 > 현재 시각
                .and(room.status.eq(StatusType.ACTIVE));

        if (dateCursor != null && roomIdCursor != null) { // 첫 페이지가 아닌 경우
            baseCondition = baseCondition.and(cursorExpr.gt(dateCursor)
                    .or(cursorExpr.eq(dateCursor).and(room.roomId.gt(roomIdCursor))));
        }

        return queryFactory
                .select(new QRoomQueryDto(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.recruitCount,
                        room.memberCount,
                        cursorExpr,
                        room.isPublic
                ))
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(baseCondition)
                .orderBy(cursorExpr.asc(), room.roomId.asc())
                .limit(pageSize + 1)
                .fetch();
    }

    private BooleanExpression findDeadlinePopularRoomCondition(Category category, Long userId) {
        return room.category.eq(category)
                .and(room.startDate.after(LocalDate.now())) // 모집 마감 시각 > 현재 시각
                .and(room.isPublic.isTrue()) // 공개 방만 조회
                .and(userJoinedRoom(userId).not()) // 유저가 참여하지 않은 방만 조회
                .and(room.status.eq(StatusType.ACTIVE));
    }

    /**
     * 유저가 참여한 방인지 여부를 확인하는 서브쿼리
     */
    private BooleanExpression userJoinedRoom(Long userId) {
        return JPAExpressions
                .selectOne()
                .from(participant)
                .where(participant.userJpaEntity.userId.eq(userId)
                        .and(participant.roomJpaEntity.roomId.eq(room.roomId)))
                .exists();
    }

    /**
     * 공통 커서 + 2단계 조회 (IDs → entities) 처리
     */
    private List<RoomQueryDto> fetchMyRooms(
            BooleanExpression baseCondition,
            DateExpression<LocalDate> cursorExpr,
            OrderSpecifier<?>[] orders,
            boolean ascending,
            LocalDate dateCursor,
            Long roomIdCursor,
            int pageSize
    ) {
        BooleanBuilder where = new BooleanBuilder(baseCondition);
        if (dateCursor != null && roomIdCursor != null) {       // 첫 페이지가 아닌 경우
            if (ascending) {
                where.and(cursorExpr.gt(dateCursor)
                        .or(cursorExpr.eq(dateCursor)
                                .and(room.roomId.gt(roomIdCursor))));
            } else {
                where.and(cursorExpr.lt(dateCursor)
                        .or(cursorExpr.eq(dateCursor)
                                .and(room.roomId.lt(roomIdCursor))));
            }
        }

        // 2) DTO 프로젝션: 필요한 필드만 바로 조회
        return queryFactory
                .select(new QRoomQueryDto(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.recruitCount,
                        room.memberCount,
                        room.startDate,
                        room.endDate,
                        room.isPublic
                ))
                .from(participant)
                .join(participant.roomJpaEntity, room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(orders)
                .limit(pageSize + 1)
                .fetch();
    }
}
