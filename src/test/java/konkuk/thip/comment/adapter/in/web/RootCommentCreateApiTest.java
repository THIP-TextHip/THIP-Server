package konkuk.thip.comment.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 루트 댓글 생성 API 통합 테스트")
class RootCommentCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;

    private UserJpaEntity user;
    private FeedJpaEntity feed;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;

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
    }

    private String toRootCommentJson(String content, String postType) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("content", content);
        req.put("postType", postType);
        return objectMapper.writeValueAsString(req);
    }

    @Test
    @DisplayName("Feed 게시물에 루트 댓글을 생성할 수 있다.")
    void createRootCommentOnFeed() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/{postId}", feed.getPostId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRootCommentJson("피드에 루트 댓글입니다", "feed"))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("Record 게시물에 루트 댓글을 생성할 수 있다.")
    void createRootCommentOnRecord() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/{postId}", record.getPostId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRootCommentJson("기록에 루트 댓글입니다", "record"))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("Vote 게시물에 루트 댓글을 생성할 수 있다.")
    void createRootCommentOnVote() throws Exception {
        // given & when & then
        mockMvc.perform(post("/comments/{postId}", vote.getPostId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRootCommentJson("투표에 루트 댓글입니다", "vote"))
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").exists());
    }

    @Test
    @DisplayName("각 게시물 타입별로 루트 댓글을 생성할 수 있다.")
    void createRootCommentEachPostType() throws Exception {
        // given
        String[] postTypes = {"feed", "record", "vote"};
        Long[] postIds = {feed.getPostId(), record.getPostId(), vote.getPostId()};

        // when & then
        for (int i = 0; i < postTypes.length; i++) {
            mockMvc.perform(post("/comments/{postId}", postIds[i])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toRootCommentJson("루트 댓글입니다", postTypes[i]))
                            .requestAttr("userId", user.getUserId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.commentId").exists());
        }
    }
}
