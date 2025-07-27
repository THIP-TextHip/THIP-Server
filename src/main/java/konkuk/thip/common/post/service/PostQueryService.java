package konkuk.thip.common.post.service;

import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final FeedCommandPort feedCommandPort;
    private final RecordCommandPort recordCommandPort;
    private final VoteCommandPort voteCommandPort;

    public CommentCountUpdatable findPost(PostType type, Long postId) {
        return switch (type) {
            case FEED -> feedCommandPort.getByIdOrThrow(postId);
            case RECORD -> recordCommandPort.getByIdOrThrow(postId);
            case VOTE -> voteCommandPort.getByIdOrThrow(postId);
        };
    }
}
