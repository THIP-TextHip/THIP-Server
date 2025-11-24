package konkuk.thip.post.application.service.handler;

import java.util.List;
import java.util.Map;
import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.post.application.port.out.PostQueryPort;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.roompost.domain.Vote;
import lombok.RequiredArgsConstructor;

@HelperService
@RequiredArgsConstructor
public class PostHandler {

    private final FeedCommandPort feedCommandPort;
    private final RecordCommandPort recordCommandPort;
    private final VoteCommandPort voteCommandPort;

    private final PostQueryPort postQueryPort;

    public CountUpdatable findPost(PostType type, Long postId) {
        return switch (type) {
            case FEED -> feedCommandPort.getByIdOrThrow(postId);
            case RECORD -> recordCommandPort.getByIdOrThrow(postId);
            case VOTE -> voteCommandPort.getByIdOrThrow(postId);
        };
    }

    public void updatePost(PostType type, CountUpdatable post) {
        switch (type) {
            case FEED -> feedCommandPort.update((Feed) post);
            case RECORD -> recordCommandPort.update((Record) post);
            case VOTE -> voteCommandPort.updateVote((Vote) post);
        }
    }

    public PostQueryDto getPostQueryDto(PostType type, Long postId) {
        return switch (type) {
            case FEED -> postQueryPort.getPostQueryDtoByFeedId(postId);
            case RECORD -> postQueryPort.getPostQueryDtoByRecordId(postId);
            case VOTE -> postQueryPort.getPostQueryDtoByVoteId(postId);
        };
    }

    public List<Long> findPostIdsByIds(PostType type, List<Long> postIds) {
        return switch(type) {
            case FEED -> feedCommandPort.findByIds(postIds);
            case RECORD -> recordCommandPort.findByIds(postIds);
            case VOTE -> voteCommandPort.findByIds(postIds);
        };
    }

    public void batchUpdateLikeCounts(PostType type, Map<Long, Integer> idToLikeCount) {
        switch(type) {
            case FEED -> feedCommandPort.batchUpdateLikeCounts(idToLikeCount);
            case RECORD -> recordCommandPort.batchUpdateLikeCounts(idToLikeCount);
            case VOTE -> voteCommandPort.batchUpdateLikeCounts(idToLikeCount);
        }
    }


}
