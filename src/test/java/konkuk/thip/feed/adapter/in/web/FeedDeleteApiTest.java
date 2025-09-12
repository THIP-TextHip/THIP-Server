package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.post.domain.PostType.FEED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 피드 삭제 api 통합 테스트")
class FeedDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;


    private UserJpaEntity user;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private CommentJpaEntity comment;

    @BeforeEach
    void setUp() {
        user = userJpaRepository.save(TestEntityFactory.createUser(Alias.ARTIST));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user, book, true,1,1,List.of("url1", "url2", "url3")));
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user,feed));
        comment = commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment,user));
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(user, feed));
        comment.updateLikeCount(1);
        commentJpaRepository.save(comment);
    }


    @Test
    @DisplayName("피드를 삭제하면 [soft delete]되고, 연관된 피드 태그 연관관계, 콘텐츠(사진), 댓글, 댓글 좋아요, 피드 저장관계도 모두 삭제된다")
    void deleteFeed_success() throws Exception {

        // when
        mockMvc.perform(delete("/feeds/{feedId}", feed.getPostId())
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk());


        // then: 1) 피드 soft delete (status=INACTIVE)
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findById(feed.getPostId()).orElse(null);
        assertThat(feedJpaEntity.getStatus()).isEqualTo(INACTIVE);

        // 4) 댓글 삭제 soft delete
        assertThat(commentJpaRepository.findById(comment.getCommentId())).isPresent();
        assertThat(commentJpaRepository.findById(comment.getCommentId()).get().getStatus()).isEqualTo(INACTIVE);

        // 5) 댓글 좋아요 삭제
        assertThat(commentLikeJpaRepository.count()).isEqualTo(0);

        // 6) 피드 저장 관계 삭제
        assertTrue(savedFeedJpaRepository.findAll().isEmpty());

        // 7) 게시글 좋아요(PostLike) 삭제
        assertThat(postLikeJpaRepository.count()).isEqualTo(0);
    }
}
