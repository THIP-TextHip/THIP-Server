package konkuk.thip.comment.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.config.TestQuerydslConfig;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestQuerydslConfig.class, CommentCommandPersistenceAdapter.class})
class CommentCommandPersistenceAdapterTest {

    @Autowired CommentCommandPersistenceAdapter adapter;    // repository 인터페이스가 아니므로 자동 스캔 X -> import 해줘야함
    @Autowired CommentJpaRepository commentJpaRepository;
    @Autowired CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired BookJpaRepository bookJpaRepository;
    @Autowired FeedJpaRepository feedJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;
    @Autowired RecordJpaRepository recordJpaRepository;
    @Autowired VoteJpaRepository voteJpaRepository;

    @MockitoBean CommentMapper commentMapper;   // Mock bean 으로 설정

    @Autowired EntityManager em;

    @Test
    @DisplayName("deleteAllByUserId: Post.commentCount는 targetUser의 댓글 수만큼 감소하고, 해당 댓글들은 INACTIVE 처리된다.")
    void deleteAllByUserId_updatesPostCount_andSoftDeletesComments() {
        // given
        UserJpaEntity targetUser = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));
        UserJpaEntity otherUser  = userJpaRepository.save(TestEntityFactory.createUser(Alias.WRITER));

        BookJpaEntity bookJpaEntity = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity feedJpaEntity = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, bookJpaEntity, true));

        CommentJpaEntity c1 = commentJpaRepository.save(TestEntityFactory.createComment(feedJpaEntity, targetUser, PostType.FEED));
        CommentJpaEntity c2 = commentJpaRepository.save(TestEntityFactory.createComment(feedJpaEntity, targetUser, PostType.FEED));
        CommentJpaEntity c3 = commentJpaRepository.save(TestEntityFactory.createComment(feedJpaEntity, otherUser, PostType.FEED));

        // 피드의 commentCount update
        feedJpaEntity.setCommentCount(3);

        // when
        adapter.deleteAllByUserId(targetUser.getUserId());

        // then
        em.flush();  // 더티체킹 → DB 반영
        em.clear();

        // 1) 게시글의 commentCount가 3 -> 1 로 감소했는지 확인 (targetUser 댓글 2개 삭제)
        FeedJpaEntity updated = feedJpaRepository.findByPostId(feedJpaEntity.getPostId()).orElseThrow();
        assertThat(updated.getCommentCount()).isEqualTo(1);

        // 2) targetUser의 댓글은 INACTIVE, otherUser의 댓글은 ACTIVE
        List<CommentJpaEntity> all = commentJpaRepository.findAll(); // 상태 필터링 AOP 없다면 전부 보임
        long targetInactive = all.stream()
                .filter(c -> c.getUserJpaEntity().getUserId().equals(targetUser.getUserId()))
                .filter(c -> StatusType.INACTIVE.name().equals(c.getStatus().name())) // BaseJpaEntity.status 타입에 맞게 비교
                .count();

        long otherActive = all.stream()
                .filter(c -> c.getUserJpaEntity().getUserId().equals(otherUser.getUserId()))
                .filter(c -> StatusType.ACTIVE.name().equals(c.getStatus().name()))
                .count();

        assertThat(targetInactive).isEqualTo(2); // targetUser가 단 댓글 2개 모두 INACTIVE
        assertThat(otherActive).isEqualTo(1);    // otherUser가 단 댓글 1개는 그대로 ACTIVE
    }
}
