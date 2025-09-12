package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.adapter.out.persistence.repository.PostJpaRepository;
import konkuk.thip.post.application.port.out.PostQueryPort;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryPersistenceAdapter implements PostQueryPort {

    private final PostJpaRepository postJpaRepository;

    @Override
    public PostQueryDto getPostQueryDtoByFeedId(Long feedId) {
        return postJpaRepository.getPostQueryDtoByFeedId(feedId);
    }

    @Override
    public PostQueryDto getPostQueryDtoByRecordId(Long recordId) {
        return postJpaRepository.getPostQueryDtoByRecordId(recordId);
    }

    @Override
    public PostQueryDto getPostQueryDtoByVoteId(Long voteId) {
        return postJpaRepository.getPostQueryDtoByVoteId(voteId);
    }

}
