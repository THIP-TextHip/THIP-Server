package konkuk.thip.post.adapter.out.persistence.repository;

import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long>, PostQueryRepository {
}
