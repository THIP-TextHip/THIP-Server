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
@DisplayName("[통합] 댓글 조회 api 통합 테스트")
@Transactional
class RootCommentShowApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private CommentLikeJpaRepository commentLikeJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final String FEED_POST_TYPE = PostType.FEED.getType();

    @Test
    @DisplayName("댓글 조회 요청에 대하여, 특정 게시글(= 피드, 기록, 투표)의 루트 댓글만 반환하고, 자식 댓글 수(descendantCount)를 포함한다.")
    void comment_show_all_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드, 댓글, 자식 댓글 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, comment1, "댓글1_답글1", 8));

        commentLikeJpaRepository.save(TestEntityFactory.createCommentLike(comment1, me));   // me가 comment1을 좋아함

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

        // descendantCount 업데이트 (자식 댓글 1개)
        jdbcTemplate.update(
                "UPDATE comments SET descendant_count = 1 WHERE comment_id = ?",
                comment1.getCommentId());

        //when //then
        mockMvc.perform(get("/comments/{postId}", f1.getPostId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("postType", FEED_POST_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(1)))
                /**
                 * 루트 댓글 : 댓글 정보, 댓글 작성자 정보, 좋아요 수, 삭제된 댓글 여부, 자식 댓글 수 등을 반환한다
                 * 자식 댓글 리스트는 반환하지 않는다 (별도 API로 조회)
                 */
                .andExpect(jsonPath("$.data.commentList[0].commentId", is(comment1.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[0].creatorNickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.commentList[0].content", is(comment1.getContent())))
                .andExpect(jsonPath("$.data.commentList[0].likeCount", is(comment1.getLikeCount())))
                .andExpect(jsonPath("$.data.commentList[0].isLike", is(true)))  // me가 comment1을 좋아함
                .andExpect(jsonPath("$.data.commentList[0].isDeleted", is(false)))
                .andExpect(jsonPath("$.data.commentList[0].descendantCount", is(1)));  // 자식 댓글 1개
    }

    @Test
    @DisplayName("루트 댓글은 최신순으로 정렬하여 반환하고, 각 루트 댓글의 자식 댓글 수(descendantCount)를 포함한다.")
    void comment_show_all_ordering_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));
        UserJpaEntity user3 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user3"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드, 댓글, 자식 댓글 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));
        CommentJpaEntity comment2 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user2, PostType.FEED, "댓글2", 10));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user3, PostType.FEED, comment1, "댓글1_답글1", 2));
        CommentJpaEntity comment1_2 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, comment1, "댓글1_답글2", 8));
        CommentJpaEntity comment1_1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, user1, PostType.FEED, comment1_1, "댓글1_답글1_답글1", 3));

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
                Timestamp.valueOf(base.minusMinutes(30)), comment2.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), comment1_1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), comment1_2.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), comment1_1_1.getCommentId());

        // descendantCount 업데이트 (comment1은 자식 댓글 3개, comment2는 0개)
        jdbcTemplate.update(
                "UPDATE comments SET descendant_count = 3 WHERE comment_id = ?",
                comment1.getCommentId());

        //when //then
        mockMvc.perform(get("/comments/{postId}", f1.getPostId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("postType", FEED_POST_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(2)))
                /**
                 * 정렬 조건: 게시글에 바로 달린 댓글들(= 루트 댓글)은 최신순 정렬
                 * 루트 댓글의 자식 댓글 리스트는 반환하지 않고, descendantCount만 반환
                 */
                // 루트 댓글 정렬 확인 (최신순: comment2 -> comment1)
                .andExpect(jsonPath("$.data.commentList[0].commentId", is(comment2.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[0].descendantCount", is(0)))     // comment2는 자식 댓글 없음

                .andExpect(jsonPath("$.data.commentList[1].commentId", is(comment1.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[1].descendantCount", is(3)));    // comment1은 총 3개의 자식 댓글
    }

    @Test
    @DisplayName("삭제된 루트 댓글의 경우, 자식 댓글 개수가 0보다 크면 쓰레기 값으로 반환하고, 0이면 응답에서 제외한다.")
    void comment_show_all_deleted_root_comment_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드, 댓글, 자식 댓글 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment2 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글2", 5));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));
        CommentJpaEntity comment1_1 = commentJpaRepository.save(TestEntityFactory.createReplyComment(f1, me, PostType.FEED, comment1, "댓글1_답글1", 8));

        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f1.getPostId());

        commentJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), comment2.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), comment1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), comment1_1.getCommentId());

        // comment1, comment2 soft delete (descendantCount는 업데이트하지 않고 테스트)
        jdbcTemplate.update(
                "UPDATE comments SET status = 'INACTIVE', descendant_count = 1 WHERE comment_id = ?",
                comment1.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET status = 'INACTIVE', descendant_count = 0 WHERE comment_id = ?",
                comment2.getCommentId());

        //when //then
        mockMvc.perform(get("/comments/{postId}", f1.getPostId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("postType", FEED_POST_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentList", hasSize(1)))      // comment1만 조회됨 (descendantCount > 0), comment2는 제외 (descendantCount == 0)
                /**
                 * 삭제된 루트 댓글 처리:
                 * - descendantCount > 0: 쓰레기 값으로 반환 (isDeleted=true, descendantCount=실제값, 나머지는 null)
                 * - descendantCount == 0: 응답에서 제외
                 */
                .andExpect(jsonPath("$.data.commentList[0].commentId", nullValue()))
                .andExpect(jsonPath("$.data.commentList[0].creatorNickname", nullValue()))
                .andExpect(jsonPath("$.data.commentList[0].content", nullValue()))
                .andExpect(jsonPath("$.data.commentList[0].isDeleted", is(true)))
                .andExpect(jsonPath("$.data.commentList[0].descendantCount", is(1)));  // 자식 댓글 1개 (실제 값)
    }

    @Test
    @DisplayName("게시글에 달린 댓글이 많을 경우, 루트 댓글을 기준으로 페이징 처리 한다.")
    void comment_show_all_page_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드, 댓글, 자식 댓글 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        CommentJpaEntity comment1 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글1", 5));
        CommentJpaEntity comment2 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글2", 5));
        CommentJpaEntity comment3 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글3", 5));
        CommentJpaEntity comment4 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글4", 5));
        CommentJpaEntity comment5 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글5", 5));
        CommentJpaEntity comment6 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글6", 5));
        CommentJpaEntity comment7 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글7", 5));
        CommentJpaEntity comment8 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글8", 5));
        CommentJpaEntity comment9 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글9", 5));
        CommentJpaEntity comment10 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글10", 5));
        CommentJpaEntity comment11 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글11", 5));
        CommentJpaEntity comment12 = commentJpaRepository.save(TestEntityFactory.createComment(f1, user1, PostType.FEED, "댓글12", 5));

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
                Timestamp.valueOf(base.minusMinutes(35)), comment2.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), comment3.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), comment4.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), comment5.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), comment6.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), comment7.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(9)), comment8.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(8)), comment9.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(7)), comment10.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(6)), comment11.getCommentId());
        jdbcTemplate.update(
                "UPDATE comments SET created_at = ? WHERE comment_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), comment12.getCommentId());

        //when //then
        MvcResult firstResult = mockMvc.perform(get("/comments/{postId}", f1.getPostId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("postType", FEED_POST_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.commentList", hasSize(10)))
                /**
                 * 루트 댓글만 반환, 자식 댓글 리스트는 포함하지 않음
                 * descendantCount는 모두 0 (자식 댓글이 없음)
                 */
                // 루트 댓글 정렬 확인 (최신순)
                .andExpect(jsonPath("$.data.commentList[0].commentId", is(comment12.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[0].descendantCount", is(0)))
                .andExpect(jsonPath("$.data.commentList[1].commentId", is(comment11.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[1].descendantCount", is(0)))
                .andExpect(jsonPath("$.data.commentList[2].commentId", is(comment10.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[3].commentId", is(comment9.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[4].commentId", is(comment8.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[5].commentId", is(comment7.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[6].commentId", is(comment6.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[7].commentId", is(comment5.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[8].commentId", is(comment4.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[9].commentId", is(comment3.getCommentId().intValue())))
                .andReturn();

        String responseBody = firstResult.getResponse().getContentAsString();
        String nextCursor = JsonPath.read(responseBody, "$.data.nextCursor");

        mockMvc.perform(get("/comments/{postId}", f1.getPostId().intValue())
                        .requestAttr("userId", me.getUserId())
                        .param("postType", FEED_POST_TYPE)
                        .param("cursor", nextCursor))       // 2페이지 요청

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.commentList", hasSize(2)))
                /**
                 * 루트 댓글만 반환, descendantCount 포함
                 */
                // 루트 댓글 정렬 확인
                .andExpect(jsonPath("$.data.commentList[0].commentId", is(comment2.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[0].descendantCount", is(0)))
                .andExpect(jsonPath("$.data.commentList[1].commentId", is(comment1.getCommentId().intValue())))
                .andExpect(jsonPath("$.data.commentList[1].descendantCount", is(0)));
    }
}
