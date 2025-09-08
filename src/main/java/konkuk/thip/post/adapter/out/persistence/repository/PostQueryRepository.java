package konkuk.thip.post.adapter.out.persistence.repository;

import konkuk.thip.post.application.port.out.dto.PostQueryDto;

public interface PostQueryRepository {
    PostQueryDto getPostQueryDtoByFeedId(Long feedId);

    PostQueryDto getPostQueryDtoByRecordId(Long recordId);
    PostQueryDto getPostQueryDtoByVoteId(Long voteId);
}
