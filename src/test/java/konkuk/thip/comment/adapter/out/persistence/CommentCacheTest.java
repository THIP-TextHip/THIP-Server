package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.List;

import static konkuk.thip.common.util.TestEntityFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 루트 댓글 조회 캐싱 동작 검증 테스트
 * - CommentCacheAdapter를 통한 캐싱 동작 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[통합] CommentCacheAdapter 테스트")
class CommentCacheTest {

    @Autowired
    private CommentCacheAdapter commentCacheAdapter;

    @MockitoSpyBean
    private CommentJpaRepository commentJpaRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("첫페이지 루트 댓글 조회 시, 캐시를 먼저 조회한다.")
    void rootCommentCachingTest() {
        // given: 테스트 데이터 준비
        UserJpaEntity user = createUser(Alias.WRITER, "테스터");
        entityManager.persist(user);

        PostJpaEntity post = createRecord(user, null);
        entityManager.persist(post);

        CommentJpaEntity comment1 = createComment(post, user, PostType.RECORD, "댓글1", 0);
        CommentJpaEntity comment2 = createComment(post, user, PostType.RECORD, "댓글2", 0);
        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();
        entityManager.clear();

        Long postId = post.getPostId();
        int pageSize = 10;

        // when: 첫 번째 조회 (Cache Miss - DB 호출)
        List<CommentQueryDto> firstResult = commentCacheAdapter.findFirstPageRootCommentsFromCache(postId, pageSize);

        // then: DB 조회 1회 발생
        verify(commentJpaRepository, times(1))
                .findRootCommentsWithDeletedByCreatedAtDesc(postId, null, pageSize);

        // then: 첫 번째 조회 결과 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult).hasSize(2);
        assertThat(firstResult.get(0).content()).isEqualTo("댓글2");
        assertThat(firstResult.get(1).content()).isEqualTo("댓글1");

        // when: 두 번째 조회 (Cache Hit - DB 호출 안 함)
        List<CommentQueryDto> secondResult = commentCacheAdapter.findFirstPageRootCommentsFromCache(postId, pageSize);

        // then: DB 조회가 추가로 발생하지 않음 (여전히 1회) - 캐시에서 조회되었음을 의미
        verify(commentJpaRepository, times(1))
                .findRootCommentsWithDeletedByCreatedAtDesc(postId, null, pageSize);

        // 두 번째 조회 결과도 동일한 사이즈와 내용이어야 함
        assertThat(secondResult).isNotNull();
        assertThat(secondResult).hasSize(2);
        assertThat(secondResult.get(0).content()).isEqualTo("댓글2");
        assertThat(secondResult.get(1).content()).isEqualTo("댓글1");
    }
}

