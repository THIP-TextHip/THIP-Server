package konkuk.thip.record.application.service;

import com.sun.jdi.request.InvalidRequestStateException;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordDto;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.in.web.response.VoteDto;
import konkuk.thip.record.adapter.out.persistence.RecordSearchSortParams;
import konkuk.thip.record.adapter.out.persistence.RecordSearchTypeParams;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import konkuk.thip.vote.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSearchService implements RecordSearchUseCase {

    private final RecordQueryPort recordQueryPort;
    private final BookCommandPort bookCommandPort;
    private final VoteCommandPort voteCommandPort;
    private final VoteQueryPort voteQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public RecordSearchResponse search(Long roomId, String type, String sort, Integer pageStart, Integer pageEnd, Boolean isOverview, Integer pageNum, Long userId) {
        // 1. 유효성 검사
        validatePageStartAndEnd(pageStart, pageEnd, isOverview);
        pageNum = validatePageNum(pageNum);

        // isOverview가 false일 때 pageStart와 pageEnd가 모두 null이면 전체 페이지 조회
        if(!isOverview && (pageStart == null || pageEnd == null)) {
            Book book = bookCommandPort.findBookByRoomId(roomId);
            pageStart = 1;
            pageEnd = book.getPageCount();
        }

        // 2. 정렬 조건 확인
        RecordSearchSortParams sortVal = sort != null ? RecordSearchSortParams.from(sort) : RecordSearchSortParams.LATEST;
        RecordSearchTypeParams typeVal = type != null ? RecordSearchTypeParams.from(type) : RecordSearchTypeParams.GROUP;

        // 3. 페이지 인덱스 및 Pageable 객체 생성
        int pageIndex = pageNum - 1;
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE, buildSort(sortVal));

        // 4. 게시글 조회
        Page<RecordSearchResponse.RecordSearchResult> result = recordQueryPort.findRecordsByRoom(
                roomId,
                typeVal.getValue(),
                pageStart,
                pageEnd,
                isOverview,
                userId,
                pageable
        );

        // 5. isLiked와 voteItems를 포함한 최종 결과 리스트 생성
        List<RecordSearchResponse.RecordSearchResult> finalList = result.getContent().stream()
                .map(post -> {
                    if (post instanceof RecordDto recordDto) {
                        boolean isLiked = checkIfLiked(recordDto.recordId(), userId);
                        return recordDto.withIsLiked(isLiked);
                    } else if (post instanceof VoteDto voteDto) {
                        boolean isLiked = checkIfLiked(voteDto.voteId(), userId);
                        List<VoteItem> items = voteCommandPort.findVoteItemsByVoteId(voteDto.voteId());
                        List<VoteDto.VoteItemDto> voteItemDtos = mapToVoteItemDtos(items, userId, voteDto.voteId());
                        return voteDto.withIsLikedAndVoteItems(isLiked, voteItemDtos);
                    } else {
                        throw new InvalidStateException(ErrorCode.API_SERVER_ERROR, new IllegalStateException("지원되지 않는 게시물 타입입니다"));
                    }
                })
                .map(finalResult -> (RecordSearchResponse.RecordSearchResult) finalResult)
                .toList();

        // 6. response 구성
        return new RecordSearchResponse(
                finalList,
                pageNum,
                result.getNumberOfElements(),
                result.isLast(),
                result.isFirst());
    }

    private void validatePageStartAndEnd(Integer pageStart, Integer pageEnd, Boolean isOverview) {
        if((pageStart != null && pageEnd == null) || (pageStart == null && pageEnd != null)) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart와 pageEnd는 모두 설정되거나(특정 페이지 조회) 모두 설정되지 않아야 합니다.(전체 페이지 조회)"));
        }
        if (pageStart != null && pageEnd != null && pageStart > pageEnd) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart는 pageEnd보다 작거나 같아야 합니다."));
        }
        if (isOverview && (pageStart != null || pageEnd != null)) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart와 pageEnd는 isOverview가 true일 때 유효한 파라미터가 아닙니다."));
        }
    }

    private Integer validatePageNum(Integer pageNum) {
        if (pageNum == null) {
            return 1; // 기본값으로 첫 페이지 반환
        }
        if (pageNum < 1) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new InvalidRequestStateException("pageNum은 1 이상의 값이어야 합니다."));
        }
        return pageNum;
    }

    private Sort buildSort(RecordSearchSortParams sort) {
        return switch (sort) {
            case LIKE -> Sort.by(Sort.Direction.DESC, "likeCount");
            case COMMENT -> Sort.by(Sort.Direction.DESC, "commentCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private List<VoteDto.VoteItemDto> mapToVoteItemDtos(List<VoteItem> items, Long userId, Long voteId) {
        int total = items.stream().mapToInt(VoteItem::getCount).sum();
        return items.stream()
                .map(item -> VoteDto.VoteItemDto.of(
                        item,
                        item.calculatePercentage(total),
                        voteQueryPort.isUserVoted(userId, voteId)
                        )
                )
                .toList();
    }

    private boolean checkIfLiked(Long postId, Long userId) {
        return postLikeQueryPort.existsByPostIdAndUserId(postId, userId);
    }
}


