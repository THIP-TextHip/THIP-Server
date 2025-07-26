package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
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

import static konkuk.thip.common.post.PostType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 댓글 생성 api 통합 테스트")
class CommentCreateAPITest {

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
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;



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
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0));
    }

    // 공통 JSON 생성 함수
    private String toJson(String content, boolean isReply, Long parentId, String postType) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("content", content);
        req.put("isReplyRequest", isReply);
        req.put("parentId", parentId);
        req.put("postType", postType);
        return objectMapper.writeValueAsString(req);
    }

    @Test
    @DisplayName("각 게시물 타입별로 존재하는 게시물에 대해 (루트)댓글 생성을 할 수 있다.")
    void createRootCommentEachPostType() throws Exception {

        // given
        String[] postTypes = {"feed", "record", "vote"};
        Long[] postIds = {feed.getPostId(), record.getPostId(), vote.getPostId()};

        // when & then
        for (int i = 0; i < postTypes.length; i++) {
            mockMvc.perform(post("/comments/{postId}", postIds[i])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson("루트 댓글입니다", false, null, postTypes[i]))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.commentId").exists());
        }
    }


    @Test
    @DisplayName("각 게시물 타입별로 존재하는 게시물 및 댓글에 대해 답글 생성을 할 수 있다.")
    void createReplyCommentEachPostType() throws Exception {

        // given
        //부모 댓글 생성
        Long feedParentId = commentJpaRepository.save(TestEntityFactory.createComment(feed,user,FEED)).getCommentId();
        Long recordParentId = commentJpaRepository.save(TestEntityFactory.createComment(record,user,RECORD)).getCommentId();
        Long voteParentId = commentJpaRepository.save(TestEntityFactory.createComment(vote,user,VOTE)).getCommentId();

        // 답글 생성 요청
        Map<String, Object>[] replyRequests = new Map[]{
                Map.of("content", "Feed 답글", "isReplyRequest", true, "parentId", feedParentId, "postType", "feed"),
                Map.of("content", "Record 답글", "isReplyRequest", true, "parentId", recordParentId, "postType", "record"),
                Map.of("content", "Vote 답글", "isReplyRequest", true, "parentId", voteParentId, "postType", "vote")
        };

        Long[] postIds = {feed.getPostId(), record.getPostId(), vote.getPostId()};

        for (int i = 0; i < replyRequests.length; i++) {
            mockMvc.perform(post("/comments/{postId}", postIds[i])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyRequests[i]))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.commentId").exists());
        }
    }

}
