package konkuk.thip.comment.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 자식 댓글 조회 api 통합 테스트")
@Transactional
class ChildCommentShowApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Depth가 3단계 이상인 대댓글들도 모두 평탄화(Flat)되어 루트 댓글 하위로 조회된다.")
    void child_comment_show_depth_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));

        // 1. 루트 댓글 생성
        CommentJpaEntity root = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "ROOT", 0));

        // 2. Depth 1 자식 생성 (Parent: Root)
        CommentJpaEntity depth1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, root, "Depth1", 0));

        // 3. Depth 2 자식 생성 (Parent: Depth1) -> Factory에 의해 Root는 'root'로 설정됨
        CommentJpaEntity depth2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, depth1, "Depth2", 0));

        // 4. Depth 3 자식 생성 (Parent: Depth2) -> Factory에 의해 Root는 'root'로 설정됨
        CommentJpaEntity depth3 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, depth2, "Depth3", 0));

        // 5. Depth 1 형제 생성 (Parent: Root)
        CommentJpaEntity depth1_sibling = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, root, "Depth1_Sibling", 0));

        //when //then
        mockMvc.perform(get("/comments/replies/{rootCommentId}", root.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(4))) // 총 4개의 자손 댓글
                // 순서 검증 (ID 생성순 = 작성순)
                .andExpect(jsonPath("$.data.childComments[0].content", is("Depth1")))
                .andExpect(jsonPath("$.data.childComments[1].content", is("Depth2")))
                .andExpect(jsonPath("$.data.childComments[2].content", is("Depth3")))
                .andExpect(jsonPath("$.data.childComments[3].content", is("Depth1_Sibling")))
                // 계층 구조 검증 (평탄화되었지만 부모 닉네임은 직계 부모를 따라가야 함)
                .andExpect(jsonPath("$.data.childComments[1].parentCommentCreatorNickname", is("me"))) // Depth2의 부모는 Depth1(me)
                .andExpect(jsonPath("$.data.childComments[2].parentCommentCreatorNickname", is("user1"))); // Depth3의 부모는 Depth2(user1)
    }

    @Test
    @DisplayName("특정 루트 댓글의 자식 댓글을 조회할 수 있다.")
    void child_comment_show_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, comment1, "댓글1_답글1", 8));
        CommentJpaEntity comment1_2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "댓글1_답글2", 3));

        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment1_1, me));

        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f1.getPostId());

        commentJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), comment1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), comment1_1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), comment1_2.getCommentId());

        //when //then
        mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(2)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.childComments[0].commentId", is(comment1_1.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.childComments[0].parentCommentCreatorNickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.childComments[0].creatorNickname", is(me.getNickname())))
                .andExpect(jsonPath("$.data.childComments[0].content", is(comment1_1.getContent())))
                .andExpect(jsonPath("$.data.childComments[0].likeCount", is(comment1_1.getLikeCount())))
                .andExpect(jsonPath("$.data.childComments[0].isLike", is(true)))
                .andExpect(jsonPath("$.data.childComments[1].commentId", is(comment1_2.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.childComments[1].parentCommentCreatorNickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.childComments[1].creatorNickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.childComments[1].content", is(comment1_2.getContent())))
                .andExpect(jsonPath("$.data.childComments[1].likeCount", is(comment1_2.getLikeCount())))
                .andExpect(jsonPath("$.data.childComments[1].isLike", is(false)));
    }

    @Test
    @DisplayName("자식 댓글이 없는 루트 댓글을 조회하면 빈 리스트를 반환한다.")
    void child_comment_show_empty_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));

        //when //then
        mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(0)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.nextCursor", nullValue()));
    }

    @Test
    @DisplayName("자식 댓글이 많을 경우, 커서 기반 페이징으로 10개씩 조회한다.")
    void child_comment_show_paging_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "루트댓글", 5));

        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식1", 1));
        CommentJpaEntity comment1_2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식2", 1));
        CommentJpaEntity comment1_3 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식3", 1));
        CommentJpaEntity comment1_4 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식4", 1));
        CommentJpaEntity comment1_5 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식5", 1));
        CommentJpaEntity comment1_6 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식6", 1));
        CommentJpaEntity comment1_7 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식7", 1));
        CommentJpaEntity comment1_8 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식8", 1));
        CommentJpaEntity comment1_9 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식9", 1));
        CommentJpaEntity comment1_10 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식10", 1));
        CommentJpaEntity comment1_11 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식11", 1));
        CommentJpaEntity comment1_12 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식12", 1));
        CommentJpaEntity comment1_13 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식13", 1));
        CommentJpaEntity comment1_14 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식14", 1));
        CommentJpaEntity comment1_15 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식15", 1));

        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f1.getPostId());

        commentJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), comment1.getCommentId());

        for (int i = 1; i <= 15; i++) {
            CommentJpaEntity childComment = commentJpaRepository.findById((long)(comment1_1.getCommentId() + i - 1)).orElse(null);
            if (childComment != null) {
                jdbcTemplate.update(
                        "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                        Timestamp.valueOf(base.minusMinutes(30 - i)), childComment.getCommentId());
            }
        }

        //when //then - 첫 번째 페이지 조회
        MvcResult firstResult = mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(10)))
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andExpect(jsonPath("$.data.childComments[0].commentId", is(comment1_1.getCommentId().intValue())))
                .andReturn();

        String responseBody = firstResult.getResponse().getContentAsString();
        String nextCursor = JsonPath.read(responseBody, "$.data.nextCursor");

        // when //then - 두 번째 페이지 조회
        mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(5)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.nextCursor", nullValue()))
                .andExpect(jsonPath("$.data.childComments[0].commentId", is(comment1_11.getCommentId().intValue())));
    }

    @Test
    @DisplayName("자식 댓글은 작성 시각순(오래된 순)으로 정렬되어 반환된다.")
    void child_comment_show_ordering_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "루트댓글", 5));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식1", 1));
        CommentJpaEntity comment1_2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user2, PostType.FEED, comment1, "자식2", 2));
        CommentJpaEntity comment1_3 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, comment1, "자식3", 3));

        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f1.getPostId());

        commentJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), comment1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), comment1_1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), comment1_2.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base), comment1_3.getCommentId());

        //when //then
        mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(3)))
                .andExpect(jsonPath("$.data.childComments[0].commentId", is(comment1_1.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.childComments[0].content", is(comment1_1.getContent())))
                .andExpect(jsonPath("$.data.childComments[1].commentId", is(comment1_2.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.childComments[1].content", is(comment1_2.getContent())))
                .andExpect(jsonPath("$.data.childComments[2].commentId", is(comment1_3.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.childComments[2].content", is(comment1_3.getContent())));
    }

    @Test
    @DisplayName("자식 댓글 조회 시 사용자가 좋아한 댓글을 표시한다.")
    void child_comment_show_like_status_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "루트댓글", 5));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식1", 1));
        CommentJpaEntity comment1_2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식2", 1));
        CommentJpaEntity comment1_3 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1, "자식3", 1));

        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment1_1, me));
        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment1_3, me));

        //when //then
        mockMvc.perform(get("/comments/replies/{rootCommentId}", comment1.getCommentId().intValue())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childComments", hasSize(3)))
                .andExpect(jsonPath("$.data.childComments[0].isLike", is(true)))
                .andExpect(jsonPath("$.data.childComments[1].isLike", is(false)))
                .andExpect(jsonPath("$.data.childComments[2].isLike", is(true)));
    }
}
