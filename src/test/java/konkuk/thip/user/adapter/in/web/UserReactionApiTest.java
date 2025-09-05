package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static konkuk.thip.post.domain.PostType.FEED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 사용자 반응 조회 API 통합 테스트")
class UserReactionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private RecordJpaRepository recordJpaRepository;

    @Autowired
    private VoteJpaRepository voteJpaRepository;

    @Autowired
    private CommentJpaRepository commentJpaRepository;

    @Autowired
    private PostLikeJpaRepository postLikeJpaRepository;

    private Alias alias;
    private UserJpaEntity user;
    private Category category;
    private BookJpaEntity book;
    private RoomJpaEntity room;
    private FeedJpaEntity feed;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;

    @BeforeEach
    void setUp() {
        alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        category = TestEntityFactory.createLiteratureCategory();
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user, book, true));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user, room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user, room));
        roomParticipantJpaRepository.save(
                TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.HOST, 0.0)
        );
    }

    @Test
    @DisplayName("좋아요 반응만 조회 성공")
    void showUserReaction_onlyLikes_success() throws Exception {
        // given
        postLikeJpaRepository.save(PostLikeJpaEntity.builder()
                .postJpaEntity(feed)
                .userJpaEntity(user)
                .build());

        // when & then
        mockMvc.perform(get("/users/reactions")
                        .param("type", "LIKE")
                        .param("size", "10")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                // 리스트 존재
                .andExpect(jsonPath("$.data.reactionList").isArray())
                .andExpect(jsonPath("$.data.reactionList.length()").value(1))
                // 첫 번째 항목 검증
                .andExpect(jsonPath("$.data.reactionList[0].label").value("좋아요"))
                .andExpect(jsonPath("$.data.reactionList[0].type").value("FEED"))
                .andExpect(jsonPath("$.data.reactionList[0].writer").value(user.getNickname()))
                .andExpect(jsonPath("$.data.reactionList[0].content").isString())
                .andExpect(jsonPath("$.data.reactionList[0].feedId").value(feed.getPostId()));
    }

    @Test
    @DisplayName("댓글 반응만 조회 성공")
    void showUserReaction_onlyComments_success() throws Exception {
        commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));

        mockMvc.perform(get("/users/reactions")
                        .param("type", "COMMENT")
                        .param("size", "10")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionList").isArray())
                .andExpect(jsonPath("$.data.reactionList.length()").value(1))
                .andExpect(jsonPath("$.data.reactionList[0].label").value("댓글"))
                .andExpect(jsonPath("$.data.reactionList[0].type").value("FEED"))
                .andExpect(jsonPath("$.data.reactionList[0].writer").value(user.getNickname()))
                .andExpect(jsonPath("$.data.reactionList[0].content").isString())
                .andExpect(jsonPath("$.data.reactionList[0].feedId").value(feed.getPostId()));
    }

    @Test
    @DisplayName("좋아요+댓글 반응 모두 조회 성공")
    void showUserReaction_both_success() throws Exception {
        postLikeJpaRepository.save(PostLikeJpaEntity.builder()
                .postJpaEntity(feed)
                .userJpaEntity(user)
                .build());
        commentJpaRepository.save(TestEntityFactory.createComment(feed, user, FEED));

        mockMvc.perform(get("/users/reactions")
                        .param("type", "BOTH")
                        .param("size", "10")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionList").isArray())
                .andExpect(jsonPath("$.data.reactionList.length()").value(2))
                .andExpect(jsonPath("$.data.reactionList[0].label").value("댓글"))
                .andExpect(jsonPath("$.data.reactionList[1].label").value("좋아요"));
    }

    @Test
    @DisplayName("커서 기반 페이징 정상 동작 확인")
    void showUserReaction_cursorPaging_success() throws Exception {
        List<CommentJpaEntity> comments = IntStream.rangeClosed(1, 15)
                .mapToObj(i -> TestEntityFactory.createComment(feed, user, FEED))
                .toList();
        commentJpaRepository.saveAll(comments);

        String jsonResponse = mockMvc.perform(get("/users/reactions")
                        .param("type", "COMMENT")
                        .param("size", "10")
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionList.length()").value(10))
                .andExpect(jsonPath("$.data.nextCursor").exists())
                .andExpect(jsonPath("$.data.isLast").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String nextCursor = objectMapper.readTree(jsonResponse)
                .path("data").path("nextCursor").asText();

        mockMvc.perform(get("/users/reactions")
                        .param("type", "COMMENT")
                        .param("size", "10")
                        .param("cursor", nextCursor)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reactionList.length()").value(5))
                .andExpect(jsonPath("$.data.isLast").value(true));
    }
}
