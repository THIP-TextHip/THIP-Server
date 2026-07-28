package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

import static konkuk.thip.comment.adapter.out.persistence.CommentCacheKey.ROOT_COMMENTS;

/**
 * 댓글 캐시 전담 컴포넌트
 * - Spring AOP 프록시가 정상 동작하도록 별도 컴포넌트로 분리
 * - CommentQueryPersistenceAdapter에서 외부 호출을 통해 캐싱 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCacheAdapter {

    private final CommentJpaRepository commentJpaRepository;

    /**
     * 루트 댓글 첫 페이지 조회 (캐싱)
     * - 캐시 키: postId
     * - 캐시 조건: 결과가 null이 아니고 비어있지 않을 때만
     */
    @Cacheable(
            value = CommentCacheKey.ROOT_COMMENTS,
            key = "#postId",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<CommentQueryDto> findFirstPageRootCommentsFromCache(Long postId, int pageSize) {
        log.debug("Cache miss - Loading first page root comments from DB for postId: {}", postId);

        return commentJpaRepository.findRootCommentsWithDeletedByCreatedAtDesc(postId, null, pageSize);
    }

    /**
     * 루트 댓글 캐시 삭제 (Evict)
     */
    @CacheEvict(
            value = CommentCacheKey.ROOT_COMMENTS,
            key = "#postId"
    )
    public void evictRootCommentsCache(Long postId) {
        log.debug("Cache Evict - Removing root comments cache for postId: {}", postId);
    }
}

