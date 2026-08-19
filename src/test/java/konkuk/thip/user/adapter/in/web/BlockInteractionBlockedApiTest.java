 package konkuk.thip.user.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 차단 상태에서의 상호작용 차단 검증")
class BlockInteractionBlockedApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private UserBlockJpaRepository userBlockJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;

    private UserJpaEntity viewer;
    private UserJpaEntity blocked;
    private FeedJpaEntity blockedUserFeed;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        viewer = userJpaRepository.save(TestEntityFactory.createUser(alias, "viewer"));
        blocked = userJpaRepository.save(TestEntityFactory.createUser(alias, "blockeduser"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        blockedUserFeed = feedJpaRepository.save(TestEntityFactory.createFeed(blocked, book, true));

        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(viewer, blocked));
        userBlockJpaRepository.flush();
    }

    @AfterEach
    void tearDown() {
        notificationJpaRepository.deleteAllInBatch();
        postLikeJpaRepository.deleteAllInBatch();
        userBlockJpaRepository.deleteAllInBatch();
        savedFeedJpaRepository.deleteAllInBatch();
        commentJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[400] 차단한 사용자의 피드에 좋아요할 수 없다.")
    void cannot_like_blocked_user_feed() throws Exception {
        mockMvc.perform(post("/feeds/{feedId}/likes", blockedUserFeed.getPostId())
                        .requestAttr("userId", viewer.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_BLOCKED_CANNOT_INTERACT.getCode()));
    }

    @Test
    @DisplayName("[400] 차단한 사용자의 피드에 댓글을 달 수 없다.")
    void cannot_comment_on_blocked_user_feed() throws Exception {
        mockMvc.perform(post("/comments/{postId}", blockedUserFeed.getPostId())
                        .requestAttr("userId", viewer.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"댓글\", \"isReplyRequest\": false, \"parentId\": null, \"postType\": \"FEED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_BLOCKED_CANNOT_INTERACT.getCode()));
    }

    @Test
    @DisplayName("[400] 차단한 사용자의 피드를 저장할 수 없다.")
    void cannot_save_blocked_user_feed() throws Exception {
        mockMvc.perform(post("/feeds/{feedId}/saved", blockedUserFeed.getPostId())
                        .requestAttr("userId", viewer.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_BLOCKED_CANNOT_INTERACT.getCode()));
    }

    @Test
    @DisplayName("[성공] 차단 관계에서는 알림이 생성되지 않는다.")
    void notification_is_suppressed_between_blocked_users() throws Exception {
        long before = notificationJpaRepository.count();

        // 차단 상태에서 팔로우를 시도하면 거부되므로 알림도 생기지 않는다
        mockMvc.perform(post("/users/following/{followingUserId}", blocked.getUserId())
                        .requestAttr("userId", viewer.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest());

        assertThat(notificationJpaRepository.count()).isEqualTo(before);
    }
}
