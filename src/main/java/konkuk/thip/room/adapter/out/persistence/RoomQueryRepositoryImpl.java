package konkuk.thip.room.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserRoomJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoomQueryRepositoryImpl implements RoomQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QUserRoomJpaEntity userRoom = QUserRoomJpaEntity.userRoomJpaEntity;


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

        NumberExpression<Long> memberCountExpr = userRoom.userRoomId.count();       // 방 별 멤버수 표현식

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
                .leftJoin(userRoom).on(userRoom.roomJpaEntity.eq(room))
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
        NumberExpression<Long> memberCountExpr = userRoom.userRoomId.count();
        List<Tuple> tuples = queryFactory
                .select(room.roomId, room.title, memberCountExpr, room.recruitCount, room.startDate)
                .from(room)
                .leftJoin(userRoom).on(userRoom.roomJpaEntity.eq(room))
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

        QUserRoomJpaEntity userRoomSub = new QUserRoomJpaEntity("userRoomSub");

        // 1. 검색 조건(where) 조립
        // 유저가 참여한 방만: userId 조건
        // 활동 기간 중인 방만: startDate ≤ today ≤ endDate
        BooleanBuilder where = new BooleanBuilder();
        where.and(userRoom.userJpaEntity.userId.eq(userId));
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
                        userRoom.userPercentage
                )
                .from(userRoom)
                .join(userRoom.roomJpaEntity, room)
                .join(room.bookJpaEntity, book)
                .where(where)
                .orderBy(
                        userRoom.userPercentage.desc(), // 진행률 높은 순(내림차순)
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
                        .userPercentage(Optional.ofNullable(t.get(userRoom.userPercentage))
                                        .map(val -> ((Number) val).doubleValue())
                                                .map(Math::round)
                                                .map(Long::intValue)
                                                .orElse(0))
                        .build()
                )
                .toList();

        // 4. 전체 개수 조회 (페이징 정보 계산용)
        Long totalCount = queryFactory
                .select(userRoom.count())
                .from(userRoom)
                .join(userRoom.roomJpaEntity, room)
                .where(where)
                .fetchOne();
        long total = (totalCount != null) ? totalCount : 0L;

        // 5. PageImpl 생성하여 반환
        return new PageImpl<>(content, pageable, total);
    }
}
