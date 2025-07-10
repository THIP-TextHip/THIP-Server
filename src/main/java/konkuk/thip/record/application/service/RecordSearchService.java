package konkuk.thip.record.application.service;

import com.sun.jdi.request.InvalidRequestStateException;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.record.adapter.in.web.response.RecordDto;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.in.web.response.VoteDto;
import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchResult;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordSearchService implements RecordSearchUseCase {

    private final RecordQueryPort recordQueryPort;
    private final UserCommandPort userCommandPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final CommentQueryPort commentQueryPort;
    private final VoteCommandPort voteCommandPort;
    private final VoteQueryPort voteQueryPort;

    private final DateUtil dateUtil;

    private static final int PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public RecordSearchResponse search(RecordSearchQuery query) {
        validateQueryParams(query);

        // 1. 파라미터에 따라 Record와 Vote를 조회
        RecordSearchResult recordSearchResult = recordQueryPort.findRecordsByRoom(
                query.roomId(),
                Optional.ofNullable(query.type()).orElse("group"),
                query.pageStart(),
                query.pageEnd(),
                query.userId(),
                query.pageNum()
        );

        List<Record> records = recordSearchResult.records();
        List<Vote> votes = recordSearchResult.votes();

        List<RecordSearchResponse.PostDto> combinedPosts = new ArrayList<>();

        // 2. Record와 Vote를 PostDto로 변환하여 combinedPosts에 추가
        for (Record record : records) {
            combinedPosts.add(createRecordDto(record, query.userId()));
        }

        for (Vote vote : votes) {
            combinedPosts.add(createVoteDto(vote, query.userId()));
        }

        // 3. sort에 따라 정렬 (기본값은 "latest")
        String sort = Optional.ofNullable(query.sort()).orElse("latest");
        sortCombinedPosts(sort, combinedPosts);

        // 4. 페이지네이션 변수 설정
        int pageNum = Optional.ofNullable(query.pageNum()).orElse(1);
        int pageSize = PAGE_SIZE;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, combinedPosts.size());

        // 5. 페이지 범위에 따라 서브리스트 생성
        List<RecordSearchResponse.PostDto> pagedList = fromIndex >= combinedPosts.size() ? new ArrayList<>() : combinedPosts.subList(fromIndex, toIndex);

        boolean isFirst = pageNum == 1;
        boolean isLast = toIndex >= combinedPosts.size();
        if (isLast) {
            pageSize = pagedList.size(); // 마지막 페이지일 경우 페이지 크기 업데이트
        }

        return RecordSearchResponse.of(pagedList, pageNum, pageSize, isFirst, isLast);
    }

    private void validateQueryParams(RecordSearchQuery query) {
        if(query.pageStart() != null && query.pageEnd() == null || query.pageStart() == null && query.pageEnd() != null) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new InvalidRequestStateException("pageStart와 pageEnd는 모두 설정되어야 합니다."));
        }

        if(query.pageNum() != null && query.pageNum() < 1) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new InvalidRequestStateException("pageNum은 1 이상의 값이어야 합니다."));
        }

        if(query.sort() != null && !List.of("latest", "like", "comment").contains(query.sort())) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new InvalidRequestStateException("sort는 'latest', 'like', 'comment' 중 하나여야 합니다."));
        }

        if(query.type() != null && !List.of("group", "mine").contains(query.type())) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new InvalidRequestStateException("type은 'group', 'mine' 중 하나여야 합니다."));
        }
    }

    private RecordSearchResponse.PostDto createRecordDto(Record record, Long userId) {
        User user = userCommandPort.findById(record.getCreatorId());
        int likeCount = postLikeQueryPort.countByPostId(record.getId());
        int commentCount = commentQueryPort.countByPostId(record.getId());
        boolean isLiked = postLikeQueryPort.existsByPostIdAndUserId(userId, record.getId());
        boolean isWriter = record.getCreatorId().equals(userId);
        return RecordDto.of(record, dateUtil.formatLastActivityTime(record.getCreatedAt()), user, likeCount, commentCount, isLiked, isWriter);
    }

    private RecordSearchResponse.PostDto createVoteDto(Vote vote, Long userId) {
        User user = userCommandPort.findById(vote.getCreatorId());
        int likeCount = postLikeQueryPort.countByPostId(vote.getId());
        int commentCount = commentQueryPort.countByPostId(vote.getId());
        boolean isLiked = postLikeQueryPort.existsByPostIdAndUserId(userId, vote.getId());
        boolean isWriter = vote.getCreatorId().equals(userId);

        List<VoteItem> voteItems = voteCommandPort.findVoteItemsByVoteId(vote.getId());
        int totalCount = voteItems.stream().mapToInt(VoteItem::getCount).sum();

        List<VoteDto.VoteItemDto> voteItemDtos = voteItems.stream()
                .map(item -> VoteDto.VoteItemDto.of(item, item.calculatePercentage(totalCount), voteQueryPort.isUserVoted(userId, item.getId())))
                .toList();

        return VoteDto.of(vote, dateUtil.formatLastActivityTime(vote.getCreatedAt()), user, likeCount, commentCount, isLiked, isWriter, voteItemDtos);
    }

    private void sortCombinedPosts(String sort, List<RecordSearchResponse.PostDto> combinedPosts) {
        switch (sort) {
            case "like" -> combinedPosts.sort(
                    Comparator.comparingInt(RecordSearchResponse.PostDto::likeCount).reversed()
            );
            case "comment" -> combinedPosts.sort(
                    Comparator.comparingInt(RecordSearchResponse.PostDto::commentCount).reversed()
            );
            default -> combinedPosts.sort(
                    Comparator.comparing(RecordSearchResponse.PostDto::postDate).reversed()
            );
        }
    }
}
