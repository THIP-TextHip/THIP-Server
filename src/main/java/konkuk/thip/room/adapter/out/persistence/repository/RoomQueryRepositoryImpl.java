package konkuk.thip.room.adapter.out.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomParticipantJpaEntity;
import konkuk.thip.room.application.port.out.dto.CursorSliceOfMyRoomView;
import konkuk.thip.room.domain.MyRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
public class RoomQueryRepositoryImpl implements RoomQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QRoomParticipantJpaEntity participant = QRoomParticipantJpaEntity.roomParticipantJpaEntity;

    @Override
    public Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable) {
        // 1. 검색 조건(where) 조립 : 방이름 or 첵제목에 keyword 포함, category 필터 적용, 멤버 모집중인(= 활동 시작전인) 방만 검색
        BooleanBuilder where = new BooleanBuilder();
        // keyword 필터 (빈 문자열이면 생략)
        if (keyword != null && !keyword.isBlank()) {
            where.and(room.title.containsIgnoreCase(keyword).or(book.title.containsIgnoreCase(keyword)));
        }
        // category 필터 (빈 문자열이면 생략)
        if (category != null && !category.isBlank()) {
            where.and(room.categoryJpaEntity.value.eq(category));
        }
        // 모집중인 방만
        where.and(room.startDate.after(LocalDate.now()));

        // 2. 페이징된 content 조회
        // 우선순위 표현식 : keyword가 방 제목에 매칭되면 1, 아니면 0
        NumberExpression<Integer> priorityExpr = new CaseBuilder()
                .when(room.title.containsIgnoreCase(keyword)).then(1)
                .otherwise(0);

        NumberExpression<Long> memberCountExpr = participant.roomParticipantId.count();       // 방 별 멤버수 표현식

        List<Tuple> tuples = queryFactory
                .select(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        memberCountExpr,
                        room.recruitCount,
                        room.startDate,
                        room.categoryJpaEntity.value
                )
                .from(room)
                .join(room.bookJpaEntity, book)
                .leftJoin(participant).on(participant.roomJpaEntity.eq(room))
                .where(where)
                .groupBy(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        room.recruitCount,
                        room.startDate,
                        room.categoryJpaEntity.value
                )
                .orderBy(
                        // 1차 정렬 : 설정된 정렬 조건, 2차 정렬 : 방이름으로 방 검색 > 책제목으로 방 검색
                        toOrderSpecifier(pageable.getSort(), room, memberCountExpr),
                        priorityExpr.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // TODO : 추후에 오프셋 페이징이 아니라, 키셋 페이징 기법 도입 검토

        // 3. Tuple → DTO 매핑
        List<RoomSearchResponse.RoomSearchResult> content = tuples.stream()
                .map(t -> new RoomSearchResponse.RoomSearchResult(
                        t.get(room.roomId),
                        t.get(book.imageUrl),
                        t.get(room.title),
                        // 참여자 수를 int로 캐스팅
                        t.get(memberCountExpr).intValue(),
                        t.get(room.recruitCount),
                        // 모집마감일 까지 남은 시간 포맷
                        DateUtil.formatAfterTime(t.get(room.startDate)),
                        t.get(room.categoryJpaEntity.value)
                ))
                .toList();

        // 4. 전체 개수 조회 (페이징 정보 계산용)
        Long totalCount = queryFactory
                .select(room.count())
                .from(room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .fetchOne();
        long total = (totalCount != null) ? totalCount : 0L;

        // 5. PageImpl 생성하여 반환
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 지원하는 정렬 키에 대해, 미리 정의된 Q 필드를 반환합니다.
     * sort가 없으면 '마감 임박순' 으로 기본 처리합니다.
     */
    private OrderSpecifier<?> toOrderSpecifier(Sort sort, QRoomJpaEntity room, NumberExpression<Long> memberCountExpr) {
        // sort 파라미터가 없으면 기본 마감 임박순
        if (sort.isUnsorted()) {
            return room.startDate.asc();
        }

        // 클라이언트가 보낸 첫 번째 sort 키를 꺼냅니다.
        String key = sort.stream().findFirst().get().getProperty();

        switch (key) {
            case "memberCount":
                // user_rooms 테이블에서 현재 참여자 수 집계 → 내림차순
                return new OrderSpecifier<>(Order.DESC, memberCountExpr);
            case "deadLine":
            default:
                // deadLine: 마감 임박순 = startDate 빠른 순서대로(오름차순)
                return room.startDate.asc();
        }
    }

    @Override
    public List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Long roomId, String category, int count) {
        NumberExpression<Long> memberCountExpr = participant.roomParticipantId.count();
        List<Tuple> tuples = queryFactory
                .select(room.roomId, room.title, memberCountExpr, room.recruitCount, room.startDate)
                .from(room)
                .leftJoin(participant).on(participant.roomJpaEntity.eq(room))
                .where(
                        room.categoryJpaEntity.value.eq(category)
                                .and(room.startDate.after(LocalDate.now()))     // 모집 마감 시각 > 현재 시각
                                .and(room.roomId.ne(roomId))       // 현재 방 제외
                )
                .groupBy(room.roomId, room.title, room.recruitCount, room.startDate)
                .orderBy(room.startDate.asc())
                .limit(count)
                .fetch();

        return tuples.stream()
                .map(t -> RoomRecruitingDetailViewResponse.RecommendRoom.builder()
                        .roomImageUrl(null)     // roomImageUrl은 추후 구현
                        .roomName(t.get(room.title))
                        .memberCount(t.get(memberCountExpr).intValue())
                        .recruitCount(t.get(room.recruitCount))
                        .recruitEndDate(DateUtil.formatAfterTime(t.get(room.startDate)))
                        .build())
                .toList();
    }

    @Override
    public Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate date, Pageable pageable) {

        QRoomParticipantJpaEntity userRoomSub = new QRoomParticipantJpaEntity("userRoomSub");

        // 1. 검색 조건(where) 조립
        // 유저가 참여한 방만: userId 조건
        // 활동 기간 중인 방만: startDate ≤ today ≤ endDate
        BooleanBuilder where = new BooleanBuilder();
        where.and(participant.userJpaEntity.userId.eq(userId));
        where.and(room.startDate.loe(date));
        where.and(room.endDate.goe(date));

        // TODO : Room 에 멤버 수 추가되면 로직 수정
        // 멤버 수 서브쿼리
        JPQLQuery<Long> memberCountSubQuery = JPAExpressions
                .select(userRoomSub.count())
                .from(userRoomSub)
                .where(userRoomSub.roomJpaEntity.roomId.eq(room.roomId));

        // 2. 페이징된 목록 조회
        List<Tuple> tuples = queryFactory
                .select(
                        room.roomId,
                        book.imageUrl,
                        room.title,
                        memberCountSubQuery,
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
        List<RoomGetHomeJoinedListResponse.RoomSearchResult> content = tuples.stream()
                .map(t -> RoomGetHomeJoinedListResponse.RoomSearchResult.builder()
                        .roomId(t.get(room.roomId))
                        .bookImageUrl(t.get(book.imageUrl))
                        .bookTitle(t.get(book.title))
                        .memberCount(Optional.ofNullable(t.get(memberCountSubQuery)).map(Number::intValue).orElse(1))
                        .userPercentage(Optional.ofNullable(t.get(participant.userPercentage))
                                        .map(val -> ((Number) val).doubleValue())
                                                .map(Math::round)
                                                .map(Long::intValue)
                                                .orElse(0))
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
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findRecruitingRoomsUserParticipated(
            Long userId, LocalDate lastDate, Long lastId, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(room.startDate.after(today));      // 유저가 참여한 방 && 모집중인 방
        DateExpression<LocalDate> cursorExpr = room.startDate;      // 커서 비교는 startDate(= 모집 마감일 - 1일)
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.asc(), room.roomId.asc()
        };
        Function<Tuple,RoomShowMineResponse.MyRoom> mapper = t -> new RoomShowMineResponse.MyRoom(      // tuple -> DTO 매핑 함수
                t.get(room.roomId),
                t.get(book.imageUrl),
                t.get(room.title),
                t.get(room.memberCount),
                DateUtil.formatAfterTime(t.get(cursorExpr))
        );
        return sliceQuery(base, cursorExpr, mapper, lastDate, lastId, pageSize, true, orders);
    }

    // 2) 진행중인 방
    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findPlayingRoomsUserParticipated(
            Long userId, LocalDate lastDate, Long lastId, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(room.startDate.loe(today))
                .and(room.endDate.goe(today));      // 유저가 참여한 방 && 현재 진행중인 방
        DateExpression<LocalDate> cursorExpr = room.endDate;        // 커서 비교는 endDate(= 진행 마감일)
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.asc(), room.roomId.asc()
        };
        Function<Tuple,RoomShowMineResponse.MyRoom> mapper = t -> new RoomShowMineResponse.MyRoom(      // tuple -> DTO 매핑 함수
                t.get(room.roomId),
                t.get(book.imageUrl),
                t.get(room.title),
                t.get(room.memberCount),
                DateUtil.formatAfterTime(t.get(cursorExpr))
        );
        return sliceQuery(base, cursorExpr, mapper, lastDate, lastId, pageSize, true, orders);
    }

    // 3) 진행＋모집 통합
    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findPlayingAndRecruitingRoomsUserParticipated(
            Long userId, LocalDate lastDate, Long lastId, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression playing   = room.startDate.loe(today).and(room.endDate.goe(today));
        BooleanExpression recruiting = room.startDate.after(today);
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and( playing.or(recruiting) );     // 유저가 참여한 방 && 현재 진행중인 방 + 모집중인 방

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
        Function<Tuple,RoomShowMineResponse.MyRoom> mapper = t -> new RoomShowMineResponse.MyRoom(      // tuple -> DTO 매핑 함수
                t.get(room.roomId),
                t.get(book.imageUrl),
                t.get(room.title),
                t.get(room.memberCount),
                DateUtil.formatAfterTime(t.get(cursorExpr))
        );
        return sliceQuery(base, cursorExpr, mapper, lastDate, lastId, pageSize, true, orders);
    }

    // 4) 만료된 방
    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findExpiredRoomsUserParticipated(
            Long userId, LocalDate lastDate, Long lastId, int pageSize
    ) {
        LocalDate today = LocalDate.now();
        BooleanExpression base = participant.userJpaEntity.userId.eq(userId)
                .and(room.endDate.before(today));       // 유저가 참여한 방 && 만료된 방

        DateExpression<LocalDate> cursorExpr = room.endDate;
        OrderSpecifier<?>[] orders = new OrderSpecifier<?>[]{
                cursorExpr.desc(), room.roomId.desc()       // 만료된 방은 가장 최근에 만료된 방부터 반환
        };
        Function<Tuple,RoomShowMineResponse.MyRoom> mapper = t -> new RoomShowMineResponse.MyRoom(      // tuple -> DTO 매핑 함수
                t.get(room.roomId),
                t.get(book.imageUrl),
                t.get(room.title),
                t.get(room.memberCount),
                null    // 만료된 방은 endDate=null
        );
        return sliceQuery(base, cursorExpr, mapper, lastDate, lastId, pageSize, false, orders);
    }

    /**
     * 공통 커서+페이징 처리
     */
    private CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> sliceQuery(
            BooleanExpression baseCondition,
            DateExpression<LocalDate> cursorDateExpr,
            Function<Tuple,RoomShowMineResponse.MyRoom> mapper,
            LocalDate lastCursorDate,
            Long lastCursorId,
            int pageSize,
            boolean ascending,
            OrderSpecifier<?>... orderSpecs
    ) {
        BooleanBuilder where = new BooleanBuilder(baseCondition);       // baseCondition + 커서 기반으로 where 절 구성
        if (lastCursorDate != null && lastCursorId != null) {
            if (ascending) {        // 진행중, 모집중, 통합
                where.and(
                        cursorDateExpr.gt(lastCursorDate)       // lastCursorDate 보다 크거나
                                .or(
                                        cursorDateExpr.eq(lastCursorDate)
                                                .and(room.roomId.goe(lastCursorId))       // 같으면 id가 lastCursorId 보다 크거나 같은 것
                                )
                );
            } else {        // 내림차순일 때는 반대로 (만료된 방)
                where.and(
                        cursorDateExpr.lt(lastCursorDate)       // lastCursorDate 보다 작거나
                                .or(
                                        cursorDateExpr.eq(lastCursorDate)
                                                .and(room.roomId.loe(lastCursorId))      // 같으면 id가 lastCursorId 보다 작거나 같은 것
                                )
                );
            }
        }

        int fetchSize = pageSize + 1;
        List<Tuple> tuples = queryFactory
                .select(room.roomId, book.imageUrl, room.title, room.memberCount, cursorDateExpr)
                .from(participant)
                .join(participant.roomJpaEntity, room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(orderSpecs)
                .limit(fetchSize)
                .fetch();       // 직접 tuple 결과를 조회하므로 lazy 로딩 적용 대상 X

        boolean hasNext = tuples.size() == fetchSize;
        List<RoomShowMineResponse.MyRoom> content = tuples.stream()
                .limit(pageSize)
                .map(mapper)
                .toList();      // pageSize 만큼만 dto로 매핑

        // 커서 값 세팅
        LocalDate nextDate = null;
        Long nextId = null;
        if (hasNext) {
            Tuple next = tuples.get(pageSize);
            nextDate = next.get(cursorDateExpr);
            nextId   = next.get(room.roomId);
        }

        return new CursorSliceOfMyRoomView<>(
                content,
                PageRequest.of(0, pageSize),
                hasNext,
                nextDate,
                nextId
        );
    }
}
