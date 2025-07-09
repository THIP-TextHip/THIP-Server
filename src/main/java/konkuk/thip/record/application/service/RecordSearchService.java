package konkuk.thip.record.application.service;

import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
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
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final PostLikeCommandPort postLikeCommandPort;
    private final CommentCommandPort commentCommandPort;
    private final VoteCommandPort voteCommandPort;

    private static final int PAGE_SIZE = 10;

    @Override
    public RecordSearchResponse search(RecordSearchQuery query) {
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

        // 5. 페이지 범위에 따라 서브리스트 생성 (pageNum이 전체 페이지 수를 초과할 경우 빈 리스트 반환)
        List<RecordSearchResponse.PostDto> pagedList = fromIndex >= combinedPosts.size() ? new ArrayList<>() : combinedPosts.subList(fromIndex, toIndex);

        boolean isFirst = pageNum == 1;
        boolean isLast = toIndex >= combinedPosts.size();
        if (isLast) {
            pageSize = pagedList.size(); // 마지막 페이지일 경우 페이지 크기 업데이트
        }

        return RecordSearchResponse.of(pagedList, pageNum, pageSize, isFirst, isLast);
    }

    private RecordSearchResponse.PostDto createRecordDto(Record record, Long userId) {
        User user = userCommandPort.findById(record.getCreatorId());
        int likeCount = postLikeCommandPort.countByPostIdAndUserId(record.getId());
        int commentCount = commentCommandPort.countByPostIdAndUserId(record.getId(), record.getCreatorId());
        boolean isLiked = postLikeCommandPort.existsByPostIdAndUserId(userId, record.getId());
        boolean isWriter = record.getCreatorId().equals(userId);
        return RecordDto.of(record, user, likeCount, commentCount, isLiked, isWriter);
    }

    private RecordSearchResponse.PostDto createVoteDto(Vote vote, Long userId) {
        User user = userCommandPort.findById(vote.getCreatorId());
        int likeCount = postLikeCommandPort.countByPostIdAndUserId(vote.getId());
        int commentCount = commentCommandPort.countByPostIdAndUserId(vote.getId(), vote.getCreatorId());
        boolean isLiked = postLikeCommandPort.existsByPostIdAndUserId(userId, vote.getId());
        boolean isWriter = vote.getCreatorId().equals(userId);

        List<VoteItem> voteItems = voteCommandPort.findVoteItemsByVoteId(vote.getId());
        int totalCount = voteItems.stream().mapToInt(VoteItem::getCount).sum();

        List<VoteDto.VoteItemDto> voteItemDtos = voteItems.stream()
                .map(item -> VoteDto.VoteItemDto.of(item, item.calculatePercentage(totalCount), voteCommandPort.isUserVoted(userId, item.getId())))
                .toList();

        return VoteDto.of(vote, user, likeCount, commentCount, isLiked, isWriter, voteItemDtos);
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
