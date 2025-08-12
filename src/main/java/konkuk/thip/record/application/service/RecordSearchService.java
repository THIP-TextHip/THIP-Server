package konkuk.thip.record.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.out.persistence.RecordSearchSortParams;
import konkuk.thip.record.adapter.out.persistence.RecordSearchTypeParams;
import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import konkuk.thip.vote.application.port.out.dto.VoteItemQueryDto;
import konkuk.thip.vote.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static konkuk.thip.common.post.PostType.RECORD;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSearchService implements RecordSearchUseCase {

    private final RecordQueryPort recordQueryPort;
    private final BookCommandPort bookCommandPort;
    private final VoteQueryPort voteQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String BLURRED_STRING = "여긴 못 지나가지롱~~";

    @Override
    @Transactional(readOnly = true)
    public RecordSearchResponse search(RecordSearchQuery recordSearchQuery) {
        // RecordSearchQuery에서 필드 반환
        Integer pageStart = recordSearchQuery.pageStart();
        Integer pageEnd = recordSearchQuery.pageEnd();
        Boolean isOverview = recordSearchQuery.isOverview();
        Boolean isPageFilter = recordSearchQuery.isPageFilter();
        Long userId = recordSearchQuery.userId();
        Long roomId = recordSearchQuery.roomId();

        Book book = bookCommandPort.findBookByRoomId(recordSearchQuery.roomId());
        RoomParticipant roomParticipant = roomParticipantCommandPort.findByUserIdAndRoomIdOptional(recordSearchQuery.userId(), recordSearchQuery.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_ACCESS_FORBIDDEN));

        // cursor 파싱
        Cursor cursor = Cursor.from(recordSearchQuery.nextCursor(), DEFAULT_PAGE_SIZE);

        // Type에 따라 그룹기록, 내 기록 조회 분기처리
        CursorBasedList<PostQueryDto> cursorBasedList = switch(RecordSearchTypeParams.from(recordSearchQuery.type())) {
            case GROUP -> {
                validateGroupRecordFilters(pageStart, pageEnd, isPageFilter, isOverview, book.getPageCount(), roomParticipant.getUserPercentage());
                // 총평 보기가 아닌 경우, pageStart와 pageEnd를 default 값 주입
                if(!isOverview) {
                    if(pageStart == null) {
                        pageStart = 0;
                    }
                    if(pageEnd == null) {
                        pageEnd = book.getPageCount();
                    }
                }
                yield getGroupRecordBySortParams(recordSearchQuery.sort(), roomId, userId, cursor, pageStart, pageEnd, isOverview);
            }
            case MINE -> {
                validateMyRecordFilters(pageStart, pageEnd, isPageFilter, isOverview, recordSearchQuery.sort());
                yield recordQueryPort.searchMyRecords(roomId, userId, cursor);
            }
        };

        // VoteItem 한번에 조회 (투표 게시물에 대한 투표 항목 조회)
        Map<Long, List<VoteItemQueryDto>> voteItemQueryMap = voteQueryPort.findVoteItemsByVoteIds(cursorBasedList.contents().stream()
                .filter(postQueryDto -> postQueryDto.postType().equals("VOTE"))
                .map(PostQueryDto::postId)
                .collect(Collectors.toSet()), userId);

        // 사용자가 좋아요를 누른 게시물 ID 목록 조회
        Set<Long> likedPostIds = postLikeQueryPort.findPostIdsLikedByUser(cursorBasedList.contents().stream()
                .map(PostQueryDto::postId)
                .collect(Collectors.toSet()), userId);

        // 게시물 DTO 변환
        var postDtos = cursorBasedList.contents().stream()
                .map(postQueryDto -> toPostDto(postQueryDto, roomParticipant, userId, voteItemQueryMap, likedPostIds))
                .toList();

        // RecordSearchResponse 생성
        return RecordSearchResponse.builder()
                .roomId(roomId)
                .isbn(book.getIsbn())
                .isOverviewEnabled(roomParticipant.getUserPercentage() >= 80)
                .postList(postDtos)
                .nextCursor(cursorBasedList.nextCursor())
                .isLast(!cursorBasedList.hasNext())
                .build();
    }

    private CursorBasedList<PostQueryDto> getGroupRecordBySortParams(String sort, Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        return switch(RecordSearchSortParams.from(sort)) {
            case LATEST -> recordQueryPort.searchGroupRecordsByLatest(roomId, userId, cursor, pageStart, pageEnd, isOverview);
            case LIKE -> recordQueryPort.searchGroupRecordsByLike(roomId, userId, cursor, pageStart, pageEnd, isOverview);
            case COMMENT -> recordQueryPort.searchGroupRecordsByComment(roomId, userId, cursor, pageStart, pageEnd, isOverview);
        };
    }

    private RecordSearchResponse.PostDto toPostDto(PostQueryDto dto, RoomParticipant participant, Long userId, Map<Long, List<VoteItemQueryDto>> voteItemMap, Set<Long> likedPostIds) {
        boolean isLocked = participant.getCurrentPage() < dto.page();
        boolean isWriter = dto.userId().equals(userId);
        String content = isLocked ? createBlurredString(dto.content()) : dto.content();

        return RecordSearchResponse.PostDto.builder()
                .postId(dto.postId())
                .postDate(DateUtil.formatBeforeTime(dto.postDate()))
                .postType(dto.postType())
                .page(dto.page())
                .userId(dto.userId())
                .nickName(dto.nickName())
                .profileImageUrl(dto.profileImageUrl())
                .content(content)
                .likeCount(dto.likeCount())
                .commentCount(dto.commentCount())
                .isLiked(likedPostIds.contains(dto.postId()))
                .isWriter(isWriter)
                .isLocked(isLocked)
                .voteItems(getVoteItemDtosIfApplicable(dto, voteItemMap, isLocked))
                .build();
    }

    private List<RecordSearchResponse.PostDto.VoteItemDto> getVoteItemDtosIfApplicable(PostQueryDto dto, Map<Long, List<VoteItemQueryDto>> voteItemMap, boolean isLocked) {
        if (RECORD.getType().equals(dto.postType())) {
            return List.of();
        }

        List<VoteItemQueryDto> items = voteItemMap.getOrDefault(dto.postId(), List.of());
        return mapToVoteItemDtos(items, isLocked);
    }

    private List<RecordSearchResponse.PostDto.VoteItemDto> mapToVoteItemDtos(List<VoteItemQueryDto> items, boolean isLocked) {
        // voteCount를 모아 리스트로 변환
        List<Integer> counts = items.stream()
                .map(VoteItemQueryDto::voteCount)
                .toList();

        // 도메인에게 계산 위임
        List<Integer> percentages = VoteItem.calculatePercentages(counts);

        // 계산 결과를 이용해 DTO 조립
        return IntStream.range(0, items.size())
                .mapToObj(i -> RecordSearchResponse.PostDto.VoteItemDto.of(
                        items.get(i).voteItemId(),
                        isLocked ? createBlurredString(items.get(i).itemName()) : items.get(i).itemName(),
                        percentages.get(i),
                        items.get(i).isVoted()
                ))
                .toList();
    }

    private String createBlurredString(String contents) {
        if (contents == null || contents.isEmpty()) {
            return contents;
        }

        int originalLength = contents.length();
        int blurLen = BLURRED_STRING.length();

        // 필요한 전체 반복 횟수 계산
        int repeat = originalLength / blurLen;

        StringBuilder sb = new StringBuilder(originalLength);

        // 몫 만큼 반복
        for (int i = 0; i < repeat + 1; i++) {
            sb.append(BLURRED_STRING);
        }

        return sb.toString();
    }

    private void validateGroupRecordFilters(Integer pageStart, Integer pageEnd, Boolean isPageFilter, Boolean isOverview, int bookPageSize, double currentPercentage) {
        if(!isPageFilter && !isOverview) { // 어떤 필터도 적용되지 않는 경우
            if (pageStart != null || pageEnd != null) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("어떤 필터도 적용되지 않는 경우 pageStart와 pageEnd는 null이어야 합니다."));
            }
        }
        if(!isPageFilter && isOverview) { // 총평보기 필터만 적용된 경우
            if (pageStart != null || pageEnd != null) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("총평보기 필터만 적용된 경우 pageStart와 pageEnd는 null이어야 합니다."));
            }
            if (currentPercentage < 80) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("총평보기 필터가 적용된 경우 현재 독서 진행률은 80% 이상이어야 합니다."));
            }
        }
        if(isPageFilter && !isOverview) { // 페이지 필터만 적용된 경우는 pageStart와 pageEnd가 null이여도 됨
            if(pageStart != null && (pageStart < 0 || pageStart > bookPageSize)) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart는 책의 페이지 범위 내에 있어야 합니다."));
            }
            if(pageEnd != null && (pageEnd < 0 || pageEnd > bookPageSize)) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageEnd는 책의 페이지 범위 내에 있어야 합니다."));
            }
            if(pageStart != null && pageEnd != null && pageStart > pageEnd) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart는 pageEnd보다 작아야 합니다."));
            }
        }
        if(isPageFilter && isOverview) { // 페이지 필터와 총평보기 필터가 동시에 적용된 경우
            throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("페이지 필터와 총평보기 필터는 동시에 적용될 수 없습니다."));
        }
    }

    private void validateMyRecordFilters(Integer pageStart, Integer pageEnd, Boolean isPageFilter, Boolean isOverview, String sort) {
        // 모든 파라미터중 하나라도 null이 아닌 경우 예외 발생
        if (pageStart != null || pageEnd != null || isPageFilter || isOverview || sort != null) {
            throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("내 기록 조회에서는 roomId, type, cursor를 제외한 모든 파라미터는 null이어야 합니다."));
        }

    }
}


