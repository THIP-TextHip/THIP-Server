package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.in.web.request.FeedIsLikeRequest;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.POST_ALREADY_LIKED;
import static konkuk.thip.common.exception.code.ErrorCode.POST_NOT_LIKED_CANNOT_CANCEL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 피드 좋아요 api 통합 테스트")
class FeedChangeLikeStatusApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;

    private UserJpaEntity user;
    private BookJpaEntity book;
    private FeedJpaEntity feed;

    private static final String FEED_LIKE_API_PATH = "/feeds/{feedId}/likes";

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
    }

    @Test
    @DisplayName("피드를 처음 좋아요하면 좋아요 저장 및 카운트 증가 [성공]")
    void likeFeed_Success() throws Exception {

        // given
        FeedIsLikeRequest request = new FeedIsLikeRequest(true);  // 좋아요 상태 변경 요청(true=좋아요)

        // when
        mockMvc.perform(post(FEED_LIKE_API_PATH, feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").value(feed.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        // 좋아요 저장 여부 확인
        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(),feed.getPostId());
        assertThat(liked).isTrue();

        // 좋아요 카운트 증가 확인
        FeedJpaEntity updatedFeed = feedJpaRepository.findById(feed.getPostId()).orElseThrow();
        assertThat(updatedFeed.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 좋아요한 피드를 다시 좋아요하면 [400 에러 발생]")
    void likeFeed_AlreadyLiked_Fail() throws Exception {

        // given: 미리 좋아요 저장
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, feed));
        FeedIsLikeRequest request = new FeedIsLikeRequest(true);

        // when & then
        mockMvc.perform(post(FEED_LIKE_API_PATH, feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_ALREADY_LIKED.getCode()));
    }

    @Test
    @DisplayName("좋아요 한 피드를 취소하면 좋아요 삭제 및 카운트 감소 [성공]")
    void unlikeFeed_Success() throws Exception {

        // given: 좋아요가 저장되어 있고, likeCount도 1 반영
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, feed));
        feed.updateLikeCount(1); // 좋아요 1개로 세팅
        feedJpaRepository.save(feed);

        FeedIsLikeRequest request = new FeedIsLikeRequest(false);

        // when
        mockMvc.perform(post(FEED_LIKE_API_PATH, feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedId").value(feed.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(false));

        // 좋아요 삭제 확인
        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(),feed.getPostId());
        assertThat(liked).isFalse();

        // 좋아요 카운트 감소 확인
        FeedJpaEntity updatedFeed = feedJpaRepository.findById(feed.getPostId()).orElseThrow();
        assertThat(updatedFeed.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 하지 않은 피드를 좋아요 취소하면 [400 에러 발생]")
    void unlikeFeed_NotLiked_Fail() throws Exception {
        // given: 좋아요 없음
        FeedIsLikeRequest request = new FeedIsLikeRequest(false);

        // when & then
        mockMvc.perform(post(FEED_LIKE_API_PATH, feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_NOT_LIKED_CANNOT_CANCEL.getCode()));
    }

}
