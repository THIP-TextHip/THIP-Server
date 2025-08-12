package konkuk.thip.common.post.service;

import konkuk.thip.common.annotation.HelperService;
import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
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
}
