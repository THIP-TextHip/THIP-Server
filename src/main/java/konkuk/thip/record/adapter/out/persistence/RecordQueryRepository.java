package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RecordQueryRepository {

    Slice<PostJpaEntity> findPostsByRoom(Long roomId, String type, String sort, Integer pageStart, Integer pageEnd, Long userId, Pageable pageable);

}
