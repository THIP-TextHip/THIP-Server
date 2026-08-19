package konkuk.thip.user.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 차단 후 콘텐츠 노출 차단 검증")
class BlockContentHiddenApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private UserBlockJpaRepository userBlockJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private UserJpaEntity viewer;   // 차단하는 사람
    private UserJpaEntity blocked;  // 차단당하는 사람
    private BookJpaEntity book;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        viewer = userJpaRepository.save(TestEntityFactory.createUser(alias, "viewer"));
        blocked = userJpaRepository.save(TestEntityFactory.createUser(alias, "blockeduser"));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
    }

    @AfterEach
    void tearDown() {
        userBlockJpaRepository.deleteAllInBatch();
        savedFeedJpaRepository.deleteAllInBatch();
        // comments 는 parent_id 로 자기 자신을 참조하므로 답글부터 지워야 FK 제약에 걸리지 않는다
        jdbcTemplate.update("DELETE FROM comments WHERE parent_id IS NOT NULL");
        commentJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] 차단하면 홈 피드에서 그 사용자의 글이 사라진다.")
    void blockedUserFeed_disappears_from_home_feed() throws Exception {
        // given : 상대가 공개 피드를 작성했고, 차단 전에는 보인다
        feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));

        mockMvc.perform(get("/feeds").requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(1)));

        // when : 차단
        block(viewer, blocked);

        // then : 홈 피드에서 사라진다
        mockMvc.perform(get("/feeds").requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단은 양방향이다 - 차단당한 사용자에게도 차단한 사용자의 글이 사라진다.")
    void block_hides_content_both_ways() throws Exception {
        // given : viewer 가 공개 피드를 작성. 차단 전에는 blocked 에게 보인다
        feedJpaRepository.save(TestEntityFactory.createFeed(viewer, book, true));

        mockMvc.perform(get("/feeds").requestAttr("userId", blocked.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(1)));

        // when : viewer 가 blocked 를 차단 (차단당한 쪽은 아무 행동도 하지 않았다)
        block(viewer, blocked);

        // then : 차단당한 쪽에서도 상대 글이 사라진다
        mockMvc.perform(get("/feeds").requestAttr("userId", blocked.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단하면 사용자 검색 결과에서 사라진다.")
    void blockedUser_disappears_from_user_search() throws Exception {
        // 다른 테스트가 남긴 유저에 영향받지 않도록 개수가 아니라 대상 포함 여부로 검증한다
        int blockedUserId = blocked.getUserId().intValue();

        // given : 차단 전에는 검색된다
        mockMvc.perform(get("/users")
                        .param("keyword", "blockeduser")
                        .param("isFinalized", "false")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[*].userId", hasItem(blockedUserId)));

        // when
        block(viewer, blocked);

        // then
        mockMvc.perform(get("/users")
                        .param("keyword", "blockeduser")
                        .param("isFinalized", "false")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList[*].userId", not(hasItem(blockedUserId))));
    }

    @Test
    @DisplayName("[성공] 차단하면 저장한 피드 목록에서도 사라진다. (저장 관계 자체는 지우지 않는다)")
    void blockedUserFeed_disappears_from_saved_feeds() throws Exception {
        // given : 상대 피드를 저장해 둔 상태
        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(viewer, feed));

        mockMvc.perform(get("/feeds/saved").requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(1)));

        // when
        block(viewer, blocked);

        // then : 목록에서는 사라지지만 saved_feeds row 는 남아 있어 차단 해제 시 복원된다
        mockMvc.perform(get("/feeds/saved").requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(0)));
    }

    @Test
    @DisplayName("[404] 차단한 사용자의 피드는 단건 상세로 직접 진입해도 볼 수 없다.")
    void blockedUserFeed_single_view_returns_404() throws Exception {
        // given
        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));
        block(viewer, blocked);

        // when & then : 목록에서 숨겨도 딥링크로 들어올 수 있으므로 서비스에서 막는다
        mockMvc.perform(get("/feeds/{feedId}", feed.getPostId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[404] 차단한 사용자의 프로필 피드 목록에 진입할 수 없다.")
    void blockedUser_profile_feeds_returns_404() throws Exception {
        // given
        feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));
        block(viewer, blocked);

        // when & then
        mockMvc.perform(get("/feeds/users/{userId}", blocked.getUserId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[404] 차단한 사용자의 프로필 정보를 조회할 수 없다.")
    void blockedUser_profile_info_returns_404() throws Exception {
        // given
        block(viewer, blocked);

        // when & then
        mockMvc.perform(get("/feeds/users/{userId}/info", blocked.getUserId())
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[성공] 차단하면 댓글 목록에서 그 사용자의 댓글이 사라진다.")
    void blockedUserComment_disappears_from_comment_list() throws Exception {
        // given : 내 피드에 상대가 댓글을 달았다
        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeed(viewer, book, true));
        commentJpaRepository.save(TestEntityFactory.createComment(feed, blocked, PostType.FEED));

        mockMvc.perform(get("/comments/{postId}", feed.getPostId())
                        .param("postType", "FEED")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(1)));

        // when
        block(viewer, blocked);

        // then
        mockMvc.perform(get("/comments/{postId}", feed.getPostId())
                        .param("postType", "FEED")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단한 사용자의 루트 댓글은 내 답글까지 스레드째로 사라진다.")
    void blockedUserRootComment_hides_whole_thread() throws Exception {
        // given : 상대의 루트 댓글에 내가 답글을 달아둔 상태
        FeedJpaEntity feed = feedJpaRepository.save(TestEntityFactory.createFeed(viewer, book, true));
        CommentJpaEntity rootComment = commentJpaRepository.save(
                TestEntityFactory.createComment(feed, blocked, PostType.FEED));
        commentJpaRepository.save(
                TestEntityFactory.createReplyComment(feed, viewer, PostType.FEED, rootComment));

        mockMvc.perform(get("/comments/{postId}", feed.getPostId())
                        .param("postType", "FEED")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(1)));

        // when
        block(viewer, blocked);

        // then : 루트가 숨겨지므로 그 아래 내 답글도 함께 사라진다 (확정된 정책)
        mockMvc.perform(get("/comments/{postId}", feed.getPostId())
                        .param("postType", "FEED")
                        .requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(0)));
    }

    @Test
    @DisplayName("[성공] 차단을 해제하면 다시 보인다.")
    void unblock_restores_visibility() throws Exception {
        // given
        feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));
        block(viewer, blocked);

        mockMvc.perform(get("/feeds").requestAttr("userId", viewer.getUserId()))
                .andExpect(jsonPath("$.data.feedList", hasSize(0)));

        // when : 차단 해제
        userBlockJpaRepository.findByUserAndBlockedUser(viewer.getUserId(), blocked.getUserId())
                .ifPresent(userBlockJpaRepository::delete);
        userBlockJpaRepository.flush();

        // then
        mockMvc.perform(get("/feeds").requestAttr("userId", viewer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(1)));
    }

    private void block(UserJpaEntity blocker, UserJpaEntity target) {
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(blocker, target));
        userBlockJpaRepository.flush();
    }
}
