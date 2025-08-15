package konkuk.thip.record.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.out.persistence.RecordSearchSortParams;
import konkuk.thip.record.adapter.out.persistence.RecordSearchTypeParams;
import konkuk.thip.record.application.mapper.RecordQueryMapper;
import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;
import konkuk.thip.record.application.service.validator.RecordAccessValidator;
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
import static konkuk.thip.common.post.PostType.VOTE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSearchService implements RecordSearchUseCase {

    private final RecordQueryPort recordQueryPort;
    private final BookCommandPort bookCommandPort;
    private final VoteQueryPort voteQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RecordAccessValidator recordAccessValidator;
    private final RecordQueryMapper recordQueryMapper;

    private static final int DEFAULT_PAGE_SIZE = 10;

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
                recordAccessValidator.validateGroupRecordFilters(pageStart, pageEnd, isPageFilter, isOverview, book.getPageCount(), roomParticipant.getUserPercentage());
                // 총평 보기가 아닌 경우, pageStart와 pageEnd를 default 값 주입
                if(!isOverview) {
                    if(pageStart == null) pageStart = 0;
                    if(pageEnd == null) pageEnd = book.getPageCount();
                }
                yield getGroupRecordBySortParams(recordSearchQuery.sort(), roomId, userId, cursor, pageStart, pageEnd, isOverview);
            }
            case MINE -> {
                recordAccessValidator.validateMyRecordFilters(pageStart, pageEnd, isPageFilter, isOverview, recordSearchQuery.sort());
                yield recordQueryPort.searchMyRecords(roomId, userId, cursor);
            }
        };

        // VoteItem 한번에 조회 (투표 게시물에 대한 투표 항목 조회)
        Map<Long, List<VoteItemQueryDto>> voteItemQueryMap = voteQueryPort.findVoteItemsByVoteIds(cursorBasedList.contents().stream()
                .filter(postQueryDto -> postQueryDto.postType().equals(VOTE.getType()))
                .map(PostQueryDto::postId)
                .collect(Collectors.toSet()), userId);

        // 사용자가 좋아요를 누른 게시물 ID 목록 조회
        Set<Long> likedPostIds = postLikeQueryPort.findPostIdsLikedByUser(cursorBasedList.contents().stream()
                .map(PostQueryDto::postId)
                .collect(Collectors.toSet()), userId);

        // 게시물 DTO 변환
        var postDtos = cursorBasedList.contents().stream()
                .map(dto -> {
                    boolean isLocked = recordAccessValidator.isLocked(roomParticipant.getCurrentPage(), dto.page());
                    boolean isWriter = dto.userId().equals(userId);
                    boolean isLiked  = likedPostIds.contains(dto.postId());
                    String content   = isLocked ? recordAccessValidator.createBlurredString(dto.content()) : dto.content();

                    List<RecordSearchResponse.PostDto.VoteItemDto> voteItems =
                            getVoteItemDtosIfApplicable(dto, voteItemQueryMap, isLocked);

                    return recordQueryMapper.toPostDto(dto, content, isLiked, isWriter, isLocked, voteItems);
                })
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

    // 그룹 기록을 정렬 파라미터에 따라 조회하는 메서드
    private CursorBasedList<PostQueryDto> getGroupRecordBySortParams(String sort, Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        return switch(RecordSearchSortParams.from(sort)) {
            case LATEST -> recordQueryPort.searchGroupRecordsByLatest(roomId, userId, cursor, pageStart, pageEnd, isOverview);
            case LIKE -> recordQueryPort.searchGroupRecordsByLike(roomId, userId, cursor, pageStart, pageEnd, isOverview);
            case COMMENT -> recordQueryPort.searchGroupRecordsByComment(roomId, userId, cursor, pageStart, pageEnd, isOverview);
        };
    }

    // 투표 게시물인 경우 VoteItem DTO 목록을 생성하는 메서드
    private List<RecordSearchResponse.PostDto.VoteItemDto> getVoteItemDtosIfApplicable(PostQueryDto dto, Map<Long, List<VoteItemQueryDto>> voteItemMap, boolean isLocked) {
        if (RECORD.getType().equals(dto.postType())) {
            return List.of();
        }

        List<VoteItemQueryDto> items = voteItemMap.getOrDefault(dto.postId(), List.of());
        return mapToVoteItemDtos(items, isLocked);
    }

    // VoteItemQueryDto 목록을 RecordSearchResponse.PostDto.VoteItemDto 목록으로 변환하는 메서드
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
                        isLocked ? recordAccessValidator.createBlurredString(items.get(i).itemName()) : items.get(i).itemName(),
                        percentages.get(i),
                        items.get(i).isVoted()
                ))
                .toList();
    }
}


