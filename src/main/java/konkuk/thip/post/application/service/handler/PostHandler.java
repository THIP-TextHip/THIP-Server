package konkuk.thip.post.application.service.handler;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Vote;
import lombok.RequiredArgsConstructor;

@HelperService
@RequiredArgsConstructor
public class PostHandler {

    private final FeedCommandPort feedCommandPort;
    private final RecordCommandPort recordCommandPort;
    private final VoteCommandPort voteCommandPort;

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
            case FEED -> feedCommandPort.getPostQueryDtoById(postId);
            default -> recordCommandPort.getPostQueryDtoById(postId);
        };
    }
}
