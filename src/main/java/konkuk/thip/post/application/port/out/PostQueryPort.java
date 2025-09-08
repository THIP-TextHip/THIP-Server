package konkuk.thip.post.application.port.out;

import konkuk.thip.post.application.port.out.dto.PostQueryDto;

public interface PostQueryPort {
    PostQueryDto getPostQueryDtoByFeedId(Long feedId);

    PostQueryDto getPostQueryDtoByRecordId(Long recordId);
    PostQueryDto getPostQueryDtoByVoteId(Long voteId);
}
