package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.common.post.PostType.FEED;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[단위] 댓글 생성 api controller 단위 테스트")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;


    private AliasJpaEntity alias;
    private UserJpaEntity user;
    private CategoryJpaEntity category;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user,room));
    }


    private Map<String, Object> buildValidRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put("content", "정상 댓글");
        req.put("isReplyRequest", false);
        req.put("parentId", null);
        req.put("postType", "feed");
        return req;
    }

    private void assertBadRequest(Map<String, Object> req, String expectedMessage) throws Exception {
        mockMvc.perform(post("/comments/{postId}", feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(API_INVALID_PARAM.getCode()))
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));
    }

    private void assertBadCommentCreateRequest(Map<String, Object> req, String expectedMessage) throws Exception {
        mockMvc.perform(post("/comments/{postId}", feed.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_COMMENT_CREATE.getCode()))
                .andExpect(jsonPath("$.message", containsString(expectedMessage)));
    }

    @Nested
    @DisplayName("댓글 내용(content) 검증")
    class ContentValidation {
        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blankContent() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("content", "");
            assertBadRequest(req, "댓글 내용은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("isReplyRequest(답글 여부) 검증")
    class IsReplyRequestValidation {
        @Test
        @DisplayName("누락될 경우 400 error")
        void missingIsReplyRequest() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.remove("isReplyRequest");
            assertBadRequest(req, "답글 여부는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("postType(게시물 타입) 검증")
    class PostTypeValidation {

        @Test
        @DisplayName("빈 문자열일 때 400 error")
        void blankPostType() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("postType", "");
            assertBadRequest(req, "게시물 타입은 필수입니다.");
        }

        @Test
        @DisplayName("지원하지 않는 게시물 타입 입력 시 400 반환")
        void invalidPostType() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("postType", "invalidType");
            mockMvc.perform(post("/comments/{postId}", 1)  // 없는 ID
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(POST_TYPE_NOT_MATCH.getCode()))
                    .andExpect(jsonPath("$.message", containsString("일치하는 게시물 타입 이름이 없습니다.")));
        }
    }

    @Nested
    @DisplayName("예외 상황 검증")
    class CommentExceptionValidation {

        @Test
        @DisplayName("존재하지 않는 FEED일 경우 404 반환")
        void feedNotFound() throws Exception {
            Map<String, Object> req = buildValidRequest();
            mockMvc.perform(post("/comments/{postId}", 99999L)  // 없는 ID
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message", containsString("존재하지 않는 FEED 입니다.")));
        }

        @Test
        @DisplayName("존재하지 않는 RECORD일 경우 404 반환")
        void recordNotFound() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("postType", "record");
            mockMvc.perform(post("/comments/{postId}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(RECORD_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message", containsString("존재하지 않는 RECORD 입니다.")));
        }

        @Test
        @DisplayName("존재하지 않는 VOTE일 경우 404 반환")
        void voteNotFound() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("postType", "vote");
            mockMvc.perform(post("/comments/{postId}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(VOTE_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message", containsString("존재하지 않는 VOTE 입니다.")));
        }

        @Test
        @DisplayName("답글인데 parentId가 null일 경우 400 반환")
        void replyWithoutParentId() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isReplyRequest", true);
            req.put("parentId", null);  // 필수인데 없음
            assertBadCommentCreateRequest(req, "답글 작성 시 parentId는 필수입니다.");
        }

        @Test
        @DisplayName("일반 댓글인데 parentId가 존재할 경우 400 반환")
        void rootCommentWithParentId() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isReplyRequest", false);
            req.put("parentId", 1L);  // 있으면 안 됨
            assertBadCommentCreateRequest(req, "일반 댓글에는 parentId가 없어야 합니다.");
        }

        @Test
        @DisplayName("parentId가 존재하지만 댓글이 실제 존재하지 않을 때 400 반환")
        void replyToNonExistentParent() throws Exception {
            Map<String, Object> req = buildValidRequest();
            req.put("isReplyRequest", true);
            req.put("parentId", 99999L);  // 존재하지 않는 parent
            assertBadCommentCreateRequest(req, "parentId에 해당하는 부모 댓글이 존재해야 합니다.");
        }

        @Test
        @DisplayName("댓글과 부모 댓글의 게시글이 일치하지 않을 경우 400 반환")
        void parentPostMismatch() throws Exception {

            // 1. 부모 댓글을 FEED에 작성
            CommentJpaEntity parentComment = commentJpaRepository.save(
                    TestEntityFactory.createComment(feed, user, FEED)
            );

            // 2. 답글 요청은 RECORD에 대해 요청
            Map<String, Object> req = new HashMap<>();
            req.put("content", "게시글 불일치");
            req.put("isReplyRequest", true);
            req.put("parentId", parentComment.getCommentId());
            req.put("postType", "record");

            mockMvc.perform(post("/comments/{postId}", record.getPostId())
                            .requestAttr("userId", user.getUserId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(INVALID_COMMENT_CREATE.getCode()))
                    .andExpect(jsonPath("$.message", containsString("댓글과 부모 댓글의 게시글이 일치하지 않습니다.")));
        }
    }
}
