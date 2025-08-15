package konkuk.thip.room.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.room.adapter.in.web.request.RoomPostIsLikeRequest;
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
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
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
@DisplayName("[통합] 방 게시물(기록,투표) 좋아요 api 통합 테스트")
class RoomPostChangeLikeStatusAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private AliasJpaRepository aliasJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private RoomJpaRepository roomJpaRepository;
    @Autowired private RoomParticipantJpaRepository roomParticipantJpaRepository;
    @Autowired private RecordJpaRepository recordJpaRepository;
    @Autowired private VoteJpaRepository voteJpaRepository;


    private UserJpaEntity user;
    private BookJpaEntity book;
    private FeedJpaEntity feed;
    private CategoryJpaEntity category;
    private RoomJpaEntity room;
    private RecordJpaEntity record;
    private VoteJpaEntity vote;

    private static final String ROOM_POST_LIKE_API_PATH = "/room-posts/{postId}/likes";

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        user = userJpaRepository.save(TestEntityFactory.createUser(alias));
        book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN("9788954682152"));
        feed = feedJpaRepository.save(TestEntityFactory.createFeed(user,book, true));
        category = categoryJpaRepository.save(TestEntityFactory.createLiteratureCategory(alias));
        room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));
        // 1번방에 유저 1이 호스트
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room,user, RoomParticipantRole.HOST, 80.0));
        record = recordJpaRepository.save(TestEntityFactory.createRecord(user,room));
        vote = voteJpaRepository.save(TestEntityFactory.createVote(user,room));
    }

    @Test
    @DisplayName("기록 게시물을 처음 좋아요하면 좋아요 저장 및 카운트 증가 [성공]")
    void likeRecordPost_Success() throws Exception {
        // given
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(true, "RECORD");

        //when
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(record.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        //then
        // 좋아요 저장 확인
        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(), record.getPostId());
        assertThat(liked).isTrue();

        // 좋아요 카운트 증가 확인
        RecordJpaEntity updatedRecord = recordJpaRepository.findById(record.getPostId()).orElseThrow();
        assertThat(updatedRecord.getLikeCount()).isEqualTo(1);
    }


    @Test
    @DisplayName("이미 좋아요한 기록 게시물을 다시 좋아요하면 [400 에러 발생]")
    void likeRecordPost_AlreadyLiked_Fail() throws Exception {
        //given
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, record));
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(true, "RECORD");

        //when & then
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_ALREADY_LIKED.getCode()));
    }

    @Test
    @DisplayName("좋아요한 기록 게시물 좋아요 취소하면 좋아요 삭제 및 카운트 감소 [성공]")
    void unlikeRecordPost_Success() throws Exception {
        //given
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, record));
        record.updateLikeCount(1);
        recordJpaRepository.save(record);
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(false, "RECORD");

        //when
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(record.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(false));

        //then
        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(), record.getPostId());
        assertThat(liked).isFalse();

        RecordJpaEntity updatedRecord = recordJpaRepository.findById(record.getPostId()).orElseThrow();
        assertThat(updatedRecord.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 하지 않은 기록 게시물을 좋아요 취소하면 [400 에러 발생]")
    void unlikeRecordPost_NotLiked_Fail() throws Exception {
        //given
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(false, "RECORD");

        //when & then
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, record.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_NOT_LIKED_CANNOT_CANCEL.getCode()));
    }

    // --- Vote 게시물에 대해서도 동일 패턴 테스트 ---

    @Test
    @DisplayName("투표 게시물을 처음 좋아요하면 좋아요 저장 및 카운트 증가 [성공]")
    void likeVotePost_Success() throws Exception {
        //given
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(true, "VOTE");

        //when
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(vote.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        //then
        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(), vote.getPostId());
        assertThat(liked).isTrue();

        VoteJpaEntity updatedVote = voteJpaRepository.findById(vote.getPostId()).orElseThrow();
        assertThat(updatedVote.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 좋아요한 투표 게시물을 다시 좋아요하면 [400 에러 발생]")
    void likeVotePost_AlreadyLiked_Fail() throws Exception {
        //given
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, vote));
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(true, "VOTE");

        //when & then
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_ALREADY_LIKED.getCode()));
    }

    @Test
    @DisplayName("좋아요한 투표 게시물 좋아요 취소하면 좋아요 삭제 및 카운트 감소 [성공]")
    void unlikeVotePost_Success() throws Exception {
        //given
        postLikeJpaRepository.save(TestEntityFactory.createPostLike(user, vote));
        vote.updateLikeCount(1);
        voteJpaRepository.save(vote);
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(false, "VOTE");

        //when
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(vote.getPostId()))
                .andExpect(jsonPath("$.data.isLiked").value(false));

        boolean liked = postLikeJpaRepository.existsByUserIdAndPostId(user.getUserId(), vote.getPostId());
        assertThat(liked).isFalse();

        VoteJpaEntity updatedVote = voteJpaRepository.findById(vote.getPostId()).orElseThrow();
        assertThat(updatedVote.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 하지 않은 투표 게시물을 좋아요 취소하면 [400 에러 발생]")
    void unlikeVotePost_NotLiked_Fail() throws Exception {
        //given
        RoomPostIsLikeRequest request = new RoomPostIsLikeRequest(false, "VOTE");

        //when & then
        mockMvc.perform(post(ROOM_POST_LIKE_API_PATH, vote.getPostId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(POST_NOT_LIKED_CANNOT_CANCEL.getCode()));
    }
}
