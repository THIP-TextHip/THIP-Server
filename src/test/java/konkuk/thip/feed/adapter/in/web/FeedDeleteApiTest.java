package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.Content.ContentJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.Tag.TagJpaRepository;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
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
import static konkuk.thip.feed.domain.Tag.FOREIGN_NOVEL;
import static konkuk.thip.feed.domain.Tag.KOREAN_NOVEL;
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

    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private FeedTagJpaRepository feedTagJpaRepository;
    @Autowired private ContentJpaRepository contentJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;


    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private TagJpaEntity tag1;
    private TagJpaEntity tag2;
    private CommentJpaEntity comment;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        tag1 = tagJpaRepository.save(TestEntityFactory.createTag(category, KOREAN_NOVEL.getValue()));
        tag2 = tagJpaRepository.save(TestEntityFactory.createTag(category, FOREIGN_NOVEL.getValue()));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user, book, true,1,1,List.of("url1", "url2", "url3")));
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(feed, tag1));
        feedTagJpaRepository.save(TestEntityFactory.createFeedTagMapping(feed, tag2));
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
        assertThat(feedJpaRepository.findByPostIdAndStatus(feed.getPostId(), INACTIVE)).isPresent();

        // 2) 피드 태그 관계 삭제
        assertTrue(feedTagJpaRepository.findAll().isEmpty());

        // 3) 콘텐츠 삭제
        assertTrue(contentJpaRepository.findAll().isEmpty());

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
