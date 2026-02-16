package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
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

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.post.domain.PostType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 자식 댓글(답글) 생성 API 통합 테스트")
class ChildCommentCreateApiTest {

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

    private UserJpaEntity user;
    private FeedJpaEntity feed;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;
    private CommentJpaEntity feedRootComment;
    private CommentJpaEntity recordRootComment;
    private CommentJpaEntity voteRootComment;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        Category category = TestEntityFactory.createLiteratureCategory();
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user, book, true));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user, room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user, room));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));

        // 루트 댓글 생성
        feedRootComment = commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));
        recordRootComment = commentJpaRepository.save(TestEntityFactory.createComment(record, user, RECORD));
        voteRootComment = commentJpaRepository.save(TestEntityFactory.createComment(vote, user, VOTE));
    }

    private String toChildCommentJson(String content, String postType, Long postId) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("content", content);
        req.put("postType", postType);
        req.put("postId", postId);
        return objectMapper.writeValueAsString(req);
    }

    @Test
    @DisplayName("Feed 게시물의 루트 댓글에 답글을 생성할 수 있다.")
    void createChildCommentOnFeedRootComment() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/replies/{parentCommentId}", feedRootComment.getCommentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toChildCommentJson("피드 답글입니다", "feed", feed.getPostId()))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("Record 게시물의 루트 댓글에 답글을 생성할 수 있다.")
    void createChildCommentOnRecordRootComment() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/replies/{parentCommentId}", recordRootComment.getCommentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toChildCommentJson("기록 답글입니다", "record", record.getPostId()))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("Vote 게시물의 루트 댓글에 답글을 생성할 수 있다.")
    void createChildCommentOnVoteRootComment() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/replies/{parentCommentId}", voteRootComment.getCommentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toChildCommentJson("투표 답글입니다", "vote", vote.getPostId()))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("각 게시물 타입별로 루트 댓글에 답글을 생성할 수 있다.")
    void createChildCommentEachPostType() throws Exception {
        // given
        String[] postTypes = {"feed", "record", "vote"};
        Long[] postIds = {feed.getPostId(), record.getPostId(), vote.getPostId()};
        Long[] parentCommentIds = {
                feedRootComment.getCommentId(),
                recordRootComment.getCommentId(),
                voteRootComment.getCommentId()
        };

        // when & then
        for (int i = 0; i < postTypes.length; i++) {
            mockMvc.perform(post("/comments/replies/{parentCommentId}", parentCommentIds[i])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toChildCommentJson("답글입니다", postTypes[i], postIds[i]))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.commentId").exists());
        }
    }

    @Test
    @DisplayName("답글에 대한 답글(depth 2 이상)을 생성할 수 있다.")
    void createChildCommentOnChildComment() throws Exception {
        // given - 1단계 답글 생성
        CommentJpaEntity childComment = commentJpaRepository.save(
                TestEntityFactory.createReplyComment(feed, user, FEED, feedRootComment)
        );

        // when & then - 2단계 답글 생성 (답글의 답글)
        mockMvc.perform(post("/comments/replies/{parentCommentId}", childComment.getCommentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toChildCommentJson("답글의 답글입니다", "feed", feed.getPostId()))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }
}
