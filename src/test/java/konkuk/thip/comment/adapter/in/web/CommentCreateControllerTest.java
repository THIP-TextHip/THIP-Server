package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("[단위] 댓글 생성 api controller 단위 테스트")
class CommentCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private CommentJpaRepository commentJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private Alias alias;
    private UserJpaEntity user;
    private Category category;
    private FeedJpaEntity feed;
    private BookJpaEntity book;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;
    private RoomJpaEntity room;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = TestEntityFactory.createLiteratureCategory();
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book,category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user,room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
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
            mockMvc.perform(post("/comments/{postId}", feed.getPostId())
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

    }
}
